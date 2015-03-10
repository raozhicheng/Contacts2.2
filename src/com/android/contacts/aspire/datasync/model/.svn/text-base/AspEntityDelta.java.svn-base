package com.android.contacts.aspire.datasync.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.datasync.icontact139.IContact139AccountManager;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.google.android.collect.Sets;

/**  
 * Filename:    AspEntityDelta.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-27 下午02:26:53  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-27    liangbo             1.0        1.0 Version  
 */

public class AspEntityDelta implements Parcelable {
    // TODO: optimize by using contentvalues pool, since we allocate so many of them

    private static final String TAG = "EntityDelta";
    private static final boolean LOGV = false;

    /**
     * Direct values from {@link Entity#getEntityValues()}.
     * 变化的数据 包含mBefore 和 mAfter 两份ContentValues
     */
    public AspValuesDelta mValues;

    /**
     * Internal map of children values from {@link Entity#getSubValues()}, which
     * we store here sorted into {@link Data#MIMETYPE} bins.
     * 存储的子数据  针对一个联系人的扩展的一对多的数据，用MIMETYPE来区分
     */
    private HashMap<String, ArrayList<AspValuesDelta>> mEntries = Maps.newHashMap();

    public HashMap<String, ArrayList<AspValuesDelta>> getMEntries() {
		return mEntries;
	}

	public AspEntityDelta() {
    }

    public AspEntityDelta(AspValuesDelta values) {
        mValues = values;
    }

    /**
     * Build an {@link EntityDelta} using the given {@link Entity} as a
     * starting point; the "before" snapshot.
     * 将一个实体数据装入到成员 ValuesDelta mValues的 ContentValues before 数据中
     */
    public static AspEntityDelta fromBefore(Entity before) {
        final AspEntityDelta entity = new AspEntityDelta();
        //将一个实体的数据装入 before，付给一个ValuesDelta
        entity.mValues = AspValuesDelta.fromBefore(before.getEntityValues());
        //设置 ID列名称
        entity.mValues.setIdColumn( BaseColumns._ID);
        //设置SOURCE_ID 也就是139数据列作为ID进行判断
        //entity.mValues.setIdColumn(RawContacts.SOURCE_ID);
        
        //从实体Entity中 取得对应的多条记录的详细信息，逐个装入EntityDelta的HashMap<String, ArrayList<ValuesDelta>>  mEntries的ContentValues mBefore中去
        for (NamedContentValues namedValues : before.getSubValues()) {
            entity.addEntry(AspValuesDelta.fromBefore(namedValues.values));
        }
        return entity;
    }
    
    
    

