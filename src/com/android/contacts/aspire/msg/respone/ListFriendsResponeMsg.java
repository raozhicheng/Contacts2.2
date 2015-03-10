package com.android.contacts.aspire.msg.respone;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.android.contacts.aspire.config.Config;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.StartElementListener;

/**  
 * Filename:    ListFriendsResponeMsg.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 上午10:09:33  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public class ListFriendsResponeMsg extends BaseWith139ResponeMsg{

	//_response	[1,1]	返回联系人列表个数
	public int resultCount;

	//_response	[0,1]	 联系人列表
	public ArrayList<ListFriendItem> friendItemList=new ArrayList<ListFriendItem>();
	private ListFriendItem listFriendItem;
	

	
	public ListFriendsResponeMsg() {
		super("address_list_listFriends_response");


		// 总计数量total
		Element resultCountTag = root.getChild(Config.URL_139_XMLNS,"resultCount");
		resultCountTag.setEndTextElementListener(new EndTextElementListener() {
			public void end(String body) {
				try {
					resultCount= Integer.parseInt(body)  ;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Element  listFriendItemsTag = root.getChild(Config.URL_139_XMLNS,"listFriend");
		setListFriendItem(listFriendItemsTag); 
		
	}

	private void setListFriendItem(Element listFriendItemsTag) 
	{
		
		Element friendItemTag = listFriendItemsTag.getChild(Config.URL_139_XMLNS,"item");
		friendItemTag.setStartElementListener(new StartElementListener() {			
			
			public void start(Attributes attributes) {
				listFriendItem=new ListFriendItem();
				
			}
		});
		friendItemTag.setEndElementListener(new EndElementListener() {

			public void end() {
				friendItemList.add(listFriendItem);
			}
		});
		
		
		//contactId	Integer	item	[1,1]	联系人/好友ID
		Element contactIdTag = friendItemTag.getChild(Config.URL_139_XMLNS,"contactId");
		contactIdTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {
				try {
					listFriendItem.contactId= Integer.parseInt(body);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}); 
		
		
		//friendName	String(64)	item	[1,1]	好友姓名
		Element friendNameTag = friendItemTag.getChild(Config.URL_139_XMLNS,"friendName");
		friendNameTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {				
					listFriendItem.friendName= body;				
			}
		}); 
		
		
		//mobile	String(16)	item	[0,1]	好友手机号码
		Element mobileTag = friendItemTag.getChild(Config.URL_139_XMLNS,"mobile");
		mobileTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {				
					listFriendItem.mobile= body;				
			}
		}); 
		
		
		//email	String(128)	item	[0,1]	好友邮件地址
		Element emailTag = friendItemTag.getChild(Config.URL_139_XMLNS,"email");
		emailTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {				
					listFriendItem.email= body;				
			}
		}); 
		
		
		//nameSpell	String(64)	item	[0,1]	返回姓名拼音
		Element nameSpellTag = friendItemTag.getChild(Config.URL_139_XMLNS,"nameSpell");
		nameSpellTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {				
					listFriendItem.nameSpell= body;				
			}
		}); 
		
		//updateTime	String(16)	item	[1,1]	联系人更新时间
		Element updateTimeTag = friendItemTag.getChild(Config.URL_139_XMLNS,"updateTime");
		updateTimeTag.setEndTextElementListener(new EndTextElementListener() {			
			
			public void end(String body) {				
					listFriendItem.updateTime= body;				
			}
		}); 
	}
	
	/**
	 * 取得当前列表中全部联系人的139的 ID列表  用来给查询详情的时候使用
	 * @return
	 */
	public String get139ContactsId()
	{
		StringBuffer ids = new StringBuffer();
		
		if(friendItemList!=null)
		{
			int i=0;
			for(ListFriendItem friendItem  :friendItemList)
			{
				//if(friendItem.contactId !==0)
				if(i==0)
				{
					ids.append(friendItem.contactId);
				}
				else
				{
					ids.append(","+friendItem.contactId);
				}
				i++;
			}
		}
		
		
		return ids.toString();
	}
	
	
	public static ListFriendsResponeMsg testListFriendsResponeMsg1()
	{
		ListFriendsResponeMsg resp= new ListFriendsResponeMsg();
		
		ListFriendItem lfi = new ListFriendItem();
		lfi.contactId = 1;
		lfi.updateTime = "2010-09-30";
		lfi.friendName ="全新高手1";
		lfi.mobile ="电话1";
		//lfi.email ="邮件1@liangbo.com";		
		
		resp.friendItemList.add(lfi);
		
		return resp;
	}
	
	public static ListFriendsResponeMsg testListFriendsResponeMsg2()
	{
		ListFriendsResponeMsg resp= new ListFriendsResponeMsg();
		
		ListFriendItem lfi = new ListFriendItem();
		lfi.contactId = 1;
		lfi.updateTime = "2010-09-31";
		lfi.friendName ="全新高手2";
		lfi.mobile ="电话2";
		lfi.email ="邮件2@liangbo.com";		
		
		resp.friendItemList.add(lfi);
		
		ListFriendItem lfi1 = new ListFriendItem();
		lfi1.contactId = 2;
		lfi1.updateTime = "2010-09-31";
		lfi1.friendName ="全新高手2000";
		lfi1.mobile ="电话2000";
		lfi1.email ="邮件2000@liangbo.com";		
		
		resp.friendItemList.add(lfi1);
		
		ListFriendItem lfi2 = new ListFriendItem();
		lfi2.contactId = 3;
		lfi2.updateTime = "2010-09-31";
		lfi2.friendName ="全新高手3000";
		lfi2.mobile ="电话3000";
			
		
		resp.friendItemList.add(lfi2);
		
		return resp;
	}
	
	
}
