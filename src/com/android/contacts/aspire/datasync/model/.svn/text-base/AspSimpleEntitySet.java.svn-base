package com.android.contacts.aspire.datasync.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.model.EntitySet;

import android.content.ContentResolver;
import android.content.Entity;
import android.content.EntityIterator;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;

/**  
 * Filename:    AspSimpleEntitySet.java  
 * Description:   
 * Copyright:   Copyright (c)2009  
 * Company:    company 
 * @author:     liangbo  
 * @version:    1.0  
 * Create at:   2010-9-27 下午06:08:20  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2010-9-27    liangbo             1.0        1.0 Version  
 */

public class AspSimpleEntitySet extends HashMap<String,AspSimpleEntity> implements
Parcelable 
{
	

    /**
     * 装载本地存数的139联系人的全部的简要信息，因为装载完整信息内存压力大，只能分段装载完整信息
     * @param resolver
     * @param account
     * @param sortOrder
     * @return
     */
	public static AspSimpleEntitySet fromQueryWith139(ContentResolver resolver,String account,
			/*String selection, String[] selectionArgs,*/ String sortOrder) {
		// 使用resolver Content解决者，读取到联系人的游标，利用RawContacts的newEntityIterator函数
		// 装换成EntityIterator
		// RawContactsEntity.CONTENT_URI 是他自己定义的一个是视图
		EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
				RawContactsEntity.CONTENT_URI, null
				, RawContacts.ACCOUNT_TYPE+ " = '"+Config.ACCOUNT_TYPE_139+"' AND "
				+ RawContacts.ACCOUNT_NAME+ " = '"+account+"' AND "
				+ RawContacts.DELETED +" = '0' "				
				, null,
				sortOrder));
		try {
			
			final AspSimpleEntitySet state = new AspSimpleEntitySet();
			// Perform background query to pull contact details
			// 迭代取得的联系人数据
			int i=0;
			while (iterator.hasNext()) {
				
				
					// Read all contacts into local deltas to prepare for edits
					final Entity before = iterator.next();
					// 取得EntityDelta 数据变化实体，将实体装入变化实体方在 before
					final AspEntityDelta entity = AspEntityDelta.fromBefore(before);
					
					
					AspSimpleEntity  ase = new AspSimpleEntity(entity);
					state.put(ase.localID, ase);
				

			}
			return state;
		} finally {
			iterator.close();
		}
	}
	
	
	/**
	 * 用远程139的联系人ID找本机对应的联系人简要信息
	 * @param remoteID
	 * @return
	 */
	public AspSimpleEntity getEntityFromRemoteID(String remoteID)
	{
		//AspSimpleEntity simpleEntity = null;
		if(remoteID!=null)
		{
			for(AspSimpleEntity se:this.values())
			{
				if(se.remoteID!=null && se.remoteID.equals(remoteID))
				{
					return se;
				}
			}  
		}
		
		
		return null;
	}


	public int describeContents() {
		return 0;

		
	}

	
	public void writeToParcel(Parcel paramParcel, int paramInt) {
	}

}
