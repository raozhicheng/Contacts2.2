package com.android.contacts.aspire.msg.request;

import com.android.contacts.aspire.msg.MessageType;

/**  
 * Filename:    BaseWith139RequestMsg.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 上午08:56:26  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public abstract class BaseWith139RequestMsg  extends XmlRequestMsg{

	/**139登录操作返回码 */
	public String session_key="";
	
	
	public BaseWith139RequestMsg(String session_key){
		super();
		this.session_key = session_key;
	}
	
	
	public String toXml() {
		// TODO Auto-generated method stub
		StringBuffer st=new StringBuffer();
		st.append("<session_key>"+session_key+"</session_key>");
		st.append(toXmlWith139());
		return st.toString();
		
	}
	
	
	public abstract String toXmlWith139();
	
}
