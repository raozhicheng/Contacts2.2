package com.android.contacts.aspire.msg.request;

import com.android.contacts.aspire.msg.MessageType;

/**  
 * Filename:    ListFriendsRequestMsg.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 上午09:46:52  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public class ListFriendsRequestMsg extends BaseWith139RequestMsg{
//注意  以后再添加数据保护
	
	
	//[0,1]	分组列表，多个分组ID间用“，”隔开
	public String group_ids = null;
	
	//[0,1]	5: 常用联系人排序 （没有常用联系人使用姓名排序） ；6： 按姓名 ；7：按最近联系时间排序  ；9：最后修改时间
	public String sort=null;
	
	//String(1)	req	[0,1]	过滤的关键字(A-Z),若指定,则仅返回姓为这个字母的列表，仅支持一个
	public String filterChar = null;
	
	//Integer	req	[0,1]	页号，默认为1
	public int page =1;
	
	//req	[0,1]	页大小,默认为20, 上限100
	public int pagesize=20;
	
	//req	[0,1]	是否仅返回有手机号的联系人，默认返回所有。
	//	0：返回没有手机号的联系人
	//	1：返回有手机号的联系人
	public String hasmobile=null;
	
//	req	[0,1]	是否仅返回中国移动手机号码的，默认返回所有。
//	0：返回非中国移动手机号码
//	1：返回中国移动手机号码
	public String isCMCCNumber=null;
	
	
//	req	[0,1]	是否仅返回有email资料的，不填时返回所有的。
//	0：返回没有email的
//	1：返回有email的
	public String hasEmail;	
	
	public ListFriendsRequestMsg(String session_key){
		super(session_key);
		this.header.msgType=MessageType.CLIENT_LIST_FRIEND_REQ;
	}
	
	@Override
	public String toXmlWith139() {
		
		StringBuffer st=new StringBuffer();	
		if(group_ids!=null && group_ids.length()>0)
		{
			st.append("<group_ids>"+group_ids+"</group_ids>");
		}
		if(sort!=null && sort.length()>0)
		{
			st.append("<sort>"+sort+"</sort>");
		}
		if(filterChar!=null && filterChar.length()>0)
		{
			st.append("<filterChar>"+filterChar+"</filterChar>");
		}
		
		
		st.append("<page>"+page+"</page>");
		st.append("<pagesize>"+pagesize+"</pagesize>");
		
		if(hasmobile!=null && hasmobile.length()>0)
		{
			st.append("<hasmobile>"+hasmobile+"</hasmobile>");
		}
		
		if(isCMCCNumber!=null && isCMCCNumber.length()>0)
		{
			st.append("<isCMCCNumber>"+isCMCCNumber+"</isCMCCNumber>");
		}
		
		if(hasEmail!=null && hasEmail.length()>0)
		{
			st.append("<hasEmail>"+hasEmail+"</hasEmail>");
		}
		
		st.append("</req>");
		return st.toString();
	}

}
