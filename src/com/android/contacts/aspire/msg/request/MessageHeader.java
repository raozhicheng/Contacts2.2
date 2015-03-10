package com.android.contacts.aspire.msg.request;

import com.android.contacts.aspire.config.Config;



/**
 * 请求消息 包头
 * @author x_liyajun
 *
 * 2010-8-20 下午08:23:02
 *  
 * MessageHeader
 *
 */
public class MessageHeader 
{	
	/**请求类型*/
	public String msgType = "";
	
	/**协议版本*/
	public String version = Config.version;

	/**发送方地址类型(0：上网本平台、1：MID客户端内置卡、2：MID客户端计费卡、3：第三方业务平台、999：自定义类别)*/
	public int sendAddressType = 1;
	
	/**发送方地址标识(上网本平台：000、MID客户端内置卡：SIM卡号码、MID客户端计费卡：计费手机号码、第三方业务平台：网元ID)*/
	public String sendAddressId = "22222222222";
	
	/**接收方地址类型(0：上网本平台、1：MID客户端内置卡、2：MID客户端计费卡、3：第三方业务平台、999：自定义类别)*/
	public int destAddressType = 0;
	
	/**接收方地址标识(上网本平台：000、MID客户端内置卡：SIM卡号码、MID客户端计费卡：计费手机号码、第三方业务平台：网元ID)*/
	public String destAddressId = "000"; 
	
	  
     /**发起方标记，0=MID终端；1=BOSS发起(上网本平台)*/
	 public String ncSendFlag = "0"; 
	
	//向支撑平台请求报文包头
	protected String getMsgHeadler()
	{
		StringBuffer st = new StringBuffer();
		st.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		st.append("<req>");
		st.append("<MsgType>"+msgType+"</MsgType>");
		st.append("<Version>"+version+"</Version>");
		st.append("<Send_Address>");
		st.append("<Type>"+sendAddressType+"</Type>");
		st.append("<ID>"+sendAddressId+"</ID>");
		st.append("</Send_Address>");
		st.append("<Dest_Address>");
		st.append("<Type>"+destAddressType+"</Type>");
		st.append("<ID>"+destAddressId+"</ID>");
		st.append("</Dest_Address>");
		return st.toString();
	}
	
	//向上网本平台请求报文包头
//	   protected String getMsgNHeadler()
//	    {
//	        StringBuffer st = new StringBuffer();
//	        st.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//	        st.append("<Req>");
//	        st.append("<MsgType>"+msgType+"</MsgType>");
//	        st.append("<Version>"+version+"</Version>");
//	        st.append("<SendFlag>"+ncSendFlag+"</SendFlag>");
//	        return st.toString();
//	    }
	///http head
	/**设备识别号*/
	public String deviceID = "1111111111";
	
	/**标识AP或者CP的代码*/
	public String platformCode = "";
	
	/**标识应用或者内容的ID或者业务代码*/
	public String serviceCode = "";

	public String contentType = "text/xml";
}
