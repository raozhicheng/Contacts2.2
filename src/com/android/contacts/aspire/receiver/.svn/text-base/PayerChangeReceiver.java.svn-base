package com.android.contacts.aspire.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.mid.broadcast.BroadcastManager;
import android.mid.service.MIDServiceManager;

import com.android.contacts.aspire.datasync.icontact139.MidPhysicsAccountManager;
import com.android.contacts.aspire.datasync.sim.SimOperation;
import com.android.contacts.aspire.datasync.task.IContacts139SyncTask;
import com.android.contacts.aspire.datasync.task.SimContactsSyncTask;
import com.android.contacts.aspire.datasync.task.TaskManager;
import com.android.contacts.aspire.service.ContactsDataSyncService;

/**  
 * Filename:    PayerChangeReceiver.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-3 下午01:22:39  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-3    liangbo             1.0        1.0 Version  
 */

public class PayerChangeReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) { 
    	String action = intent.getAction();
    	
		if (action.equals(BroadcastManager.PASSPORT_BIND_CHANGE_ACTION)
				||
				action.equals(BroadcastManager.FEE_BIND_STATE_CHANGE_ACTION)	) {	
			
			System.out.println(" get Broadcast action="+action);
			
			Intent tIntent = new Intent(context, ContactsDataSyncService.class);
			tIntent.setAction(intent.getAction());
			if(intent.getExtras()!=null)
			{
				//int a= 1;
				tIntent.putExtras(intent.getExtras());				
			}
			
			//驱动139 数据同步服务
			context.startService(tIntent);
		}
    }    
}


