package com.android.contacts.aspire.datasync.icontact139;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.datasync.model.AspEntityDelta;
import com.android.contacts.aspire.datasync.model.AspEntitySet;
import com.android.contacts.aspire.datasync.model.AspSimpleEntity;
import com.android.contacts.aspire.datasync.model.AspSimpleEntitySet;
import com.android.contacts.aspire.datasync.model.AspValuesDelta;
import com.android.contacts.aspire.datasync.util.ChineseSpelling;
import com.android.contacts.aspire.datasync.util.ContentProviderUtil;
import com.android.contacts.aspire.msg.respone.FriendItem;
import com.android.contacts.aspire.msg.respone.GetCountInfoResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg1;
import com.android.contacts.aspire.msg.respone.GroupItem;
import com.android.contacts.aspire.msg.respone.ListFriendItem;
import com.android.contacts.aspire.msg.respone.ListFriendsResponeMsg;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.EntityIterator;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

/**
 * Filename: TranslateToolBy139.java Description: Copyright: Copyright (c)2009
 * Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-8-31 下午06:14:42
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-8-31 liangbo 1.0 1.0 Version
 */

public class TranslateToolBy139 {

    /**
     * 根据本地数据以及，139取得的部分数据，对比得到同步的操作项目
	 * 本操作将对比循环网络取得的数据，在本地的数据中找到对应的数据，判断版本号码（网络时间）是否修改，如果则row_content网络数据覆盖本地
	 * 网络的的 用户名称，电话号码覆盖data本地。
     * @param account139LoginName  139账号的登录名
     * @param local  本地数据（部分 分段查询上来的）
     * @param remote  139数据（部分 分段从139获取的）
     * @param localContactEntityIds  本地完整数据的状态，用来最后判断139上删除了的，本地还保存的数据
     * @return
     */
	public static ArrayList<ContentProviderOperation> buildDiffFromListFriends(
			String account139LoginName, AspEntitySet local,
			ListFriendsResponeMsg remote/*,AspSimpleEntitySet localAllSimpleEntitySet*/) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();
		
//		if(localAllSimpleEntitySet==null)
//		{
//			localAllSimpleEntitySet =new AspSimpleEntitySet();
//		}
		
		if (remote != null && remote.friendItemList != null) {
			for (ListFriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
			{
				
				//先在全表中查询  说明找到匹配的了 将当前远端的版本信息压入,并标志是否需要新建标志
//				boolean  needCreateNew = true;
//				AspSimpleEntity ase= localAllSimpleEntitySet.getEntityFromRemoteID(""+friendItem.contactId);
//				//如果搜索全部本地都都找不到 说明是远端有 本地没有的 需要新建
//				if(ase!=null)
//				{
//					needCreateNew =false;
//				}
				
				
				AspEntityDelta entityDelta = local.getBy139ContactInfo(
						account139LoginName, ("" + friendItem.contactId));
				
				if(entityDelta!=null)
				{
					//从本次这一段详情中查找这个远端的详情 如果哟 则处理  如果没有 并且总表有  则在其他的本地详情况的分段中处理
//					if(ase!=null)
//					{
//						ase.remoteEdition = friendItem.updateTime;
//					}
					// 执行一个联系人对象的操作
//					buildDiffAspEntityDeltaFromListFriendItem(cpol, entityDelta,
//							friendItem, account139LoginName);
				}
//				else if(needCreateNew)
//				{
//					//说明 不再本分段中  也不存在其他分段中  需要新建操作
//					// 执行一个联系人对象的操作
//					buildDiffAspEntityDeltaFromListFriendItem(cpol, entityDelta,
//							friendItem, account139LoginName);
//					
//				}
//				else
//				{
//					// 说明不再此次分片的本地详细信息中  但是在其他的分段中 本次本分段不处理
//				}

				buildDiffAspEntityDeltaFromListFriendItem(cpol, entityDelta,
						friendItem, account139LoginName);

			}

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		}

		return cpol;
	}
	
	

