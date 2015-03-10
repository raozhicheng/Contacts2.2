package com.android.contacts.aspire.msg.respone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.android.contacts.aspire.config.Config;


import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;


public abstract class XmlResponseMsg implements IResponseMsg 
{
	
	
	
    /**区分不同平台返报文跟节点是resp或Resp*/
    protected String resp="resp";
//	/** 消息类型 */
//	public String msgType = "";
//
//	/** 协议版本号 */
//	public String version = "1.0";
//
//	/** 返回值ֵ */
//	public int hRet = 1;

	public RootElement root;

	public XmlResponseMsg(String resp) 
	{
		this.resp = resp;
		
		
		root = new RootElement(Config.URL_139_XMLNS,resp);
//		Element msgTypeTag = root.getChild("MsgType");
//		// 监听标签体,标签体结束的时候将文本内容放到ArrayList里面,下同
//		msgTypeTag.setEndTextElementListener(new EndTextElementListener()
//		{
//			public void end(String body) 
//			{
//				msgType = body;
//			}
//		});

//		Element versionTag = root.getChild("Version");
//		versionTag.setEndTextElementListener(new EndTextElementListener()
//		{
//			public void end(String body)
//			{
//				version = body;
//			}
//		});

//		Element retCodeTag = root.getChild("hRet");
//		retCodeTag.setEndTextElementListener(new EndTextElementListener() 
//		{
//			public void end(String body) 
//			{
//				hRet = Integer.parseInt(body);
//			}
//		});
	}

	public void parseInputStream(InputStream in) throws SAXException,
			ParserConfigurationException, FactoryConfigurationError,
			IOException 
			{
		// sax解析
		String xml = convertStreamToString(in);
		System.out.println("----response xmlMsg=" + xml);
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
				.getXMLReader();
		reader.setContentHandler(root.getContentHandler());

		InputStream ins = new ByteArrayInputStream(xml.getBytes("UTF-8"));

		InputSource isx = new InputSource(ins);

		reader.parse(isx);
		// in.close();

	}

	private String convertStreamToString(InputStream is)
	{
		BufferedReader reader = null;
		try 
		{
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} 
		catch (UnsupportedEncodingException e1) 
		{
			e1.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try
			{
				// MidLog.servLogger.info("mid MidResp "+sb.toString());
				is.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}

	public void parseInputStream(InputStream in, HttpURLConnection httpConn) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
