package com.android.contacts.aspire.datasync.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Entity;
import android.content.ContentProviderOperation.Builder;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.google.android.collect.Sets;
/**  
 * Filename:    AspValuesDelta.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-27 下午02:46:22  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-27    liangbo             1.0        1.0 Version  
 */

/**
 * Type of {@link ContentValues} that maintains both an original state and a
 * modified version of that state. This allows us to build insert, update,
 * or delete operations based on a "before" {@link Entity} snapshot.
 */
public  class AspValuesDelta implements Parcelable {
    protected ContentValues mBefore;
    protected ContentValues mAfter;
    
    
    
    protected String mIdColumn = BaseColumns._ID;
    
    //修改  需要使用 139的或者SIM卡的原联系人编号地址来判断，注意 SIM卡的原联系人编号我们使用 联系人名称+电话号码 
    protected String mSourecIdColumn =RawContacts.SOURCE_ID;
    
    private boolean mFromTemplate;

    /**
     * Next value to assign to {@link #mIdColumn} when building an insert
     * operation through {@link #fromAfter(ContentValues)}. This is used so
     * we can concretely reference this {@link ValuesDelta} before it has
     * been persisted.
     */
    protected static int sNextInsertId = -1;

    protected AspValuesDelta() {
    }

    /**
     * Create {@link ValuesDelta}, using the given object as the
     * "before" state, usually from an {@link Entity}.
     * 创建一个AspValuesDelta 利用一个ContentValues（也就是一个数据行），放到before上 
     */
    public static AspValuesDelta fromBefore(ContentValues before) {
        final AspValuesDelta entry = new AspValuesDelta();
        entry.mBefore = before;
        entry.mAfter = new ContentValues();
        return entry;
    }

    /**
     * Create {@link ValuesDelta}, using the given object as the "after"
     * state, usually when we are inserting a row instead of updating.
     * 创建一个AspValuesDelta 利用一个ContentValues（也就是一个数据行），放到after上 
     */
    public static AspValuesDelta fromAfter(ContentValues after) {
        final AspValuesDelta entry = new AspValuesDelta();
        entry.mBefore = null;
        entry.mAfter = after;

        // Assign temporary id which is dropped before insert.
        entry.mAfter.put(entry.mIdColumn, sNextInsertId--);
        return entry;
    }

    public ContentValues getAfter() {
        return mAfter;
    }
    
