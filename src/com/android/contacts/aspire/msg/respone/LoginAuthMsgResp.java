package com.android.contacts.aspire.msg.respone;

import com.android.contacts.aspire.config.Config;

import android.sax.Element;
import android.sax.EndTextElementListener;

/**  
 * Filename:    LoginAuthMsgResp.java  
 * Description:   登录139响应消息
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-26 下午01:59:22  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-26    liangbo             1.0        1.0 Version  
 */

public class LoginAuthMsgResp extends BaseWith139ResponeMsg {
	
	/**登录成功的 session_key*/
	public String sessionkey="";
	
	public LoginAuthMsgResp() {
		super("users_loginAuth_response");		
		
		//登录成功sessionkey
		Element  licenseTag = root.getChild(Config.URL_139_XMLNS,"result");
		licenseTag.setEndTextElementListener(new EndTextElementListener() {	
			public void end(String body) {
				sessionkey=body;	
			}
		}); 
		
	}
	
     @Override
     public String toString(){
 		String xml = " sessionkey = "+ sessionkey ;	
		return super.toString() + xml.toString();
     }
}
