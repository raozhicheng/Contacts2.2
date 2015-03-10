package com.android.contacts.aspire.service;

import com.android.contacts.aspire.datasync.sim.SimOperation;
import com.android.contacts.aspire.datasync.task.SimContactsSyncTask;
import com.android.contacts.aspire.datasync.task.TaskManager;
import com.android.contacts.aspire.service.ContactsDataSyncService.PayerChangeReceiverThread;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**  
 * Filename:    ContactsDataSyncSimService.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-11 上午08:13:56  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-11    liangbo             1.0        1.0 Version  
 */

public class ContactsDataSyncSimService extends Service{
	public int onStartCommand(Intent intent, int flags, int startId) {

		new BootCompletedReceiverThread( getBaseContext() ,intent).start();
		
		
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public IBinder onBind(Intent paramIntent) {

		return null;
	}
	
	
	class BootCompletedReceiverThread extends Thread
    {
    	Context context;
    	Intent intent;
    	
    	public BootCompletedReceiverThread(Context context, Intent intent)
    	{
    		this.context = context;
    		
    		this.intent = intent;
    	}
    	
    	public void run()
		{
				String simAccount;
				//读取任务sim卡信息
				simAccount = SimOperation.getSimSerialNumber(context);
				if (simAccount == null) {
					//如果没有就使用默认账号
					simAccount = SimContactsSyncTask.DEFAULT_ACCOUNT;
				}
				
				
				//构造 同步SIM卡任务
				SimContactsSyncTask scst = new SimContactsSyncTask(simAccount,context);
				
				//将任务加入任务管理器执行
				//尝试执行  如果有一个一样的任务  则本任务就不再执行
				//一样是说明  如果类型一样  并且账号也一样
				TaskManager.tryStartTask(scst);

		}
    }
}