    public ContentValues getBefore() {
        return this.mBefore;
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

    public String getId() {
        return getAsString(mIdColumn);
    }
    
    //----------139添加--begin-------
    /**
     * 取得raw_contacts表的 SOURCE_ID 也就是139的ID
     */
    public String getSourecId() {
        return getAsString(mSourecIdColumn);
    }
    
    /**
     * 取得data表的子数据类型字段
     * @return
     */
    public String getSubMimetype() {
        return getAsString(Data.DATA2);
    }
    
    
    /**
     * 取得data表的数据值字段
     * @return
     */
    public String getSubMimeTypeValue() {
        return getAsString(Data.DATA1);
    }
    
    //----------139添加--end----------
    

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
    
    /**
     * 使用关键列检查 判断原先的数据是否有效 如果before没有这个列,即使有数据也认为是无效的
     * @param keyColumn 关键列列名称
     * @return
     */
    public boolean beforeExists(String keyColumn) {
        return (mBefore != null && mBefore.containsKey(keyColumn));
    }
    
    

    public boolean isVisible() {
        // When "after" is present, then visible
        return (mAfter != null);
    }

    public boolean isDelete() {
        // When "after" is wiped, action is "delete"
        return beforeExists() && (mAfter == null || mAfter.size()==0);
    }
    
    /**
     * 使用关键列检查 这个数据是否是删除的 如果before没有这个列,即使有数据也认为是无效的
     * @param keyColumn 关键列列名称
     * @return
     */
    public boolean isDelete(String keyColumn) {
        // When "after" is wiped, action is "delete"
        return beforeExists(keyColumn) && (mAfter == null || mAfter.size()==0);
    }
    

    public boolean isTransient() {
        // When no "before" or "after", is transient
        return (mBefore == null) && (mAfter == null);
    }

    public boolean isUpdate() {
        // When "after" has some changes, action is "update"
        return beforeExists() && (mAfter != null && mAfter.size() > 0);
    }
    
    public boolean isUpdate(String keyColumn) {
        // When "after" has some changes, action is "update"
        return beforeExists(keyColumn) && (mAfter != null && mAfter.size() > 0);
    }

    public boolean isNoop() {
        // When "after" has no changes, action is no-op
        return beforeExists() && (mAfter != null && mAfter.size() == 0);
    }

    public boolean isInsert() {
        // When no "before" id, and has "after", action is "insert"
        return !beforeExists() && (mAfter != null) && (mAfter.size()>0);
    }
    
    /**
     * 需要关键字来判断是否是有效的数据，用此判断是否需要增加
     * @param keyColumn
     * @return
     */
    public boolean isInsert(String keyColumn) {
        // When no "before" id, and has "after", action is "insert"
        return !beforeExists(keyColumn) && (mAfter != null) && (mAfter.size()>0);
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
     * 用来对比本地的数据 和远端的数据  确定数据的更新（双向），
     * 一行数据的对比，一个数据行，可能是data表，可能是raw_contacts表
     */
    public static AspValuesDelta mergeAfter(AspValuesDelta local, AspValuesDelta remote) {
        // Bail early if trying to merge delete with missing local
    	//如果 本地没有 远端是删除或是临时的  则合并为空
        if (local == null && (remote.isDelete() || remote.isTransient())) return null;

        // Create local version if none exists yet
        //创建空的本地
        if (local == null) local = new AspValuesDelta();
        
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
    
    /**
     * 比对139的数据的时候，我们不能用ID来判断数据列是否有效，需要制定列来确认数据，
     * 对于raw_contacts是判断sourceid,
     * 对于data 是判断Data.DATA1
     * @param local
     * @param remote
     * @param keyColumn
     * @return
     */
    public static AspValuesDelta mergeAfterFromKeyColumnData(AspValuesDelta local, AspValuesDelta remote,String keyColumn) {
        // Bail early if trying to merge delete with missing local
    	//如果 本地没有 远端是删除或是临时的  则合并为空
    	//检测远端的数据是否是有效的，data表的话 值都放在Data1中，如果没有也认为是无效，参与判断是否是删除和是否是临时
        if (local == null && (remote.isDelete(keyColumn/*Data.DATA1*/) || remote.isTransient())) return null;

        //创建空的本地
        if (local == null) local = new AspValuesDelta();
        
        //如果 本地的 before数据是无效的 
        if (!local.beforeExists(keyColumn/*Data.DATA1*/)) {
            // Any "before" record is missing, so take all values as "insert"
            //将远端的 before和 after  合并(after增量覆盖 before)  付给本地的mAfter
        	//这样 本地before无 本地mAfter 有 则会后续生成 insert操作
        	local.mAfter = remote.getCompleteValues();
            
        } else {
            // Existing "update" with only "after" values
        	//将远端的  after  付给本地的mAfter
        	//以后在生成数据操作码的时候会 判断本地的 before和after,  后续会生成update操作
        	//远端取mAfter 是希望取得最新的数据
            local.mAfter = remote.mAfter;
            //远端的数据  都是用其他的列条件来判断的，所以需要将本地的ID 赋给远端的数据
            local.mAfter.put(BaseColumns._ID, local.mBefore.getAsString(BaseColumns._ID));
        }

        return local;
    }
    
    

    @Override
    public boolean equals(Object object) {
        if (object instanceof ValuesDelta) {
            // Only exactly equal with both are identical subsets
            final AspValuesDelta other = (AspValuesDelta)object;
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
    public boolean subsetEquals(AspValuesDelta other) {
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
//    public ContentProviderOperation.Builder buildDiff(Uri targetUri) {
//        Builder builder = null;
//        if (isInsert()) {
//        	//对比 before和after数据  如果是插入
//            // Changed values are "insert" back-referenced to Contact
//        	//修改mAfter 数据 删除ID
//            mAfter.remove(mIdColumn);
//            //根据传入的URL 生成插入操作构造器（插入操作）
//            builder = ContentProviderOperation.newInsert(targetUri);
//            //将mAfter数据都装入 数据操作构造器
//            builder.withValues(mAfter);
//        } else if (isDelete()) {
//            // When marked for deletion and "before" exists, then "delete"
//        	//如果是删除 根据传入的URL 生成插入操作构造器（删除操作）
//            builder = ContentProviderOperation.newDelete(targetUri);
//            //装入删除条件  mIdColumn getId此时取得的是before的ID
//            builder.withSelection(mIdColumn + "=" + getId(), null);
//        } else if (isUpdate()) {
//            // When has changes and "before" exists, then "update"
//        	//如果是更新 根据传入的URL 生成插入操作构造器（更新操作）
//            builder = ContentProviderOperation.newUpdate(targetUri);
//            //设置条件
//            builder.withSelection(mIdColumn + "=" + getId(), null);
//            //装入数据
//            builder.withValues(mAfter);
//        }
//        return builder;
//    }

    /**
     * Build a {@link ContentProviderOperation} that will transform our
     * "before" state into our "after" state, using insert, update, or
     * delete as needed.
     * 根据 Insert Delete Update 状态 利用传入的Uri 构造数据操作的构造者
     */
    public ContentProviderOperation.Builder buildDiffFormRawContacts(Uri targetUri) {
        Builder builder = null;
        if (isInsert(RawContacts.SOURCE_ID)) {
        	//对比 before和after数据  如果是插入
            // Changed values are "insert" back-referenced to Contact
        	//修改mAfter 数据 删除ID
            mAfter.remove(mIdColumn);
            //根据传入的URL 生成插入操作构造器（插入操作）
            builder = ContentProviderOperation.newInsert(targetUri);
            //将mAfter数据都装入 数据操作构造器
            builder.withValues(mAfter);
        } else if (isDelete(RawContacts.SOURCE_ID)) {
        	
        	String tp139ID = getSourecId();
        	String tpAccountName = getAsString(RawContacts.ACCOUNT_NAME);
        	if(tp139ID!=null && tpAccountName!=null && tpAccountName.length()>0 && tpAccountName.length()>0)
        	{
        		 // When marked for deletion and "before" exists, then "delete"
            	//如果是删除 根据传入的URL 生成插入操作构造器（删除操作）
                builder = ContentProviderOperation.newDelete(targetUri);
                //装入删除条件  mIdColumn getId此时取得的是before的ID
                //builder.withSelection(mIdColumn + "=" + getId(), null);
                builder.withSelection(RawContacts.SOURCE_ID + "='" + getSourecId()+"'"
                		+" AND "+RawContacts.DELETED +" = '0'"
                		+" AND "+RawContacts.ACCOUNT_TYPE +" = '"+ getAsString(RawContacts.ACCOUNT_TYPE) +"'"
                		+" AND "+RawContacts.ACCOUNT_NAME +" = '"+ getAsString(RawContacts.ACCOUNT_NAME) +"'", null);
        	}
        	
           
        } else if (isUpdate(RawContacts.SOURCE_ID)) {
        	
        	String tp139ID = getSourecId();
        	String tpAccountName = getAsString(RawContacts.ACCOUNT_NAME);
        	if(tp139ID!=null && tpAccountName!=null && tpAccountName.length()>0 && tpAccountName.length()>0)
        	{
	            // When has changes and "before" exists, then "update"
	        	//如果是更新 根据传入的URL 生成插入操作构造器（更新操作）
	            builder = ContentProviderOperation.newUpdate(targetUri);
	            //设置条件
	            //builder.withSelection(RawContacts.SOURCE_ID + "='" + getSourecId()+"'"
	            builder.withSelection(RawContacts._ID + "='" + getId()+"'"
	            		+" AND "+RawContacts.DELETED +" = '0'"
	            		+" AND "+RawContacts.ACCOUNT_TYPE +" = '"+ Config.ACCOUNT_TYPE_139 +"'"
	            		+" AND "+RawContacts.ACCOUNT_NAME +" = '"+ getAsString(RawContacts.ACCOUNT_NAME) +"'", null);
	            
	            
	          //判断下 如果是SIM的类型 则需要从重新计算SOURCE_ID 我们将临时的计算的SOURCE_ID 先放在SYNC1
	            if(getAsString(RawContacts.ACCOUNT_TYPE).equals(Config.ACCOUNT_TYPE_SIM))
	            {
	            	String newSourceID = mAfter.getAsString(RawContacts.SYNC1);
	            	if(newSourceID!=null && newSourceID.length()>0)
	            	{
	            		mAfter.put(RawContacts.SOURCE_ID, mAfter.getAsString(RawContacts.SYNC1));
	            	}	            	
	            }
	            
	            //装入数据
	            builder.withValues(mAfter);
	            //mAfter.remove(mIdColumn);
            }
        }
        return builder;
    }
    
    /**
     * Build a {@link ContentProviderOperation} that will transform our
     * "before" state into our "after" state, using insert, update, or
     * delete as needed.
     * 根据 Insert Delete Update 状态 利用传入的Uri 构造数据操作的构造者
     */
    public ContentProviderOperation.Builder buildDiffFormData(Uri targetUri/*,String rawContactId*/) {
     
      Builder builder = null;
      if (isInsert(Data.DATA1)) {
      	//对比 before和after数据  如果是插入
          // Changed values are "insert" back-referenced to Contact
      	//修改mAfter 数据 删除ID
          mAfter.remove(mIdColumn);
          //mAfter.remove(mSourecIdColumn);//此处的ID,需要利用刚才添加的联系人返回的本地id
          //根据传入的URL 生成插入操作构造器（插入操作）
          builder = ContentProviderOperation.newInsert(targetUri);
          //将mAfter数据都装入 数据操作构造器
          builder.withValues(mAfter);
      } else if (isDelete(Data.DATA1)) {
          // When marked for deletion and "before" exists, then "delete"
      	//如果是删除 根据传入的URL 生成插入操作构造器（删除操作）
          builder = ContentProviderOperation.newDelete(targetUri);
          //装入删除条件  mIdColumn getId此时取得的是before的ID  //这里的ID是本地缓存中的data的ID
          builder.withSelection(mIdColumn + "=" + getId(), null);
      } else if (isUpdate(Data.DATA1)) {
          // When has changes and "before" exists, then "update"
      	//如果是更新 根据传入的URL 生成插入操作构造器（更新操作）
          builder = ContentProviderOperation.newUpdate(targetUri);
          //设置条件
          builder.withSelection(mIdColumn + "=" + getId(), null);//这里的ID是本地缓存中的data的ID
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

    public static final Parcelable.Creator<AspValuesDelta> CREATOR = new Parcelable.Creator<AspValuesDelta>() {
        public AspValuesDelta createFromParcel(Parcel in) {
            final AspValuesDelta values = new AspValuesDelta();
            values.readFromParcel(in);
            return values;
        }

        public AspValuesDelta[] newArray(int size) {
            return new AspValuesDelta[size];
        }
    };
}
