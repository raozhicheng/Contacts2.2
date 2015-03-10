package com.android.contacts.aspire.datasync.icontact139;

import android.content.Context;
import android.mid.service.MIDServiceManager;

import com.android.contacts.aspire.datasync.icontact139.model.IContact139Account;

/**  
 * Filename:    IContact139AccountManager.java  
 * Description:   139网络用户管理类，利用托座提供的功能，
 *                根据自身业务原则，确定当前应使用的139网络用户的登录账号
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-25 下午01:52:00  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-25    liangbo             1.0        1.0 Version  
 */


public class MidPhysicsAccountManager {
	/**
	 * 利用托座提供的功能，判断sim卡、绑定用户、FID刷卡用户，取得联系人业务需要使用的139账号
	 * @return  IContact139Account
	 */
	public static IContact139Account get139AccountFromMid(/*Context context,*/MIDServiceManager midMgr1)
	{
		IContact139Account t139Account = new IContact139Account();
		MIDServiceManager midMgr=midMgr1;
//		try {
//		midMgr = MIDServiceManager.getInstance(context);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if(midMgr!=null)
		{
			//调用托座功能，的到sim卡 用户信息（卡类型  手机号码）
			
			
			//取得计费用户号码  也就是绑定的号码 //调用托座功能,得到绑定用户信息或者本身的SIM卡号
			String feeMsisdn=null;
			try {
				feeMsisdn = midMgr.getFeeMsisdn();
				System.out.println("-----------------feeMsisdn="+feeMsisdn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(feeMsisdn!=null && feeMsisdn.length()>0)
			{
				t139Account.physicsAccountType = IContact139Account.PHYSICS_ACCOUNT_TYPE_BIND;
				t139Account.accountName = feeMsisdn;				
			}
			
			//取得计费用户号码  也就是绑定的号码 //调用托座功能，得到MID登录用户的信息( MID状态,手机号码)
			String userMsisdn=null;
			try {
				userMsisdn = midMgr.getUserMsisdn();
				System.out.println("-----------------userMsisdn="+userMsisdn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(userMsisdn!=null && userMsisdn.length()>0)
			{
				t139Account.physicsAccountType = IContact139Account.PHYSICS_ACCOUNT_TYPE_MID;
				t139Account.accountName = userMsisdn;				
			}
			
			
		}
//		if(midMgr!=null)
//		{
//			try {
//			midMgr.unbindMIDService();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		
		
		
		
		
		
		//目前需求不需要关心  SIM卡  绑定用户信息，只关心 MID登录用户信息，如果MID是已经登录的，则返回 MID的手机信息，用 这个号码去登录139爱联系
		
		System.out.println("-----------------t139Account="+t139Account.accountName);
		return t139Account;
	}

}
