package com.android.contacts.aspire.msg.request;

import java.util.ArrayList;

import com.android.contacts.aspire.msg.MessageType;

/**  
 * Filename:    GetInfoByContactIdRequestMsg.java  
 * Description:  根据ID获取联系人详情
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 上午11:46:26  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public class GetInfoByContactIdRequestMsg extends BaseWith139RequestMsg{
	//req	[1,1]	联系人Id集合。数据类型以,分割的联系人ID（要注意，是联系人的ID，不是139用户ID）。一次查询不宜超过100个；
	public String contact_ids="";
	
	public GetInfoByContactIdRequestMsg(String session_key){
		super(session_key);
		this.header.msgType=MessageType.CLIENT_GET_CONTACTINFO_BYID_REQ;
	}
	
	@Override
	public String toXmlWith139() {		
		StringBuffer st=new StringBuffer();	
		st.append("<contact_ids>"+contact_ids+"</contact_ids>");
		st.append("</req>");
		return st.toString();
	}
	
	public void setContactIds(String  contactIds)
	{
		contact_ids = contactIds;
	}
	
	
	public void setContactIds(ArrayList<String>  contactIds)
	{
		if(contactIds!=null)
		{
			StringBuffer tsb = new StringBuffer();
			int i=0;
			for( String cid  :contactIds)
			{
				if(i==0)
				{
					tsb.append(cid);	
				}
				else
				{
					tsb.append(","+cid);
				}
				i++;
			}
			contact_ids = tsb.toString();
		}
	}

}
