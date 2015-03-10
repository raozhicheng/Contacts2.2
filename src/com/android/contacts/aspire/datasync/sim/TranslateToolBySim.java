package com.android.contacts.aspire.datasync.sim;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;

import com.android.contacts.aspire.datasync.model.AspEntityDelta;
import com.android.contacts.aspire.datasync.model.AspEntitySet;
import com.android.contacts.aspire.datasync.model.AspValuesDelta;
import com.android.contacts.aspire.datasync.sim.model.SimContact;
import com.android.contacts.aspire.datasync.sim.model.SimContactList;
import com.android.contacts.aspire.datasync.util.ContentProviderUtil;
import com.android.contacts.aspire.msg.respone.GetCountInfoResponeMsg;
import com.android.contacts.aspire.msg.respone.GroupItem;
import com.android.contacts.aspire.msg.respone.ListFriendItem;
import com.android.contacts.aspire.msg.respone.ListFriendsResponeMsg;

/**  
 * Filename:    TranslateToolBySim.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-2 上午12:40:29  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-2    liangbo             1.0        1.0 Version  
 */

public class TranslateToolBySim {
	
	public static ArrayList<ContentProviderOperation> buildDiffFromSimContacts(
			String accountSimCardID, AspEntitySet local,
			SimContactList remote,HashMap<String,String> iSimGroupIdToLocalGroupId) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		if (remote != null && remote.simContactList != null) {
			for (SimContact simContact : remote.simContactList)// 用远端的来迭代，和本地的对比
			{
				//我们用name+mobile做为 sourceID
				AspEntityDelta entityDelta = local.getBySimContactInfo(
						accountSimCardID, (simContact.friendName+simContact.friendMobile));

				// 执行一个联系人对象的操作
				buildDiffAspEntityDeltaFromSimContact(cpol, entityDelta,
						simContact, accountSimCardID,iSimGroupIdToLocalGroupId);

			}

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		}

