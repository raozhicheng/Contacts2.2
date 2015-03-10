package com.android.contacts.aspire.datasync.task;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.model.AspEntityDelta;
import com.android.contacts.aspire.datasync.model.AspEntitySet;
import com.android.contacts.aspire.datasync.sim.SimOperation;
import com.android.contacts.aspire.datasync.sim.TranslateToolBySim;
import com.android.contacts.aspire.datasync.sim.model.SimContact;
import com.android.contacts.aspire.datasync.sim.model.SimContactList;
import com.android.contacts.aspire.datasync.util.ContentProviderUtil;
import com.android.contacts.aspire.msg.respone.FriendItem;
import com.android.contacts.aspire.msg.respone.GroupItem;

/**
 * Filename: SimContactsSyncTask.java Description: Copyright: Copyright (c)2009
 * Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-9-2 下午03:43:34
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-9-2 liangbo 1.0 1.0 Version
 */

public class SimContactsSyncTask extends BaseTask {

	//如果没有有效账号的时候  任务管理需要一个默认账号
	public static final String DEFAULT_ACCOUNT="SIM_ACCOUNT_DEFAULT";
	
	Context appContext;

	public SimContactsSyncTask(String taskAccount, Context appContext) {
		super(Config.ACCOUNT_TYPE_SIM, taskAccount);
		this.appContext = appContext;
	}

	@Override
	public void taskExecute() {

		/*检测SIM卡是否可用*/
		if (this.getTaskState() == this.TASK_STATE_RUN)// 如果是运行状态 继续操作
		{
			// 检查SIM卡是否有效 如果无效 则退出
			if (SimOperation.getSimState(appContext) != TelephonyManager.SIM_STATE_READY) {
				// 删除本地全部缓存的SIM联系人
				ContentProviderUtil.delProviderSimData(appContext
						.getContentResolver());
				return;
			}
			else
			{
				//继续
			}
		} else {
			return;
		}
		
		
		/* 检测SIM卡编号是否有效*/
		if (this.getTaskState() == this.TASK_STATE_RUN)// 如果是运行状态 继续操作
		{
			// 是否有有效的SIM同步任务账号，没有则退出
			if (this.getTaskAccount()==null||this.getTaskAccount().length()==0||
					this.getTaskAccount().equals(DEFAULT_ACCOUNT)) {
				// 删除本地全部缓存的SIM联系人
				ContentProviderUtil.delProviderSimData(appContext
						.getContentResolver());
				return;
			}
			else
			{
				//继续
			}
		} else {
			return;
		}
		
		// 读取SIM编号 
//		String simAccount;
//		if (this.getTaskState() == this.TASK_STATE_RUN)// 如果是运行状态 继续操作
//		{
//			simAccount = SimOperation.getSimSerialNumber(appContext);
//			if (simAccount == null) {
//				simAccount = "SIM_DEFTER";
//			}
//		} else {
//			return;
//		}
		

		// 删除本地非这个账号的 SIM 卡缓存的联系人信息
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			ContentProviderUtil.delProviderSimDataOutOfSimId(appContext
					.getContentResolver(), getTaskAccount());
		} else {
			return;
		}

		
		
		
		//本地组和远程组的ID对应信息
		HashMap<String,String> iSimGroupIdToLocalGroupId = new HashMap<String,String>();
		
		//判断并且添加一个未分组的默认组	(这个和139的公用)	
		
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			ArrayList<String> unMenberGroupId = new ArrayList<String>(); 
		int error =  ContentProviderUtil.createProviderGroupUnMember(appContext.getContentResolver(),unMenberGroupId);
		if(error==ErrorInfo.SUCCESS && unMenberGroupId.size()>0)
		{
			 String localGroupId = 	unMenberGroupId.get(0);
			 String iUnMenberGroupId = 	Config.DEFAULT_GROUP_SOURCE_ID;
			//压入我们的 远程组ID和本地组ID的对应工具
			 iSimGroupIdToLocalGroupId.put(iUnMenberGroupId, localGroupId);
		}
		} else {
			return;
		}
		
		

		// 读取 SIM卡上全部的联系人信息
		SimContactList simContactListt;
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			simContactListt = SimOperation.getSimContacts(appContext
					.getContentResolver());

			
			
			//如果成功  则迭代判断一遍  给没有组的用户添加上默认组
			
			for( SimContact simContact:simContactListt.simContactList)
			{
				if(simContact.typeIdList==null || simContact.typeIdList.size()==0)
				{
					 ArrayList<String> typeIdList=new ArrayList<String>();
					 typeIdList.add(Config.DEFAULT_GROUP_SOURCE_ID);
					 simContact.typeIdList = typeIdList;
				}
			}
			
			
		} else {
			return;
		}

		// 读取本地缓存的SIM联系人信息
		AspEntitySet simEntitySet;
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			simEntitySet = AspEntitySet.fromQueryWithSim(appContext
					.getContentResolver(),getTaskAccount(), null);
		} else {
			return;
		}

		// 对比本地缓存的数据 生成 操作代码
		ArrayList<ContentProviderOperation> cpo = null;
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			if (simEntitySet != null)// simContactList的NULL检查在函数内buildDiffFromSimContacts
			{
				cpo = TranslateToolBySim.buildDiffFromSimContacts(getTaskAccount(),
						simEntitySet, simContactListt,iSimGroupIdToLocalGroupId);
			}

		} else {
			return;
		}

		// 执行 此处涉及新建和修改
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			try {
				if (cpo != null) {
					appContext.getContentResolver().applyBatch(
							ContactsContract.AUTHORITY, cpo);
				}

			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return;
		}

		// 过滤对比过的数据，生成删除代码
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			if(simEntitySet!=null)
			{
				cpo = TranslateToolBySim.buildDelDiffFromEntitySet(getTaskAccount(),
						simEntitySet);	
			}

		} else {
			return;
		}

		// 执行 此处涉及删除
		if (this.getTaskState() == this.TASK_STATE_RUN) {
			try {
				if (cpo != null) {
					appContext.getContentResolver().applyBatch(
							ContactsContract.AUTHORITY, cpo);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return;
		}
		
		
	
	}

	@Override
	public void afterExecute() {
		
		
	}

	@Override
	public void beforeExecute() {
		
		
	}

}
