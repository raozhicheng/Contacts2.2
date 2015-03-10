package com.android.contacts.aspire.msg.request;

import com.android.contacts.aspire.msg.MessageType;

/**  
 * Filename:    LoginAuthMsgReq.java  
 * Description:   用户登录139请求消息
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-26 下午01:52:51  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-26    liangbo             1.0        1.0 Version  
 */

public class LoginAuthMsgReq extends XmlRequestMsg {
	
	/**139登录用户号 即手机号 */
	public String loginName="";
	
	
	public LoginAuthMsgReq(){
		super();
		this.header.msgType=MessageType.CLIENTLOGIN_REQ;
	}

	@Override
	public String toXml() {		
		// TODO Auto-generated method stub
		StringBuffer st=new StringBuffer();
		st.append("<Mobile>"+loginName+"</Mobile>");
		st.append("</req>");
		return st.toString();
	}

}
