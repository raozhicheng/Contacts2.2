package com.android.contacts.aspire.msg.request;

import java.util.Hashtable;

/**
 * 请求消息接口
 * @author liangbo
 *
 * 2010-8-20 下午08:19:24
 *  
 * IRequestMsg
 *
 */
public interface IRequestMsg 
{
	/**
	 * 获取数据，一次性获取全部数据
	 * 
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] getData() throws Exception;

	/**
	 * 分批次获取数据接口，获取数据长度，最大为length，主要是为上传提供的接口
	 * 
	 * @param length
	 *            int
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] getData(int length) throws Exception;

	/**
	 * 添加需要从http协议header中获取数据的属性
	 */
	public void addNeedRespHeaderProp(String key,String value);

	/**
	 * 需要从http协议header中获取数据的属性
	 */
	public Hashtable getNeedRespHeaderProps();

	public String getContentType();
	
	public String getURL();

	public boolean isCMWap();
	
	public String getMsgType();
}