		return cpol;
	}
	
	
	
	private static AspEntityDelta buildDiffAspEntityDeltaFromSimContact(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta,
			SimContact remoteSimContact, String accountSimCardID,HashMap<String,String> iSimGroupIdToLocalGroupId) {

		// 如果远端联系人数据是空 ，则什么都不处理，直接返回本地的联系人信息
		if (remoteSimContact == null) {
			return localEentityDelta;
		}

		// 标志位，说明是新建的本地联系人，即远程有 本地没有
		boolean isNewLocalEentityDelta = false;

		// 记录下进入此处的操作队列的数字，如果我们新建联系人，则在添加data数据的时候，Data.RAW_CONTACT_ID需要利用这个取得返回新建联系人的ID
		final int firstIndex = diff.size();

		if (localEentityDelta == null) {
			localEentityDelta = new AspEntityDelta();

			// 标志这个是新建的联系人
			isNewLocalEentityDelta = true;

			// 新建联系人数据添加
			ContentValues remoteContentValues = new ContentValues();
			// 压入SIM的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteSimContact.friendName+remoteSimContact.friendMobile);
			
			// 将数据添加到localEentityDelta的 befor和 after
			localEentityDelta.mValues = AspValuesDelta
					.fromBefore(remoteContentValues);// 构建新的 mValues 和 before
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after，这样在后续判断删除的时候就不会执行删除

			// 添加插入row_content的表的操作语句
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newInsterRawContactsFromSIM(accountSimCardID, remoteSimContact.friendName+remoteSimContact.friendMobile);

			// 装入操作队列
			diff.add(builder.build());

			// 对data表进行操作 涉及 新增和更新
			buildDiffAspMimeValuesDeltaFromSimContact(diff,
					localEentityDelta, remoteSimContact,
					isNewLocalEentityDelta, firstIndex,iSimGroupIdToLocalGroupId);

		} else {

			isNewLocalEentityDelta = false;
			// 本地不是空的，远端也不是空的 则修改 匹配版本 修改本地的基本数据

			// 对比这两个数据的版本号码，如果相同就不处理，如果不同，则用网络的更新本地 不能放在RawContacts.VERSION中
			// 因为整形的
//			String localVersion = localEentityDelta.getValues().getAsString(
//					RawContacts.SOURCE_ID);
//			String remoteVersion = remoteSimContact.friendName+remoteSimContact.friendMobile;
//			if (localVersion != null && remoteVersion != null
//					&& !remoteVersion.equals(localVersion)) {
				// 版本不同 需要修改

				// 得到本地的rawContentID
				int localRawContentID = Integer.parseInt(localEentityDelta
						.getValues().getId());

				// 更新rawContent表
				ContentProviderOperation.Builder builder = ContentProviderUtil
						.newUpdateRawContactsFromSim(localRawContentID,
								remoteSimContact.friendName+remoteSimContact.friendMobile);

				// 装入操作队列
				diff.add(builder.build());

				// 对data表进行操作 涉及 新增和更新
				buildDiffAspMimeValuesDeltaFromSimContact(diff,
						localEentityDelta, remoteSimContact,
						isNewLocalEentityDelta, localRawContentID,iSimGroupIdToLocalGroupId);

//			} else {
//				// 版本相同 或是有没有版本信息的（此处没有版本信息就是错误数据，不处理）
//			}

			// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
			// 新建联系人数据添加
			ContentValues remoteContentValues = localEentityDelta.mValues
					.getBefore();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteSimContact.friendName+remoteSimContact.friendMobile);
			
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after

		}

		// 找到原来的data表中的

		return localEentityDelta;
	}
	
	
	
	public static ArrayList<ContentProviderOperation> buildDelDiffFromEntitySet(
			String accountSimCardID, AspEntitySet local) {

		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		for (AspEntityDelta entityDelta : local)// 用本地的来对迭代，
		{
			// 判断如果本地的联系人中 如果联系人基本信息 mValues的 after中没有数据的,说明是需要删除的数据，调用删除
			if (entityDelta.getValues() != null
					&& (entityDelta.getValues().getAfter() == null || entityDelta
							.getValues().getAfter().size() == 0)) {
				// 取得联系人本地编号
				int localContactId1 = Integer.parseInt(entityDelta.getValues()
						.getId());

				// 添加删除操作
				buildDiffDelEentityDelta(cpol, localContactId1);
			}
		}

		return cpol;
	}
	
	
	private static void buildDiffDelEentityDelta(
			ArrayList<ContentProviderOperation> diff, int rawContactId) {
		if (rawContactId > 0) {


			// 删除data表 联系人详细信息
			ContentProviderOperation.Builder builder1 = ContentProviderUtil
					.newDeleteDataInfo(rawContactId);
			diff.add(builder1.build());
			
			
			// 删除RawContacts表 联系人基本信息
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newDeleteRawContacts(rawContactId);
			diff.add(builder.build());
		}
	}
	
	
	
	
	private static void buildDiffAspMimeValuesDeltaFromSimContact (
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta,
			SimContact simContact,
			boolean isNewLocalEentityDelta, int rawContactId,HashMap<String,String> iSimGroupIdToLocalGroupId) {
		// --------data表操begin-------------

		// ---------联系人名字的操作--begin--------
		if (simContact.friendName != null)// 说明是有联系人的名字 进行联系人名字的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 根据数据的mimetype在本地data缓存数据中找到联系人名字信息行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

			if (localMimeValuesDelta != null) {

				// 从联系人名字信息行取得本地的联系人名字
				String localName = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);

				// 判断远端数据和本地数据（名字）
				if (localName != null
						&& simContact.friendName.equals(localName)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredName(localDataId,
									simContact.friendName,""+System.currentTimeMillis(),true);

					// 装入操作队列
					diff.add(builder.build());
				}

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;
				// if (isNewLocalEentityDelta)// 判断这整个联系人是不是新建的
				// {
				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_StructuredName(
						isNewLocalEentityDelta, rawContactId,
						simContact.friendName,""+System.currentTimeMillis(),true);
				// } else {
				// // 利用原来的联系人编号 插入Data表联系人名字
				// builder = ContentProviderUtil.newInsterData_StructuredName(
				// isNewLocalEentityDelta, rawContactId,
				// remoteListFriendItem.friendName);
				// }

				// 装入操作队列
				diff.add(builder.build());
			}

		}
		// ---------联系人名字的操作--end--------

		// ---------联系人电话的操作--begin--------
		if (simContact.friendMobile != null)// 说明是有联系人的电话 进行联系人电话的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到移动电话的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& simContact.friendMobile.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_MOBILE(localDataId,
									simContact.friendMobile);

					// 装入操作队列
					diff.add(builder.build());
				}

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;
				// if (isNewLocalEentityDelta)// 判断这整个联系人是不是新建的
				// {
				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_MOBILE(
						isNewLocalEentityDelta, rawContactId,
						simContact.friendMobile);
				// } else {
				// // 利用原来的联系人编号 插入Data表联系人名字
				// builder =
				// ContentProviderUtil.newInsterData_Phone_TYPE_MOBILE(
				// isNewLocalEentityDelta, rawContactId,
				// remoteListFriendItem.mobile);
				// }

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人电话的操作--end--------
		
		
		// ---------联系人所属组的操作  --begin--------
		//
		if (simContact.typeIdList != null )// 说明是有组的操作
		{
			
			//迭代所属组的 139
			for(String remoteGroupID:simContact.typeIdList)
			{
				
				// 在本地data缓存数据中找到（此所属组）的 data记录行  利用原来的139组 ID
				AspValuesDelta localMimeValuesDelta = localEentityDelta
						.getSubMimeTypeEntryFromGroup(
								ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
								,ContactsContract.CommonDataKinds.GroupMembership.DATA2
								,remoteGroupID
						);
				
				
				
				if (localMimeValuesDelta != null) {
				

									// 利用原来的联系人Data表数据的ID
									int localDataId = Integer.parseInt(localMimeValuesDelta
											.getId());
									
									//取得原来联系人data表中属于组的本地groupID;
									//String localGroupId = localMimeValuesDelta.getAsString(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID);
									
									ContentProviderOperation.Builder builder = ContentProviderUtil
											.newUpdateData_GroupMembership_GROUP_SOURCE_ID(localDataId,
													iSimGroupIdToLocalGroupId.get(remoteGroupID.toString()),remoteGroupID.toString() );		
									// 装入操作队列
									diff.add(builder.build());

				
				
								// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
								// 
								ContentValues remoteDataValues = localMimeValuesDelta
										.getBefore();
								// 
								remoteDataValues.put(
										ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
										iSimGroupIdToLocalGroupId.get(remoteGroupID.toString()));
								
								remoteDataValues.put(
										ContactsContract.CommonDataKinds.GroupMembership.DATA2,
										remoteGroupID.toString());
								// after
								localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after
				
							} else {
								// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
								ContentProviderOperation.Builder builder;
				
								// 利用刚才新建的联系人编号 插入Data表联系人名字
								//i139GroupIdToLocalGroupId 用 远程的组ID 转换到本地的组ID
								builder = ContentProviderUtil.newInsterData_GroupMembership_GROUP_SOURCE_ID(
										isNewLocalEentityDelta, rawContactId,iSimGroupIdToLocalGroupId.get(remoteGroupID.toString()),
										remoteGroupID.toString());
				
								// 装入操作队列
								diff.add(builder.build());
							}
				
				
				
			}
		
		}
		// ---------联系人所属组的操作--end--------
		

	}
	
	
	/**
	 * 根据本地数据以及，139取得的部分数据，对比得到同步的操作项目
	 * 本操作将对比循环网络取得的数据，在本地的数据中找到对应的数据，判断版本号码（网络时间）是否修改，如果则groups网络数据覆盖本地
	 * 对比联系人组
	 * 
	 * @param local
	 * @param remote
	 * @return
	 */
	public static ArrayList<ContentProviderOperation> buildDiffFromGroup(
			String accountSimLoginName, AspEntitySet localGroups,
			GroupItem remoteGroupItem) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		
				AspEntityDelta entityDelta = localGroups.getBySimContactGroupInfo(
						accountSimLoginName, ("" + remoteGroupItem.groupId));

				// 执行一个联系人对象的操作
				buildDiffAspEntityDeltaFromGroupItem(cpol, entityDelta,
						remoteGroupItem, accountSimLoginName);

			

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		

		return cpol;
	}
	
	
	
	/**
	 * 对比联系人组信息操作
	 * 将 ListFriendItem对象装入一个AspEntityDelta对象，方便后面利用Diff语句 生成
	 * ContentProviderOperation 此函数操作原则是 不删除data数据，只将新增加的数据更新或者插入进去
	 * 并将ListFriendItem 做成一个数据放入 AspEntityDelta的 mValues的 after
	 * 方便后面判断是否删除，从而生成删除代码
	 * 
	 * @param listFriendItem
	 * @return
	 */
	private static AspEntityDelta buildDiffAspEntityDeltaFromGroupItem(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localGrouplEentityDelta,
			GroupItem remoteGroupItem, String accountSimLoginName) {

		// 如果远端联系人数据是空 ，则什么都不处理，直接返回本地的联系人信息
		if (remoteGroupItem == null) {
			return localGrouplEentityDelta;
		}

		// 标志位置，说明是新建的本地联系人，即远程有 本地没有
		boolean isNewLocalGroupEentityDelta = false;

		// 记录下进入此处的操作队列的数字，如果我们新建联系人，则在添加data数据的时候，Data.RAW_CONTACT_ID需要利用这个取得返回新建联系人的ID
		//final int firstIndex = diff.size();

		if (localGrouplEentityDelta == null) {
			localGrouplEentityDelta = new AspEntityDelta();

			// 标志这个是新建的联系人
			isNewLocalGroupEentityDelta = true;

			// 新建联系人组数据添加
			ContentValues remoteContentValues = new ContentValues();
			// 压入 139的groupID
			remoteContentValues.put(ContactsContract.Groups.SOURCE_ID,
					remoteGroupItem.groupId);
			// 压入 139的groupName
			remoteContentValues.put(ContactsContract.Groups.TITLE,
					remoteGroupItem.groupName);
			
			// 压入 139的版本信息
//			remoteContentValues.put(ContactsContract.RawContacts.SYNC1,
//					remoteListFriendItem.updateTime);

			// 将数据添加到localEentityDelta的 befor和 after
			localGrouplEentityDelta.mValues = AspValuesDelta
					.fromBefore(remoteContentValues);// 构建新的 mValues 和 before
			// after
			localGrouplEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after
			// 这样在后续判断删除的时候就不会执行删除

			// 添加插入group的表的操作语句
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newInsterGroupFromSIM(accountSimLoginName, ""
							+ remoteGroupItem.groupId,remoteGroupItem.groupName);

			// 装入操作队列
			diff.add(builder.build());

			// 对data表进行操作 涉及 新增和更新
//			buildDiffAspMimeValuesDeltaFromListFriendItem(diff,
//					localEentityDelta, remoteListFriendItem,
//					isNewLocalEentityDelta, firstIndex);

		} else {

			isNewLocalGroupEentityDelta = false;
			// 本地不是空的，远端也不是空的 则修改 匹配版本 修改本地的基本数据

			// 对比这两个数据的版本号码，如果相同就不处理，如果不同，则用网络的更新本地 不能放在RawContacts.VERSION中
			// 因为整形的
//			String localVersion = localEentityDelta.getValues().getAsString(
//					RawContacts.SYNC1);
//			String remoteUpdataTime = remoteListFriendItem.updateTime;
//			if (localVersion != null && remoteUpdataTime != null
//					&& !remoteUpdataTime.equals(localVersion)) {
				// 版本不同 需要修改

				// 得到本地的rawContentID
				int localRawContentID = Integer.parseInt(localGrouplEentityDelta
						.getValues().getId());

				// 更新rawContent表
				ContentProviderOperation.Builder builder = ContentProviderUtil
						.newUpdateGroupFromSim(localRawContentID,
								remoteGroupItem.groupName);

				// 装入操作队列
				diff.add(builder.build());

				// 对data表进行操作 涉及 新增和更新
//				buildDiffAspMimeValuesDeltaFromListFriendItem(diff,
//						localEentityDelta, remoteListFriendItem,
//						isNewLocalEentityDelta, localRawContentID);

//			} else {
//				// 版本相同 或是有没有版本信息的（此处没有版本信息就是错误数据，不处理）
//			}

			// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
			// 新建联系人数据添加
			ContentValues remoteContentValues = localGrouplEentityDelta.mValues
					.getBefore();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.Groups.SOURCE_ID,
					remoteGroupItem.groupId);
			// 压入 139的版本信息
			remoteContentValues.put(ContactsContract.Groups.TITLE,
					remoteGroupItem.groupName);
			// after
			localGrouplEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after

		}

		// 找到原来的data表中的

		return localGrouplEentityDelta;
	}

}
