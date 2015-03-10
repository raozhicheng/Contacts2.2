package com.android.contacts.aspire.datasync.model;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.OperationApplicationException;
import android.content.ContentProviderOperation.Builder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Contacts.Groups;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntitySet;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.google.android.collect.Lists;

/**
 * Filename: AspEntitySet.java Description: Copyright: Copyright (c)2009
 * Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-8-27 下午03:02:36
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-8-27 liangbo 1.0 1.0 Version
 */

public class AspEntitySet extends ArrayList<AspEntityDelta> implements
		Parcelable {
	private boolean mSplitRawContacts;

	private AspEntitySet() {
	}

	/**
	 * Create an {@link EntitySet} that contains the given {@link EntityDelta},
	 * usually when inserting a new {@link Contacts} entry.
	 */
	public static AspEntitySet fromSingle(AspEntityDelta delta) {
		final AspEntitySet state = new AspEntitySet();
		state.add(delta);
		return state;
	}
	
	/**
	 * Create an {@link EntitySet} that contains the given {@link EntityDelta},
	 * usually when inserting a new {@link Contacts} entry.
	 */
	public static AspEntitySet fromNull() {
		final AspEntitySet state = new AspEntitySet();
		return state;
	}

	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人数据（raw_contact_entities）到EntitySet
	 */
	public static AspEntitySet fromQuery(ContentResolver resolver,
			String selection, String[] selectionArgs, String sortOrder) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null, selection, selectionArgs,
				sortOrder));
		try {
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			while (iterator.hasNext()) {
				// Read all contacts into local deltas to prepare for edits
				final Entity before = iterator.next();
				// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
				final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
				state.add(entity);
			}
			return state;
		} finally {
			iterator.close();
		}
	}
	
	
	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人数据（raw_contact_entities）到EntitySet
	 * 只装载缓存的SIM卡联系人数据
	 */
	public static AspEntitySet fromQueryWithSim(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null
				, RawContacts.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_SIM+"' AND "
				+ RawContacts.ACCOUNT_NAME+ " = '"+account+"' AND "
				+ RawContacts.DELETED +" = '0'"
				, null,
				sortOrder));
		try {
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			while (iterator.hasNext()) {
				// Read all contacts into local deltas to prepare for edits
				final Entity before = iterator.next();
				// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
				final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
				state.add(entity);
			}
			return state;
		} finally {
			iterator.close();
		}
	}
	
	
	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人数据（raw_contact_entities）到EntitySet
	 * 只装载缓存的SIM卡联系人数据
	 */
	public static AspEntitySet fromQueryWith139(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder,int begin,int size) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null
				, RawContacts.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_139+"' AND "
				+ RawContacts.ACCOUNT_NAME+ " = '"+account+"' AND "
				+ RawContacts.DELETED +" = '0' "				
				, null,
				sortOrder));
		try {
			
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			int i=0;
			while (iterator.hasNext()) {
				final Entity before = iterator.next();
				if(i>=begin)
				{
					// Read all contacts into local deltas to prepare for edits
					
					// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
					final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
					state.add(entity);
				}
				i++;
				if(i>=begin+size)
				{
					break;
				}

			}
			return state;
		} finally {
			iterator.close();
		}
	}

	
	
	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人数据（raw_contact_entities）到EntitySet
	 * 只装载缓存的SIM卡联系人数据
	 */
	public static AspEntitySet fromQueryWith139(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder,ArrayList<String> localIds) {
		StringBuffer ids = new StringBuffer();
		int i=0;
		for(String id  :localIds)
		{
			if(i>0)
			{
				ids.append(","+id);
			}
			else
			{
				ids.append(id);
			}
			i++;
		}
		
		
		
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null
				,""
//				RawContacts.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_139+"' AND "
//				+ RawContacts.ACCOUNT_NAME+ " = '"+account+"' AND "
//				+ RawContacts.DELETED +" = '0' AND "
				+ RawContacts._ID +"  IN("+ids.toString()+")"
				, null,
				sortOrder));
		try {
			
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			
			while (iterator.hasNext()) {
				final Entity before = iterator.next();
				
					// Read all contacts into local deltas to prepare for edits
					
					// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
					final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
					state.add(entity);
				

			}
			return state;
		} finally {
			iterator.close();
		}
	}

	
	
	/**
	 * 本地存储的139用户的数量，因为数量过多使用fromQueryWith139装载的时候会缓存不足，所以需要分段读取
	 */
	public static int getCountfromQueryWith139(ContentResolver resolver,
			String account
	/* String selection, String[] selectionArgs, */) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null,
				RawContacts.ACCOUNT_TYPE + " = '" + Config.ACCOUNT_TYPE_139
						+ "' AND " + RawContacts.ACCOUNT_NAME + " = '"
						+ account + "' AND " + RawContacts.DELETED + " = '0' "
						, null, null));

		int i = 0;
		try {

			// 迭代取得的联系人数据
			while (iterator.hasNext()) {
				iterator.next();
				i++;
			}
			return i;
		} finally {
			iterator.close();
		}
	}
	
	
	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人组数据（raw_contact_entities）到EntitySet
	 * 只装载缓存的SIM卡联系人数据
	 */
	public static AspEntitySet fromQueryWithSimGroup(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图

		EntityIterator iterator = ContactsContract.Groups.newEntityIterator(resolver.query(
				ContactsContract.Groups.CONTENT_URI, null
				, ContactsContract.Groups.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_SIM+"' AND "
				+ ContactsContract.Groups.ACCOUNT_NAME+ " = '"+account+"' AND "
				+ ContactsContract.Groups.DELETED +" = '0'"
				, null,
				sortOrder));
		try {
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			while (iterator.hasNext()) {
				// Read all contacts into local deltas to prepare for edits
				final Entity before = iterator.next();
				// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
				final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
				state.add(entity);
			}
			return state;
		} finally {
			iterator.close();
		}
	}
	
	
	
	/**
	 * Create an {@link EntitySet} based on {@link Contacts} specified by the
	 * given query parameters. This closes the {@link EntityIterator} when
	 * finished, so it doesn't subscribe to updates.
	 * 从本地数据库（Provider）中装载数据联系人组数据（raw_contact_entities）到EntitySet
	 * 只装载缓存的SIM卡联系人数据
	 */
	public static AspEntitySet fromQueryWith139Group(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图

		EntityIterator iterator = ContactsContract.Groups.newEntityIterator(resolver.query(
				ContactsContract.Groups.CONTENT_URI, null
				, ContactsContract.Groups.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_139+"' AND "
				+ ContactsContract.Groups.ACCOUNT_NAME+ " = '"+account+"' AND "
				+ ContactsContract.Groups.DELETED +" = '0'"
				, null,
				sortOrder));
		try {
			final AspEntitySet state = new AspEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			while (iterator.hasNext()) {
				// Read all contacts into local deltas to prepare for edits
				final Entity before = iterator.next();
				// 取得EntityDelta 数据变化实体，将实体装入变化实体放在 before
				final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
				state.add(entity);
			}
			return state;
		} finally {
			iterator.close();
		}
	}
	
	
	
	
   /**
    * 对比一个远程联系人集合数据，一个本地联系人集合缓存数据，根据规则生成一个新的联系人集合数据
    * @param local 本地数据
    * @param remote 远程数据
    * @param accountType 是139的规则，还是SIM卡的规则
    * @return
    */
	public static AspEntitySet mergeAfterWithAccountType(AspEntitySet local,
			AspEntitySet remote, String accountType) {
		if (local == null) {
			local = new AspEntitySet();
		}
		if (accountType.equals(Config.ACCOUNT_TYPE_139)) {
			local = mergeAfterWithAccountType139(local, remote);
		}
		if (accountType.equals(Config.ACCOUNT_TYPE_SIM)) {
			local = mergeAfterWithAccountTypeSIM(local, remote);
		}
		return local;
	}
	
	
	/**
	 * 使用远程的一个数据，139取得的，来和本机的数据对比，得到一个新的待修改的本地数据
	 */
	public static AspEntitySet mergeAfterWithAccountType139(AspEntitySet local,
			AspEntitySet remote) {
		if (local == null)
			local = new AspEntitySet();

		// For each entity in the remote set, try matching over existing
		// 使用远程的数据来循环
		for (AspEntityDelta remoteEntity : remote) {
			// 取得 ID
			//final Long rawContactId = remoteEntity.getValues().getId();
			// 取得139 ID 已经修改 得到的是139的ID
			final String rawContact139Id = remoteEntity.getValues().getSourecId();
			// 取得139 数据的139登录名称
			final String rawAccountName = remoteEntity.getValues().getAsString(RawContacts.ACCOUNT_NAME);

			// 用远端的139的ID在本地数据中查找，如果相同，则说明是同一个联系人
			// final AspEntityDelta localEntity =
			// local.getByRawContactId(rawContactId);
//			final AspEntityDelta localEntity = local
//					.getByRawContactContactsColumnValue(RawContacts.SOURCE_ID,
//							rawContact139Id.toString());
			final AspEntityDelta localEntity = local
			.getBy139ContactInfo(rawAccountName,
					rawContact139Id);
			

			// 调用实体的比对数据，得到新的实体数据（一个实体就是一个联系人）
			final AspEntityDelta merged = AspEntityDelta.mergeAfter(
					localEntity, remoteEntity);

			// 如果本地没有并且者比对结果是有数据，说明本地没有网络有
			if (localEntity == null && merged != null) {
				// No local entry before, so insert
				// 本地添加
				local.add(merged);
			}
		}
		// 返回修改过的本地数据
		return local;
	}
	
	/**
	 * 使用远程的一个数据，139取得的，来和本机的数据对比，得到一个新的待修改的本地数据
	 */
	public static AspEntitySet mergeAfterWithAccountTypeSIM(AspEntitySet local,
			AspEntitySet remote) {
		if (local == null)
			local = new AspEntitySet();

		// For each entity in the remote set, try matching over existing
		// 使用远程的数据来循环
		for (AspEntityDelta remoteEntity : remote) {
			// 取得 ID
			//final Long rawContactId = remoteEntity.getValues().getId();
			// 取得SIM卡 的ID  SIM卡ID 我们定义为 联系人名称+电话号码 
			// 注意，在以后修改的时候，转换到AspEntitySet对象的时候  这里用老的名称+老的号码
			final String rawContactSimId = remoteEntity.getValues().getSourecId();
			// 取得SIM数据的SIM卡卡号码
			final String rawAccountName = remoteEntity.getValues().getAsString(RawContacts.ACCOUNT_NAME);

			// 用远端的139的ID在本地数据中查找，如果相同，则说明是同一个联系人
			// final AspEntityDelta localEntity =
			// local.getByRawContactId(rawContactId);
//			final AspEntityDelta localEntity = local
//					.getByRawContactContactsColumnValue(RawContacts.SOURCE_ID,
//							rawContact139Id.toString());
			final AspEntityDelta localEntity = local
			.getBySimContactInfo(rawAccountName,
					rawContactSimId);
			

			// 调用实体的比对数据，得到新的实体数据（一个实体就是一个联系人）
			final AspEntityDelta merged = AspEntityDelta.mergeAfter(
					localEntity, remoteEntity);

			// 如果本地没有并且者比对结果是有数据，说明本地没有网络有
			if (localEntity == null && merged != null) {
				// No local entry before, so insert
				// 本地添加
				local.add(merged);
			}
		}
		// 返回修改过的本地数据
		return local;
	}
	
	

	/**
	 * 使用远程的一个数据，139取得的，来和本机的数据对比，得到一个新的待修改的本地数据
	 */
	public static AspEntitySet mergeAfter(AspEntitySet local,
			AspEntitySet remote) {
		if (local == null)
			local = new AspEntitySet();

		// For each entity in the remote set, try matching over existing
		// 使用远程的数据来循环
		for (AspEntityDelta remoteEntity : remote) {
			// 取得 ID
			//final Long rawContactId = remoteEntity.getValues().getId();
			// 取得139 ID 已经修改 得到的是139的ID
			final String rawContact139Id = remoteEntity.getValues().getSourecId();
			// 取得139 数据的139登录名称
			final String rawAccountName = remoteEntity.getValues().getAsString(RawContacts.ACCOUNT_NAME);

			// 用远端的139的ID在本地数据中查找，如果相同，则说明是同一个联系人
			// final AspEntityDelta localEntity =
			// local.getByRawContactId(rawContactId);
//			final AspEntityDelta localEntity = local
//					.getByRawContactContactsColumnValue(RawContacts.SOURCE_ID,
//							rawContact139Id.toString());
			final AspEntityDelta localEntity = local
			.getBy139ContactInfo(rawAccountName,
					rawContact139Id);
			

			// 调用实体的比对数据，得到新的实体数据（一个实体就是一个联系人）
			final AspEntityDelta merged = AspEntityDelta.mergeAfter(
					localEntity, remoteEntity);

			// 如果本地没有并且者比对结果是有数据，说明本地没有网络有
			if (localEntity == null && merged != null) {
				// No local entry before, so insert
				// 本地添加
				local.add(merged);
			}
		}
		// 返回修改过的本地数据
		return local;
	}

	/**
	 * Build a list of {@link ContentProviderOperation} that will transform all
	 * the "before" {@link Entity} states into the modified state which all
	 * {@link EntityDelta} objects represent. This method specifically creates
	 * any {@link AggregationExceptions} rules needed to groups edits together.
	 * 建立一份ContentProviderOperation，将修改后的状态转变为所有EntityDelta对象代表所有的“before”实体状态。
	 * 这种方法特别需要创建任何AggregationExceptions组规则编辑在一起 得到一组ContentProvider的操作
	 * ContentProviderOperation 全部联系人的操作
	 */
	public ArrayList<ContentProviderOperation> buildDiff(String accountType) {
		// 定义一个空的操作列表
		final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();

		// 取得第一个的有效
		final long rawContactId = this.findRawContactId();
		int firstInsertRow = -1;

		// First pass enforces versions remain consistent
		// 迭代全部的EntityDelta， 执行版本保持一致
//		for (AspEntityDelta delta : this) {
//			// 编写一些了需要判断版本信息的ContentProviderOperation操作，
//			// 添加到Operation存放 的ArrayList中(只对联系人信息判断版本，不需要判断详细的联系信息)
//			delta.buildAssert(diff,accountType);
//		}
		// 记录需要判断版本号码的操作的数量
		final int assertMark = diff.size();
		// 定义一个版本判断的结果的容器，存放版本判断的结果，每个单元代表一个联系人
		//，每个单元存放的内容，就是在整个操作队列中，本连联系人操作的开始位置
		int backRefs[] = new int[size()];

		int rawContactIndex = 0;

		// Second pass builds actual operations
		// 迭代得到每一个EntityDelta
		for (AspEntityDelta delta : this) {
			// 第一批地数量 （刚才判断的 不是新建的 操作的数量）
			final int firstBatch = diff.size();
			// 每个定义一个操作数量 EntityDelta
			backRefs[rawContactIndex++] = firstBatch;
			delta.buildDiff(diff);

			// Only create rules for inserts
			if (!delta.isContactInsert())
				continue;

			// If we are going to split all contacts, there is no point in first
			// combining them
			if (mSplitRawContacts)
				continue;

			if (rawContactId != -1) {
				//自动合并的一些事物
				// Has existing contact, so bind to it strongly
//				final Builder builder = beginKeepTogether();
//				builder.withValue(AggregationExceptions.RAW_CONTACT_ID1,
//						rawContactId);
//				builder.withValueBackReference(
//						AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
//				diff.add(builder.build());

			} else if (firstInsertRow == -1) {
				// First insert case, so record row
				firstInsertRow = firstBatch;

			} else {
				//自动合并的一些事物
				// Additional insert case, so point at first insert
//				final Builder builder = beginKeepTogether();
//				builder.withValueBackReference(
//						AggregationExceptions.RAW_CONTACT_ID1, firstInsertRow);
//				builder.withValueBackReference(
//						AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
//				diff.add(builder.build());
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
		builder.withValue(AggregationExceptions.TYPE,
				AggregationExceptions.TYPE_KEEP_TOGETHER);
		return builder;
	}

	/**
	 * Builds {@link AggregationExceptions} to split all constituent raw
	 * contacts into separate contacts.
	 */
	private void buildSplitContactDiff(
			final ArrayList<ContentProviderOperation> diff, int[] backRefs) {
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
	private void buildSplitContactDiff(
			ArrayList<ContentProviderOperation> diff, int index1, int index2,
			int[] backRefs) {
		Builder builder = ContentProviderOperation
				.newUpdate(AggregationExceptions.CONTENT_URI);
		builder.withValue(AggregationExceptions.TYPE,
				AggregationExceptions.TYPE_KEEP_SEPARATE);

		Long rawContactId1 = get(index1).getValues().getAsLong(RawContacts._ID);
		if (rawContactId1 != null && rawContactId1 >= 0) {
			builder.withValue(AggregationExceptions.RAW_CONTACT_ID1,
					rawContactId1);
		} else {
			builder.withValueBackReference(
					AggregationExceptions.RAW_CONTACT_ID1, backRefs[index1]);
		}

		Long rawContactId2 = get(index2).getValues().getAsLong(RawContacts._ID);
		if (rawContactId2 != null && rawContactId2 >= 0) {
			builder.withValue(AggregationExceptions.RAW_CONTACT_ID2,
					rawContactId2);
		} else {
			builder.withValueBackReference(
					AggregationExceptions.RAW_CONTACT_ID2, backRefs[index2]);
		}
		diff.add(builder.build());
	}

	/**
	 * Search all contained {@link EntityDelta} for the first one with an
	 * existing {@link RawContacts#_ID} value. Usually used when creating
	 * {@link AggregationExceptions} during an update.
	 * 搜索所有包含的数据，检查第一个EntityDelta的有效ID。 在创建过程中通常使用的更新AggregationExceptions。
	 * 取得集合中的第一个用户信息的 用户的编号
	 */
	public long findRawContactId() {
		for (AspEntityDelta delta : this) {
			final Long rawContactId = delta.getValues().getAsLong(
					RawContacts._ID);
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
			final AspEntityDelta delta = this.get(index);
			final AspValuesDelta values = delta.getValues();
			if (values.isVisible()) {
				return values.getAsLong(RawContacts._ID);
			}
		}
		return null;
	}

	/**
	 * 根据传入的列名称，取得某行的该列的值
	 * 
	 * @param RawContactsColumn
	 *            列名称
	 * @param index
	 *            行号
	 * @return 单元的值
	 */
	public String getRawContactColumnValue(String RawContactsColumn, int index) {
		if (index >= 0 && index < this.size()) {
			final AspEntityDelta delta = this.get(index);
			final AspValuesDelta values = delta.getValues();
			if (values.isVisible()) {
				// return values.getAsLong(RawContacts._ID);
				return values.getAsString(RawContactsColumn);
			}
		}
		return null;
	}

	public AspEntityDelta getByRawContactId(Long rawContactId) {
		final int index = this.indexOfRawContactId(rawContactId);
		return (index == -1) ? null : this.get(index);
	}

	/**
	 * 根据制定的列和列的数值，查找List中，列和列值符合要求的第一个AspEntityDelta（一个用户的完整信息）对象
	 * 
	 * @param rawContactsColumn
	 *            列名称
	 * @param comparObj
	 *            用来比对的列值
	 * @return
	 */
	public AspEntityDelta getByRawContactContactsColumnValue(
			String rawContactsColumn, String comparObj) {
		final int index = this.indexOfRawContactColumnValueId(
				rawContactsColumn, comparObj);
		return (index == -1) ? null : this.get(index);
	}
	
	/**
	 * 根据制定的列和列的数值，查找List中，列和列值符合要求的第一个AspEntityDelta（一个用户的完整信息）对象
	 * 139用户  需要判断 ACCOUNT_TYPE 是com.139 ，ACCOUNT_NAME,SOURCE_ID 相同,DIRTY是0的数据
	 * @param account139LoginName
	 *            139登录的账号
	 * @param source139Id
	 *            139联系人的编号
	 * @return
	 */
	public AspEntityDelta getBy139ContactInfo(
			String account139LoginName, String source139Id) {
	
		String[] rawContactsColumns = new String[4];
		String[] comparObjs = new String[4];
		
		rawContactsColumns[0] = RawContacts.ACCOUNT_TYPE;
		comparObjs[0] = Config.ACCOUNT_TYPE_139;
		
		rawContactsColumns[1] = RawContacts.ACCOUNT_NAME;
		comparObjs[1] = account139LoginName;
		
		rawContactsColumns[2] = RawContacts.SOURCE_ID;
		comparObjs[2] = source139Id;
		
		rawContactsColumns[3] = RawContacts.DELETED;
		comparObjs[3] = "0";
		
		
//		final int index = this.indexOfRawContactColumnValueId(
//		rawContactsColumn, comparObj);
		final int index = this.indexOfRawContactColumnValueId(
				rawContactsColumns, comparObjs);
		
		return (index == -1) ? null : this.get(index);
	}
	
	
	
	/**
	 * 根据制定的列和列的数值，查找List中，列和列值符合要求的第一个AspEntityDelta（一个用户的完整信息）对象
	 * 139用户  需要判断 ACCOUNT_TYPE 是com.139 ，ACCOUNT_NAME,SOURCE_ID 相同,DIRTY是0的数据
	 * @param account139LoginName
	 *            139登录的账号
	 * @param source139Id
	 *            139联系人组的编号
	 * @return
	 */
	public AspEntityDelta getBy139ContactGroupInfo(
			String account139LoginName, String source139GroupId) {
	
		String[] rawContactsColumns = new String[4]; 
		String[] comparObjs = new String[4];
		
		rawContactsColumns[0] = ContactsContract.Groups.ACCOUNT_TYPE;
		comparObjs[0] = Config.ACCOUNT_TYPE_139;
		
		rawContactsColumns[1] = ContactsContract.Groups.ACCOUNT_NAME;
		comparObjs[1] = account139LoginName;
		
		rawContactsColumns[2] = ContactsContract.Groups.SOURCE_ID;
		comparObjs[2] = source139GroupId;
		
		rawContactsColumns[3] = ContactsContract.Groups.DELETED;
		comparObjs[3] = "0";
		
		
//		final int index = this.indexOfRawContactColumnValueId(
//		rawContactsColumn, comparObj);
		final int index = this.indexOfRawContactColumnValueId(
				rawContactsColumns, comparObjs);
		
		return (index == -1) ? null : this.get(index);
	}
	
	
	
	/**
	 * 根据制定的列和列的数值，查找List中，列和列值符合要求的第一个AspEntityDelta（一个用户的完整信息）对象
	 * 139用户  需要判断 ACCOUNT_TYPE 是com.139 ，ACCOUNT_NAME,SOURCE_ID 相同,DIRTY是0的数据
	 * @param account139LoginName
	 *            139登录的账号
	 * @param source139Id
	 *            139联系人组的编号
	 * @return
	 */
	public AspEntityDelta getBySimContactGroupInfo(
			String accountSimLoginName, String sourceSimGroupId) {
	
		String[] rawContactsColumns = new String[4]; 
		String[] comparObjs = new String[4];
		
		rawContactsColumns[0] = ContactsContract.Groups.ACCOUNT_TYPE;
		comparObjs[0] = Config.ACCOUNT_TYPE_SIM;
		
		rawContactsColumns[1] = ContactsContract.Groups.ACCOUNT_NAME;
		comparObjs[1] = accountSimLoginName;
		
		rawContactsColumns[2] = ContactsContract.Groups.SOURCE_ID;
		comparObjs[2] = sourceSimGroupId;
		
		rawContactsColumns[3] = ContactsContract.Groups.DELETED;
		comparObjs[3] = "0";
		
		
//		final int index = this.indexOfRawContactColumnValueId(
//		rawContactsColumn, comparObj);
		final int index = this.indexOfRawContactColumnValueId(
				rawContactsColumns, comparObjs);
		
		return (index == -1) ? null : this.get(index);
	}
	
	
	/**
	 * 根据制定的列和列的数值，查找List中，列和列值符合要求的第一个AspEntityDelta（一个用户的完整信息）对象
	 * SIM用户  需要判断 ACCOUNT_TYPE 是SIM ，ACCOUNT_NAME,SOURCE_ID 相同,DIRTY是0的数据
	 * @param accountSimName
	 *            Sim卡的卡号
	 * @param sourceSimId
	 *            Sim卡联人的编号，此处我们处理为 联系人名称+电话号码
	 * @return
	 */
	public AspEntityDelta getBySimContactInfo(
			String accountSimName, String sourceSimId) {
	
		String[] rawContactsColumns = new String[4];
		String[] comparObjs = new String[4];
		
		rawContactsColumns[0] = RawContacts.ACCOUNT_TYPE;
		comparObjs[0] = Config.ACCOUNT_TYPE_SIM;
		
		rawContactsColumns[1] = RawContacts.ACCOUNT_NAME;
		comparObjs[1] = accountSimName;
		
		rawContactsColumns[2] = RawContacts.SOURCE_ID;
		comparObjs[2] = sourceSimId;
		
		rawContactsColumns[3] = RawContacts.DELETED;
		comparObjs[3] = "0";
		
		
//		final int index = this.indexOfRawContactColumnValueId(
//		rawContactsColumn, comparObj);
		final int index = this.indexOfRawContactColumnValueId(
				rawContactsColumns, comparObjs);
		
		return (index == -1) ? null : this.get(index);
	}
	

	/**
	 * Find index of given {@link RawContacts#_ID} when present.
	 */
	public int indexOfRawContactId(Long rawContactId) {
		if (rawContactId == null)
			return -1;
		final int size = this.size();
		for (int i = 0; i < size; i++) {
			final Long currentId = getRawContactId(i);
			if (rawContactId.equals(currentId)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 根据输入的比较参数，在数据中查找，得到第一个匹配的数据的索引（主要用来对比139网络数据和本地数据，用sourceid列来判断）
	 * 
	 * @param rawContactsColumn
	 *            比对的列名称
	 * @param comparObj
	 *            用来对比的对象
	 * @return
	 */
	public int indexOfRawContactColumnValueId(String rawContactsColumn,
			String comparObj) {
		if (comparObj == null)
			return -1;
		final int size = this.size();
		for (int i = 0; i < size; i++) {
			final String cellValue = getRawContactColumnValue(
					rawContactsColumn, i);
			if (comparObj.equals(cellValue)) {
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * 根据输入的比较参数组，在数据中查找，必须是所有参数都满足要求，得到第一个匹配的数据的索引（主要用来对比139网络数据和本地数据，用sourceid列来判断）
	 * 
	 * @param rawContactsColumn[]
	 *            比对的列名称数组
	 * @param comparObj[]
	 *            用来对比的对象数组
	 * @return
	 */
	public int indexOfRawContactColumnValueId(String[] rawContactsColumn,
			String[] comparObj) {
		if (comparObj == null || comparObj==null || comparObj.length!=rawContactsColumn.length)
			return -1;
		final int size = this.size();
		for (int i = 0; i < size; i++) {
			boolean isEquals = true;
			for(int j=0;j<rawContactsColumn.length;j++)
			{
				String cellValue = getRawContactColumnValue(
						rawContactsColumn[j], i);
				if (cellValue == null || !(comparObj[j].equals(cellValue))) {
					isEquals = false;
				}
			}
			
			if(isEquals)
			{
				return i;
			}
		}
		return -1;
	}
	

	public AspValuesDelta getSuperPrimaryEntry(final String mimeType) {
		AspValuesDelta primary = null;
		AspValuesDelta randomEntry = null;
		for (AspEntityDelta delta : this) {
			final ArrayList<AspValuesDelta> mimeEntries = delta
					.getMimeEntries(mimeType);
			if (mimeEntries == null)
				return null;

			for (AspValuesDelta entry : mimeEntries) {
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
		for (AspEntityDelta delta : this) {
			dest.writeParcelable(delta, flags);
		}
	}

	public void readFromParcel(Parcel source) {
		final ClassLoader loader = getClass().getClassLoader();
		final int size = source.readInt();
		for (int i = 0; i < size; i++) {
			this.add(source.<AspEntityDelta> readParcelable(loader));
		}
	}

	public static final Parcelable.Creator<AspEntitySet> CREATOR = new Parcelable.Creator<AspEntitySet>() {
		public AspEntitySet createFromParcel(Parcel in) {
			final AspEntitySet state = new AspEntitySet();
			state.readFromParcel(in);
			return state;
		}

		public AspEntitySet[] newArray(int size) {
			return new AspEntitySet[size];
		}
	};
	
	
public static AspEntitySet testfromQuery139()
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来没有的数据 begin ----------------
	{
	//制作一份联系人基本信息数据（一个RawContacts表数据行）
	String idFrom139 =  "139-10";//139的联系人ID
	ContentValues cv = new ContentValues();
	cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
	cv.put(RawContacts.ACCOUNT_NAME, "13714669692");
	cv.put(RawContacts.SOURCE_ID, idFrom139);
	cv.put(RawContacts.DIRTY, "1");
	cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
	//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
	final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
	values.mBefore = cv;
	
	//构造一个联系人变化实体,装入联系人基本信息
	final AspEntityDelta entity =new AspEntityDelta(values);
	
	//定义一个联系人名字的数据（一个data表数据行）
	ContentValues name = new ContentValues();
	//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
	name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
	name.put(StructuredName.DISPLAY_NAME, "梁波");//压入联系人名
	//构建一个联系人名字的变化数据
	final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
	nameVD.mBefore = name;
	//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
	entity.addEntry(nameVD);
	
	
	//定义一个联系人电话的数据（一个data表数据行）
	ContentValues mobile = new ContentValues();
	mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
	mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
	mobile.put(Phone.NUMBER, "10086");//压入联系人移动电话号码
	//构建一个联系人名字的变化数据
	final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
	mobileVD.mBefore = mobile;
	//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
	entity.addEntry(mobileVD);
	
	
	state.add(entity);
	}
	//----------------添加一份原来没有的数据 end ----------------
	
	
	//----------------添加一份原来已经有的数据 begin ----------------
	{
//	//制作一份联系人基本信息数据（一个RawContacts表数据行）
//	String idFrom139 =  "139-0";//139的联系人ID
//	ContentValues cv = new ContentValues();
//	cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
//	cv.put(RawContacts.ACCOUNT_NAME, "13714669692");
//	cv.put(RawContacts.SOURCE_ID, idFrom139);	
//	cv.put(RawContacts.DIRTY, "1");
//	cv.put(RawContacts.VERSION, System.currentTimeMillis());	
//	//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
//	final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
//	values.mBefore = cv;
//	
//	//构造一个联系人变化实体,装入联系人基本信息
//	final AspEntityDelta entity =new AspEntityDelta(values);
//	
//	//定义一个联系人名字的数据（一个data表数据行）
//	ContentValues name = new ContentValues();
//	//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
//	name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
//	name.put(StructuredName.DISPLAY_NAME, "梁波");//压入联系人名
//	//构建一个联系人名字的变化数据
//	final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
//	nameVD.mBefore = name;
//	//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//	entity.addEntry(nameVD);
//	
//	
//	//定义一个联系人电话的数据（一个data表数据行）
//	ContentValues mobile = new ContentValues();
//	mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
//	mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
//	mobile.put(Phone.NUMBER, "10086");//压入联系人移动电话号码
//	//构建一个联系人名字的变化数据
//	final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
//	mobileVD.mBefore = mobile;
//	//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//	entity.addEntry(mobileVD);
//	
//	
//	state.add(entity);
	}
	//----------------添加一份原来已经有的数据 end ----------------
	
	
	//----------------添加一份原来已经有的数据 但是网络账号不同 begin ----------------
	{
	//制作一份联系人基本信息数据（一个RawContacts表数据行）
//	String idFrom139 =  "139-9";//139的联系人ID
//	ContentValues cv = new ContentValues();
//	cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
//	cv.put(RawContacts.ACCOUNT_NAME, "12");
//	cv.put(RawContacts.SOURCE_ID, idFrom139);	
//	cv.put(RawContacts.DIRTY, "1");
//	cv.put(RawContacts.VERSION, System.currentTimeMillis());	
//	//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
//	final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
//	values.mBefore = cv;
//	
//	//构造一个联系人变化实体,装入联系人基本信息
//	final AspEntityDelta entity =new AspEntityDelta(values);
//	
//	//定义一个联系人名字的数据（一个data表数据行）
//	ContentValues name = new ContentValues();
//	//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
//	name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
//	name.put(StructuredName.DISPLAY_NAME, "梁波");//压入联系人名
//	//构建一个联系人名字的变化数据
//	final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
//	nameVD.mBefore = name;
//	//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//	entity.addEntry(nameVD);
//	
//	
//	//定义一个联系人电话的数据（一个data表数据行）
//	ContentValues mobile = new ContentValues();
//	mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
//	mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
//	mobile.put(Phone.NUMBER, "10086");//压入联系人移动电话号码
//	//构建一个联系人名字的变化数据
//	final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
//	mobileVD.mBefore = mobile;
//	//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//	entity.addEntry(mobileVD);
//	
//	
//	state.add(entity);
	}
	//----------------添加一份原来已经有的数据 但是网络账号不同end ----------------
	
	
	return state;
}
	


public static AspEntitySet testfromQuerySimAdd(String simName,String contactsName,String contactsNumber)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, cName+cNumber);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}


public static AspEntitySet testfromQuerySimAddSub(String simName,String contactsName,String contactsNumber,String subNumber)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;
		String cHomeNumber =  subNumber;
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, cName+cNumber);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
		ContentValues homePhone = new ContentValues();
		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);
		homePhoneVD.mBefore = homePhone;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}


public static AspEntitySet testfromQuerySimDelSub(String simName,String contactsName,String contactsNumber)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, cName+cNumber);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
//		ContentValues homePhone = new ContentValues();
//		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
//		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
//		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
//		//构建一个联系人名字的变化数据
//		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);//这里只装载在befor上，这样判断的时候就是删除
//		homePhoneVD.mBefore = homePhone;
//		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}


public static AspEntitySet testfromQuerySimUpdataSub(String simName,String contactsNameOld,String contactsNumberOld,String contactsNameNew,String contactsNumberNew)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsNameOld;	
		String cNumber = contactsNumberOld;	
		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, cName+cNumber);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		
		//生成一个临时的新的资源ID 因为在 SIM卡中我们规定 所有的原ID就是  名字+电话
		//在 valuesDelta的 buildDiffFormRawContacts函数中，我们会读取这个值，覆盖到SOURCE_ID列，之前不覆盖是因为查找等 都依赖老的SOURCE_ID来找
		cv.put(RawContacts.SYNC1, contactsNameNew+contactsNumberNew/*System.currentTimeMillis()*/);	
		
		
		
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromBefore(name);
		
		ContentValues nameNew = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		nameNew.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		nameNew.put(StructuredName.DISPLAY_NAME, contactsNameNew);//压入新联系人名		
		nameNew.put(StructuredName.FAMILY_NAME, "");//压入新联系人名	
		nameNew.put(StructuredName.PREFIX, "");//压入新联系人名	
		nameNew.put(StructuredName.MIDDLE_NAME, "");//压入新联系人名
		nameVD.mAfter = nameNew;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromBefore(mobile);
		
		ContentValues mobileNew = new ContentValues();
		mobileNew.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobileNew.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobileNew.put(Phone.NUMBER, contactsNumberNew);//压入联系人移动电话号码
		
		mobileVD.mAfter = mobileNew;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
//		ContentValues homePhone = new ContentValues();
//		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
//		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
//		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
//		//构建一个联系人名字的变化数据
//		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);//这里只装载在befor上，这样判断的时候就是删除
//		homePhoneVD.mBefore = homePhone;
//		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}


public static void testAddSIMContact(ContentResolver cr,String simName,String contactsName,String contactsNumber) {
	try {
		//for (int j = 0; j < 1; j++) {
		ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
		//for (int i = 0; i < 1; i++) {

			
			int rawContactInsertIndex = ops.size();
			// public static final String ACCOUNT_NAME = "account_name";
			// public static final String ACCOUNT_TYPE = "account_type";
			// public static final String SOURCE_ID = "sourceid";
			// public static final String VERSION = "version";
			// public static final String DIRTY = "dirty";
			ops.add(ContentProviderOperation.newInsert(
					RawContacts.CONTENT_URI).withValue(
					RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM).withValue(
					RawContacts.ACCOUNT_NAME, simName).withValue(
					RawContacts.SOURCE_ID, contactsName+contactsNumber).withValue(//使用 联系人名称和联系人号码共同作为SOURCE_ID
					RawContacts.VERSION, "10000").withValue(
					RawContacts.DIRTY, "1").build());
			// try {
			// new Object().wait(2000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex).withValue(Data.MIMETYPE,
							StructuredName.CONTENT_ITEM_TYPE).withValue(
							StructuredName.DISPLAY_NAME, contactsName).build());

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex).withValue(Data.MIMETYPE,
							Phone.CONTENT_ITEM_TYPE).withValue(
							Phone.NUMBER, contactsNumber).withValue(
							Phone.TYPE, Phone.TYPE_MOBILE).build());

		//}
		cr.applyBatch(ContactsContract.AUTHORITY, ops);
		//}
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OperationApplicationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}

