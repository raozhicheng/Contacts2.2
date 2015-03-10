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

import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.google.android.collect.Sets;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Entity;
import android.content.ContentProviderOperation.Builder;
import android.content.Entity.NamedContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains an {@link Entity} and records any modifications separately so the
 * original {@link Entity} can be swapped out with a newer version and the
 * changes still cleanly applied.
 * <p>
 * One benefit of this approach is that we can build changes entirely on an
 * empty {@link Entity}, which then becomes an insert {@link RawContacts} case.
 * <p>
 * When applying modifications over an {@link Entity}, we try finding the
 * original {@link Data#_ID} rows where the modifications took place. If those
 * rows are missing from the new {@link Entity}, we know the original data must
 * be deleted, but to preserve the user modifications we treat as an insert.
 */
public class EntityDelta implements Parcelable {
    // TODO: optimize by using contentvalues pool, since we allocate so many of them

    private static final String TAG = "EntityDelta";
    private static final boolean LOGV = false;

    /**
     * Direct values from {@link Entity#getEntityValues()}.
     * 变化的数据 包含mBefore 和 mAfter 两份ContentValues
     */
    private ValuesDelta mValues;

    /**
     * Internal map of children values from {@link Entity#getSubValues()}, which
     * we store here sorted into {@link Data#MIMETYPE} bins.
     * 存储的子数据  针对一个联系人的扩展的一对多的数据，用MIMETYPE来区分
     */
    private HashMap<String, ArrayList<ValuesDelta>> mEntries = Maps.newHashMap();

    public EntityDelta() {
    }

    public EntityDelta(ValuesDelta values) {
        mValues = values;
    }

    /**
     * Build an {@link EntityDelta} using the given {@link Entity} as a
     * starting point; the "before" snapshot.
     * 将一个实体数据装入到成员 ValuesDelta mValues的 ContentValues before 数据中
     */
    public static EntityDelta fromBefore(Entity before) {
        final EntityDelta entity = new EntityDelta();
        //将一个实体的数据装入 before，付给一个ValuesDelta
        entity.mValues = ValuesDelta.fromBefore(before.getEntityValues());
        //设置 ID列名称
        entity.mValues.setIdColumn(RawContacts._ID);
        
        //从实体Entity中 取得对应的多条记录的详细信息，逐个装入EntityDelta的HashMap<String, ArrayList<ValuesDelta>>  mEntries的ContentValues mBefore中去
        for (NamedContentValues namedValues : before.getSubValues()) {
            entity.addEntry(ValuesDelta.fromBefore(namedValues.values));
        }
        return entity;
    }

    /**
     * Merge the "after" values from the given {@link EntityDelta} onto the
     * "before" state represented by this {@link EntityDelta}, discarding any
     * existing "after" states. This is typically used when re-parenting changes
     * onto an updated {@link Entity}.
     */
    public static EntityDelta mergeAfter(EntityDelta local, EntityDelta remote) {
        // Bail early if trying to merge delete with missing local
    	//丢器早期的数据 尝试合并删除  缺少本地的
        final ValuesDelta remoteValues = remote.mValues;
        //如果没有本地的 远端的是删除 或远端是临时的  合并的结果是空的
        if (local == null && (remoteValues.isDelete() || remoteValues.isTransient())) return null;

        // Create local version if none exists yet
        //如果本地是空的 创建一个空差异实体
        if (local == null) local = new EntityDelta();

        if (LOGV) {
            final Long localVersion = (local.mValues == null) ? null : local.mValues
                    .getAsLong(RawContacts.VERSION);
            final Long remoteVersion = remote.mValues.getAsLong(RawContacts.VERSION);
            Log.d(TAG, "Re-parenting from original version " + remoteVersion + " to "
                    + localVersion);
        }

        // Create values if needed, and merge "after" changes
        //本地的值就是远端和本地merge后的结果
        local.mValues = ValuesDelta.mergeAfter(local.mValues, remote.mValues);

        // Find matching local entry for each remote values, or create
      //本地的mimeEntries值就是远端mimeEntries和本地mimeEntries merge后的结果
        for (ArrayList<ValuesDelta> mimeEntries : remote.mEntries.values()) {
            for (ValuesDelta remoteEntry : mimeEntries) {
                final Long childId = remoteEntry.getId();

                // Find or create local match and merge
                final ValuesDelta localEntry = local.getEntry(childId);
                final ValuesDelta merged = ValuesDelta.mergeAfter(localEntry, remoteEntry);

                if (localEntry == null && merged != null) {
                    // No local entry before, so insert
                    local.addEntry(merged);
                }
            }
        }

        return local;
    }

    public ValuesDelta getValues() {
        return mValues;
    }

    public boolean isContactInsert() {
        return mValues.isInsert();
    }

    /**
     * Get the {@link ValuesDelta} child marked as {@link Data#IS_PRIMARY},
     * which may return null when no entry exists.
     */
    public ValuesDelta getPrimaryEntry(String mimeType) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;

        for (ValuesDelta entry : mimeEntries) {
            if (entry.isPrimary()) {
                return entry;
            }
        }

        // When no direct primary, return something
        return mimeEntries.size() > 0 ? mimeEntries.get(0) : null;
    }

    /**
     * calls {@link #getSuperPrimaryEntry(String, boolean)} with true
     * @see #getSuperPrimaryEntry(String, boolean)
     */
    public ValuesDelta getSuperPrimaryEntry(String mimeType) {
        return getSuperPrimaryEntry(mimeType, true);
    }

    /**
     * Returns the super-primary entry for the given mime type
     * @param forceSelection if true, will try to return some value even if a super-primary
     *     doesn't exist (may be a primary, or just a random item
     * @return
     */
    public ValuesDelta getSuperPrimaryEntry(String mimeType, boolean forceSelection) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;

        ValuesDelta primary = null;
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isSuperPrimary()) {
                return entry;
            } else if (entry.isPrimary()) {
                primary = entry;
            }
        }

        if (!forceSelection) {
            return null;
        }

        // When no direct super primary, return something
        if (primary != null) {
            return primary;
        }
        return mimeEntries.size() > 0 ? mimeEntries.get(0) : null;
    }

    /**
     * Return the list of child {@link ValuesDelta} from our optimized map,
     * creating the list if requested.
     */
    private ArrayList<ValuesDelta> getMimeEntries(String mimeType, boolean lazyCreate) {
        ArrayList<ValuesDelta> mimeEntries = mEntries.get(mimeType);
        if (mimeEntries == null && lazyCreate) {
            mimeEntries = Lists.newArrayList();
            mEntries.put(mimeType, mimeEntries);
        }
        return mimeEntries;
    }

    public ArrayList<ValuesDelta> getMimeEntries(String mimeType) {
        return getMimeEntries(mimeType, false);
    }

    public int getMimeEntriesCount(String mimeType, boolean onlyVisible) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType);
        if (mimeEntries == null) return 0;

        int count = 0;
        for (ValuesDelta child : mimeEntries) {
            // Skip deleted items when requesting only visible
            if (onlyVisible && !child.isVisible()) continue;
            count++;
        }
        return count;
    }

    public boolean hasMimeEntries(String mimeType) {
        return mEntries.containsKey(mimeType);
    }

    public ValuesDelta addEntry(ValuesDelta entry) {
        final String mimeType = entry.getMimetype();
        getMimeEntries(mimeType, true).add(entry);
        return entry;
    }

    /**
     * Find entry with the given {@link BaseColumns#_ID} value.
     */
    public ValuesDelta getEntry(Long childId) {
        if (childId == null) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta entry : mimeEntries) {
                if (childId.equals(entry.getId())) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Return the total number of {@link ValuesDelta} contained.
     */
    public int getEntryCount(boolean onlyVisible) {
        int count = 0;
        for (String mimeType : mEntries.keySet()) {
            count += getMimeEntriesCount(mimeType, onlyVisible);
        }
        return count;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof EntityDelta) {
            final EntityDelta other = (EntityDelta)object;

            // Equality failed if parent values different
            if (!other.mValues.equals(mValues)) return false;

            for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
                for (ValuesDelta child : mimeEntries) {
                    // Equality failed if any children unmatched
                    if (!other.containsEntry(child)) return false;
                }
            }

            // Passed all tests, so equal
            return true;
        }
        return false;
    }

    private boolean containsEntry(ValuesDelta entry) {
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                // Contained if we find any child that matches
                if (child.equals(entry)) return true;
            }
        }
        return false;
    }

    /**
     * Mark this entire object deleted, including any {@link ValuesDelta}.
     */
    public void markDeleted() {
        this.mValues.markDeleted();
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                child.markDeleted();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("\n(");
        builder.append(mValues.toString());
        builder.append(") = {");
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                builder.append("\n\t");
                child.toString(builder);
            }
        }
        builder.append("\n}\n");
        return builder.toString();
    }

    /**
     * Consider building the given {@link ContentProviderOperation.Builder} and
     * appending it to the given list, which only happens if builder is valid.
     */
    private void possibleAdd(ArrayList<ContentProviderOperation> diff,
            ContentProviderOperation.Builder builder) {
        if (builder != null) {
            diff.add(builder.build());
        }
    }

    /**
     * Build a list of {@link ContentProviderOperation} that will assert any
     * "before" state hasn't changed. This is maintained separately so that all
     * asserts can take place before any updates occur.
     * 建设一份ContentProviderOperation操作名单，
     * 判断“before” 状态都没有改变
     * 这是保持分开，让所有声称可以采取任何更新之前发生的地方。
     * 给所有非新建的用户数据都添加上 Assert操作
     */
    public void buildAssert(ArrayList<ContentProviderOperation> buildInto) {
    	//判断是否是新建
        final boolean isContactInsert = mValues.isInsert();
        //不是新建的都需要添加到脏标志的判断中 （将ID和版本信息放入）
        if (!isContactInsert) {
            // Assert version is consistent while persisting changes
        	//判断版本的变化
        	
        	//取得ID 如果有后来的数据用后来的ID,然后如果有新的数据用新数据的ID
            final Long beforeId = mValues.getId();
            //取得版本  如果有后来的数据用后来的版本,然后如果有新的数据用新数据的版本
            final Long beforeVersion = mValues.getAsLong(RawContacts.VERSION);
            //如果没有版本 没有ID则不能处理
            if (beforeId == null || beforeVersion == null) return;
            //建立一个ContentProviderOperation的建筑者，制定URI是 RawContacts 操作类型是Assert
            final ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newAssertQuery(RawContacts.CONTENT_URI);
            //设置查询条件是ID
            builder.withSelection(RawContacts._ID + "=" + beforeId, null);
            //设置数据的值
            builder.withValue(RawContacts.VERSION, beforeVersion);
            //将操作者添加到列表
            buildInto.add(builder.build());
        }
    }

    /**
     * Build a list of {@link ContentProviderOperation} that will transform the
     * current "before" {@link Entity} state into the modified state which this
     * {@link EntityDelta} represents.
     * 建立一份ContentProviderOperation，
     * 将before ContentValues 的状态设置为 modified
     * 创建详细的数据操作
     */
    public void buildDiff(ArrayList<ContentProviderOperation> buildInto) {
        //记录下传入的时候，已经有多少操作了，他们的索引地址 主要给反向引用使用？？添加详细信息的时候 标示对应的用户的RAW_CONTACT_ID
    	final int firstIndex = buildInto.size();
        
        //判断状态 是新建 删除 还是修改
        final boolean isContactInsert = mValues.isInsert();//before没有 after有
        final boolean isContactDelete = mValues.isDelete();//before有 after没有
        final boolean isContactUpdate = !isContactInsert && !isContactDelete;//不是删除不是添加就是修改

        //取得ID 如果after有就afterID，不然如果before有就是beforeID
        final Long beforeId = mValues.getId();
       
        
        Builder builder;
        
        //如果是新建
        if (isContactInsert) {
            // TODO: for now simply disabling aggregation when a new contact is
            // created on the phone.  In the future, will show aggregation suggestions
            // after saving the contact.
        	// 目前简单的禁用 在手机上创建一个联系人 暂停自动合并功能
        	// 给 mAfter ContentValues 在数据库中添加 aggregation_mode 字段为 AGGREGATION_MODE_SUSPENDED
            mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
        }

        // Build possible operation at Contact level
        // 调用ValueDelta的方法创建详细的数据操作的构建器   创建一个raw_contacts表的   ContentProviderOperation.Builder 操作构造器
        builder = mValues.buildDiff(RawContacts.CONTENT_URI);
        //把builder创建的 ContentProviderOperation 添加到buildInto 这个list中
        possibleAdd(buildInto, builder);

        // Build operations for all children
        //先迭代每个用户的各种MIMETYPE数据
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
        	//迭代每个MIMETYPE下的多条数据
            for (ValuesDelta child : mimeEntries) {
                // Ignore children if parent was deleted
            	//如果删除状态 不处理
                if (isContactDelete) continue;
                //创建一个Data表的   ContentProviderOperation.Builder 操作构造器
                builder = child.buildDiff(Data.CONTENT_URI);
                if (child.isInsert()) {//本项数据时添加状态
                    if (isContactInsert) {//本联系人是添加状态
                        // Parent is brand new insert, so back-reference _id
                    	//添加值 Data.RAW_CONTACT_ID 为新的用户的ID？？用操作起始索引？设置反项引用，表示对应那个RAW_CONTACT_ID
                        builder.withValueBackReference(Data.RAW_CONTACT_ID, firstIndex);
                    } else {
                        // Inserting under existing, so fill with known _id
                    	//仅仅是在原来用户上修改了新的子数据，还用原来的RAW_CONTACT_ID
                        builder.withValue(Data.RAW_CONTACT_ID, beforeId);
                    }
                } else if (isContactInsert && builder != null) {
                	//如果本子项目不是添加状态，而联系人是添加状态 肯定是不对的
                    // Child must be insert when Contact insert
                    throw new IllegalArgumentException("When parent insert, child must be also");
                }
                //把builder创建的 ContentProviderOperation 添加到buildInto 这个list中
                possibleAdd(buildInto, builder);
            }
        }
        //如果现在的操作数量比之前的多，说明我们在这里面添加了一些操作
        final boolean addedOperations = buildInto.size() > firstIndex;
        if (addedOperations && isContactUpdate) {
        	//如果是更新联系人的操作
            // Suspend aggregation while persisting updates
        	//新建一个操作构造器（更新操作）更新raw_contacts表的 的AGGREGATION_MODE  模式为  暂停合并 在维持更新
            builder = buildSetAggregationMode(beforeId, RawContacts.AGGREGATION_MODE_SUSPENDED);
            //在原来的操作末尾，本函数的开始的位置添加 raw_contacts表的 的AGGREGATION_MODE模式设置为 暂停聚合
            buildInto.add(firstIndex, builder.build());

            // Restore aggregation mode as last operation
            //在操作的末尾 设置恢复AGGREGATION_MODE_DEFAULT
            builder = buildSetAggregationMode(beforeId, RawContacts.AGGREGATION_MODE_DEFAULT);
            buildInto.add(builder.build());
        } else if (isContactInsert) {
        	//如果是插入 
            // Restore aggregation mode as last operation
            builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
            builder.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT);
            //添加一个选择条件
            builder.withSelection(RawContacts._ID + "=?", new String[1]);
            builder.withSelectionBackReference(0, firstIndex);
            buildInto.add(builder.build());
        }
    }

    /**
     * Build a {@link ContentProviderOperation} that changes
     * {@link RawContacts#AGGREGATION_MODE} to the given value.
     */
    protected Builder buildSetAggregationMode(Long beforeId, int mode) {
        Builder builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.AGGREGATION_MODE, mode);
        builder.withSelection(RawContacts._ID + "=" + beforeId, null);
        return builder;
    }

    /** {@inheritDoc} */
    public int describeContents() {
        // Nothing special about this parcel
        return 0;
    }

    /** {@inheritDoc} */
    public void writeToParcel(Parcel dest, int flags) {
        final int size = this.getEntryCount(false);
        dest.writeInt(size);
        dest.writeParcelable(mValues, flags);
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                dest.writeParcelable(child, flags);
            }
        }
    }

    public void readFromParcel(Parcel source) {
        final ClassLoader loader = getClass().getClassLoader();
        final int size = source.readInt();
        mValues = source.<ValuesDelta> readParcelable(loader);
        for (int i = 0; i < size; i++) {
            final ValuesDelta child = source.<ValuesDelta> readParcelable(loader);
            this.addEntry(child);
        }
    }

    public static final Parcelable.Creator<EntityDelta> CREATOR = new Parcelable.Creator<EntityDelta>() {
        public EntityDelta createFromParcel(Parcel in) {
            final EntityDelta state = new EntityDelta();
            state.readFromParcel(in);
            return state;
        }

        public EntityDelta[] newArray(int size) {
            return new EntityDelta[size];
        }
    };

    /**
     * Type of {@link ContentValues} that maintains both an original state and a
     * modified version of that state. This allows us to build insert, update,
     * or delete operations based on a "before" {@link Entity} snapshot.
     */
    public static class ValuesDelta implements Parcelable {
        protected ContentValues mBefore;
        protected ContentValues mAfter;
        protected String mIdColumn = BaseColumns._ID;
        private boolean mFromTemplate;

        /**
         * Next value to assign to {@link #mIdColumn} when building an insert
         * operation through {@link #fromAfter(ContentValues)}. This is used so
         * we can concretely reference this {@link ValuesDelta} before it has
         * been persisted.
         */
        protected static int sNextInsertId = -1;

        protected ValuesDelta() {
        }

        /**
         * Create {@link ValuesDelta}, using the given object as the
         * "before" state, usually from an {@link Entity}.
         */
        public static ValuesDelta fromBefore(ContentValues before) {
            final ValuesDelta entry = new ValuesDelta();
            entry.mBefore = before;
            entry.mAfter = new ContentValues();
            return entry;
        }

        /**
         * Create {@link ValuesDelta}, using the given object as the "after"
         * state, usually when we are inserting a row instead of updating.
         */
        public static ValuesDelta fromAfter(ContentValues after) {
            final ValuesDelta entry = new ValuesDelta();
            entry.mBefore = null;
            entry.mAfter = after;

            // Assign temporary id which is dropped before insert.
            entry.mAfter.put(entry.mIdColumn, sNextInsertId--);
            return entry;
        }

        public ContentValues getAfter() {
            return mAfter;
        }

        public String getAsString(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsString(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsString(key);
            } else {
                return null;
            }
        }

        public byte[] getAsByteArray(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsByteArray(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsByteArray(key);
            } else {
                return null;
            }
        }

        public Long getAsLong(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsLong(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsLong(key);
            } else {
                return null;
            }
        }

        public Integer getAsInteger(String key) {
            return getAsInteger(key, null);
        }

        public Integer getAsInteger(String key, Integer defaultValue) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsInteger(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsInteger(key);
            } else {
                return defaultValue;
            }
        }

        public String getMimetype() {
            return getAsString(Data.MIMETYPE);
        }

        public Long getId() {
            return getAsLong(mIdColumn);
        }

        public void setIdColumn(String idColumn) {
            mIdColumn = idColumn;
        }

        public boolean isPrimary() {
            final Long isPrimary = getAsLong(Data.IS_PRIMARY);
            return isPrimary == null ? false : isPrimary != 0;
        }

        public void setFromTemplate(boolean isFromTemplate) {
            mFromTemplate = isFromTemplate;
        }

        public boolean isFromTemplate() {
            return mFromTemplate;
        }

        public boolean isSuperPrimary() {
            final Long isSuperPrimary = getAsLong(Data.IS_SUPER_PRIMARY);
            return isSuperPrimary == null ? false : isSuperPrimary != 0;
        }

        public boolean beforeExists() {
            return (mBefore != null && mBefore.containsKey(mIdColumn));
        }

        public boolean isVisible() {
            // When "after" is present, then visible
            return (mAfter != null);
        }

        public boolean isDelete() {
            // When "after" is wiped, action is "delete"
            return beforeExists() && (mAfter == null);
        }

        public boolean isTransient() {
            // When no "before" or "after", is transient
            return (mBefore == null) && (mAfter == null);
        }

        public boolean isUpdate() {
            // When "after" has some changes, action is "update"
            return beforeExists() && (mAfter != null && mAfter.size() > 0);
        }

        public boolean isNoop() {
            // When "after" has no changes, action is no-op
            return beforeExists() && (mAfter != null && mAfter.size() == 0);
        }

        public boolean isInsert() {
            // When no "before" id, and has "after", action is "insert"
            return !beforeExists() && (mAfter != null);
        }

        public void markDeleted() {
            mAfter = null;
        }

        /**
         * Ensure that our internal structure is ready for storing updates.
         * 确保我们的内部结构mAfter在存储更新的时候是准备好的
         */
        private void ensureUpdate() {
            if (mAfter == null) {
                mAfter = new ContentValues();
            }
        }

        public void put(String key, String value) {
            ensureUpdate();
            mAfter.put(key, value);
        }

        public void put(String key, byte[] value) {
            ensureUpdate();
            mAfter.put(key, value);
        }

        public void put(String key, int value) {
            ensureUpdate();
            mAfter.put(key, value);
        }

        /**
         * Return set of all keys defined through this object.
         */
        public Set<String> keySet() {
            final HashSet<String> keys = Sets.newHashSet();

            if (mBefore != null) {
                for (Map.Entry<String, Object> entry : mBefore.valueSet()) {
                    keys.add(entry.getKey());
                }
            }

            if (mAfter != null) {
                for (Map.Entry<String, Object> entry : mAfter.valueSet()) {
                    keys.add(entry.getKey());
                }
            }

            return keys;
        }

        /**
         * Return complete set of "before" and "after" values mixed together,
         * giving full state regardless of edits.
         * 返回 "before" and "after"的合在一起(如果有相同 after覆盖 before)的最大 的values
         */
        public ContentValues getCompleteValues() {
            final ContentValues values = new ContentValues();
            if (mBefore != null) {
                values.putAll(mBefore);
            }
            if (mAfter != null) {
                values.putAll(mAfter);
            }
            if (values.containsKey(GroupMembership.GROUP_ROW_ID)) {
                // Clear to avoid double-definitions, and prefer rows
            	//防止出现重复组定义 发现有 data1的 就删除 group_sourceid
                values.remove(GroupMembership.GROUP_SOURCE_ID);
                
            }

            return values;
        }

        /**
         * Merge the "after" values from the given {@link ValuesDelta},
         * discarding any existing "after" state. This is typically used when
         * re-parenting changes onto an updated {@link Entity}.
         * ValuesDelta的合并
         */
        public static ValuesDelta mergeAfter(ValuesDelta local, ValuesDelta remote) {
            // Bail early if trying to merge delete with missing local
        	//如果 本地没有 远端是删除或是临时的  则合并为空
            if (local == null && (remote.isDelete() || remote.isTransient())) return null;

            // Create local version if none exists yet
            //创建空的本地
            if (local == null) local = new ValuesDelta();
            
            //如果 本地的 before数据是无效的 
            if (!local.beforeExists()) {
                // Any "before" record is missing, so take all values as "insert"
                //将远端的 before和 after  合并(after增量覆盖 before)  付给本地的mAfter
            	//这样 本地before无 本地mAfter 有 则会后续生成 insert操作
            	local.mAfter = remote.getCompleteValues();
                
            } else {
                // Existing "update" with only "after" values
            	//将远端的  after  付给本地的mAfter
            	//后续会生成update操作
                local.mAfter = remote.mAfter;
            }

            return local;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ValuesDelta) {
                // Only exactly equal with both are identical subsets
                final ValuesDelta other = (ValuesDelta)object;
                return this.subsetEquals(other) && other.subsetEquals(this);
            }
            return false;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            toString(builder);
            return builder.toString();
        }

        /**
         * Helper for building string representation, leveraging the given
         * {@link StringBuilder} to minimize allocations.
         */
        public void toString(StringBuilder builder) {
            builder.append("{ ");
            for (String key : this.keySet()) {
                builder.append(key);
                builder.append("=");
                builder.append(this.getAsString(key));
                builder.append(", ");
            }
            builder.append("}");
        }

        /**
         * Check if the given {@link ValuesDelta} is both a subset of this
         * object, and any defined keys have equal values.
         */
        public boolean subsetEquals(ValuesDelta other) {
            for (String key : this.keySet()) {
                final String ourValue = this.getAsString(key);
                final String theirValue = other.getAsString(key);
                if (ourValue == null) {
                    // If they have value when we're null, no match
                    if (theirValue != null) return false;
                } else {
                    // If both values defined and aren't equal, no match
                    if (!ourValue.equals(theirValue)) return false;
                }
            }
            // All values compared and matched
            return true;
        }

        /**
         * Build a {@link ContentProviderOperation} that will transform our
         * "before" state into our "after" state, using insert, update, or
         * delete as needed.
         * 根据 Insert Delete Update 状态 利用传入的Uri 构造数据操作的构造者
         */
        public ContentProviderOperation.Builder buildDiff(Uri targetUri) {
            Builder builder = null;
            if (isInsert()) {
            	//对比 before和after数据  如果是插入
                // Changed values are "insert" back-referenced to Contact
            	//修改mAfter 数据 删除ID
                mAfter.remove(mIdColumn);
                //根据传入的URL 生成插入操作构造器（插入操作）
                builder = ContentProviderOperation.newInsert(targetUri);
                //将mAfter数据都装入 数据操作构造器
                builder.withValues(mAfter);
            } else if (isDelete()) {
                // When marked for deletion and "before" exists, then "delete"
            	//如果是删除 根据传入的URL 生成插入操作构造器（删除操作）
                builder = ContentProviderOperation.newDelete(targetUri);
                //装入删除条件  mIdColumn getId此时取得的是before的ID
                builder.withSelection(mIdColumn + "=" + getId(), null);
            } else if (isUpdate()) {
                // When has changes and "before" exists, then "update"
            	//如果是更新 根据传入的URL 生成插入操作构造器（更新操作）
                builder = ContentProviderOperation.newUpdate(targetUri);
                //设置条件
                builder.withSelection(mIdColumn + "=" + getId(), null);
                //装入数据
                builder.withValues(mAfter);
            }
            return builder;
        }

        /** {@inheritDoc} */
        public int describeContents() {
            // Nothing special about this parcel
            return 0;
        }

        /** {@inheritDoc} */
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mBefore, flags);
            dest.writeParcelable(mAfter, flags);
            dest.writeString(mIdColumn);
        }

        public void readFromParcel(Parcel source) {
            final ClassLoader loader = getClass().getClassLoader();
            mBefore = source.<ContentValues> readParcelable(loader);
            mAfter = source.<ContentValues> readParcelable(loader);
            mIdColumn = source.readString();
        }

        public static final Parcelable.Creator<ValuesDelta> CREATOR = new Parcelable.Creator<ValuesDelta>() {
            public ValuesDelta createFromParcel(Parcel in) {
                final ValuesDelta values = new ValuesDelta();
                values.readFromParcel(in);
                return values;
            }

            public ValuesDelta[] newArray(int size) {
                return new ValuesDelta[size];
            }
        };
    }
}
