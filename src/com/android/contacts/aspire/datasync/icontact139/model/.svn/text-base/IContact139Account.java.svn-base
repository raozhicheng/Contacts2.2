package com.android.contacts.aspire.datasync.icontact139.model;
/**  
 * Filename:    IContact139Account.java  
 * Description:   爱联系的账户信息
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-25 下午03:00:54  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-25    liangbo             1.0        1.0 Version  
 */

public class IContact139Account {
	
	//本地设备上，关联去使用139爱联系的账号类型
	public static final int PHYSICS_ACCOUNT_TYPE_UNKNOW = 0;//未知卡 用户
	public static final int PHYSICS_ACCOUNT_TYPE_147 = 1;//147SIM卡 用户
	public static final int PHYSICS_ACCOUNT_TYPE_NO147 = 2;//非147SIM卡 用户
	public static final int PHYSICS_ACCOUNT_TYPE_BIND = 3;//绑定的付费用户（只有147卡可以绑定）
	public static final int PHYSICS_ACCOUNT_TYPE_MID = 4;//MID登录的账号用户
	
	
	//账户状态
	public static final int ACCOUNT_STATE_UNKNOWN = -1;//未知
	public static final int ACCOUNT_STATE_UNLOGIN = 0;//未登录
	public static final int ACCOUNT_STATE_LOGIN = 1;//已经登录
	public static final int ACCOUNT_STATE_LOGINING = 2;//正在登录
	public static final int ACCOUNT_STATE_ERROR = 3;//登录失败
	public static final int ACCOUNT_STATE_TIMEOUT = 4;//SESSION超时
	
	
	
	//设定超时时间
	public static final long SESSION_TIME_OUT = 3600*1000;
	
	
	//本机物理上绑定给139登录使用的账号类型
	public  int physicsAccountType ;
	
	//账户名 139用来登录的手机号码
	public String accountName =null;
	
	//139账号的登录状态
	public int state=ACCOUNT_STATE_UNKNOWN;
	
	//139登录后139返回操作的操作码
	public String sessionKey;
	
	//139登录后的超时时间
	public long sessionTimeOut;
	
	//我们本次发生的登录操作的操作序列码，用来处理登录响应的匹配
	public long loginOperationNumber;
	

}