public static void testAdd139Contact(ContentResolver cr,String simName,String contactsName,String contactsNumber,String sourceId) {
	try {
		//for (int j = 0; j < 1; j++) {
		ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
		//for (int i = 0; i < 1; i++) {

			
			int rawContactInsertIndex = ops.size();
			// public static final String ACCOUNT_NAME = "account_name";
			// public static final String ACCOUNT_TYPE = "account_type";
			// public static final String SOURCE_ID = "sourceid";
			// public static final String VERSION = "version";
			// public static final String DIRTY = "dirty";
			ops.add(ContentProviderOperation.newInsert(
					RawContacts.CONTENT_URI).withValue(
					RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139).withValue(
					RawContacts.ACCOUNT_NAME, simName).withValue(
					RawContacts.SOURCE_ID, sourceId).withValue(//使用 联系人名称和联系人号码共同作为SOURCE_ID
					RawContacts.VERSION, "10000").withValue(
					RawContacts.DIRTY, "1").build());
			// try {
			// new Object().wait(2000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex).withValue(Data.MIMETYPE,
							StructuredName.CONTENT_ITEM_TYPE).withValue(
							StructuredName.DISPLAY_NAME, contactsName).build());

			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID,
							rawContactInsertIndex).withValue(Data.MIMETYPE,
							Phone.CONTENT_ITEM_TYPE).withValue(
							Phone.NUMBER, contactsNumber).withValue(
							Phone.TYPE, Phone.TYPE_MOBILE).build());

		//}
		cr.applyBatch(ContactsContract.AUTHORITY, ops);
		//}
	} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (OperationApplicationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}


