package com.android.contacts.aspire.receiver;

import com.android.contacts.aspire.datasync.sim.SimOperation;
import com.android.contacts.aspire.datasync.task.SimContactsSyncTask;
import com.android.contacts.aspire.datasync.task.TaskManager;
import com.android.contacts.aspire.service.ContactsDataSyncService;
import com.android.contacts.aspire.service.ContactsDataSyncSimService;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.mid.broadcast.BroadcastManager;

/**  
 * Filename:    BootCompletedReceiver.java  
 * Description: 开机广播监听器  
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-3 下午12:24:27  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-3    liangbo             1.0        1.0 Version  
 */

public class BootCompletedReceiver  extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) { 

String action = intent.getAction();

		//if (action.equals("android.mid.fee.liangbotest")
		if (action.equals("android.intent.action.BOOT_COMPLETED")
					) {	
			Intent tIntent = new Intent(context, ContactsDataSyncSimService.class);
			tIntent.setAction(intent.getAction());
			if(intent.getExtras()!=null)
			{
				int a= 1;
			}
			//tIntent.putExtras(intent.getExtras());
			//驱动139 数据同步服务
			context.startService(tIntent);
		}
    }
    
    

}