    /**
     * Merge the "after" values from the given {@link EntityDelta} onto the
     * "before" state represented by this {@link EntityDelta}, discarding any
     * existing "after" states. This is typically used when re-parenting changes
     * onto an updated {@link Entity}.
     */
    public static AspEntityDelta mergeAfter(AspEntityDelta local,AspEntityDelta remote) {
        // Bail early if trying to merge delete with missing local
    	//定义一个对象来缓存远程对象的 AspValuesDelta mValues（也就是用户的基本信息，对应raw_contacts表的一行）
        final AspValuesDelta remoteValues = remote.mValues;
        
        //如果没有本地的 远端的是删除 或远端是临时的  合并的结果是空的
        //也就是 本地数据不需要修改 
        //if (local == null && (remoteValues.isDelete() || remoteValues.isTransient())) return null;
        //对于联系人基本数据实体，需要检查是否包含139的ID来作为数据有效性质检查
        if (local == null && (remoteValues.isDelete(RawContacts.SOURCE_ID) || remoteValues.isTransient())) return null;

        //如果本地是空的 创建一个空差异实体，上面本地数据不需要修改的我们已经排除了，下面本地数据肯定是要修改的，所以保护空
        if (local == null) local = new AspEntityDelta();

        if (LOGV) {
            final Long localVersion = (local.mValues == null) ? null : local.mValues
                    .getAsLong(RawContacts.VERSION);
            final Long remoteVersion = remote.mValues.getAsLong(RawContacts.VERSION);
            Log.d(TAG, "Re-parenting from original version " + remoteVersion + " to "
                    + localVersion);
        }

        // Create values if needed, and merge "after" changes
        //本地的基本用户数据，就是远端的基本用户数据和本地的基本用户数据 merge的结果
        //local.mValues = AspValuesDelta.mergeAfter(local.mValues, remote.mValues);
        local.mValues = AspValuesDelta.mergeAfterFromKeyColumnData(local.mValues, remote.mValues,RawContacts.SOURCE_ID);

        
        //本地的mimeEntries值就是远端mimeEntries和本地mimeEntries merge后的结果
        //循环迭代远端数据的所有用户的扩展的数据类型（也就是一个联系人的在 Data表中的信息行）
        for (ArrayList<AspValuesDelta> mimeEntries : remote.mEntries.values()) {
        	//得到一个数据类型（mimetype）的多条数据（例如：电话号码在data中有多行数据【手机、家庭、工作等】）
            for (AspValuesDelta remoteEntry : mimeEntries) {
            	//迭代一条具体的数据，对应的就是 data的一条数据
                //final Long childId = remoteEntry.getId();// 这个ID需要 判断疑问？是data的ID
            	//我们需要判断是否是一条data数据的判断标准，前提是这个联系人的，联系人ID不需要判断
            	//判断data的 mimetype_id  data2   其取值是data1
            	final String mimetype_v = remoteEntry.getMimetype();
            	final String submimetype_v = remoteEntry.getSubMimetype();

                // 利用数据类型  数据子类型 得到一个本地的联系人扩展信息【 data表的一行数据】
            	//如果是用户名字的数据 则只需要检查mimetype_v 就可以 如果是其他的数据 则需要检查mimetype_v 和
            	AspValuesDelta localEntry=null;
            	if(mimetype_v.equals("vnd.android.cursor.item/name"))
            	{
            		localEntry = local.getSubMimeTypeEntry(mimetype_v);
            	}
            	else if(mimetype_v.equals("vnd.android.cursor.item/phone_v2"))//如果是电话号码 需要检查mimetype_v submimetype_v
            	{
            		localEntry = local.getSubMimeTypeEntry(mimetype_v, submimetype_v);
            	}
            	else
            	{
            		localEntry = local.getSubMimeTypeEntry(mimetype_v, submimetype_v);
            		//暂时不处理其他数据类新的处理
            	}
                
                
                //比对这两个数据
                //final AspValuesDelta merged = AspValuesDelta.mergeAfter(localEntry, remoteEntry);
                final AspValuesDelta merged = AspValuesDelta.mergeAfterFromKeyColumnData(localEntry, remoteEntry,Data.DATA1);
                
                
                //如果本地数据空，比对结果有，说明是新的数据，需要添加
                if (localEntry == null && merged != null) {
                    // No local entry before, so insert
                    local.addEntry(merged);
                }
            }
        }

        return local;
    }

    public AspValuesDelta getValues() {
        return mValues;
    }

    public boolean isContactInsert() {
        return mValues.isInsert();
    }

