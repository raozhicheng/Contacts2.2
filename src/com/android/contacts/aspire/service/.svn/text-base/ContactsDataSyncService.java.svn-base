package com.android.contacts.aspire.service;



import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.datasync.icontact139.MidPhysicsAccountManager;
import com.android.contacts.aspire.datasync.icontact139.model.IContact139Account;
import com.android.contacts.aspire.datasync.task.IContacts139SyncTask;
import com.android.contacts.aspire.datasync.task.TaskManager;
import com.android.contacts.util.Constants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.mid.service.MIDServiceManager;
import android.mid.service.MIDServiceManager.InitListener;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Filename: ContactsDataSyncService.java Description: Copyright: Copyright
 * (c)2009 Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-9-10 下午05:33:09
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-9-10 liangbo 1.0 1.0 Version
 */

public class ContactsDataSyncService extends Service implements InitListener {

	//public static String SERVICEPHONE="13425149524";
//	public String SERVICEPHONE="1";  //测试用
//	
//	public void setmy(String n){  //测试用
//		SERVICEPHONE=n;
//	}
	
	private static MIDServiceManager midManager = null;
	private boolean flag = true;
  
	


	@Override
	public void onCreate() {
		 midManager = MIDServiceManager.getInstance(this.getApplicationContext(),this);
		 System.out.println("-------------------onCreate() -------------");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		System.out.println("-------------------onStartCommand() ------------- flag="+flag);
        if (flag )
        {
        	System.out.println("-------------------onStartCommand() -------------");
		new PayerChangeReceiverThread( getBaseContext() ,intent).start();		
        }	
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public IBinder onBind(Intent paramIntent) {

		return null;
	}
	
	
	
	
	   class  PayerChangeReceiverThread extends Thread
	    {
	    	Context context;
	    	Intent intent;
	    	
	    	public  PayerChangeReceiverThread(Context context, Intent intent)
	    	{
	    		this.context = context;
	    		
	    		this.intent = intent;
	    	}
	    	
	    	public void run()
			{
//	    		SharedPreferences prefs = getSharedPreferences(
//	    				Constants.FILTER_SEE_CONTACTS, MODE_WORLD_READABLE);
//	    		String Phone = prefs.getString("servicephone", "12356");
	    		
	    		boolean autosycFlag = true;
	    		
	    		//判断是否有自动同步
	    		//MIDServiceManager midMgr =null;
	    		
	    		
	    		try {
//	    			midManager = MIDServiceManager.getInstance(context);
					autosycFlag = midManager.getAutosycFlag() ;
					
					autosycFlag = true;
					System.out.println("---------------------------  autosycFlag="+autosycFlag);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

//	    		try{
//	    			midManager.unbindMIDService();
//	    		}
//	    		catch(Exception e)
//	    		{
//	    			e.printStackTrace();
//	    		}
	    		
	    		
	    		if(autosycFlag){
	    		
					String account139=null;
					//读取付费用户信息
					//account139 = MidPhysicsAccountManager.get139AccountFromMid(context).accountName;
					IContact139Account t139Account = MidPhysicsAccountManager.get139AccountFromMid(midManager);
					System.out.println("-----------------accountName="+t139Account.accountName);
					account139 = t139Account.accountName;
					System.out.println("account139++++="+account139);
					//需要托座提供 方法
					if (account139 == null) {
						//如果没有就使用默认账号
						account139 = IContacts139SyncTask.DEFAULT_ACCOUNT;
					}
					if(Config.IS_DEBUG)
					{
						account139 = "13510779142";
					}
					//为了模拟器测试 
		            if(Config.IS_SIMULATOR_TEST)
		            {
//		            	Bundle  myBundelForGetName=intent.getExtras();
//		            	String  name=myBundelForGetName.getString("servicephone");
		            	// account139= "13760277830";
		            	// account139=AspireDialog.getphone();
	            	    //account139=Config.getConfig().getSERVICEPHONE();
		            	//account139 = intent.getStringExtra("SERVICEPHONE");
	            	    //System.out.println("********************* Sync Config = "+Config.getConfig());
		            	System.out.println("account139++++="+account139);
		            }
		            
		            //测试意向数据
		            String tIopn =  intent.getStringExtra("SERVICEPHONE");
		            {
		            	if(tIopn!=null && tIopn.length()>0)
		            	{
		            		account139 = tIopn;
		            	}
		            	System.out.println("account139++++="+account139);
		            }
						
					//构造 同步139用户联系人任务
					IContacts139SyncTask scst = new IContacts139SyncTask(account139,context);
					
					//将任务加入任务管理器执行
					//尝试执行  如果有一个一样的任务  则本任务就不再执行
					//一样是说明  如果类型一样  并且账号也一样
					TaskManager.tryStartTask(scst);
	    		}
			}
	    }




	@Override
	public void onInit() {
		
		System.out.println("-------------------onInit() -------------");
		new PayerChangeReceiverThread( getBaseContext() ,new Intent()).start();		
		flag = true;
	}


}
