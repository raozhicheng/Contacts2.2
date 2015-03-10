package com.android.contacts.aspire.datasync.icontact139;

import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.icontact139.model.IContact139Account;
import com.android.contacts.aspire.msg.request.LoginAuthMsgReq;
import com.android.contacts.aspire.msg.respone.LoginAuthMsgResp;
import com.android.contacts.aspire.network.HttpConnectWrapper;

/**  
 * Filename:    IContact139AccountManager.java  
 * Description:   负责139账号登录，登录状态的维护等
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


public class IContact139AccountManager {
	
	private static IContact139Account iContact139Account=new IContact139Account();
	
	private long LOGIN_TIME_OUT = 60000;
	
	//登录同步塞子（利用syncObj对象的机锁实现）
    public static Object syncObj=new Object();
    
    
    public static String get139AccountLoginName()
    {
//    	if( get139AccountStateAutoProcTimeOut()== IContact139Account.ACCOUNT_STATE_LOGIN)
//    		{
//    		if(iContact139Account!=null 
//    			&&iContact139Account.accountName!=null
//    			&& iContact139Account.accountName.length()>0)
//    		{
    			return iContact139Account.accountName;
//    		}
//    	}
//    		return null;
    } 
	
	/**
	 * 取得139登录账号的状态
	 * @return
	 */
	public int get139AccountState()
	{			
		int state = IContact139Account.ACCOUNT_STATE_UNKNOWN;
		
		if(iContact139Account!=null)
		{
			//如果是登录状态，就要检查下是否超时
			if(iContact139Account.state==IContact139Account.ACCOUNT_STATE_LOGIN)
			{
				//long time=System.currentTimeMillis();
	            //当前时间小于超时时间 就是有效的 否则就无效
				if(System.currentTimeMillis()>iContact139Account.sessionTimeOut)
				{
					//设置当前的状态为超时
					iContact139Account.state =  IContact139Account.ACCOUNT_STATE_TIMEOUT;
				}			
			}		
			
			state = iContact139Account.state;
		}		
		return state;
	}
	
	/**
	 * 取得139的登录状态，并自动处理如果超时就再登录一次
	 * @return
	 */
	public int get139AccountStateAutoProcTimeOut()
	{
		int state = get139AccountState();
		if(state == IContact139Account.ACCOUNT_STATE_TIMEOUT)
		{
			//使用原来的账号  登录一次
			state = login(iContact139Account.accountName);
		}
		
		return state;
	}
	
	
	//检查登录状态:
	public int get139AccountState(String loginName)
	{
		if(loginName.equals(iContact139Account.accountName))
		{
			return get139AccountState();
		}
		else
		{
			return IContact139Account.ACCOUNT_STATE_UNKNOWN;//其他用户的状态
		}
	}
	
	
	
	/**
	 * 取得139登录后的操作序列号
	 * @return
	 */
	public String get139SessionKey()
	{
		
		if(iContact139Account!=null  )
		{
			return  iContact139Account.sessionKey;
		}
		
		return null;
	} 
	
	

	
	
	/**
	 * 使用一个139账号来登录
	 * @param iContact139Account
	 * @return
	 */
	public int login(String accountName)
	{
		if(accountName==null)
		{
			return IContact139Account.ACCOUNT_STATE_ERROR;
		}
		
		//检查当前的账号是否是相同，因为可能以前发生过登录，且登录的账号一样
		if(iContact139Account.accountName!=null &&  iContact139Account.accountName.equals(accountName))
		{
			//如果账号相同，则判断是否是登录的了
			if(iContact139Account.state == IContact139Account.ACCOUNT_STATE_LOGIN)
			{
				//则说明不需要再登录了
				return  iContact139Account.state;
			}
			//如果账号相同，则判断是否是正在登录的
			else if(iContact139Account.state == IContact139Account.ACCOUNT_STATE_LOGINING)
			{
				//自己不需要发起登录请求，直接在这里等待上个登录成功的返回
				try {
					
					synchronized (syncObj) {
						syncObj.wait(LOGIN_TIME_OUT);
					}
					if(iContact139Account.accountName.equals(accountName))
					{
						return iContact139Account.state ;//返回另外那个登录返回的状态
					}
					else
					{
						return iContact139Account.ACCOUNT_STATE_ERROR;//说明等待的那个线程被其他的用户登陆请求终断了，这里的这个用户登录是失败的
					}
					
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return IContact139Account.ACCOUNT_STATE_ERROR;
				}				
			}			
		}
		
		
		//新账号登录的，或者原来的账号的状态不是登录 也不是正在登录
		{
			//清除原来的登录状态，并 设置为此次登录的信息  发起一个新的登录 
			
			iContact139Account.accountName = accountName;
			iContact139Account.state = IContact139Account.ACCOUNT_STATE_LOGINING;
			long temploginOperationNumber = System.currentTimeMillis();//取得自己的操作时间
			iContact139Account.loginOperationNumber = temploginOperationNumber;//设置此次登录操作的操作码，用来在消息匹配的时候使用
			iContact139Account.sessionKey = "";
			iContact139Account.sessionTimeOut = 0;
			
			
			new Login139Thread(accountName,temploginOperationNumber).start();
			
			try {
				synchronized (syncObj) {
				syncObj.wait(LOGIN_TIME_OUT);
				}
				
				//判断返回的是不自己这个请求（即登录线程使用的账户是否是自己本来要登的那个账户）
				if(iContact139Account.accountName.equals(accountName)
						&&(iContact139Account.loginOperationNumber==temploginOperationNumber))
				{
					return iContact139Account.state ;//返回登录返回的状态
				}
				else
				{
					return iContact139Account.ACCOUNT_STATE_ERROR;//说明等待的那个线程被其他的用户登陆请求终断了，这里的这个用户登录是失败的
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return IContact139Account.ACCOUNT_STATE_ERROR;
			}
		}
		
	}
	
	
	public class Login139Thread extends Thread
	{
		String mLoginName;
		long mOpid;
		/**
		 * 构造
		 * @param loginName 登录的账号
		 * @param iContact139Account 存储返回结果的结构
		 */
		public Login139Thread(String loginName,long opid)
		{
			this.mLoginName = loginName;
			this.mOpid= opid;
		}
		
		public void run()
		{
			LoginAuthMsgResp resp = loginOp(mLoginName);

			// 判断这个线程操作返回的结果是当前最新的请求结果
			if (mOpid == iContact139Account.loginOperationNumber) {
				//if (resp.hRet == 0)// 如果登录成功
				if (resp.getErrorResponse().error_code == ErrorInfo.SUCCESS)
				{

					iContact139Account.state = IContact139Account.ACCOUNT_STATE_LOGIN;
					iContact139Account.sessionKey = resp.sessionkey;
					iContact139Account.sessionTimeOut = System
							.currentTimeMillis()
							+ IContact139Account.SESSION_TIME_OUT;

				} else {
					//失败 就设置失败状态
					iContact139Account.state = IContact139Account.ACCOUNT_STATE_ERROR;
				}
			} else {
				//如果不是这个操作的，则可能是被取消（即被终断）的操作，不处理了
			}
			synchronized (syncObj) {
			syncObj.notifyAll();
			}
		}
	}
	
	
	private LoginAuthMsgResp loginOp(String loginName)
	{
		LoginAuthMsgReq req = new LoginAuthMsgReq();
        req.loginName = loginName;        
        LoginAuthMsgResp resp = new LoginAuthMsgResp();		
        
		HttpConnectWrapper http = new HttpConnectWrapper();
		http.send(req, resp);
        return resp;	       
	}
	
}
