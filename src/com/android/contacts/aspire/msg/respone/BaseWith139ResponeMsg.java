package com.android.contacts.aspire.msg.respone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;

import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.util.StringAndInputStreamUtil;
import com.android.contacts.aspire.msg.request.IRequestMsg;

/**  
 * Filename:    BaseWith139ResponeMsg.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-8-31 上午10:49:31  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-8-31    liangbo             1.0        1.0 Version  
 */

public abstract class BaseWith139ResponeMsg extends XmlResponseMsg {

	ErrorResponse errorResponse = new ErrorResponse();
	
	
	public ErrorResponse getErrorResponse() {
		return errorResponse;
	}


	public BaseWith139ResponeMsg(String resp) {
		super(resp);
		//默认是 我们定义的未知错误
		errorResponse.error_code = ErrorInfo.ERROR_UNKNOW;
		// TODO Auto-generated constructor stub
	}

	
	public void parseInputStream(InputStream in) throws SAXException,
	ParserConfigurationException, FactoryConfigurationError,
	IOException 
	{
		
		String msgCatch =  StringAndInputStreamUtil.inputStream2String(in);
		System.out.println(msgCatch);
		//String msgCatch ="";
		//String tryString =  msgCatch.substring(0, 16).toLowerCase();
		
		//首先检测字符串流的开头有没有 <error_response>
		if(msgCatch.indexOf("<error_response>")!=-1/*tryString.equals("<error_response>")*/)
		{
			//如果有 则说明是错误的返回  使用错误返回解析
			// TODO Auto-generated catch block
			//如果是正常的消息结果解码错误，
			//则尝试是使用错误消息结构解码
			
			msgCatch = msgCatch.substring(msgCatch.indexOf("?>")+2);
			
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
			.getXMLReader();

			
			
			
			root = new RootElement("","error_response");
						
			Element errorCode1Tag = root.getChild("error_code");
			// 监听标签体,标签体结束的时候将文本内容放到ArrayList里面,下同
			errorCode1Tag.setEndTextElementListener(new EndTextElementListener()
			{
				public void end(String body) 
				{
					if(errorResponse==null)
					{
						errorResponse =new ErrorResponse();
					}
					try {
						errorResponse.error_code = Integer.parseInt(body);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			Element errorMsg2Tag = root.getChild("error_msg");
			// 监听标签体,标签体结束的时候将文本内容放到ArrayList里面,下同
			errorMsg2Tag.setEndTextElementListener(new EndTextElementListener()
			{
				public void end(String body) 
				{
					if(errorResponse==null)
					{
						errorResponse =new ErrorResponse();
					}
					
					errorResponse.error_msg = body;
					
				}
			});
			
			
			reader.setContentHandler(root.getContentHandler());

			

			InputSource isx = new InputSource(StringAndInputStreamUtil.String2InputStream(msgCatch));

			reader.parse(isx);
			
		}
		else
		{
			//给同一错误消息头赋值为正常
			//标识 这个不是返回的错误消息
			errorResponse.error_code = ErrorInfo.SUCCESS;
			//
			//super.parseInputStream(in);
			super.parseInputStream(StringAndInputStreamUtil.String2InputStream(msgCatch));
		}
			
		
}

}