public static AspEntitySet testfromQuery139Add(String simName,String contactsName,String contactsNumber,String sourceId)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, sourceId);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}
	

public static AspEntitySet testfromQuery139AddSub(String name139,String contactsName,String contactsNumber,String subNumber,String sourceID)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;
		String cHomeNumber =  subNumber;
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
		cv.put(RawContacts.ACCOUNT_NAME, name139);
		cv.put(RawContacts.SOURCE_ID, sourceID);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
		ContentValues homePhone = new ContentValues();
		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);
		homePhoneVD.mBefore = homePhone;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}

public static AspEntitySet testfromQuery139UpdataSub(String simName,String contactsNameOld,String contactsNumberOld,String contactsNameNew,String contactsNumberNew,String sourceID)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsNameOld;	
		String cNumber = contactsNumberOld;	
		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, sourceID);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		
		//生成一个临时的新的资源ID 因为在 SIM卡中我们规定 所有的原ID就是  名字+电话
		//在 valuesDelta的 buildDiffFormRawContacts函数中，我们会读取这个值，覆盖到SOURCE_ID列，之前不覆盖是因为查找等 都依赖老的SOURCE_ID来找
		cv.put(RawContacts.SYNC1, contactsNameNew+contactsNumberNew/*System.currentTimeMillis()*/);	
		
		
		
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromBefore(name);
		
		ContentValues nameNew = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		nameNew.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		nameNew.put(StructuredName.DISPLAY_NAME, contactsNameNew);//压入新联系人名		
		nameNew.put(StructuredName.FAMILY_NAME, "");//压入新联系人名	
		nameNew.put(StructuredName.PREFIX, "");//压入新联系人名	
		nameNew.put(StructuredName.MIDDLE_NAME, "");//压入新联系人名
		nameVD.mAfter = nameNew;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromBefore(mobile);
		
		ContentValues mobileNew = new ContentValues();
		mobileNew.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobileNew.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobileNew.put(Phone.NUMBER, contactsNumberNew);//压入联系人移动电话号码
		
		mobileVD.mAfter = mobileNew;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
