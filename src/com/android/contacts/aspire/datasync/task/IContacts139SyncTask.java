package com.android.contacts.aspire.datasync.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.android.contacts.ContactsListActivity;
import com.android.contacts.DialtactsActivity;
import com.android.contacts.R;
import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.icontact139.IContact139Operation;
import com.android.contacts.aspire.datasync.icontact139.TranslateToolBy139;
import com.android.contacts.aspire.datasync.icontact139.model.IContact139Account;
import com.android.contacts.aspire.datasync.model.AspEntityDelta;
import com.android.contacts.aspire.datasync.model.AspEntitySet;
import com.android.contacts.aspire.datasync.model.AspSimpleEntitySet;
import com.android.contacts.aspire.datasync.sim.SimOperation;
import com.android.contacts.aspire.datasync.sim.TranslateToolBySim;
import com.android.contacts.aspire.datasync.sim.model.SimContactList;
import com.android.contacts.aspire.datasync.util.ContentProviderUtil;
import com.android.contacts.aspire.msg.respone.FriendItem;
import com.android.contacts.aspire.msg.respone.GetCountInfoResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg1;
import com.android.contacts.aspire.msg.respone.GroupItem;
import com.android.contacts.aspire.msg.respone.ListFriendsResponeMsg;


/**  
 * Filename:    IContacts139SyncTask.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-2 下午04:58:10  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-2    liangbo             1.0        1.0 Version  
 */

public class IContacts139SyncTask extends BaseTask{
	
	
	
	private static final int ID_NOTIFICATION = 100001;
	
	//如果没有有效账号的时候  任务管理需要一个默认账号
	public static final String DEFAULT_ACCOUNT="139_ACCOUNT_DEFAULT";
	
	Context appContext;
	
	
	//声明通知管理器
	NotificationManager mNotificationManager;
	Notification notification_begin;
	Notification notification_end;
	private final int NOTE_BEGIN_ID = 7749;
	private final int NOTE_END_ID = 7750;
	

