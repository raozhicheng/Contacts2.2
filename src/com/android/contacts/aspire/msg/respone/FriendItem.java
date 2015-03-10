package com.android.contacts.aspire.msg.respone;

import java.util.ArrayList;

import android.content.ContentValues;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

/**  
 * Filename:    FriendItem.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 下午01:24:02  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public class FriendItem {
	
	static ContentValues  tv= new ContentValues();
	
	
	//--------联系人基本数据信息--begin----
	//
	//item	[1,1]	联系人/好友ID
	//{tv.put(ContactsContract.RawContacts.SOURCE_ID,"***");}
	public int contactId;
	
	//item	[0,1]	好友的用户id 这个在我们这边没有用
	public String friendUserId=null;	
	
	//item	[1,1]	联系人更新时间  这里我们当做版本来使用 不使用VERSION因为 数据库是int类型 不方便存储日期
	//{tv.put(ContactsContract.RawContacts.SYNC1,"***");}
	public String updateTime="0";	
	
	//item	[1,1]	常用标记，该值的大小对应常用联系人的顺序
	public String commonlyFlag	="0";
	
	//--------联系人基本数据信息--end----
	
	
	//--------联系人基本信息--begin----
	//mimetypes:vnd.android.cursor.item/name StructuredName
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);}
	
	//item	[1,1]	好友姓名
	//ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
	//{tv.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,"***");}
	public String friendName = "";
	
	//item	[0,1]	返回姓名拼音
	//{tv.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,"???");}
	public String nameSpell	=null;
	
	//--------联系人基本信息--end----
	
	
	//--------联系人电话信息--begin----
	//mimetypes:vnd.android.cursor.item/phone_v2
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的手机号
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String friendMobile=null;
	
	//item	[0,1]	好友的手机1（工作手机）
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String friendOtherNumber	=null;
	
	//item	[0,1]	好友的电话(家庭)
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_HOME);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String friendTel=null;	
	
	//item	[0,1]	好友的电话1(其他)
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String  friendOtherTel=null;
	
	//item	[0,1]	好友的工作电话
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_WORK);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String officePhone=null;
		
	//item	[0,1]	好友的传真（工作传真）
	//{tv.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);}
	//{tv.put(ContactsContract.CommonDataKinds.Phone.NUMBER,"***");}
	public String friendEleTel=null;
	
	
	//--------联系人电话信息--end----
	
	
	//--------联系人email信息--begin----
	//mimetypes:vnd.android.cursor.item/email_v2
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的Email  (家庭email)
	//{tv.put(ContactsContract.CommonDataKinds.Email.TYPE,ContactsContract.CommonDataKinds.Email.TYPE_HOME);}
	//{tv.put(ContactsContract.CommonDataKinds.Email.ADDRESS,"***");}
	//{tv.put(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,"***");}
	public String email	=null;
	
	//--------联系人email信息--end----
	
	
	
	//--------联系人IM信息--begin----	
	//mimetypes:vnd.android.cursor.item/im
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的QQ
//	{tv.put(ContactsContract.CommonDataKinds.Im.TYPE,ContactsContract.CommonDataKinds.Im.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.DATA1,"***");}
	public String friendQQ=null;
	
	//item	[0,1]	好友的飞信
//	{tv.put(ContactsContract.CommonDataKinds.Im.TYPE,ContactsContract.CommonDataKinds.Im.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL,"***");}
//	{tv.put(ContactsContract.CommonDataKinds.Im.DATA1,"***");}
	public String friendFetion=null;
	public static final String IM_PROTOCOL_CUSTOM_FETION = "Fetion飞信";
	
	//item	[0,1]	好友的MSN
//	{tv.put(ContactsContract.CommonDataKinds.Im.TYPE,ContactsContract.CommonDataKinds.Im.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN);}
//	{tv.put(ContactsContract.CommonDataKinds.Im.DATA1,"***");}
	public String friendMsn	=null;
	
	//--------联系人IM信息--end----
	
	
	//--------联系人Url信息--begin----
	//mimetypes:vnd.android.cursor.item/website
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的个人主页
//	{tv.put(ContactsContract.CommonDataKinds.Website.TYPE,ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE);}
//	{tv.put(ContactsContract.CommonDataKinds.Website.URL,"***");}
	public String friendURL=null;
	
	//item	[0,1]	公司网址
//	{tv.put(ContactsContract.CommonDataKinds.Website.TYPE,ContactsContract.CommonDataKinds.Website.TYPE_WORK);}
//	{tv.put(ContactsContract.CommonDataKinds.Website.URL,"***");}
	public String companyURL	=null;
	
	//--------联系人Url信息--end----
	
	
	//--------联系人organization信息--begin----	
	//mimetypes:vnd.android.cursor.item/organization
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的公司
//	{tv.put(ContactsContract.CommonDataKinds.Organization.TYPE,ContactsContract.CommonDataKinds.Organization.TYPE_WORK);}
//	{tv.put(ContactsContract.CommonDataKinds.Organization.COMPANY,"***");}
	public String friendCompany	=null;
	
	//item	[0,1]	好友的职位
//	{tv.put(ContactsContract.CommonDataKinds.Organization.TYPE,ContactsContract.CommonDataKinds.Organization.TYPE_WORK);}
//	{tv.put(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,"***");}
	public String friendPosition=null;
	
	//--------联系人organization信息--end----
	
	
	//--------联系人postal-address_v2信息--begin----	
	//mimetypes:vnd.android.cursor.item/postal-address_v2
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友所在的省
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION,"***");}
	public String friendState=null;
	
	//item	[0,1]	好友所在的市
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,"***");}
	public String friendCity=null;
	
	//item	[0,1]	邮编
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,"***");}
	public String friendPostalCode=null;
	
	//item	[0,1]	地址
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);}
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,"***");}
	public String friendAddress	=null;
	
	//item	[0,1]	公司地址
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);}
//	{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,"***");}
	//{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION,"***");}//省
	//{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,"***");}//市
	//{tv.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,"***");}//邮编
	public String  companyAddress=null;
	
	//--------联系人postal-address_v2信息--end----
	
	
	//--------联系人group信息--begin----	
	//mimetypes:vnd.android.cursor.item/group_membership
	{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);}
	
	
	//[0,1]	联系人归属的群组列表
	//{tv.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,"***");}
	public ArrayList<String> typeIdList=null;
	//--------联系人group信息--end----
	
	
	//--------联系人event信息--begin----
	//mimetypes:vnd.android.cursor.item/contact_event
	//{tv.put(Data.MIMETYPE,ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);}
	
	//item	[0,1]	好友的生日
//	{tv.put(ContactsContract.CommonDataKinds.Event.TYPE,ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);}
//	{tv.put(ContactsContract.CommonDataKinds.Event.START_DATE,"***");}
	public String friendBirthday=null;
	
	
	//--------联系人event信息--end----
	
	
	
	//item	[0,1]	好友的性别0女1男
	public String friendSex	=null;

	//item	[0,1]	备注
	public String note=null;
	
	//item	[0,1]	Remark
	public String friendRemark=null;
	
	
	
	//Item	[0,1]	最近联系时间
	public String contactTime=null;
	
	//item	[0,1]	0：非黑名单(默认值)，1：黑名单
	public String blackFlag=null;

	
	//item	[0,1]	双向标记： 1：双向，0：单向
	public String twowayFlag=null;
	
	//item	[0,1]	0：web添加，（暂时未提供）1：手机， 2： outlook插件 ，（暂时未提供）3：导入。（暂时未提供）
	public String dataFromFlag=null;
	

	
   
	
	
	
	
	
}