//		ContentValues homePhone = new ContentValues();
//		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
//		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
//		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
//		//构建一个联系人名字的变化数据
//		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);//这里只装载在befor上，这样判断的时候就是删除
//		homePhoneVD.mBefore = homePhone;
//		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}

public static AspEntitySet testfromQuery139DelSub(String simName,String contactsName,String contactsNumber,String sourceID)
{
	final AspEntitySet state = new AspEntitySet();
	
	
	//----------------添加一份原来已经有的数据   但是多出一个联系电话 begin ----------------
	{
		//制作一份联系人基本信息数据（一个RawContacts表数据行）
		String cName =  contactsName;	
		String cNumber = contactsNumber;		
		ContentValues cv = new ContentValues();
		cv.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
		cv.put(RawContacts.ACCOUNT_NAME, simName);
		cv.put(RawContacts.SOURCE_ID, sourceID);
		cv.put(RawContacts.DIRTY, "1");
		cv.put(RawContacts.VERSION, "10001"/*System.currentTimeMillis()*/);	
		//构造一个联系人基本信息变化数据（一个RawContacts表数据行）
		final AspValuesDelta values = AspValuesDelta.fromAfter(cv);
		values.mBefore = cv;
		
		//构造一个联系人变化实体,装入联系人基本信息
		final AspEntityDelta entity =new AspEntityDelta(values);
		
		//定义一个联系人名字的数据（一个data表数据行）
		ContentValues name = new ContentValues();
		//cv.put(Data.RAW_CONTACT_ID, idFrom139);//网上装载过来的没有这个ID,RawContacts和data的从属关系是依赖数据结果来实现。
		name.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//压入数据类型  人名类型
		name.put(StructuredName.DISPLAY_NAME, cName);//压入联系人名
		//构建一个联系人名字的变化数据
		final AspValuesDelta nameVD = AspValuesDelta.fromAfter(name);
		nameVD.mBefore = name;
		//将联系人名字数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(nameVD);
		
		
		//定义一个联系人电话的数据（一个data表数据行）
		ContentValues mobile = new ContentValues();
		mobile.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系电话类型
		mobile.put(Phone.TYPE, Phone.TYPE_MOBILE);//压入联系电话子类型 移动电话	
		mobile.put(Phone.NUMBER, cNumber);//压入联系人移动电话号码
		//构建一个联系人名字的变化数据
		final AspValuesDelta mobileVD = AspValuesDelta.fromAfter(mobile);
		mobileVD.mBefore = mobile;
		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
		entity.addEntry(mobileVD);
		
		
		//定义一个联系人家庭电话的数据（一个data表数据行）
//		ContentValues homePhone = new ContentValues();
//		homePhone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);//压入数据类型  联系家庭电话类型
//		homePhone.put(Phone.TYPE, Phone.TYPE_HOME);//压入联系电话子类型 家庭电话	
//		homePhone.put(Phone.NUMBER, cHomeNumber);//压入联系人家庭电话号码
//		//构建一个联系人名字的变化数据
//		final AspValuesDelta homePhoneVD = AspValuesDelta.fromAfter(homePhone);//这里只装载在befor上，这样判断的时候就是删除
//		homePhoneVD.mBefore = homePhone;
//		//将联系人data数据行信息添加到联系人变化实体，会根据MIMETYPE 装入不同的Map
//		entity.addEntry(homePhoneVD);
		
		
		
		state.add(entity);
		}
	//----------------添加一份原来已经有的数据 end ----------------
	return state;
}


}