	public IContacts139SyncTask(String taskAccount, Context appContext) {
		super(Config.ACCOUNT_TYPE_139, taskAccount);
		this.appContext = appContext;
		mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notification_begin = new Notification();
		notification_end= new Notification();
	}
	
	
	
	
	@Override
	public void taskExecute() {
		//登录139
		long TIME_LOGIN139_BEGIN;
		long TIME_LOGIN139_END;
		
		//删除非此用户的139联系人（联系人+组）
		long TIME_DEL139OUT_BEGIN;
		long TIME_DEL139OUT_END;
		
		//读取本地联系人
		long TIME_READLOCGROUP_BEGIN;
		long TIME_READLOCGROUP_END;
		
		//获取网络139联系人组数据
		long TIME_GET139GROUP_BEGIN;
		long TIME_GET139GROUP_END;
		
		//对比联系人组 生成添加修改代码
		long TIME_BIFF_ADDUPDATA_GROUP_BEGIN;
		long TIME_BIFF_ADDUPDATA_GROUP_END;
		
		//执行联系人组 生成添加修改代码
		long TIME_APPLY_ADDUPDATA_GROUP_BEGIN;
		long TIME_APPLY_ADDUPDATA_GROUP_END;
		
		//对比生成联系人组删除代码
		long TIME_BIFF_DEL_GROUP_BEGIN;
		long TIME_BIFF_DEL_GROUP_END;
		
		//执行联系人组删除代码
		long TIME_APPLY_DEL_GROUP_BEGIN;
		long TIME_APPLY_DEL_GROUP_END;
		
		//建立139和本地的组的ID映射
		long TIME_MAP_LOC139GROUPID_BEGIN;
		long TIME_MAP_LOC139GROUPID_END;
		
		//读取本地的139基本信息
		long TIME_READ_LOC139BASE_BEGIN;
		long TIME_READ_LOC139BASE_END;
		
		//读取网络139基本信息
		long TIME_GET_LOC139BASE_BEGIN=0;
		long TIME_GET_LOC139BASE_END=0;
		
		//对比本地和网络基本信息 生产 添加和更新
		long TIME_DIFF_ADDUPDATALOC139BASE_BEGIN=0;
		long TIME_DIFF_ADDUPDATALOC139BASE_END=0;
		
		//执行添加和更新本地基本数据
		long TIME_APPLY_ADDUPDATALOC139BASE_BEGIN=0;
		long TIME_APPLY_ADDUPDATALOC139BASE_END=0;
		
		//生成基本信息 删除
		long TIME_DIFF_DELLOC139BASE_BEGIN;
		long TIME_DIFF_DELLOC139BASE_END;
		
		//执行基本信息删除
		long TIME_APPLY_DELLOC139BASE_BEGIN;
		long TIME_APPLY_DELLOC139BASE_END;
		
		//再次读取本地基本信息
		long TIME_READ_LOC139BASE_BEGIN2;
		long TIME_READ_LOC139BASE_END2;
		
		//读取139详情
		long TIME_GET_LOC139INFO_BEGIN=0;
		long TIME_GET_LOC139INFO_END=0;
		
		//迭代给详情添加默认组
		long TIME_LOCADDEFAULTDGROUP_BEGIN=0;
		long TIME_LOCADDEFAULTDGROUP_END=0;
		
		//对比和取得本地详情与服务器生成添加和更新
		long TIME_DIFF_ADDUPDATALOC139INFO_BEGIN=0;
		long TIME_DIFF_ADDUPDATALOC139INFO_END=0;
		
		
		//执行详情更新的添加
		long TIME_APPLY_ADDUPDATALOC139INFO_BAGIN=0;
		long TIME_APPLY_ADDUPDATALOC139INFO_END=0;
		
		//对比生成删除联系人详情
		long TIME_BIFF_DELLOC139INFO_BEGIN;
		long TIME_BIFF_DELLOC139INFO_END;
		
		//执行删除联系人详情
		long TIME_APPLY_DELLOC139INFO_BEGIN;
		long TIME_APPLY_DELLOC139INFO_END;
		
		
		//139同步任务开始
		System.out.println("-----------Begin 139 SynTask  Account:"+this.getTaskAccount());
		
		/* --  弹出提示任务 begin   -- */
		/*NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon_msg; //通知图标
		CharSequence tickerText = "开始同步网络数据";  //状态栏(Status Bar)显示的通知文本提示
		long when = System.currentTimeMillis(); //通知产生的时间，会在通知信息里显示
		Notification notification = new Notification();
		notification.tickerText = tickerText;
		//notification.when =System.currentTimeMillis();
		//notification.icon = icon;
		//notification.defaults = Notification.DEFAULT_SOUND;
		mNotificationManager.notify(0, notification);*/
		
		
		notification_begin.tickerText = "开始同步网络数据";  //状态栏(Status Bar)显示的通知文本提示
		notification_begin.icon = R.drawable.ic_notify; //通知图标
		Intent m_Intent = new Intent(appContext, DialtactsActivity.class);
		PendingIntent m_PendingIntent = PendingIntent.getActivity(appContext, 0, m_Intent, 0);
		notification_begin.setLatestEventInfo(appContext, "网络数据同步通知", "正在同步网络数据，请稍候...",m_PendingIntent );
		
		mNotificationManager.notify(NOTE_BEGIN_ID, notification_begin);
		/* --  弹出提示任务 end   -- */
		
		/* --  139账号检测 begin   -- */
		TIME_LOGIN139_BEGIN = System.currentTimeMillis();
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		
		// 检查看是不是当前这个任务联系人的登录状态
		if (this.getTaskAccount() != null
				&& IContact139Operation.get139AccountState(this
						.getTaskAccount()) == IContact139Account.ACCOUNT_STATE_LOGIN) {
			// 已经登录  继续
		} else {
			// 如果是没有账号或账号是默认账号
			if (this.getTaskAccount() == null
					|| this.getTaskAccount().length() == 0
					|| this.getTaskAccount().equals(DEFAULT_ACCOUNT)) {
				// 说明这是一个登出任务 说明用户没有取得网络联系人的需求

				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				// 删除全部139缓存数据
				ContentProviderUtil.delProvider139Data(appContext
							.getContentResolver());				
				return;

			} else {
				// 说明用户切换了139用户 或再次登录
				int error = IContact139Operation.login(this.getTaskAccount());
				if (error == IContact139Account.ACCOUNT_STATE_LOGIN) {
					// 登录成功 继续
				} else {
					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
					// 删除本地所有139联系人缓存用户
					ContentProviderUtil.delProvider139Data(appContext
								.getContentResolver());					
					// 登录失败 退出
					return;
				}
			}
		}
		TIME_LOGIN139_END = System.currentTimeMillis();
		/* --  139账号检测  end   -- */
		
		
		/* --  同步前清理数据 begin   -- */	
		TIME_DEL139OUT_BEGIN = System.currentTimeMillis();
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		// 删除本地非此139账户的缓存的其他的139账户联系人信息
		ContentProviderUtil.delProvider139DataOutOfId(appContext
					.getContentResolver(), getTaskAccount());
		TIME_DEL139OUT_END = System.currentTimeMillis();
		
		/* --  同步前清理数据 end   -- */
		
		/* --  联系人组信息处理 begin   -- */
		
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		//读取本地缓存的此139账号的联系人组信息
		TIME_READLOCGROUP_BEGIN = System.currentTimeMillis();
		AspEntitySet  i139GroupEntitySet;
		i139GroupEntitySet = AspEntitySet.fromQueryWith139Group(appContext
					.getContentResolver(),getTaskAccount(), null);	
		TIME_READLOCGROUP_END = System.currentTimeMillis();	
		
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		// 读取139联系人数量  以及联系人组的相关信息
		TIME_GET139GROUP_BEGIN = System.currentTimeMillis();	
		GetCountInfoResponeMsg resp = new GetCountInfoResponeMsg();
		int error = IContact139Operation.getFriendsCount(resp);
		if (error == ErrorInfo.SUCCESS
				&& resp.getErrorResponse().error_code == ErrorInfo.SUCCESS) {
			// 继续
		} else {
			// 取得联系人数量错误
			return;
		}
		TIME_GET139GROUP_END = System.currentTimeMillis();
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		TIME_BIFF_ADDUPDATA_GROUP_BEGIN = System.currentTimeMillis();
		//对比本地组合远端组信息  生成添加和修改					
		// 对比此批次的联系人列表信息
		ArrayList<ContentProviderOperation> cpo = TranslateToolBy139
				.buildDiffFromListFriendGroups(this.getTaskAccount(),
						i139GroupEntitySet, resp);
		TIME_BIFF_ADDUPDATA_GROUP_END = System.currentTimeMillis();
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		// 执行更新 只涉及到修改和增加 执行后 利用数据变动 会更新界面
		TIME_APPLY_ADDUPDATA_GROUP_BEGIN = System.currentTimeMillis();
		try {
			this.appContext.getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, cpo);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		TIME_APPLY_ADDUPDATA_GROUP_END = System.currentTimeMillis();
		
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		TIME_BIFF_DEL_GROUP_BEGIN = System.currentTimeMillis();
		//循环完成后  执行删除一起本地需要删除的联系人组
		ArrayList<ContentProviderOperation> cpoDelGroup =  TranslateToolBy139.buildDelDiffFromGroupEntitySet(this.getTaskAccount(), i139GroupEntitySet);
		TIME_BIFF_DEL_GROUP_END = System.currentTimeMillis();
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		
		TIME_APPLY_DEL_GROUP_BEGIN = System.currentTimeMillis();
		//执行更新  只涉及到删除 执行后  利用数据变动 会更新界面
		try {
			 this.appContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoDelGroup);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TIME_APPLY_DEL_GROUP_END = System.currentTimeMillis();
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		
		TIME_MAP_LOC139GROUPID_BEGIN = System.currentTimeMillis();
		//重新读取一遍组数据  得到新的本地组和远程组的ID对应信息
		HashMap<String,String> i139GroupIdToLocalGroupId = new HashMap<String,String>();
		//读取本地缓存的此139账号的联系人组信息
		AspEntitySet  i139GroupEntitySetNew;
		i139GroupEntitySetNew = AspEntitySet.fromQueryWith139Group(appContext
				.getContentResolver(), getTaskAccount(), null);

		if (i139GroupEntitySetNew != null) {
			for (AspEntityDelta groupEntityDelta : i139GroupEntitySetNew) {
				String localGroupId = groupEntityDelta.mValues
						.getAsString(ContactsContract.Groups._ID);
				String i139GroupId = groupEntityDelta.mValues
						.getAsString(ContactsContract.Groups.SOURCE_ID);
				// 压入我们的 远程组ID和本地组ID的对应工具
				i139GroupIdToLocalGroupId.put(i139GroupId, localGroupId);
			}
		}
		
	
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		//判断并且添加一个未分组的默认组		
		ArrayList<String> unMenberGroupId = new ArrayList<String>(); 
		int error1 =  ContentProviderUtil.createProviderGroupUnMember(appContext.getContentResolver(),unMenberGroupId);
		if(error1==ErrorInfo.SUCCESS && unMenberGroupId.size()>0)
		{
			 String localGroupId = 	unMenberGroupId.get(0);
			 String iUnMenberGroupId = 	Config.DEFAULT_GROUP_SOURCE_ID;
			//压入我们的 远程组ID和本地组ID的对应工具
			 i139GroupIdToLocalGroupId.put(iUnMenberGroupId, localGroupId);
		}
		TIME_MAP_LOC139GROUPID_END = System.currentTimeMillis();
		
		/* --  联系人组信息处理 end   -- */
		
		
		/* --  联系人基本信息处理 begin   -- */
		
		//查询本地全部联系人简要信息表，用来记录本地的全部联系人 ID，在和服务器对比后，记录标志，如果无标识的就是服务器有，本地没有的 ，需要删除
		//key是本地的 联系人ID,value是139的联系人ID  本地保存原来139版本（时间） 此次修改的时间 
		TIME_READ_LOC139BASE_BEGIN = System.currentTimeMillis();
		AspSimpleEntitySet localAllSimpleEntitySet = AspSimpleEntitySet.fromQueryWith139(appContext
				.getContentResolver(),getTaskAccount(),null) ;
		TIME_READ_LOC139BASE_END = System.currentTimeMillis();
		//循环分组 去查询列表联系人信息
		final int PAGE_SIZE = Config.SYNC_PAGE_SIZE;
		//需要去139请求的次数
		int get139Count = 0;
		if(resp.total!=0)
		{
			get139Count = ((int)resp.total/PAGE_SIZE)+(resp.total%PAGE_SIZE==0?0:1);;
		}
		
				
		//存储分段从139取得联系人的简要信息的联系人的139联系人ID  
		String[] contactIdsArray = new String[get139Count];
		
		
		for(int i=0;i<get139Count;i++)
		{
			if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
			TIME_GET_LOC139BASE_BEGIN = System.currentTimeMillis();
				// 发消息得到一批的联系人简要信息
			ListFriendsResponeMsg lfresp = new ListFriendsResponeMsg();
			int errorCode = IContact139Operation.listFriends(i + 1, PAGE_SIZE,
					lfresp);
			TIME_GET_LOC139BASE_END = System.currentTimeMillis();
			
			// 此处判断我们的消息发送成功 并且139返回的消息也是成功的（错误消息成功 没有错误消息时 响应消息的错误码成功）
			if (errorCode == ErrorInfo.SUCCESS
					&& lfresp.getErrorResponse().error_code == ErrorInfo.SUCCESS) {
				
				// 需要查询的联系人详细信息时候使用的IDs
				contactIdsArray[i] = lfresp.get139ContactsId();
				
				ArrayList<ContentProviderOperation> cpoTsum = new ArrayList<ContentProviderOperation>();
				
				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				//和总的对比，确定出那些是需要添加的  那些是需要删除的  那些是需要更新的 
				//需要添加到本地的139的新的联系人
				ListFriendsResponeMsg needAddFriendsResponeMsg =new ListFriendsResponeMsg();
				//需要更新到本地的139的新的联系人
				ListFriendsResponeMsg needUpdataFriendsResponeMsg =new ListFriendsResponeMsg();
				//需要更新的 本地的联系人的 ID
				ArrayList needUpdataLocalID = new ArrayList();
				TIME_DIFF_ADDUPDATALOC139BASE_BEGIN = System.currentTimeMillis();
				TranslateToolBy139.differFromListFriends(lfresp,localAllSimpleEntitySet,needAddFriendsResponeMsg,needUpdataFriendsResponeMsg,needUpdataLocalID);
				
				//生成添加
				{
					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
					// 对比此批次的联系人列表信息 用本地为null装入对比 就会生成新建本地的操作
					ArrayList<ContentProviderOperation> cpo1 = TranslateToolBy139
							.buildDiffFromListFriends(this.getTaskAccount(),
									AspEntitySet.fromNull(), needAddFriendsResponeMsg);
					cpoTsum.addAll(cpo1);
				}
				
				//生成更新
				{
					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
					// 读取本地缓存的此139账号的联系人信息 TEMP
					AspEntitySet i139EntitySet;
                    //根据更新的ID 去取的制定的本地联系人详情
					i139EntitySet = AspEntitySet.fromQueryWith139(appContext
							.getContentResolver(), getTaskAccount(), null,needUpdataLocalID);
					
					
					// 对比此批次的联系人列表信息
					ArrayList<ContentProviderOperation> cpo1 = TranslateToolBy139
							.buildDiffFromListFriends(this.getTaskAccount(),
									i139EntitySet, needUpdataFriendsResponeMsg);
					cpoTsum.addAll(cpo1);
				}
				TIME_DIFF_ADDUPDATALOC139BASE_END = System.currentTimeMillis();
				
				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				TIME_APPLY_ADDUPDATALOC139BASE_BEGIN = System.currentTimeMillis();
				// 执行更新 只涉及到修改和增加 执行后 利用数据变动 会更新界面
				try {
					this.appContext.getContentResolver().applyBatch(
							ContactsContract.AUTHORITY, cpoTsum);

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TIME_APPLY_ADDUPDATALOC139BASE_END = System.currentTimeMillis();
			}
				
		}
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		//循环完成后  执行删除一起本地需要删除的联系人
		TIME_DIFF_DELLOC139BASE_BEGIN = System.currentTimeMillis();
		ArrayList<ContentProviderOperation> cpoDel =  TranslateToolBy139.buildDelDiffFromEntityIds(this.getTaskAccount(), localAllSimpleEntitySet);
		TIME_DIFF_DELLOC139BASE_END = System.currentTimeMillis();
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		
		TIME_APPLY_DELLOC139BASE_BEGIN = System.currentTimeMillis();
		//执行更新  只涉及到删除  利用数据变动 会更新界面
		try {
			 this.appContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoDel);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TIME_APPLY_DELLOC139BASE_END = System.currentTimeMillis();

		
		
		/* --  联系人基本信息处理 end   -- */
		
		if(true){
		/* --  联系人详细信息处理 begin   -- */
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		//再次读取本地缓存的此139账号的全部信息的简要联系人信息 
		TIME_READ_LOC139BASE_BEGIN2 = System.currentTimeMillis();
		localAllSimpleEntitySet = AspSimpleEntitySet.fromQueryWith139(appContext
				.getContentResolver(),getTaskAccount(),null) ;
		TIME_READ_LOC139BASE_END2 = System.currentTimeMillis();
		
		//再次循环一遍 取得联系人呢的详细信息
		for(int i=0;i<get139Count;i++)
		{
			if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
			
			TIME_GET_LOC139INFO_BEGIN = System.currentTimeMillis();
			//发消息得到一批的联系人详细信息
			GetInfoByContactIdResponeMsg1 lfInforesp = new GetInfoByContactIdResponeMsg1();
			int errorCode = IContact139Operation.getInfoByContactId1(contactIdsArray[i], lfInforesp);
			TIME_GET_LOC139INFO_END = System.currentTimeMillis();
			int c = 0;
			
			//此处判断我们的消息发送成功  并且139返回的消息也是成功的（错误消息成功   没有错误消息时 响应消息的错误码成功）
			if(errorCode==ErrorInfo.SUCCESS && lfInforesp.getErrorResponse().error_code==ErrorInfo.SUCCESS
					&& lfInforesp.result==ErrorInfo.IC139_OP_SUCCESS)
			{
				
				//如果成功  则迭代判断一遍  若不存在有组的用户则换个消息结构再解析一遍
//				for( FriendItem friendItem:lfInforesp.friendItemList)
//				{
//					
//					if(friendItem.typeIdList!=null ) {
//						c=1;
//						break;
//					}
//				}
				
//				if(c==0){
//				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//				//换个消息结构再解析一遍
//				GetInfoByContactIdResponeMsg1 lfInforesp1 = new GetInfoByContactIdResponeMsg1();
//				int errorCode1 = IContact139Operation.getInfoByContactId1(contactIdsArray[i], lfInforesp1);
//				if(errorCode1==ErrorInfo.SUCCESS && lfInforesp1.getErrorResponse().error_code==ErrorInfo.SUCCESS
//							&& lfInforesp1.result==ErrorInfo.IC139_OP_SUCCESS){
//				
//				//如果成功 再迭代判断一遍  给没有组的用户添加上默认组
//				for( FriendItem friendItem:lfInforesp1.friendItemList)
//				{
//					if(friendItem.typeIdList==null || friendItem.typeIdList.size()==0)
//					{
//						
//						 ArrayList<String> typeIdList=new ArrayList<String>();
//						 typeIdList.add(Config.DEFAULT_GROUP_SOURCE_ID);
//						 
//						 friendItem.typeIdList= typeIdList;
//					}
//					
//					//如果是网络上未分组，则替换为默认组
//					if(friendItem.typeIdList.contains("0")){
//						friendItem.typeIdList.clear();
//						ArrayList<String> typeIdList=new ArrayList<String>();
//						typeIdList.add(Config.DEFAULT_GROUP_SOURCE_ID);
//						friendItem.typeIdList= typeIdList;
//					}
//				}
//								
//				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//				
//				
//				ArrayList<ContentProviderOperation> cpoTsum = new ArrayList<ContentProviderOperation>();
//				
//				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//				//和总的对比，确定出那些是需要添加的  那些是需要删除的  那些是需要更新的 
//				//需要添加到本地的139的新的联系人
//				GetInfoByContactIdResponeMsg1 needAddFriendsResponeMsg =new GetInfoByContactIdResponeMsg1();
//				//需要更新到本地的139的新的联系人
//				GetInfoByContactIdResponeMsg1 needUpdataFriendsResponeMsg =new GetInfoByContactIdResponeMsg1();
//				//需要更新的 本地的联系人的 ID
//				ArrayList needUpdataLocalID = new ArrayList();
//				TranslateToolBy139.differFromListFriends1(lfInforesp1,localAllSimpleEntitySet,needAddFriendsResponeMsg,needUpdataFriendsResponeMsg,needUpdataLocalID);
//
//				//生成添加
//				{
//					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//					// 对比此批次的联系人列表信息 用本地为null装入对比 就会生成新建本地的操作
//					ArrayList<ContentProviderOperation> cpo3 =  TranslateToolBy139.buildDiffFromContactsInfo1(this.getTaskAccount(), AspEntitySet.fromNull(), needAddFriendsResponeMsg,i139GroupIdToLocalGroupId);
//					
//					cpoTsum.addAll(cpo3);
//				}
//				
//				
//				//生成更新
//				{
//					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//					// 读取本地缓存的此139账号的联系人信息 TEMP
//					AspEntitySet i139EntitySet;
//                    //根据更新的ID 去取的制定的本地联系人详情
//					i139EntitySet = AspEntitySet.fromQueryWith139(appContext
//							.getContentResolver(), getTaskAccount(), null,needUpdataLocalID);
//					
//					
//					// 对比此批次的联系人列表信息
//					ArrayList<ContentProviderOperation> cpo4 =  TranslateToolBy139.buildDiffFromContactsInfo1(this.getTaskAccount(), i139EntitySet, needUpdataFriendsResponeMsg,i139GroupIdToLocalGroupId);
//
//					cpoTsum.addAll(cpo4);
//				}
//				
//				
//				//对比此批次的联系人列表信息
//				//ArrayList<ContentProviderOperation> cpo3 =  TranslateToolBy139.buildDiffFromContactsInfo(this.getTaskAccount(), i139EntitySet, lfInforesp,i139GroupIdToLocalGroupId);
//				
//				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
//				
//				//执行更新  只涉及到修改和增加  执行后  利用数据变动 会更新界面
//				try {
//					 this.appContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoTsum);
//					
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (OperationApplicationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				}	
//			}else if(c==1)
				{
				//不用再解析了 再迭代判断一遍  给没有组的用户添加上默认组
				TIME_LOCADDEFAULTDGROUP_BEGIN = System.currentTimeMillis();
				for( FriendItem friendItem:lfInforesp.friendItemList)
				{
					if(friendItem.typeIdList==null || friendItem.typeIdList.size()==0)
					{
						
						 ArrayList<String> typeIdList=new ArrayList<String>();
						 typeIdList.add(Config.DEFAULT_GROUP_SOURCE_ID);
						 
						 friendItem.typeIdList= typeIdList;
					}
					//如果是网络上未分组，则替换为默认组
					if(friendItem.typeIdList.contains("0")){
						friendItem.typeIdList.clear();
						ArrayList<String> typeIdList=new ArrayList<String>();
						typeIdList.add(Config.DEFAULT_GROUP_SOURCE_ID);
						friendItem.typeIdList= typeIdList;
					}
				}
				TIME_LOCADDEFAULTDGROUP_END = System.currentTimeMillis();
								
				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				
				
				ArrayList<ContentProviderOperation> cpoTsum = new ArrayList<ContentProviderOperation>();
				
				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				TIME_DIFF_ADDUPDATALOC139INFO_BEGIN =  System.currentTimeMillis();
				//和总的对比，确定出那些是需要添加的  那些是需要删除的  那些是需要更新的 
				//需要添加到本地的139的新的联系人
				GetInfoByContactIdResponeMsg needAddFriendsResponeMsg =new GetInfoByContactIdResponeMsg();
				//需要更新到本地的139的新的联系人
				GetInfoByContactIdResponeMsg needUpdataFriendsResponeMsg =new GetInfoByContactIdResponeMsg();
				//需要更新的 本地的联系人的 ID
				ArrayList needUpdataLocalID = new ArrayList();
				TranslateToolBy139.differFromListFriends(lfInforesp,localAllSimpleEntitySet,needAddFriendsResponeMsg,needUpdataFriendsResponeMsg,needUpdataLocalID);

				//生成添加
				{
					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
					// 对比此批次的联系人列表信息 用本地为null装入对比 就会生成新建本地的操作
					ArrayList<ContentProviderOperation> cpo3 =  TranslateToolBy139.buildDiffFromContactsInfo(this.getTaskAccount(), AspEntitySet.fromNull(), needAddFriendsResponeMsg,i139GroupIdToLocalGroupId);
					
					cpoTsum.addAll(cpo3);
				}
				
				
				//生成更新
				{
					if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
					// 读取本地缓存的此139账号的联系人信息 TEMP
					AspEntitySet i139EntitySet;
                    //根据更新的ID 去取的制定的本地联系人详情
					i139EntitySet = AspEntitySet.fromQueryWith139(appContext
							.getContentResolver(), getTaskAccount(), null,needUpdataLocalID);
					
					
					// 对比此批次的联系人列表信息
					ArrayList<ContentProviderOperation> cpo4 =  TranslateToolBy139.buildDiffFromContactsInfo(this.getTaskAccount(), i139EntitySet, needUpdataFriendsResponeMsg,i139GroupIdToLocalGroupId);

					cpoTsum.addAll(cpo4);
				}
				TIME_DIFF_ADDUPDATALOC139INFO_END =  System.currentTimeMillis();
				
				//对比此批次的联系人列表信息
				//ArrayList<ContentProviderOperation> cpo3 =  TranslateToolBy139.buildDiffFromContactsInfo(this.getTaskAccount(), i139EntitySet, lfInforesp,i139GroupIdToLocalGroupId);
				
				if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
				TIME_APPLY_ADDUPDATALOC139INFO_BAGIN =  System.currentTimeMillis();
				
				//执行更新  只涉及到修改和增加  执行后  利用数据变动 会更新界面
				try {
					 this.appContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoTsum);
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TIME_APPLY_ADDUPDATALOC139INFO_END =  System.currentTimeMillis();
			}
			}
			
		
		}
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		//循环完成后  执行删除一起本地需要删除的联系人
		TIME_BIFF_DELLOC139INFO_BEGIN =  System.currentTimeMillis();
		ArrayList<ContentProviderOperation> cpoDel1 =  TranslateToolBy139.buildDelDiffFromEntityIds(this.getTaskAccount(), localAllSimpleEntitySet);
		TIME_BIFF_DELLOC139INFO_END =  System.currentTimeMillis();
		
		if (this.getTaskState() != this.TASK_STATE_RUN) return; // 如果不是运行状态退出
		
		//执行更新  只涉及到删除  利用数据变动 会更新界面
		TIME_APPLY_DELLOC139INFO_BEGIN =  System.currentTimeMillis();
		try {
			 this.appContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoDel1);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TIME_APPLY_DELLOC139INFO_END =  System.currentTimeMillis();
		
		/* --  联系人详细信息处理 end   -- */
		}
		
		/* --  XX begin   -- */
		/* --  XX end   -- */
		
		
		//登录139
		System.out.println("TIME_LOGIN139 = "+(TIME_LOGIN139_BEGIN-TIME_LOGIN139_END));
//		long TIME_LOGIN139_BEGIN;
//		long TIME_LOGIN139_END;
		
		//删除非此用户的139联系人（联系人+组）
		System.out.println("TIME_DEL139OUT = "+(TIME_DEL139OUT_BEGIN-TIME_DEL139OUT_END));
//		long TIME_DEL139OUT_BEGIN;
//		long TIME_DEL139OUT_END;
		
		//读取本地联系人组
		System.out.println("TIME_READLOCGROUP = "+(TIME_READLOCGROUP_BEGIN-TIME_READLOCGROUP_END));
//		long TIME_READLOCGROUP_BEGIN;
//		long TIME_READLOCGROUP_END;
		
		//获取网络139联系人组数据
		System.out.println("TIME_GET139GROUP = "+(TIME_GET139GROUP_BEGIN-TIME_GET139GROUP_END));
//		long TIME_GET139GROUP_BEGIN;
//		long TIME_GET139GROUP_END;
		
		//对比联系人组 生成添加修改代码
		System.out.println("TIME_BIFF_ADDUPDATA_GROUP = "+(TIME_BIFF_ADDUPDATA_GROUP_BEGIN-TIME_BIFF_ADDUPDATA_GROUP_END));
//		long TIME_BIFF_ADDUPDATA_GROUP_BEGIN;
//		long TIME_BIFF_ADDUPDATA_GROUP_END;
		
		//执行联系人组 生成添加修改代码
		System.out.println("TIME_APPLY_ADDUPDATA_GROUP = "+(TIME_APPLY_ADDUPDATA_GROUP_BEGIN-TIME_APPLY_ADDUPDATA_GROUP_END));
//		long TIME_APPLY_ADDUPDATA_GROUP_BEGIN;
//		long TIME_APPLY_ADDUPDATA_GROUP_END;
		
		//对比生成联系人组删除代码
		System.out.println("TIME_BIFF_DEL_GROUP = "+(TIME_BIFF_DEL_GROUP_BEGIN-TIME_BIFF_DEL_GROUP_END));
//		long TIME_BIFF_DEL_GROUP_BEGIN;
//		long TIME_BIFF_DEL_GROUP_END;
		
		//执行联系人组删除代码
		System.out.println("TIME_APPLY_DEL_GROUP = "+(TIME_APPLY_DEL_GROUP_BEGIN-TIME_APPLY_DEL_GROUP_END));
//		long TIME_APPLY_DEL_GROUP_BEGIN;
//		long TIME_APPLY_DEL_GROUP_END;
		
		//建立139和本地的组的ID映射
		System.out.println("TIME_MAP_LOC139GROUPID = "+(TIME_MAP_LOC139GROUPID_BEGIN-TIME_MAP_LOC139GROUPID_END));
//		long TIME_MAP_LOC139GROUPID_BEGIN;
//		long TIME_MAP_LOC139GROUPID_END;
		
		//读取本地的139基本信息
		System.out.println("TIME_READ_LOC139BASE = "+(TIME_READ_LOC139BASE_BEGIN-TIME_READ_LOC139BASE_END));
//		long TIME_READ_LOC139BASE_BEGIN;
//		long TIME_READ_LOC139BASE_END;
		
		//读取网络139基本信息
		System.out.println("TIME_GET_LOC139BASE = "+(TIME_GET_LOC139BASE_BEGIN-TIME_GET_LOC139BASE_END));
//		long TIME_GET_LOC139BASE_BEGIN;
//		long TIME_GET_LOC139BASE_END;
		
		//对比本地和网络基本信息 生产 添加和更新
		System.out.println("TIME_DIFF_ADDUPDATALOC139BASE = "+(TIME_DIFF_ADDUPDATALOC139BASE_BEGIN-TIME_DIFF_ADDUPDATALOC139BASE_END));
//		long TIME_DIFF_ADDUPDATALOC139BASE_BEGIN;
//		long TIME_DIFF_ADDUPDATALOC139BASE_END;
		
		//执行添加和更新本地基本数据
		System.out.println("TIME_APPLY_ADDUPDATALOC139BASE = "+(TIME_APPLY_ADDUPDATALOC139BASE_BEGIN-TIME_APPLY_ADDUPDATALOC139BASE_END));
//		long TIME_APPLY_ADDUPDATALOC139BASE_BEGIN;
//		long TIME_APPLY_ADDUPDATALOC139BASE_END;
		
		//生成基本信息 删除
		System.out.println("TIME_DIFF_DELLOC139BASE = "+(TIME_DIFF_DELLOC139BASE_BEGIN-TIME_DIFF_DELLOC139BASE_END));
//		long TIME_DIFF_DELLOC139BASE_BEGIN;
//		long TIME_DIFF_DELLOC139BASE_END;
		
		//执行基本信息删除
		System.out.println("TIME_APPLY_DELLOC139BASE = "+(TIME_APPLY_DELLOC139BASE_BEGIN-TIME_APPLY_DELLOC139BASE_END));
//		long TIME_APPLY_DELLOC139BASE_BEGIN;
//		long TIME_APPLY_DELLOC139BASE_END;
		
		//再次读取本地基本信息
		System.out.println("TIME_READ_LOC139BASE = "+(TIME_READ_LOC139BASE_BEGIN2-TIME_READ_LOC139BASE_END2));
//		long TIME_READ_LOC139BASE_BEGIN2;
//		long TIME_READ_LOC139BASE_END2;
		
		//读取139详情
		System.out.println("TIME_GET_LOC139INFO = "+(TIME_GET_LOC139INFO_BEGIN-TIME_GET_LOC139INFO_END));
//		long TIME_GET_LOC139INFO_BEGIN;
//		long TIME_GET_LOC139INFO_END;
		
		//迭代给详情添加默认组
		System.out.println("TIME_LOCADDEFAULTDGROUP = "+(TIME_LOCADDEFAULTDGROUP_BEGIN-TIME_LOCADDEFAULTDGROUP_END));
//		long TIME_LOCADDEFAULTDGROUP_BEGIN;
//		long TIME_LOCADDEFAULTDGROUP_END;
		
		//对比和取得本地详情与服务器生成添加和更新
		System.out.println("TIME_DIFF_ADDUPDATALOC139INFO = "+(TIME_DIFF_ADDUPDATALOC139INFO_BEGIN-TIME_DIFF_ADDUPDATALOC139INFO_END));
//		long TIME_DIFF_ADDUPDATALOC139INFO_BEGIN;
//		long TIME_DIFF_ADDUPDATALOC139INFO_END;
		
		
		//执行详情更新的添加
		System.out.println("TIME_APPLY_ADDUPDATALOC139INFO = "+(TIME_APPLY_ADDUPDATALOC139INFO_BAGIN-TIME_APPLY_ADDUPDATALOC139INFO_END));
//		long TIME_APPLY_ADDUPDATALOC139INFO_BAGIN;
//		long TIME_APPLY_ADDUPDATALOC139INFO_END;
		
		//对比生成删除联系人详情
		System.out.println("TIME_BIFF_DELLOC139INFO = "+(TIME_BIFF_DELLOC139INFO_BEGIN-TIME_BIFF_DELLOC139INFO_END));
//		long TIME_BIFF_DELLOC139INFO_BEGIN;
//		long TIME_BIFF_DELLOC139INFO_END;
		
		//执行删除联系人详情
		System.out.println("TIME_APPLY_DELLOC139INFO = "+(TIME_APPLY_DELLOC139INFO_BEGIN-TIME_APPLY_DELLOC139INFO_END));
//		long TIME_APPLY_DELLOC139INFO_BEGIN;
//		long TIME_APPLY_DELLOC139INFO_END;
		
	}

	@Override
	public void afterExecute() {
		
		mNotificationManager.cancel(NOTE_BEGIN_ID);
		notification_end.tickerText = "网络数据同步完成";  //状态栏(Status Bar)显示的通知文本提示
		notification_end.icon = R.drawable.ic_notify; //通知图标
		Intent m_Intent = new Intent(appContext, DialtactsActivity.class);
		m_Intent.setAction("Group");
		
		PendingIntent m_PendingIntent = PendingIntent.getActivity(appContext, 0, m_Intent, 0);
		notification_end.setLatestEventInfo(appContext, "网络数据同步通知", "网络数据已经同步完成！",m_PendingIntent );
		
		mNotificationManager.notify(NOTE_END_ID, notification_end);
	}

	@Override
	public void beforeExecute() {
		
		
	}

}
