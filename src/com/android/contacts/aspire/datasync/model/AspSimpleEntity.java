package com.android.contacts.aspire.datasync.model;

import java.util.ArrayList;

import android.provider.ContactsContract;

/**  
 * Filename:    AspSimpleEntity.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-27 下午06:11:46  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-27    liangbo             1.0        1.0 Version  
 */

public class AspSimpleEntity {
	public	String localID =null; //本地的 联系人 ID
	public	String remoteID =null;//远程的联系人 ID
	
	public String localEdition = null;//本地保存上次服务器同步的版本（时间）
	public String remoteEdition = null;//本地服务器传回的时间
	
	
	/**
	 * 构造函数，从一个详细的本地实体信息中，提取必要的信息，创建本对象
	 * @param entity
	 */
	public AspSimpleEntity(AspEntityDelta entity)
	{
		if(entity!=null)
		{
			if(entity.mValues!=null)
			{
				localID = entity.mValues.getId();
				remoteID = entity.mValues.getAsString(ContactsContract.RawContacts.SOURCE_ID);
			}
			ArrayList<AspValuesDelta> mimeEntries =entity.getMimeEntries(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
			
			if (mimeEntries != null && mimeEntries.size()>0) 
			{
				//姓名项目在 data中只有一条数据
				AspValuesDelta name = mimeEntries.get(0);
				localEdition = name.getAsString(ContactsContract.CommonDataKinds.StructuredName.SYNC2);				
			}	        
		}
	}
	
	/**
	 * 判断是否需要删除本地的该联系人的数据
	 * @return
	 */
	public boolean needLocalDel()
	{
		if(localID!=null && remoteEdition==null)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	

}