	/**
	 * 判断local的AspEntityDelta中的mValues的 after中没有数据的，就是删除数据，生成删除的语句
	 * 
	 * @param account139LoginName
	 * @param local
	 * @return
	 */
	public static ArrayList<ContentProviderOperation> buildDelDiffFromEntitySet(
			String account139LoginName, AspEntitySet local) {

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
	
	/**
	 * 判断local的AspEntityDelta中的mValues的 after中没有数据的，就是删除数据，生成删除的语句
	 * 
	 * @param account139LoginName
	 * @param local
	 * @return
	 */
	public static ArrayList<ContentProviderOperation> buildDelDiffFromEntityIds(
			String account139LoginName, AspSimpleEntitySet localAllSimpleEntitySet) {

		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		for (AspSimpleEntity ase : localAllSimpleEntitySet.values())// 用本地的来对迭代，
		{
			// 判断如果本地的联系人中 如果联系人基本信息 mValues的 after中没有数据的,说明是需要删除的数据，调用删除
			
			if (ase!=null && ase.needLocalDel()) {
				// 取得联系人本地编号
				int localContactId1 = Integer.parseInt(ase.localID);

				// 添加删除操作
				buildDiffDelEentityDelta(cpol, localContactId1);
			}
		}

		return cpol;
	}
	
	
	/**
	 * 删除本地有远端没有的联系人组
	 * 判断local的AspEntityDelta中的mValues的 after中没有数据的，就是删除数据，生成删除的语句
	 * 
	 * @param account139LoginName
	 * @param local
	 * @return
	 */
	public static ArrayList<ContentProviderOperation> buildDelDiffFromGroupEntitySet(
			String account139LoginName, AspEntitySet local) {

		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		for (AspEntityDelta entityDelta : local)// 用本地的来对迭代，
		{
			// 判断如果本地的联系人中 如果联系人基本信息 mValues的 after中没有数据的,说明是需要删除的数据，调用删除
			if (entityDelta.getValues() != null
					&& (entityDelta.getValues().getAfter() == null || entityDelta
							.getValues().getAfter().size() == 0)) {
				// 取得联系人组本地编号
				int localContactId1 = Integer.parseInt(entityDelta.getValues()
						.getId());

				// 添加删除操作
				buildDiffDelGroupEentityDelta(cpol, localContactId1);
			}
		}

		return cpol;
	}
	

	/**
	 * 根据本地数据以及，139取得的全数据，对比得到同步的操作项目
	 * 
	 * @param local
	 * @param remote
	 * @return
	 */
	public static ArrayList<ContentProviderOperation> buildDiffFromContactsInfo(
			String account139LoginName, AspEntitySet local,
			GetInfoByContactIdResponeMsg remote,HashMap<String,String> i139GroupIdToLocalGroupId) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		if (remote != null && remote.friendItemList != null) {
			for (FriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
			{
				AspEntityDelta entityDelta = local.getBy139ContactInfo(
						account139LoginName, ("" + friendItem.contactId));

				// 执行一个联系人对象的操作
				buildDiffAspEntityDeltaFromContactsInfo(cpol, entityDelta,
						friendItem, account139LoginName,i139GroupIdToLocalGroupId);

			}

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		}

		return cpol;
	}
	
	public static ArrayList<ContentProviderOperation> buildDiffFromContactsInfo1(
			String account139LoginName, AspEntitySet local,
			GetInfoByContactIdResponeMsg1 remote,HashMap<String,String> i139GroupIdToLocalGroupId) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		if (remote != null && remote.friendItemList != null) {
			for (FriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
			{
				AspEntityDelta entityDelta = local.getBy139ContactInfo(
						account139LoginName, ("" + friendItem.contactId));

				// 执行一个联系人对象的操作
				buildDiffAspEntityDeltaFromContactsInfo(cpol, entityDelta,
						friendItem, account139LoginName,i139GroupIdToLocalGroupId);

			}

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		}

		return cpol;
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
	public static ArrayList<ContentProviderOperation> buildDiffFromListFriendGroups(
			String account139LoginName, AspEntitySet localGroups,
			GetCountInfoResponeMsg remoteGroups) {
		ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();

		if (remoteGroups != null && remoteGroups.groupItemList != null) {
			for (GroupItem groupItem : remoteGroups.groupItemList)// 用远端的来对迭代，和本地的对比
			{
				AspEntityDelta entityDelta = localGroups.getBy139ContactGroupInfo(
						account139LoginName, ("" + groupItem.groupId));

				// 执行一个联系人对象的操作
				buildDiffAspEntityDeltaFromListFriends(cpol, entityDelta,
						groupItem, account139LoginName);

			}

			// 迭代完成后，如果本地的没有匹配到的 涉及删除（因为支持分段操作，所以这个用 after是空来代表，在最后的时候执行一次操作）
			// 从而避免 分段下载网络数据的时候，对比一段后就删除，其实可能在下一段中

		}

		return cpol;
	}
	

	/**
	 * 将 ListFriendItem对象装入一个AspEntityDelta对象，方便后面利用Diff语句 生成
	 * ContentProviderOperation 此函数操作原则是 不删除data数据，只将新增加的数据更新或者插入进去
	 * 并将ListFriendItem 做成一个数据放入 AspEntityDelta的 mValues的 after
	 * 方便后面判断是否删除，从而生成删除代码
	 * 
	 * @param listFriendItem
	 * @return
	 */
	private static AspEntityDelta buildDiffAspEntityDeltaFromListFriendItem(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta,
			ListFriendItem remoteListFriendItem, String account139LoginName) {

		// 如果远端联系人数据是空 ，则什么都不处理，直接返回本地的联系人信息
		if (remoteListFriendItem == null) {
			return localEentityDelta;
		}

		// 标志位置，说明是新建的本地联系人，即远程有 本地没有
		boolean isNewLocalEentityDelta = false;

		// 记录下进入此处的操作队列的数字，如果我们新建联系人，则在添加data数据的时候，Data.RAW_CONTACT_ID需要利用这个取得返回新建联系人的ID
		final int firstIndex = diff.size();

		//本地没有  
		if (localEentityDelta == null) {
					
			localEentityDelta = new AspEntityDelta();

			// 标志这个是新建的联系人
			isNewLocalEentityDelta = true;

			// 新建联系人数据添加
			ContentValues remoteContentValues = new ContentValues();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteListFriendItem.contactId);
			// 压入 139的版本信息
//			remoteContentValues.put(ContactsContract.RawContacts.VERSION,
//					remoteListFriendItem.updateTime);
			
//			// 压入拼音信息到  sync2  其实这里这样做不需要的   android有自己的处理方案
//			String nameSpell="";
//			if(remoteListFriendItem.nameSpell!=null)
//			{
//				nameSpell = remoteListFriendItem.nameSpell;
//			}
//			else
//			{
//				//转换名称
//				ChineseSpelling finder = ChineseSpelling.getInstance();
//				nameSpell = finder.getSelling(remoteListFriendItem.friendName);
//			}
//			remoteContentValues.put(ContactsContract.RawContacts.SYNC2,
//					nameSpell);
		

			// 将数据添加到localEentityDelta的 befor和 after
			localEentityDelta.mValues = AspValuesDelta
					.fromBefore(remoteContentValues);// 构建新的 mValues 和 before
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after
			// 这样在后续判断删除的时候就不会执行删除

			// 添加插入row_content的表的操作语句
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newInsterRawContactsFrom139(account139LoginName, ""
							+ remoteListFriendItem.contactId,
							remoteListFriendItem.updateTime
							,remoteListFriendItem.friendName
							,remoteListFriendItem.nameSpell
					);

			// 装入操作队列
			diff.add(builder.build());

			// 对data表进行操作 涉及 新增和更新
			buildDiffAspMimeValuesDeltaFromListFriendItem(diff,
					localEentityDelta, remoteListFriendItem,
					isNewLocalEentityDelta, firstIndex);
			
			
			//更新表中的 RawContacts.SORT_KEY_PRIMARY和 SORT_KEY_ALTERNATIVE
			ContentProviderOperation.Builder builder1 =ContentProviderUtil.newUpdateRawContactsSpellFrom139Id139(""+remoteListFriendItem.contactId
					,remoteListFriendItem.friendName
					,remoteListFriendItem.nameSpell);
			// 装入操作队列
			diff.add(builder1.build());

		} else {

			isNewLocalEentityDelta = false;
			// 本地不是空的，远端也不是空的 则修改 匹配版本 修改本地的基本数据

			// 对比这两个数据的版本号码，如果相同就不处理，如果不同，则用网络的更新本地 不能放在RawContacts.VERSION中
			// 因为整形的
			String localVersion = localEentityDelta.getSubMimeTypeEntry(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).getAsString(ContactsContract.CommonDataKinds.StructuredName.SYNC2);
			String remoteUpdataTime = remoteListFriendItem.updateTime;
			if (localVersion != null && remoteUpdataTime != null
					&& !remoteUpdataTime.equals(localVersion)) {
				// 版本不同 需要修改

				// 得到本地的rawContentID
				int localRawContentID = Integer.parseInt(localEentityDelta
						.getValues().getId());

				// 更新rawContent表
				ContentProviderOperation.Builder builder = ContentProviderUtil
						.newUpdateRawContactsFrom139(localRawContentID,
								remoteUpdataTime,remoteListFriendItem.friendName
								,remoteListFriendItem.nameSpell,""+remoteListFriendItem.contactId);

				// 装入操作队列
				diff.add(builder.build());

				// 对data表进行操作 涉及 新增和更新
				buildDiffAspMimeValuesDeltaFromListFriendItem(diff,
						localEentityDelta, remoteListFriendItem,
						isNewLocalEentityDelta, localRawContentID);
				
				
				//更新表中的 RawContacts.SORT_KEY_PRIMARY和 SORT_KEY_ALTERNATIVE
				ContentProviderOperation.Builder builder1 =ContentProviderUtil.newUpdateRawContactsSpellFrom139(localRawContentID
						,remoteListFriendItem.friendName
						,remoteListFriendItem.nameSpell);
				// 装入操作队列
				diff.add(builder1.build());
				

			} else {
				// 版本相同 或是有没有版本信息的（此处没有版本信息就是错误数据，不处理）
			}

			// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
			// 新建联系人数据添加
			ContentValues remoteContentValues = localEentityDelta.mValues
					.getBefore();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteListFriendItem.contactId);
			// 压入 139的版本信息
//			remoteContentValues.put(ContactsContract.RawContacts.VERSION,
//					remoteListFriendItem.updateTime);
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after

		}
		
		
		
		// 找到原来的data表中的

		return localEentityDelta;
	}
	
	
	

