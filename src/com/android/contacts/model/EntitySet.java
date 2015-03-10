/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.model;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.ContentProviderOperation.Builder;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;

import com.google.android.collect.Lists;

import com.android.contacts.model.EntityDelta.ValuesDelta;

import java.util.ArrayList;

/**
 * Container for multiple {@link EntityDelta} objects, usually when editing
 * together as an entire aggregate. Provides convenience methods for parceling
 * and applying another {@link EntitySet} over it.
 */
public class EntitySet extends ArrayList<EntityDelta> implements Parcelable {
    private boolean mSplitRawContacts;

    private EntitySet() {
    }

    /**
     * Create an {@link EntitySet} that contains the given {@link EntityDelta},
     * usually when inserting a new {@link Contacts} entry.
     */
    public static EntitySet fromSingle(EntityDelta delta) {
        final EntitySet state = new EntitySet();
        state.add(delta);
        return state;
    }

    /**
     * Create an {@link EntitySet} based on {@link Contacts} specified by the
     * given query parameters. This closes the {@link EntityIterator} when
     * finished, so it doesn't subscribe to updates.
     * 从本地数据库（Provider）中装载数据联系人数据（raw_contact_entities）到EntitySet
     */
    public static EntitySet fromQuery(ContentResolver resolver, String selection,
            String[] selectionArgs, String sortOrder) {
    	//使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
    	//装换成EntityIterator
    	//RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
        EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
                RawContactsEntity.CONTENT_URI, null, selection, selectionArgs,
                sortOrder));
        try {
            final EntitySet state = new EntitySet();
            // Perform background query to pull contact details
            //迭代取得的联系人数据
            while (iterator.hasNext()) {
                // Read all contacts into local deltas to prepare for edits
                final Entity before = iterator.next();
                //取得EntityDelta 数据变化实体
                final EntityDelta entity = EntityDelta.fromBefore(before);
                state.add(entity);
            }
            return state;
        } finally {
            iterator.close();
        }
    }

    /**
     * Merge the "after" values from the given {@link EntitySet}, discarding any
     * previous "after" states. This is typically used when re-parenting user
     * edits onto an updated {@link EntitySet}.
     * 根据一个本地和远端的EntitySet，依照策略，合并成一个
     */
    public static EntitySet mergeAfter(EntitySet local, EntitySet remote) {
        if (local == null) local = new EntitySet();

        // For each entity in the remote set, try matching over existing
        //使用远程的数据来循环
        for (EntityDelta remoteEntity : remote) {
        	//取得 ID
            final Long rawContactId = remoteEntity.getValues().getId();

            // Find or create local match and merge
            // 用ID 查找本地的数据
            final EntityDelta localEntity = local.getByRawContactId(rawContactId);
            //比对数据，得到新的数据
            final EntityDelta merged = EntityDelta.mergeAfter(localEntity, remoteEntity);
            
            //如果本地没有并且者比对结果是有数据，说明本地没有网络有
            if (localEntity == null && merged != null) {
                // No local entry before, so insert
            	//本地添加
                local.add(merged);
            }
        }

        return local;
    }

    /**
     * Build a list of {@link ContentProviderOperation} that will transform all
     * the "before" {@link Entity} states into the modified state which all
     * {@link EntityDelta} objects represent. This method specifically creates
     * any {@link AggregationExceptions} rules needed to groups edits together.
     * 建立一份ContentProviderOperation，将修改后的状态转变为所有EntityDelta对象代表所有的“before”实体状态。
     * 这种方法特别需要创建任何AggregationExceptions组规则编辑在一起
     * 得到一组ContentProvider的操作  ContentProviderOperation
     */
    public ArrayList<ContentProviderOperation> buildDiff() {
    	//定义一个空的操作列表
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        
        //取得第一个的有效 
        final long rawContactId = this.findRawContactId();
        int firstInsertRow = -1;

        // First pass enforces versions remain consistent
        // 迭代全部的EntityDelta， 执行版本保持一致
        for (EntityDelta delta : this) {
        	//邦定断言，将ID和版本信息添加到ContentProviderOperation
            delta.buildAssert(diff);
        }
        //脏标志数量
        final int assertMark = diff.size();
        //本容器大小的数组
        int backRefs[] = new int[size()];

        int rawContactIndex = 0;

        // Second pass builds actual operations
        //迭代得到每一个EntityDelta
        for (EntityDelta delta : this) {
        	//第一批地数量 （刚才判断的 不是新建的 操作的数量）
            final int firstBatch = diff.size();
            //每个定义一个操作数量 EntityDelta
            backRefs[rawContactIndex++] = firstBatch;
            delta.buildDiff(diff);

            // Only create rules for inserts
            if (!delta.isContactInsert()) continue;

            // If we are going to split all contacts, there is no point in first combining them
            if (mSplitRawContacts) continue;

            if (rawContactId != -1) {
                // Has existing contact, so bind to it strongly
                final Builder builder = beginKeepTogether();
                builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId);
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
                diff.add(builder.build());

            } else if (firstInsertRow == -1) {
                // First insert case, so record row
                firstInsertRow = firstBatch;

            } else {
                // Additional insert case, so point at first insert
                final Builder builder = beginKeepTogether();
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID1, firstInsertRow);
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
                diff.add(builder.build());
            }
        }

        if (mSplitRawContacts) {
            buildSplitContactDiff(diff, backRefs);
        }

        // No real changes if only left with asserts
        if (diff.size() == assertMark) {
            diff.clear();
        }

        return diff;
    }

    /**
     * Start building a {@link ContentProviderOperation} that will keep two
     * {@link RawContacts} together.
     */
    protected Builder beginKeepTogether() {
        final Builder builder = ContentProviderOperation
                .newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        return builder;
    }

    /**
     * Builds {@link AggregationExceptions} to split all constituent raw contacts into
     * separate contacts.
     */
    private void buildSplitContactDiff(final ArrayList<ContentProviderOperation> diff,
            int[] backRefs) {
        int count = size();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (i != j) {
                    buildSplitContactDiff(diff, i, j, backRefs);
                }
            }
        }
    }

    /**
     * Construct a {@link AggregationExceptions#TYPE_KEEP_SEPARATE}.
     */
    private void buildSplitContactDiff(ArrayList<ContentProviderOperation> diff, int index1,
            int index2, int[] backRefs) {
        Builder builder =
                ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_SEPARATE);

        Long rawContactId1 = get(index1).getValues().getAsLong(RawContacts._ID);
        if (rawContactId1 != null && rawContactId1 >= 0) {
            builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        } else {
            builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID1, backRefs[index1]);
        }

        Long rawContactId2 = get(index2).getValues().getAsLong(RawContacts._ID);
        if (rawContactId2 != null && rawContactId2 >= 0) {
            builder.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        } else {
            builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, backRefs[index2]);
        }
        diff.add(builder.build());
    }

    /**
     * Search all contained {@link EntityDelta} for the first one with an
     * existing {@link RawContacts#_ID} value. Usually used when creating
     * {@link AggregationExceptions} during an update.
     * 搜索所有包含的数据，检查第一个EntityDelta的有效ID。
     * 在创建过程中通常使用的更新AggregationExceptions。
     * 取得集合中的第一个用户信息的  用户的编号
     */
    public long findRawContactId() {
        for (EntityDelta delta : this) {
            final Long rawContactId = delta.getValues().getAsLong(RawContacts._ID);
            if (rawContactId != null && rawContactId >= 0) {
                return rawContactId;
            }
        }
        return -1;
    }

    /**
     * Find {@link RawContacts#_ID} of the requested {@link EntityDelta}.
     */
    public Long getRawContactId(int index) {
        if (index >= 0 && index < this.size()) {
            final EntityDelta delta = this.get(index);
            final ValuesDelta values = delta.getValues();
            if (values.isVisible()) {
                return values.getAsLong(RawContacts._ID);
            }
        }
        return null;
    }

    public EntityDelta getByRawContactId(Long rawContactId) {
        final int index = this.indexOfRawContactId(rawContactId);
        return (index == -1) ? null : this.get(index);
    }

    /**
     * Find index of given {@link RawContacts#_ID} when present.
     */
    public int indexOfRawContactId(Long rawContactId) {
        if (rawContactId == null) return -1;
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            final Long currentId = getRawContactId(i);
            if (rawContactId.equals(currentId)) {
                return i;
            }
        }
        return -1;
    }

    public ValuesDelta getSuperPrimaryEntry(final String mimeType) {
        ValuesDelta primary = null;
        ValuesDelta randomEntry = null;
        for (EntityDelta delta : this) {
            final ArrayList<ValuesDelta> mimeEntries = delta.getMimeEntries(mimeType);
            if (mimeEntries == null) return null;

            for (ValuesDelta entry : mimeEntries) {
                if (entry.isSuperPrimary()) {
                    return entry;
                } else if (primary == null && entry.isPrimary()) {
                    primary = entry;
                } else if (randomEntry == null) {
                    randomEntry = entry;
                }
            }
        }
        // When no direct super primary, return something
        if (primary != null) {
            return primary;
        }
        return randomEntry;
    }

    public void splitRawContacts() {
        mSplitRawContacts = true;
    }

    /** {@inheritDoc} */
    public int describeContents() {
        // Nothing special about this parcel
        return 0;
    }

    /** {@inheritDoc} */
    public void writeToParcel(Parcel dest, int flags) {
        final int size = this.size();
        dest.writeInt(size);
        for (EntityDelta delta : this) {
            dest.writeParcelable(delta, flags);
        }
    }

    public void readFromParcel(Parcel source) {
        final ClassLoader loader = getClass().getClassLoader();
        final int size = source.readInt();
        for (int i = 0; i < size; i++) {
            this.add(source.<EntityDelta> readParcelable(loader));
        }
    }

    public static final Parcelable.Creator<EntitySet> CREATOR = new Parcelable.Creator<EntitySet>() {
        public EntitySet createFromParcel(Parcel in) {
            final EntitySet state = new EntitySet();
            state.readFromParcel(in);
            return state;
        }

        public EntitySet[] newArray(int size) {
            return new EntitySet[size];
        }
    };
}
