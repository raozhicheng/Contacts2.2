package com.android.contacts.aspire.datasync.icontact139;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.icontact139.model.IContact139Account;
import com.android.contacts.aspire.msg.request.GetCountInfoRequestMsg;
import com.android.contacts.aspire.msg.request.GetInfoByContactIdRequestMsg;
import com.android.contacts.aspire.msg.request.IRequestMsg;
import com.android.contacts.aspire.msg.request.ListFriendsRequestMsg;
import com.android.contacts.aspire.msg.request.LoginAuthMsgReq;
import com.android.contacts.aspire.msg.respone.GetCountInfoResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg;
import com.android.contacts.aspire.msg.respone.GetInfoByContactIdResponeMsg1;
import com.android.contacts.aspire.msg.respone.GroupItem;
import com.android.contacts.aspire.msg.respone.IResponseMsg;
import com.android.contacts.aspire.msg.respone.ListFriendsResponeMsg;
import com.android.contacts.aspire.msg.respone.LoginAuthMsgResp;
import com.android.contacts.aspire.network.HttpConnectWrapper;



/**
 * 本类的操作都是针对网络上的联系人数据进行操作
 * @author liangbo
 *
 */
public class IContact139Operation {
	
	//登录账户管理器
	static private IContact139AccountManager contact139Account = new IContact139AccountManager();
	
	/**
	 * 139用户登录 
	 * @param loginName  手机号码
	 * @return
	 */
	public static int login(String loginName)
	{
		
        return contact139Account.login(loginName);
	}
	
	//检查登录状态:
	public static int get139AccountState()
	{
		 return contact139Account.get139AccountState();
	}
	
	//检查登录状态:使用一个用户名检查
	public static int get139AccountState(String loginName)
	{
		 return contact139Account.get139AccountState(loginName);
	}
	
	
	//检查登录状态:如果超时自动登陆一次
	public static int get139AccountStateAutoProcTimeOut()
	{
		 return contact139Account.get139AccountStateAutoProcTimeOut();
	}
	
    /**
     * 查询当前用户一批的联系人列表
     * @param page 页号
     * @param pageSize 页大小
     * @return
     */
 	public static int listFriends(int page,int pageSize,ListFriendsResponeMsg resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;
		if(get139AccountStateAutoProcTimeOut()==IContact139Account.ACCOUNT_STATE_LOGIN)
		{
			//请添加
			//......
//			请求消息结构
			ListFriendsRequestMsg req = new ListFriendsRequestMsg(contact139Account.get139SessionKey());
	        req.page = page;
	        req.pagesize = pageSize;

	        
			HttpConnectWrapper http = new HttpConnectWrapper();
			errorCode = http.send(req, resp);
	           
		}
		else
		{
			errorCode = ErrorInfo.IC139_OP_ERROR_UNLOGIN;
		}		
		return errorCode;        
	}
 	
 	/**
 	 * 查询用户的联系人的数量，总数量，分组数量，分组中联系人数量
 	 * @return
 	 */
 	public static int getFriendsCount(GetCountInfoResponeMsg resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;
		if(get139AccountStateAutoProcTimeOut()==IContact139Account.ACCOUNT_STATE_LOGIN)
		{
			//请添加
			//......
//			请求消息结构
			GetCountInfoRequestMsg req = new GetCountInfoRequestMsg(contact139Account.get139SessionKey());
	              
			HttpConnectWrapper http = new HttpConnectWrapper();
			errorCode = http.send(req, resp);
			
			if(errorCode == ErrorInfo.SUCCESS)
			{
				ArrayList<GroupItem> groupItemList  = resp.groupItemList;
				
				
				if(groupItemList!=null)
				{
					ArrayList<GroupItem> delgroupItemList= new ArrayList<GroupItem>();
					for(GroupItem groupItem:groupItemList)
					{
						if(groupItem.groupId.equals("0"))
						{
							delgroupItemList.add(groupItem);
						}
						
					}
					
					for(GroupItem groupItem:delgroupItemList)
					{
						
						groupItemList.remove(groupItem);
					}
					
				}
			}
	        	    
		}
		else
		{
			errorCode = ErrorInfo.IC139_OP_ERROR_UNLOGIN;
		}		
		return errorCode;        
	}
	
 	
 	
    /**
     * 利用联系人的ID（139的ID）,查询联系人的详细信息 取得一批联系人详细信息
     * @param ontactId
     * @return
     */
 	public static int getInfoByContactId(/*ArrayList<String> ontactId*/String ontactIds,GetInfoByContactIdResponeMsg resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;
		if(get139AccountStateAutoProcTimeOut()==IContact139Account.ACCOUNT_STATE_LOGIN)
		{
			//请添加
			//......
//			请求消息结构
			GetInfoByContactIdRequestMsg req = new GetInfoByContactIdRequestMsg(contact139Account.get139SessionKey());
			req.setContactIds(ontactIds);
 

      
			HttpConnectWrapper http = new HttpConnectWrapper();
			errorCode = http.send(req, resp);
	           
		}
		else
		{
			errorCode = ErrorInfo.IC139_OP_ERROR_UNLOGIN;
		}		
		return errorCode;        
	}
	
 	public static int getInfoByContactId1(/*ArrayList<String> ontactId*/String ontactIds,GetInfoByContactIdResponeMsg1 resp)
	{
		int errorCode = ErrorInfo.ERROR_UNKNOW;
		if(get139AccountStateAutoProcTimeOut()==IContact139Account.ACCOUNT_STATE_LOGIN)
		{
			//请添加
			//......
//			请求消息结构
			GetInfoByContactIdRequestMsg req = new GetInfoByContactIdRequestMsg(contact139Account.get139SessionKey());
			req.setContactIds(ontactIds);
 

      
			HttpConnectWrapper http = new HttpConnectWrapper();
			errorCode = http.send(req, resp);
	           
		}
		else
		{
			errorCode = ErrorInfo.IC139_OP_ERROR_UNLOGIN;
		}		
		return errorCode;        
	}

	
	//
	
	//取得联系人组列表
	
	//上传一批联系人
	
	//------二期实现  
	//添加一个联系人
	
	//删除一个联系人
	
	//修改一个联系人
	
	//添加一个联系人组
	
	//删除一个联系人组
	
	//编辑一个联系人组的从属关系
	
	

}