	private static void buildDelDiffEntityMimeDate(ArrayList<ContentProviderOperation> cpol,
			String account139LoginName, AspEntityDelta localEentityDelta) {

		//ArrayList<ContentProviderOperation> cpol = new ArrayList<ContentProviderOperation>();
		
		//凡是在执行过匹配以后after中没有设置数据的 说明远端没有在本地找到匹配的，说明远端删除，所以本地也要删除
		for (ArrayList<AspValuesDelta> mimeEntries : localEentityDelta.getMEntries().values()) {
            for (AspValuesDelta entry : mimeEntries) {
                if (entry.getAfter()==null || entry.getAfter().size()==0) {
                	// 取得data本地编号
    				int localDataId1 = Integer.parseInt(entry.getId());
    				
    				// 添加删除操作    				
    				ContentProviderOperation.Builder builder1 = ContentProviderUtil
					.newDeleteDataInfoFromID(localDataId1);
    				cpol.add(builder1.build());
                }
            }
        }		
	}
	

	/**
	 * 使用网络返回的139的详细信息来和本地的联系人对比，生成操作
	 * 
	 * @param diff
	 * @param localEentityDelta
	 * @param remoteListFriendItem
	 * @param account139LoginName
	 * @return
	 */
	private static AspEntityDelta buildDiffAspEntityDeltaFromContactsInfo(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta, FriendItem remoteFriendItem,
			String account139LoginName,HashMap<String,String> i139GroupIdToLocalGroupId) {

		// 如果远端联系人数据是空 ，则什么都不处理，直接返回本地的联系人信息
		if (remoteFriendItem == null) {
			return localEentityDelta;
		}

		// 标志位置，说明是新建的本地联系人，即远程有 本地没有
		boolean isNewLocalEentityDelta = false;

		// 记录下进入此处的操作队列的数字，如果我们新建联系人，则在添加data数据的时候，Data.RAW_CONTACT_ID需要利用这个取得返回新建联系人的ID
		final int firstIndex = diff.size();

		if (localEentityDelta == null) {
			localEentityDelta = new AspEntityDelta();

			// 标志这个是新建的联系人
			isNewLocalEentityDelta = true;

			// 新建联系人数据添加
			ContentValues remoteContentValues = new ContentValues();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteFriendItem.contactId);
			// 压入 139的版本信息
//			remoteContentValues.put(ContactsContract.RawContacts.VERSION,
//					remoteFriendItem.updateTime);

			// 将数据添加到localEentityDelta的 befor和 after
			localEentityDelta.mValues = AspValuesDelta
					.fromBefore(remoteContentValues);// 构建新的 mValues 和 before
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after
			// 这样在后续判断删除的时候就不会执行删除

			// 添加插入row_content的表的操作语句
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newInsterRawContactsFrom139(account139LoginName, ""
							+ remoteFriendItem.contactId,
							remoteFriendItem.updateTime
							,remoteFriendItem.friendName,remoteFriendItem.nameSpell);

			// 装入操作队列
			diff.add(builder.build());

			// 对data表进行操作 涉及 新增和更新
			buildDiffAspMimeValuesDeltaFromFriendItem(diff, localEentityDelta,
					remoteFriendItem, isNewLocalEentityDelta, firstIndex,i139GroupIdToLocalGroupId);

		} else {

			isNewLocalEentityDelta = false;
			// 本地不是空的，远端也不是空的 则修改 匹配版本 修改本地的基本数据

			// 对比这两个数据的版本号码，如果相同就不处理，如果不同，则用网络的更新本地 不能放在RawContacts.VERSION中
			// 因为整形的  注意 这里取详细信息  不再判断版本号码  
			String localVersion = localEentityDelta.getSubMimeTypeEntry(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).getAsString(ContactsContract.CommonDataKinds.StructuredName.SYNC2);
			String remoteUpdataTime = remoteFriendItem.updateTime;
			if (localVersion != null && remoteUpdataTime != null
					&& !remoteUpdataTime.equals(localVersion)) {
				// 版本不同 需要修改

				// 得到本地的rawContentID
				int localRawContentID = Integer.parseInt(localEentityDelta
						.getValues().getId());

				// 更新rawContent表
				ContentProviderOperation.Builder builder = ContentProviderUtil
						.newUpdateRawContactsFrom139(localRawContentID,
								remoteUpdataTime,remoteFriendItem.friendName,remoteFriendItem.nameSpell,""+remoteFriendItem.contactId);

				// 装入操作队列
				diff.add(builder.build());

				

				// 对data表进行操作 涉及 新增和更新
				buildDiffAspMimeValuesDeltaFromFriendItem(diff,
						localEentityDelta, remoteFriendItem,
						isNewLocalEentityDelta, localRawContentID, i139GroupIdToLocalGroupId);
				
				
				// 此处考虑如何实现 删除本地 当远端没有的时候
				//根据上面匹配过的数据  删除139上删除了 在本地还有的;
				buildDelDiffEntityMimeDate(diff,account139LoginName, localEentityDelta);
				
				

			} else {
				// 版本相同 或是有没有版本信息的（此处没有版本信息就是错误数据，不处理）
			}

			// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
			// 新建联系人数据添加
			ContentValues remoteContentValues = localEentityDelta.mValues
					.getBefore();
			// 压入 139的ID
			remoteContentValues.put(ContactsContract.RawContacts.SOURCE_ID,
					remoteFriendItem.contactId);
			// 压入 139的版本信息
//			remoteContentValues.put(ContactsContract.RawContacts.VERSION,
//					remoteFriendItem.updateTime);
			// after
			localEentityDelta.mValues.getAfter().putAll(remoteContentValues);// 将数据也添加到after

		}