    /**
     * Get the {@link ValuesDelta} child marked as {@link Data#IS_PRIMARY},
     * which may return null when no entry exists.
     */
    public AspValuesDelta getPrimaryEntry(String mimeType) {
        final ArrayList<AspValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;

        for (AspValuesDelta entry : mimeEntries) {
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
    public AspValuesDelta getSuperPrimaryEntry(String mimeType) {
        return getSuperPrimaryEntry(mimeType, true);
    }

    /**
     * Returns the super-primary entry for the given mime type
     * @param forceSelection if true, will try to return some value even if a super-primary
     *     doesn't exist (may be a primary, or just a random item
     * @return
     */
    public AspValuesDelta getSuperPrimaryEntry(String mimeType, boolean forceSelection) {
        final ArrayList<AspValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;

        AspValuesDelta primary = null;
        for (AspValuesDelta entry : mimeEntries) {
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
    private ArrayList<AspValuesDelta> getMimeEntries(String mimeType, boolean lazyCreate) {
        ArrayList<AspValuesDelta> mimeEntries = mEntries.get(mimeType);
        if (mimeEntries == null && lazyCreate) {
            mimeEntries = Lists.newArrayList();
            mEntries.put(mimeType, mimeEntries);
        }
        return mimeEntries;
    }

    public ArrayList<AspValuesDelta> getMimeEntries(String mimeType) {
        return getMimeEntries(mimeType, false);
    }

    public int getMimeEntriesCount(String mimeType, boolean onlyVisible) {
        final ArrayList<AspValuesDelta> mimeEntries = getMimeEntries(mimeType);
        if (mimeEntries == null) return 0;

        int count = 0;
        for (AspValuesDelta child : mimeEntries) {
            // Skip deleted items when requesting only visible
            if (onlyVisible && !child.isVisible()) continue;
            count++;
        }
        return count;
    }

    public boolean hasMimeEntries(String mimeType) {
        return mEntries.containsKey(mimeType);
    }

    public AspValuesDelta addEntry(AspValuesDelta entry) {
        final String mimeType = entry.getMimetype();
        getMimeEntries(mimeType, true).add(entry);
        return entry;
    }

    /**
     * Find entry with the given {@link BaseColumns#_ID} value.
     */
    public AspValuesDelta getEntry(Long childId) {
        if (childId == null) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (childId.equals(entry.getId())) {
                    return entry;
                }
            }
        }
        return null;
    }
    
    /**
     * 匹配联系人的扩展信息使用，139的数据不能用 data的ID来判断，必须判断完整的数据类型，子类型才能确定这个是这个用户的一条具体数据
     * 我们139版本不支持每个子类型下还可以放多条数据
     * 取得数据类型和数据子类型都匹配的一条数据联系人的某个扩展信息条目【例如 电话->家庭电话】
     * @param mimetype 数据类型
     * @param submimetype 数据子类型
     * @return
     */
    public AspValuesDelta getSubMimeTypeEntry(String mimetype,String submimetype) {
        if (mimetype == null || submimetype  == null) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (mimetype.equals(entry.getMimetype())
                		&& submimetype.equals(entry.getSubMimetype())) {
                    return entry;
                }
            }
        }
        return null;
    }
    
    
    /**
     * 匹配联系人的扩展信息使用，139的数据不能用 data的ID来判断，必须判断完整的数据类型，子类型才能确定这个是这个用户的一条具体数据
     * 我们139版本不支持每个子类型下还可以放多条数据
     * 取得数据类型和数据子类型都匹配的一条数据联系人的某个扩展信息条目【例如 电话->家庭电话】
     * @param mimetype 数据类型
     * @param submimetype 数据子类型
     * @return
     */
    public AspValuesDelta getSubMimeTypeEntry(String mimetype,String submimetype,String columnName,String columnValue) {
        if (mimetype == null || submimetype  == null) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (mimetype.equals(entry.getMimetype())
                		&& submimetype.equals(entry.getSubMimetype())) {
                	
                	//除了主要类型  子类型判断后  还要判断某个指定列的值
                	String value = entry.getAsString(columnName);
                	
                	if(value!=null  && columnValue!=null && columnValue.equals(value))
                	{
                		return entry;
                	}
                    
                }
            }
        }
        return null;
    }
    
    
    /**
     * 匹配联系人的扩展信息使用，139的数据不能用 data的ID来判断，必须判断完整的数据类型，子类型才能确定这个是这个用户的一条具体数据
     * 我们139版本不支持每个子类型下还可以放多条数据
     * 取得数据类型和数据子类型都匹配的一条数据联系人的某个扩展信息条目【例如 电话->家庭电话】
     * @param mimetype 数据类型
     * @param submimetype 数据子类型
     * @return
     */
    public AspValuesDelta getSubMimeTypeEntryFromGroup(String mimetype,String columnName,String columnValue) {
        if (mimetype == null ) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (mimetype.equals(entry.getMimetype())) {
                	
                	//除了主要类型  子类型判断后  还要判断某个指定列的值
                	String value = entry.getAsString(columnName);
                	
                	if(value!=null  && columnValue!=null && columnValue.equals(value))
                	{
                		return entry;
                	}
                    
                }
            }
        }
        return null;
    }
    
    
    /**
     * 匹配联系人的扩展信息使用，139的数据不能用 data的ID来判断，必须判断完整的数据类型，子类型才能确定这个是这个用户的一条具体数据
     * 我们139版本不支持每个子类型下还可以放多条数据
     * 取得数据类型和数据子类型都匹配的一条数据联系人的某个扩展信息条目【例如 电话->家庭电话】
     * @param mimetype 数据类型
     * @param submimetype 数据子类型
     * @return
     */
    public AspValuesDelta getSubMimeTypeEntry(String mimetype) {
        if (mimetype == null ) {
            // Requesting an "insert" entry, which has no "before"
            return null;
        }

        // Search all children for requested entry
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (mimetype.equals(entry.getMimetype())) {
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
            final AspEntityDelta other = (AspEntityDelta)object;

            // Equality failed if parent values different
            if (!other.mValues.equals(mValues)) return false;

            for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
                for (AspValuesDelta child : mimeEntries) {
                    // Equality failed if any children unmatched
                    if (!other.containsEntry(child)) return false;
                }
            }

            // Passed all tests, so equal
            return true;
        }
        return false;
    }

    private boolean containsEntry(AspValuesDelta entry) {
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta child : mimeEntries) {
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
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta child : mimeEntries) {
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
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta child : mimeEntries) {
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
     * 主要是比对当前内存里面缓存的数据和真实的provider中数据的版本进行比较，检查版本是否一致
     * 对于139应用，主要是检查RawContacts.SOURCE_ID 和RawContacts.VERSION
     */
    public void buildAssert(ArrayList<ContentProviderOperation> buildInto,String accountType) {
    	//判断是否是新建
        //final boolean isContactInsert = mValues.isInsert();
    	//判断原来本地的数据（联系人基本信息行）是不是新建的，用139的 ID作为有效字段来检查
    	//（新建原则 :before的数据无效，after的数据有）
    	final boolean isContactInsert = mValues.isInsert(RawContacts.SOURCE_ID);
        //不是新建的都需要判断版本号的信息
        if (!isContactInsert) {
            // Assert version is consistent while persisting changes
        	//判断版本的变化        	
        	//取得ID 如果有after的数据用after的ID,然后如果没有用before新的的139的ID
            //final Long beforeId = mValues.getId();
        	final String beforeSourecId = mValues.getSourecId();
            //取得版本  如果有after的数据用after的版本,然后如果没有用before的数据 版本
            final Long beforeVersion = mValues.getAsLong(RawContacts.VERSION);
          //取得版本  如果有after的数据用after的版本,然后如果没有用before的数据 版本
            final String accountName = mValues.getAsString(RawContacts.ACCOUNT_NAME);
            //如果没有版本 没有ID则不能处理
            if (beforeSourecId == null || beforeVersion == null) return;
            //建立一个ContentProviderOperation的建筑者，制定URI是 RawContacts 操作类型是Assert 判断
            final ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newAssertQuery(RawContacts.CONTENT_URI);
            //设置查询条件是ID
            //builder.withSelection(RawContacts._ID + "=" + beforeId, null);
            builder.withSelection(RawContacts.SOURCE_ID + "='" + beforeSourecId+"'"
            		+" AND "+RawContacts.DELETED +" = '0'"
            		+" AND "+RawContacts.ACCOUNT_TYPE +" = '"+ accountType +"'"
            		+" AND "+RawContacts.ACCOUNT_NAME +" = '"+ accountName +"'"
            		, null);

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
//    	final boolean isContactInsert = mValues.isInsert();//before没有 after有
//      final boolean isContactDelete = mValues.isDelete();//before有 after没有
        final boolean isContactInsert = mValues.isInsert(RawContacts.SOURCE_ID);//before没有 after有
        final boolean isContactDelete = mValues.isDelete(RawContacts.SOURCE_ID);//before有 after没有
        final boolean isContactUpdate = !isContactInsert && !isContactDelete;//不是删除不是添加就是修改

        //取得ID 如果after有就afterID，不然如果before有就是beforeID
        final String beforeId = mValues.getId();
       
        
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
        //builder = mValues.buildDiff(RawContacts.CONTENT_URI);
        builder = mValues.buildDiffFormRawContacts(RawContacts.CONTENT_URI);
        //把builder创建的 ContentProviderOperation 添加到buildInto 这个list中
        possibleAdd(buildInto, builder);

        // Build operations for all children
        //先迭代每个用户的各种MIMETYPE数据
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
        	//迭代每个MIMETYPE下的多条数据
            for (AspValuesDelta child : mimeEntries) {
                // Ignore children if parent was deleted
            	//如果删除状态 不处理
                if (isContactDelete) continue;
                //创建一个Data表的   ContentProviderOperation.Builder 操作构造器,利用联系人本地的ID在data表中操作详情
                //builder = child.buildDiff(Data.CONTENT_URI);
                builder = child.buildDiffFormData(Data.CONTENT_URI/*,mValues.getId()*/);
                if (child.isInsert(Data.DATA1)) {//本项数据时添加状态
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
//            //添加一个选择条件
            builder.withSelection(RawContacts._ID + "=?", new String[1]);
            builder.withSelectionBackReference(0, firstIndex);
            buildInto.add(builder.build());
        }
    }

    /**
     * Build a {@link ContentProviderOperation} that changes
     * {@link RawContacts#AGGREGATION_MODE} to the given value.
     */
    protected Builder buildSetAggregationMode(String beforeId, int mode) {
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
        for (ArrayList<AspValuesDelta> mimeEntries : mEntries.values()) {
            for (AspValuesDelta child : mimeEntries) {
                dest.writeParcelable(child, flags);
            }
        }
    }

    public void readFromParcel(Parcel source) {
        final ClassLoader loader = getClass().getClassLoader();
        final int size = source.readInt();
        mValues = source.<AspValuesDelta> readParcelable(loader);
        for (int i = 0; i < size; i++) {
            final AspValuesDelta child = source.<AspValuesDelta> readParcelable(loader);
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
   }