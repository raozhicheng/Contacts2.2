/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.aspire.datasync.sim;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.datasync.sim.model.SimContact;
import com.android.contacts.aspire.datasync.sim.model.SimContactList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;

/**
 *本类的所有操作都是对SIM卡的操作，并不是对本地库中缓存的SIM卡的操作
 */
public class SimOperation {

	private static final Uri URL = Uri.parse("content://icc/adn");

	public static final int SIM_STATE_ABSENT = TelephonyManager.SIM_STATE_ABSENT;// ("无卡");
	public static final int SIM_STATE_UNKNOWN = TelephonyManager.SIM_STATE_UNKNOWN;// ("未知状态");
	public static final int SIM_STATE_NETWORK_LOCKED = TelephonyManager.SIM_STATE_NETWORK_LOCKED;// ("需要NetworkPIN解锁");
	public static final int SIM_STATE_PIN_REQUIRED = TelephonyManager.SIM_STATE_PIN_REQUIRED;// ("需要PIN解锁");
	public static final int SIM_STATE_PUK_REQUIRED = TelephonyManager.SIM_STATE_PUK_REQUIRED;// ("需要PUK解锁");
	public static final int SIM_STATE_READY = TelephonyManager.SIM_STATE_READY;// ("良好");

	// 删除一个用户 用 参数：oldname oldnumber
	//
	public int delContact() {
		return -1;
	}

	// 添加一个用户 参数： name number

	// 修改一个用户 参数：oldname oldnumber newname number

	// 取得联系人数量 返回size

	/**
	 * 从SIM卡读取全部的卡上联系人数据
	 */
	public static SimContactList getSimContacts(ContentResolver cr) {
		if(Config.IS_DEBUG)
		{
			return SimContactList.testSimContactList2();
		}
		else
		{
			return getSimContactsRun(cr);
		}
	}
	
	
	
	private static SimContactList getSimContactsRun(ContentResolver cr) {
		SimContactList simContactList = new SimContactList();
		
		Cursor cursor = cr.query(URL, null, null, null, null);
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			SimContact simContact = new SimContact();

			simContact.friendName = cursor.getString(cursor
					.getColumnIndex("name"));
			simContact.friendMobile = cursor.getString(cursor
					.getColumnIndex("number"));

			// Log.i("tom", "_id==>"
			// + cursor.getString(cursor.getColumnIndex("_id"))
			// //不是SIM卡中的ID，应该只是Provider自己补充的
			// + " name==>"
			// + cursor.getString(cursor.getColumnIndex("name"))
			// + " number==>"
			// + cursor.getString(cursor.getColumnIndex("number"))
			// + " emails==>"
			// + cursor.getString(cursor.getColumnIndex("emails")));//暂不支持存储

			simContactList.simContactList.add(simContact);
		}
		cursor.close();

		return simContactList;
	}
	

	public static int getSimState(Context ct) {
		// TelephonyManager.SIM_STATE_ABSENT ://("无卡");

		// TelephonyManager.SIM_STATE_UNKNOWN ://("未知状态");

		// TelephonyManager.SIM_STATE_NETWORK_LOCKED ://("需要NetworkPIN解锁");

		// TelephonyManager.SIM_STATE_PIN_REQUIRED ://("需要PIN解锁");

		// TelephonyManager.SIM_STATE_PUK_REQUIRED ://("需要PUK解锁");

		// TelephonyManager.SIM_STATE_READY ://("良好");

		// TelephonyManager tm = TelephonyManager.getDefault();
		TelephonyManager tm = (TelephonyManager) ct.getSystemService("phone");// 取得相关系统服务

		return tm.getSimState();
	}

	public static String getSimSerialNumber(Context ct) {
		TelephonyManager tm = (TelephonyManager) ct.getSystemService("phone");// 取得相关系统服务

		return tm.getSimSerialNumber();
	}

}