		// 找到原来的data表中的

		return localEentityDelta;
	}

	/**
	 * 对联系人的 详细信息 即Data表进行的对比操作，根据ListFriendIte
	 * 
	 * @param diff
	 * @param localEentityDelta
	 * @param remoteListFriendItem
	 * @param isNewLocalEentityDelta
	 *            这个联系人是不是新建的
	 * @param firstIndex
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size()
	 *   
	 */
	private static void buildDiffAspMimeValuesDeltaFromListFriendItem(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta,
			ListFriendItem remoteListFriendItem,
			boolean isNewLocalEentityDelta, int rawContactId) {
		// --------data表操begin-------------

		// ---------联系人名字的操作--begin--------
		if (remoteListFriendItem.friendName != null)// 说明是有联系人的名字 进行联系人名字的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到联系人名字信息行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

			if (localMimeValuesDelta != null) {

				// 取得本地的联系人名字
				String localName = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);

//				String localUpdateTime = localMimeValuesDelta
//				.getAsString(ContactsContract.CommonDataKinds.StructuredName.SYNC2);

				// 判断远端数据和本地数据（名字）
				if (localName != null
						&& remoteListFriendItem.friendName.equals(localName)
				/*&& localUpdateTime!=null && remoteListFriendItem.updateTime.equals(localUpdateTime)*/
				) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredName(localDataId,
									remoteListFriendItem.friendName,remoteListFriendItem.updateTime,false);

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
						remoteListFriendItem.friendName,remoteListFriendItem.updateTime,false);
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
		if (remoteListFriendItem.mobile != null)// 说明是有联系人的电话 进行联系人电话的操作
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
						&& remoteListFriendItem.mobile.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_MOBILE(localDataId,
									remoteListFriendItem.mobile);

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
						remoteListFriendItem.mobile);
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

		// ---------联系人邮件的操作--begin--------
		if (remoteListFriendItem.email != null)// 说明是有联系人的邮件 进行联系人邮件的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到移动电话的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Email.TYPE_HOME);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localAddress = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Email.ADDRESS);

				// 判断远端数据和本地数据（电话）
				if (localAddress != null
						&& remoteListFriendItem.email.equals(localAddress)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Email_TYPE_HOME(localDataId,
									remoteListFriendItem.email);

					// 装入操作队列
					diff.add(builder.build());
				}

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;
				// if (isNewLocalEentityDelta)// 判断这整个联系人是不是新建的
				// {
				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Email_TYPE_HOME(
						isNewLocalEentityDelta, rawContactId,
						remoteListFriendItem.email);
				// } else {
				// // 利用原来的联系人编号 插入Data表联系人名字
				// builder = ContentProviderUtil.newInsterData_Email_TYPE_HOME(
				// isNewLocalEentityDelta, rawContactId,
				// remoteListFriendItem.email);
				// }

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人邮件的操作--end--------

	}
	
	
	
	

	private static void buildDiffAspMimeValuesDeltaFromFriendItem(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localEentityDelta, FriendItem remoteFriendItem,
			boolean isNewLocalEentityDelta, int rawContactId,HashMap<String,String> i139GroupIdToLocalGroupId) {
		// --------data表操begin-------------

		// ---------联系人名字的操作--begin--------
		if (remoteFriendItem.friendName != null  && remoteFriendItem.updateTime!=null)// 说明是有联系人的名字 进行联系人名字的操作
		{
			// 在本地data缓存数据中找到联系人名字信息行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

			if (localMimeValuesDelta != null) {

				// 取得本地的联系人名字
				String localName = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
				
				String localUpdateTime = localMimeValuesDelta
				.getAsString(ContactsContract.CommonDataKinds.StructuredName.SYNC2);

				// 判断远端数据和本地数据（名字）
				if (localName != null
						&& remoteFriendItem.friendName.equals(localName) && localUpdateTime!=null && remoteFriendItem.updateTime.equals(localUpdateTime)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredName(localDataId,
									remoteFriendItem.friendName,remoteFriendItem.updateTime,true);

					// 装入操作队列
					diff.add(builder.build());

				}

				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 名字添加
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 压入 远程得到的名字
				remoteDataValues
						.put(
								ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
								remoteFriendItem.friendName);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_StructuredName(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendName,remoteFriendItem.updateTime,true);
				// 装入操作队列
				diff.add(builder.build());
			}

		}
		// ---------联系人名字的操作--end--------

		// ---------联系人电话的操作--begin--------
		if (remoteFriendItem.friendMobile != null)// 说明是有联系人的电话 进行联系人电话的操作
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
						&& remoteFriendItem.friendMobile.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_MOBILE(localDataId,
									remoteFriendItem.friendMobile);

					// 装入操作队列
					diff.add(builder.build());
					
				}
				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.friendMobile);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_MOBILE(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendMobile);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人电话的操作--end--------

		// ---------联系人邮件的操作--begin--------
		if (remoteFriendItem.email != null)// 说明是有联系人的邮件 进行联系人邮件的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到移动电话的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Email.TYPE_HOME);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localAddress = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Email.ADDRESS);

				// 判断远端数据和本地数据（电话）
				if (localAddress != null
						&& remoteFriendItem.email.equals(localAddress)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Email_TYPE_HOME(localDataId,
									remoteFriendItem.email);

					// 装入操作队列
					diff.add(builder.build());
				}
				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Email.ADDRESS,
						remoteFriendItem.email);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Email_TYPE_HOME(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.email);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人邮件的操作--end--------
		
		
		
		
		
		// ---------联系人好友的手机1（工作手机）的操作--begin--------
		if (remoteFriendItem.friendOtherNumber != null)// 说明是有联系人的手机1（工作手机） 进行联系人邮件的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& remoteFriendItem.friendOtherNumber.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_WORK_MOBILE(localDataId,
									remoteFriendItem.friendOtherNumber);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.friendOtherNumber);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_COMPANY_MAIN(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendOtherNumber);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的手机1（工作手机）的操作--end--------
		
		
		
		
		// ---------联系人好友的电话(家庭)的操作--begin--------
		if (remoteFriendItem.friendTel != null)// 说明是有联系人好友的电话(家庭)的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& remoteFriendItem.friendTel.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_HOME(localDataId,
									remoteFriendItem.friendTel);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.friendTel);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_HOME(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendTel);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的电话(家庭)的操作--end--------
		
		
		// ---------联系人好友的电话1(其他)的操作--begin--------
		if (remoteFriendItem.friendOtherTel != null)// 说明是有联系人好友的电话1(其他)的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& remoteFriendItem.friendOtherTel.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_OTHER(localDataId,
									remoteFriendItem.friendOtherTel);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.friendOtherTel);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_OTHER(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendOtherTel);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的电话1(其他)的操作--end--------
		
		
		
		// ---------联系人好友的工作电话的操作--begin--------
		if (remoteFriendItem.officePhone != null)// 说明是有联系人工作电话的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& remoteFriendItem.officePhone.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_WORK(localDataId,
									remoteFriendItem.officePhone);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.officePhone);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_WORK(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.officePhone);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的工作电话的操作--end--------
		
		
		// ---------联系人好友的工作传真的操作--begin--------
		if (remoteFriendItem.friendEleTel != null)// 说明是有联系人工作传真的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
							""
									+ ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的电话号码
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);

				// 判断远端数据和本地数据（电话）
				if (localNumber != null
						&& remoteFriendItem.friendEleTel.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Phone_TYPE_FAX_WORK(localDataId,
									remoteFriendItem.friendEleTel);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						remoteFriendItem.friendEleTel);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Phone_TYPE_FAX_WORK(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendEleTel);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的工作传真的操作--end--------
		

		
		
		
		
		// ---------联系人好友的QQ的操作--begin--------
		if (remoteFriendItem.friendQQ != null)// 说明是有联系人QQ的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Im.TYPE_HOME
							,ContactsContract.CommonDataKinds.Im.PROTOCOL
							,""+ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的qq
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Im.DATA);

				// 判断远端数据和本地数据（qq）
				if (localNumber != null
						&& remoteFriendItem.friendQQ.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Im_PROTOCOL_QQ(localDataId,
									remoteFriendItem.friendQQ);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Im.DATA,
						remoteFriendItem.friendQQ);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Im_PROTOCOL_QQ(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendQQ);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的QQ的操作--end--------
		
		
		
		
		// ---------联系人好友的飞信的操作--begin--------
		if (remoteFriendItem.friendFetion != null)// 说明是有联系人飞信的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Im.TYPE_HOME
							,ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL
							,FriendItem.IM_PROTOCOL_CUSTOM_FETION
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的qq
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Im.DATA);

				// 判断远端数据和本地数据（qq）
				if (localNumber != null
						&& remoteFriendItem.friendFetion.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Im_PROTOCOL_CUSTOM_FETION(localDataId,
									remoteFriendItem.friendFetion);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Im.DATA,
						remoteFriendItem.friendFetion);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Im_PROTOCOL_CUSTOM_FETION(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendFetion);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的飞信的操作--end--------
		
		
		
		// ---------联系人好友的MSN的操作--begin--------
		if (remoteFriendItem.friendMsn != null)// 说明是有联系人MSN的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Im.TYPE_HOME
							,ContactsContract.CommonDataKinds.Im.PROTOCOL
							,""+ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Im.DATA);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.friendMsn.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Im_PROTOCOL_MSN(localDataId,
									remoteFriendItem.friendMsn);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Im.DATA,
						remoteFriendItem.friendMsn);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Im_PROTOCOL_MSN(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendMsn);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的MSN的操作--end--------
		
		
		// ---------联系人好友的个人主页的操作--begin--------
		if (remoteFriendItem.friendURL != null)// 说明是有联系人个人主页的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE							
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Website.URL);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.friendURL.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Website_TYPE_HOMEPAGE(localDataId,
									remoteFriendItem.friendURL);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Website.URL,
						remoteFriendItem.friendURL);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Website_TYPE_HOMEPAGE(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendURL);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的个人主页的操作--end--------
		
		
		
		// ---------联系人好友的公司网址的操作--begin--------
		if (remoteFriendItem.companyURL != null)// 说明是有联系人公司网址的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Website.TYPE_WORK							
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Website.URL);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.companyURL.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Website_TYPE_WORK(localDataId,
									remoteFriendItem.companyURL);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Website.URL,
						remoteFriendItem.companyURL);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Website_TYPE_WORK(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.companyURL);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的公司网址的操作--end--------
		
		
		// ---------联系人好友的公司  职务的操作--begin--------
		if (remoteFriendItem.friendCompany != null || remoteFriendItem.friendPosition!=null)// 说明是有联系人公司的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Organization.TYPE_WORK
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Organization.COMPANY);
				
				String localFriendPosition = localMimeValuesDelta
				.getAsString(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);

				// 判断远端数据和本地数据（friendMsn）
				if (
					(
						remoteFriendItem.friendCompany==null 
						||
						(localNumber != null	&& remoteFriendItem.friendCompany.equals(localNumber))
					)
					&&
					(
						remoteFriendItem.friendPosition==null 
						||
						(localFriendPosition != null	&& remoteFriendItem.friendPosition.equals(localFriendPosition))
					)
				) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_Organization_TYPE_WORK(localDataId,
									remoteFriendItem.friendCompany,remoteFriendItem.friendPosition);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Organization.COMPANY,
						remoteFriendItem.friendCompany);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_Organization_TYPE_WORK(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendCompany,remoteFriendItem.friendPosition);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的公司 职务的操作--end--------
		
		
		
		// ---------联系人好友的省 市 邮编 地址的操作  --begin--------
		//这里简化了 只以地址为主，不考虑 省市 邮编的变化，只要地址变了 就修改 否则都不修改
		if (remoteFriendItem.friendAddress != null )// 说明是有联系人地址的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.friendAddress.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredPostal_FORMATTED_ADDRESS_TYPE_HOME(localDataId,
									remoteFriendItem.friendState,remoteFriendItem.friendCity,
									remoteFriendItem.friendPostalCode,remoteFriendItem.friendAddress);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						remoteFriendItem.friendAddress);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_StructuredPostal_FORMATTED_ADDRESS_TYPE_HOME(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendState,remoteFriendItem.friendCity,
						remoteFriendItem.friendPostalCode,remoteFriendItem.friendAddress);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友的省 市 邮编 地址的操作--end--------
		
		
		
		// ---------联系人好友公司的省 市 邮编 地址的操作  --begin--------
		//这里简化了 只以地址为主，不考虑 省市 邮编的变化，只要地址变了 就修改 否则都不修改
		if (remoteFriendItem.companyAddress != null )// 说明是有联系人地址的操作
		{
			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
			// boolean isFindLocal = false;

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.companyAddress.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredPostal_FORMATTED_ADDRESS_TYPE_WORK(localDataId,
									remoteFriendItem.friendState,remoteFriendItem.friendCity,
									remoteFriendItem.friendPostalCode,remoteFriendItem.companyAddress);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						remoteFriendItem.companyAddress);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_StructuredPostal_FORMATTED_ADDRESS_TYPE_WORK(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendState,remoteFriendItem.friendCity,
						remoteFriendItem.friendPostalCode,remoteFriendItem.companyAddress);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友公司的省 市 邮编 地址的操作--end--------
		
		
		// ---------联系人好友生日的操作  --begin--------
		//这里简化了 只以地址为主，不考虑 省市 邮编的变化，只要地址变了 就修改 否则都不修改
		if (remoteFriendItem.friendBirthday != null )// 说明是有生日的操作
		{

			// 在本地data缓存数据中找到（工作手机）的 data记录行
			AspValuesDelta localMimeValuesDelta = localEentityDelta
					.getSubMimeTypeEntry(
							ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
							,""+ ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
					);

			if (localMimeValuesDelta != null) {

				// 取得本地缓存的friendMsn
				String localNumber = localMimeValuesDelta
						.getAsString(ContactsContract.CommonDataKinds.Event.START_DATE);

				// 判断远端数据和本地数据（friendMsn）
				if (localNumber != null
						&& remoteFriendItem.friendBirthday.equals(localNumber)) {
					// 如果远端数据和本地数据（名字）相同，则不需要处理
				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
				{
					// 利用原来的联系人Data表数据的ID
					int localDataId = Integer.parseInt(localMimeValuesDelta
							.getId());
					ContentProviderOperation.Builder builder = ContentProviderUtil
							.newUpdateData_StructuredPostal_Event_TYPE_BIRTHDAY(localDataId,
									remoteFriendItem.friendBirthday);		
					// 装入操作队列
					diff.add(builder.build());
				}


				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
				// 
				ContentValues remoteDataValues = localMimeValuesDelta
						.getBefore();
				// 
				remoteDataValues.put(
						ContactsContract.CommonDataKinds.Event.START_DATE,
						remoteFriendItem.friendBirthday);
				// after
				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after

			} else {
				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
				ContentProviderOperation.Builder builder;

				// 利用刚才新建的联系人编号 插入Data表联系人名字
				builder = ContentProviderUtil.newInsterData_StructuredPostal_Event_TYPE_BIRTHDAY(
						isNewLocalEentityDelta, rawContactId,
						remoteFriendItem.friendBirthday);

				// 装入操作队列
				diff.add(builder.build());
			}
		}
		// ---------联系人好友生日的操作--end--------
		
		
		
		
		// ---------联系人所属组的操作  --begin--------
		//
		if (remoteFriendItem.typeIdList != null )// 说明是有组的操作
		{
			
			//迭代所属组的 139
			for(String remoteGroupID:remoteFriendItem.typeIdList)
			{
				
				// 在本地data缓存数据中找到（此所属组）的 data记录行  利用原来的139组 ID
				AspValuesDelta localMimeValuesDelta = localEentityDelta
						.getSubMimeTypeEntryFromGroup(
								ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
								,ContactsContract.CommonDataKinds.GroupMembership.DATA2
								,remoteGroupID
						);
				
				
				
				if (localMimeValuesDelta != null) {
				
								// 取得本地缓存的friendMsn
//								String localNumber = localMimeValuesDelta
//										.getAsString(ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID);
//				
//								// 判断远端数据和本地数据（friendMsn）
//								if (localNumber != null
//										&& remoteFriendItem.friendBirthday.equals(localNumber)) {
//									// 如果远端数据和本地数据（名字）相同，则不需要处理
//								} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
//								{
									// 利用原来的联系人Data表数据的ID
									int localDataId = Integer.parseInt(localMimeValuesDelta
											.getId());
									
									//取得原来联系人data表中属于组的本地groupID;
									//String localGroupId = localMimeValuesDelta.getAsString(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID);
									
									ContentProviderOperation.Builder builder = ContentProviderUtil
											.newUpdateData_GroupMembership_GROUP_SOURCE_ID(localDataId,
													i139GroupIdToLocalGroupId.get(remoteGroupID.toString()),remoteGroupID.toString() );		
									// 装入操作队列
									diff.add(builder.build());
//								}
				
				
								// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
								// 
								ContentValues remoteDataValues = localMimeValuesDelta
										.getBefore();
								// 
								remoteDataValues.put(
										ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
										i139GroupIdToLocalGroupId.get(remoteGroupID.toString()));
								
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
										isNewLocalEentityDelta, rawContactId,i139GroupIdToLocalGroupId.get(remoteGroupID.toString()),
										remoteGroupID.toString());
				
								// 装入操作队列
								diff.add(builder.build());
							}
				
				
				
			}
		
		}
		// ---------联系人所属组的操作--end--------
		
		
	}
	
	
//	private static void xx(AspValuesDelta localMimeValuesDelt,String remoteVaue,)
//	{
//		// ---------联系人好友的电话1(其他)的操作--begin--------
//		if (remoteFriendItem.friendOtherTel != null)// 说明是有联系人好友的电话1(其他)的操作
//		{
//			// 本地data数据中是否找对应的数据行的标签 如果找到 是true,如果找不到 需要考虑是添加操作（对于
//			// 网络列表联系人的操作，不需要考虑删除本地data的操作，原来有就还是有）
//			// boolean isFindLocal = false;
//
//			// 在本地data缓存数据中找到（工作手机）的 data记录行
//			AspValuesDelta localMimeValuesDelta = localEentityDelta
//					.getSubMimeTypeEntry(
//							ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
//							""
//									+ ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
//
//			if (localMimeValuesDelta != null) {
//
//				// 取得本地缓存的电话号码
//				String localNumber = localMimeValuesDelta
//						.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
//
//				// 判断远端数据和本地数据（电话）
//				if (localNumber != null
//						&& remoteFriendItem.friendOtherTel.equals(localNumber)) {
//					// 如果远端数据和本地数据（名字）相同，则不需要处理
//				} else// 如果远端数据和本地数据（名字）不同 则需要更新处理
//				{
//					// 利用原来的联系人Data表数据的ID
//					int localDataId = Integer.parseInt(localMimeValuesDelta
//							.getId());
//					ContentProviderOperation.Builder builder = ContentProviderUtil
//							.newUpdateData_Phone_TYPE_ HOME(localDataId,
//									remoteFriendItem.friendOtherTel);		
//					// 装入操作队列
//					diff.add(builder.build());
//				}
//
//
//				// 给本地数据的after添加内容，标志后，之后的删除操作不会再删除
//				// 
//				ContentValues remoteDataValues = localMimeValuesDelta
//						.getBefore();
//				// 
//				remoteDataValues.put(
//						ContactsContract.CommonDataKinds.Phone.NUMBER,
//						remoteFriendItem.friendOtherTel);
//				// after
//				localMimeValuesDelta.getAfter().putAll(remoteDataValues);// 将数据也添加到after
//
//			} else {
//				// 在本地data缓存数据中找不到联系人名字信息行，则需要在data数据中添加
//				ContentProviderOperation.Builder builder;
//
//				// 利用刚才新建的联系人编号 插入Data表联系人名字
//				builder = ContentProviderUtil.newInsterData_Phone_TYPE _HOME(
//						isNewLocalEentityDelta, rawContactId,
//						remoteFriendItem.friendOtherTel);
//
//				// 装入操作队列
//				diff.add(builder.build());
//			}
//		}
//		// ---------联系人好友的电话1(其他)的操作--end--------
//	}
	
	

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
	
	
	private static void buildDiffDelGroupEentityDelta(
			ArrayList<ContentProviderOperation> diff, int groupId) {
		if (groupId > 0 ) {


			// 删除data表 联系人详细信息
//			ContentProviderOperation.Builder builder1 = ContentProviderUtil
//					.newDeleteDataInfo(rawContactId);
//			diff.add(builder1.build());
			
			
			// 删除Group联系人基本信息
			ContentProviderOperation.Builder builder = ContentProviderUtil
					.newDeleteGroup(groupId);
			diff.add(builder.build());
		}
	}
	
	
	
	
	/**
	 * 对比联系人组信息操作
	 * 将 ListFriendItem对象装入一个AspEntityDelta对象，方便后面利用Diff语句 生成ContentProviderOperation 
	 * 此函数操作原则是 不删除data数据，只将新增加的数据更新或者插入进去
	 * 并将ListFriendItem 做成一个数据放入 AspEntityDelta的 mValues的 after
	 * 方便后面判断是否删除，从而生成删除代码
	 * 
	 * @param listFriendItem
	 * @return
	 */
	private static AspEntityDelta buildDiffAspEntityDeltaFromListFriends(
			ArrayList<ContentProviderOperation> diff,
			AspEntityDelta localGrouplEentityDelta,
			GroupItem remoteGroupItem, String account139LoginName) {

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
					.newInsterGroupFrom139(account139LoginName, ""
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

				// 更新Group表
				ContentProviderOperation.Builder builder = ContentProviderUtil
						.newUpdateGroupFrom139(localRawContentID,
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
	
	
	
	
	public static void differFromListFriends(			
			ListFriendsResponeMsg remote,AspSimpleEntitySet localAllSimpleEntitySet,ListFriendsResponeMsg returnNeedAdd,ListFriendsResponeMsg returnNeedupdata,ArrayList returnNeedUpdataLocalID) {
		
		if(returnNeedAdd==null)
		{
			returnNeedAdd = new ListFriendsResponeMsg();
		}
		if(returnNeedUpdataLocalID==null)
		{
			returnNeedUpdataLocalID = new ArrayList();
		}
		
		if (remote != null && remote.friendItemList != null) {
			for (ListFriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
			{
				AspSimpleEntity ase= localAllSimpleEntitySet.getEntityFromRemoteID(""+friendItem.contactId);
				if(ase!=null)
				{
					if( ase.localEdition!=null && friendItem.updateTime!=null && ase.localEdition.equals(friendItem.updateTime))
					{
						//说明完全相同 不需要处理
					}
					else
					{
						//  需要更新
						//添加需要更新的本地数据的ID  留待后续用这个查询本地数据
						returnNeedUpdataLocalID.add(ase.localID);
						returnNeedupdata.friendItemList.add(friendItem);
					}
					
					//能找到匹配的 就将远程版本信息加入  在最后删除的时候就不会删除这些匹配过的数据了
					ase.remoteEdition = friendItem.updateTime;
				}
				else
				{
					//需要新建
					returnNeedAdd.friendItemList.add(friendItem);
				}
			}
		}

	}
	
	
	public static void differFromListFriends(			
			GetInfoByContactIdResponeMsg1 remote,AspSimpleEntitySet localAllSimpleEntitySet,GetInfoByContactIdResponeMsg returnNeedAdd,GetInfoByContactIdResponeMsg returnNeedupdata,ArrayList returnNeedUpdataLocalID) {
		
		if(returnNeedAdd==null)
		{
			returnNeedAdd = new GetInfoByContactIdResponeMsg();
		}
		if(returnNeedUpdataLocalID==null)
		{
			returnNeedUpdataLocalID = new ArrayList();
		}
		
		if (remote != null && remote.friendItemList != null) {
			for (FriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
			{
				AspSimpleEntity ase= localAllSimpleEntitySet.getEntityFromRemoteID(""+friendItem.contactId);
				if(ase!=null)
				{
					if( ase.localEdition!=null && friendItem.updateTime!=null && ase.localEdition.equals(friendItem.updateTime))
					{
						//说明完全相同 不需要处理
					}
					else
					{
						//  需要更新
						//添加需要更新的本地数据的ID  留待后续用这个查询本地数据
						returnNeedUpdataLocalID.add(ase.localID);
						returnNeedupdata.friendItemList.add(friendItem);
					}
					
					//能找到匹配的 就将远程版本信息加入  在最后删除的时候就不会删除这些匹配过的数据了
					ase.remoteEdition = friendItem.updateTime;
				}
				else
				{
					//需要新建
					returnNeedAdd.friendItemList.add(friendItem);
				}
			}
		}

	}




public static void differFromListFriends1(			
		GetInfoByContactIdResponeMsg1 remote,AspSimpleEntitySet localAllSimpleEntitySet,GetInfoByContactIdResponeMsg1 returnNeedAdd,GetInfoByContactIdResponeMsg1 returnNeedupdata,ArrayList returnNeedUpdataLocalID) {
	
	if(returnNeedAdd==null)
	{
		returnNeedAdd = new GetInfoByContactIdResponeMsg1();
	}
	if(returnNeedUpdataLocalID==null)
	{
		returnNeedUpdataLocalID = new ArrayList();
	}
	
	if (remote != null && remote.friendItemList != null) {
		for (FriendItem friendItem : remote.friendItemList)// 用远端的来对迭代，和本地的对比
		{
			AspSimpleEntity ase= localAllSimpleEntitySet.getEntityFromRemoteID(""+friendItem.contactId);
			if(ase!=null)
			{
				if( ase.localEdition!=null && friendItem.updateTime!=null && ase.localEdition.equals(friendItem.updateTime))
				{
					//说明完全相同 不需要处理
				}
				else
				{
					//  需要更新
					//添加需要更新的本地数据的ID  留待后续用这个查询本地数据
					returnNeedUpdataLocalID.add(ase.localID);
					returnNeedupdata.friendItemList.add(friendItem);
				}
				
				//能找到匹配的 就将远程版本信息加入  在最后删除的时候就不会删除这些匹配过的数据了
				ase.remoteEdition = friendItem.updateTime;
			}
			else
			{
				//需要新建
				returnNeedAdd.friendItemList.add(friendItem);
			}
		}
	}

}


}
