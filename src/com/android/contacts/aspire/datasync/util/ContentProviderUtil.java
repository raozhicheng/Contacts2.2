package com.android.contacts.aspire.datasync.util;

import java.util.ArrayList;
import net.sourceforge.pinyin4j.PinyinHelper;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import com.android.contacts.aspire.config.Config;
import com.android.contacts.aspire.config.ErrorInfo;
import com.android.contacts.aspire.datasync.icontact139.IContact139AccountManager;
import com.android.contacts.aspire.datasync.sim.model.SimContact;
import com.android.contacts.aspire.msg.respone.FriendItem;
import com.google.android.collect.Lists;

/**
 * Filename: ContentProviderUtil.java Description: 对本地数据库操作（针对Provider缓存139
 * 和Provider缓存SIM数据的操作） Copyright: Copyright (c)2009 Company: company
 * 
 * @author: liangbo
 * @version: 1.0 Create at: 2010-8-27 上午10:31:34
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2010-8-27 liangbo 1.0 1.0 Version
 */

public class ContentProviderUtil {

	/**
	 * 利用139信息，在RawContacts表中新建一条联系人记录
	 * 
	 * @param account139LoginName
	 * @param contact139Id
	 * @param updateTime
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterRawContactsFrom139(
			String account139LoginName, String contact139Id, String updateTime,
			String friendName, String nameSpell) {
		ContentValues values = new ContentValues();
		values.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139);
		values.put(RawContacts.ACCOUNT_NAME, account139LoginName);
		values.put(RawContacts.SOURCE_ID, contact139Id);

		values.put(RawContacts.DIRTY, "1");

		// values.put(RawContacts.VERSION, Math.abs(updateTime.hashCode()) );
		//		
		// String tNameSpell="";
		// if(nameSpell!=null)
		// {
		// tNameSpell = nameSpell;
		// }
		// else
		// {
		// //转换名称为拼音
		// ChineseSpelling finder = ChineseSpelling.getInstance();
		// tNameSpell = finder.getSelling(friendName);
		// }
		// values.put(RawContacts.SYNC2, tNameSpell);

		return newInsterRawContacts(values);
	}

	/**
	 * 利用SIM信息，在RawContacts表中新建一条联系人记录
	 * 
	 * @param account139LoginName
	 * @param contact139Id
	 * @param updateTime
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterRawContactsFromSIM(
			String accountSimCardID, String contactSimId) {
		ContentValues values = new ContentValues();
		values.put(RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM);
		values.put(RawContacts.ACCOUNT_NAME, accountSimCardID);
		values.put(RawContacts.SOURCE_ID, contactSimId);
		values.put(RawContacts.DIRTY, "1");

		return newInsterRawContacts(values);
	}

	/**
	 * 利用139信息，在Group表中新建一条联系人组记录
	 * 
	 * @param account139LoginName
	 * @param contact139Id
	 * @param updateTime
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterGroupFrom139(
			String account139LoginName, String contact139GroupId,
			String groupName) {

		ContentValues values = new ContentValues();
		values.put(ContactsContract.Groups.ACCOUNT_TYPE,
				Config.ACCOUNT_TYPE_139);
		values.put(ContactsContract.Groups.ACCOUNT_NAME, account139LoginName);
		values.put(ContactsContract.Groups.SOURCE_ID, contact139GroupId);
		values.put(ContactsContract.Groups.TITLE, groupName);
		values.put(ContactsContract.Groups.DIRTY, "1");

		// 把中文组名转换成拼音
		String tgroupName = ChineseSpelling.getSortKey(groupName);

		// 为了对组名排序
//		if (!tgroupName.equals(groupName) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}
//		if (groupName.equals(Config.DEFAULT_GROUP_NAME) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}

		values.put(ContactsContract.Groups.SYNC4, tgroupName);

		return newInsterGroup(values);
	}

	/**
	 * 利用139信息，在Group表中新建一条联系人组记录
	 * 
	 * @param account139LoginName
	 * @param contact139Id
	 * @param updateTime
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterGroupFromSIM(
			String accountSimLoginName, String contactSimGroupId,
			String groupName) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.Groups.ACCOUNT_TYPE,
				Config.ACCOUNT_TYPE_SIM);
		values.put(ContactsContract.Groups.ACCOUNT_NAME, accountSimLoginName);
		values.put(ContactsContract.Groups.SOURCE_ID, contactSimGroupId);
		values.put(ContactsContract.Groups.TITLE, groupName);
		values.put(ContactsContract.Groups.DIRTY, "1");

		// 把中文组名转换成拼音
		String tgroupName = ChineseSpelling.getSortKey(groupName);

		// 为了对组名排序
//		if (!tgroupName.equals(groupName) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}
//		if (groupName.equals(Config.DEFAULT_GROUP_NAME) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}

		values.put(ContactsContract.Groups.SYNC4, tgroupName);

		return newInsterGroup(values);
	}

	public static ContentProviderOperation.Builder newUpdateRawContactsFrom139(
			int localContactId, String updateTime, String friendName,
			String nameSpell, String sourceId) {
		ContentValues values = new ContentValues();
		values.put(RawContacts.SOURCE_ID, sourceId);
		// values.put(RawContacts.VERSION, Math.abs(updateTime.hashCode()));
		//		
		// String tNameSpell="";
		// if(nameSpell!=null)
		// {
		// tNameSpell = nameSpell;
		// }
		// else
		// {
		// //转换名称为拼音
		// ChineseSpelling finder = ChineseSpelling.getInstance();
		// tNameSpell = finder.getSelling(friendName);
		// }
		// values.put(RawContacts.SYNC2, nameSpell);

		return newUpdateRawContacts(localContactId, values);
	}

	public static ContentProviderOperation.Builder newUpdateRawContactsSpellFrom139(
			int localContactId, String friendName, String nameSpell) {
		ContentValues values = new ContentValues();

		String tNameSpell = "";

		// 转换名称为拼音

		tNameSpell = ChineseSpelling.getSortKey(friendName);

		values.put(RawContacts.SORT_KEY_PRIMARY, tNameSpell);
		values.put(RawContacts.SORT_KEY_ALTERNATIVE, tNameSpell);

		return newUpdateRawContacts(localContactId, values);
	}

	public static ContentProviderOperation.Builder newUpdateRawContactsSpellFrom139Id139(
			String id139, String friendName, String nameSpell) {
		ContentValues values = new ContentValues();

		String tNameSpell = "";

		// 转换名称为拼音

		tNameSpell = ChineseSpelling.getSortKey(friendName);

		values.put(RawContacts.SORT_KEY_PRIMARY, tNameSpell);
		values.put(RawContacts.SORT_KEY_ALTERNATIVE, tNameSpell);

		return newUpdateRawContacts139ID(id139, values);
	}

	public static ContentProviderOperation.Builder newUpdateGroupFrom139(
			int localGroupId, String groupName) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.Groups.TITLE, groupName);

		// 把中文组名转换成拼音
		String tgroupName = ChineseSpelling.getSortKey(groupName);

		// 为了对组名排序
//		if (!tgroupName.equals(groupName) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}
//		if (groupName.equals(Config.DEFAULT_GROUP_NAME) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}

		values.put(ContactsContract.Groups.SYNC4, tgroupName);

		return newUpdateGroup(localGroupId, values);
	}

	public static ContentProviderOperation.Builder newUpdateGroupFromSim(
			int localGroupId, String groupName) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.Groups.TITLE, groupName);

		// 把中文组名转换成拼音
		String tgroupName = ChineseSpelling.getSortKey(groupName);

		// 为了对组名排序
//		if (!tgroupName.equals(groupName) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}
//		if (groupName.equals(Config.DEFAULT_GROUP_NAME) && groupName != null) {
//			tgroupName = "奡" + tgroupName;
//		}

		values.put(ContactsContract.Groups.SYNC4, tgroupName);
		return newUpdateGroup(localGroupId, values);
	}

	public static ContentProviderOperation.Builder newUpdateRawContactsFromSim(
			int localContactId, String newSouiceID) {
		ContentValues values = new ContentValues();
		values.put(RawContacts.SOURCE_ID, newSouiceID);
		return newUpdateRawContacts(localContactId, values);
	}

	/**
	 * 向Data表添加一个联系人的联系人名称记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param displayName
	 *            联系人的名字
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_StructuredName(
			boolean isNewLocalEentityDelta, int rawContactId,
			String displayName, String updataTime, boolean isUpdataVERSION) {
		ContentValues values = new ContentValues();
		values
				.put(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

		values.put(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				displayName);
		if (isUpdataVERSION)// list数据的时候 不需要跟新版本 只有详细信息取得完成 才能刷新版本号码和网络相同
		{

			// 压入139版本号 更新时间
			values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC2,
					updataTime);
		} else {
			// 压入139版本号 更新时间
			values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC2,
					"" + System.currentTimeMillis());
		}

		// 压入拼音

		values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC1,
				ChineseSpelling.Cn2Spell(displayName));

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的联系人名称记录
	 * 
	 * @param dataId
	 *            Data表的ID
	 * @param displayName
	 *            联系人的名字
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_StructuredName(
			int dataId, String displayName, String updataTime,
			boolean isUpdataVERSION) {
		ContentValues values = new ContentValues();
		values.put(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				displayName);

		if (isUpdataVERSION)// list数据的时候 不需要跟新版本 只有详细信息取得完成 才能刷新版本号码和网络相同
		{
			// 压入139版本号 更新时间
			values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC2,
					updataTime);
		} else {
			// 说明是list简要信息的操作
		}

		// 压入拼音的 临时放在教名 这个字段

		values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC1,
				ChineseSpelling.Cn2Spell(displayName));

		// 判断是否有中文 如果有 则在SYNC3字段压入 z 方便排序
		if (!displayName.equals(ChineseSpelling.Cn2Spell(displayName))) {
			values.put(ContactsContract.CommonDataKinds.StructuredName.SYNC3,
					"z");
		}

		return newUpdateDataInfo(dataId, values);

	}

	/**
	 * 向Data表添加一个联系人的联系电话（手机）记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_MOBILE(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的联系人电话号码记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            联系人的电话
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_MOBILE(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的联系邮件（家庭）记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param address
	 *            邮件地址
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Email_TYPE_HOME(
			boolean isNewLocalEentityDelta, int rawContactId, String address) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Email.TYPE,
				ContactsContract.CommonDataKinds.Email.TYPE_HOME);

		values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, address);

		// values.put(
		// ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
		// address);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的联系人电话号码记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param address
	 *            邮件地址
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Email_TYPE_HOME(
			int dataId, String address) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, address);

		// values.put(
		// ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
		// address);

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的手机（工作）记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_COMPANY_MAIN(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的联系人电话号码(工作)记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_WORK_MOBILE(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的电话(家庭)记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_HOME(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的电话(家庭)记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_HOME(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的电话1(其他)记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_OTHER(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的电话1(其他)记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_OTHER(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的工作电话记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_WORK(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的工作电话记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_WORK(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的工作传真记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Phone_TYPE_FAX_WORK(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
				ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);

		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的工作传真记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Phone_TYPE_FAX_WORK(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的qq记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Im_PROTOCOL_QQ(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Im.TYPE,
				ContactsContract.CommonDataKinds.Im.TYPE_HOME);

		values.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,
				ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ);

		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的qq记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Im_PROTOCOL_QQ(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的飞信记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Im_PROTOCOL_CUSTOM_FETION(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Im.TYPE,
				ContactsContract.CommonDataKinds.Im.TYPE_HOME);

		values.put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL,
				FriendItem.IM_PROTOCOL_CUSTOM_FETION);

		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的飞信记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Im_PROTOCOL_CUSTOM_FETION(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的MSN记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Im_PROTOCOL_MSN(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Im.TYPE,
				ContactsContract.CommonDataKinds.Im.TYPE_HOME);

		values.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,
				ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN);

		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的MSN记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Im_PROTOCOL_MSN(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Im.DATA, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的个人主页记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Website_TYPE_HOMEPAGE(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Website.TYPE,
				ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE);

		values.put(ContactsContract.CommonDataKinds.Website.URL, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的个人主页记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Website_TYPE_HOMEPAGE(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Website.URL, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的公司主页记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Website_TYPE_WORK(
			boolean isNewLocalEentityDelta, int rawContactId, String number) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Website.TYPE,
				ContactsContract.CommonDataKinds.Website.TYPE_WORK);

		values.put(ContactsContract.CommonDataKinds.Website.URL, number);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的公司主页记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Website_TYPE_WORK(
			int dataId, String number) {
		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Website.URL, number);
		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的公司记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_Organization_TYPE_WORK(
			boolean isNewLocalEentityDelta, int rawContactId,
			String companyName, String job) {
		ContentValues values = new ContentValues();

		values
				.put(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Organization.TYPE,
				ContactsContract.CommonDataKinds.Organization.TYPE_WORK);

		if (companyName != null) {
			values.put(ContactsContract.CommonDataKinds.Organization.COMPANY,
					companyName);
		}

		if (job != null) {
			values
					.put(
							ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
							job);
		}

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的公司记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_Organization_TYPE_WORK(
			int dataId, String companyName, String job) {
		ContentValues values = new ContentValues();
		if (companyName != null) {
			values.put(ContactsContract.CommonDataKinds.Organization.COMPANY,
					companyName);
		}

		if (job != null) {
			values
					.put(
							ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
							job);
		}

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的地址记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_StructuredPostal_FORMATTED_ADDRESS_TYPE_HOME(
			boolean isNewLocalEentityDelta, int rawContactId, String regin,
			String city, String postcode, String address) {
		ContentValues values = new ContentValues();

		values
				.put(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
				ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);
		// 省
		if (regin != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.REGION,
					regin);
		}

		// 市
		if (city != null) {
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					city);
		}

		// 邮编
		if (postcode != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					postcode);
		}
		// 地址
		values
				.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						address);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的地址记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_StructuredPostal_FORMATTED_ADDRESS_TYPE_HOME(
			int dataId, String regin, String city, String postcode,
			String address) {
		ContentValues values = new ContentValues();
		// 省
		if (regin != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.REGION,
					regin);
		}

		// 市
		if (city != null) {
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					city);
		}

		// 邮编
		if (postcode != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					postcode);
		}
		// 地址
		values
				.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						address);

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的公司地址记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_StructuredPostal_FORMATTED_ADDRESS_TYPE_WORK(
			boolean isNewLocalEentityDelta, int rawContactId, String regin,
			String city, String postcode, String address) {
		ContentValues values = new ContentValues();

		values
				.put(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
				ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
		// 省
		if (regin != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.REGION,
					regin);
		}

		// 市
		if (city != null) {
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					city);
		}

		// 邮编
		if (postcode != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					postcode);
		}
		// 地址
		values
				.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						address);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的公司地址记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_StructuredPostal_FORMATTED_ADDRESS_TYPE_WORK(
			int dataId, String regin, String city, String postcode,
			String address) {
		ContentValues values = new ContentValues();
		// 省
		if (regin != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.REGION,
					regin);
		}

		// 市
		if (city != null) {
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					city);
		}

		// 邮编
		if (postcode != null) {
			values.put(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					postcode);
		}
		// 地址
		values
				.put(
						ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						address);

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的生日记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_StructuredPostal_Event_TYPE_BIRTHDAY(
			boolean isNewLocalEentityDelta, int rawContactId, String birthday) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);

		values.put(ContactsContract.CommonDataKinds.Event.TYPE,
				ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);

		// 生日
		values.put(ContactsContract.CommonDataKinds.Event.START_DATE, birthday);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的生日记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_StructuredPostal_Event_TYPE_BIRTHDAY(
			int dataId, String birthday) {
		ContentValues values = new ContentValues();

		values.put(ContactsContract.CommonDataKinds.Event.START_DATE, birthday);

		return newUpdateDataInfo(dataId, values);
	}

	/**
	 * 向Data表添加一个联系人的所属组记录
	 * 
	 * @param isNewLocalEentityDelta
	 *            是否是新建的联系人
	 * @param rawContactId
	 *            不是新建的联系人 就是联系人的RAW_CONTACT_ID，如果是新建的联系人
	 *            则就是操作序列中，插入联系人操作的序列好前firstIndex = diff.size();
	 * @param number
	 *            手机号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newInsterData_GroupMembership_GROUP_SOURCE_ID(
			boolean isNewLocalEentityDelta, int rawContactId,
			String localGroupId, String i139roupID) {
		ContentValues values = new ContentValues();

		values
				.put(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
		// 所属组 本地组ID
		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
				localGroupId);

		// 所属组 远程139组 ID
		values.put(ContactsContract.CommonDataKinds.GroupMembership.DATA2,
				i139roupID);

		if (isNewLocalEentityDelta) {

			return newInsterDataInfoWithBackReference(rawContactId, values);
		} else {
			return newInsterDataInfo(rawContactId, values);
		}
	}

	/**
	 * 在Data表添更新一个联系人的生日记录
	 * 
	 * @param dataId
	 *            data表的ID
	 * @param number
	 *            电话号码
	 * @return
	 */
	public static ContentProviderOperation.Builder newUpdateData_GroupMembership_GROUP_SOURCE_ID(
			int dataId, String localGroupId, String i139roupID) {
		ContentValues values = new ContentValues();

		// 所属组 本地组ID
		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
				localGroupId);

		// 所属组 远程139组 ID
		values.put(ContactsContract.CommonDataKinds.GroupMembership.DATA2,
				i139roupID);

		return newUpdateDataInfo(dataId, values);
	}

	private static ContentProviderOperation.Builder newInsterDataInfo(
			int rawContactId, ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);// 这里用的的ID是本地缓存的rawContactId
		// 因为原来的联系人本来就存在需要取新的联系人ID
		// 装入数据
		builder.withValues(values);
		return builder;
	}

	private static ContentProviderOperation.Builder newInsterDataInfoWithBackReference(
			int rawContactId, ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				rawContactId);// 这里的ID是之前插入row_content表的时候返回的ID
		// 装入数据
		builder.withValues(values);
		return builder;
	}

	private static ContentProviderOperation.Builder newUpdateDataInfo(
			int dataId, ContentValues values) {
		// 建立一条Data表的更新 依赖于data表的ID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(ContactsContract.Data.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.Data._ID + " = " + dataId, null);// 

		// 装入数据
		builder.withValues(values);
		return builder;
	}

	public static ContentProviderOperation.Builder newDeleteDataInfo(
			int localContactId) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newDelete(ContactsContract.Data.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.Data.RAW_CONTACT_ID + " = "
				+ localContactId, null);// 
		return builder;
	}

	private static ContentProviderOperation.Builder newInsterRawContacts(
			ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI);
		// 装入数据
		builder.withValues(values);
		return builder;
	}

	private static ContentProviderOperation.Builder newUpdateRawContacts(
			int localContactId, ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.RawContacts._ID + " = "
				+ localContactId, null);// 

		// 装入数据
		builder.withValues(values);
		return builder;
	}

	private static ContentProviderOperation.Builder newUpdateRawContacts139ID(
			String id139, ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.RawContacts.SOURCE_ID + " = "
				+ id139, null);// 

		// 装入数据
		builder.withValues(values);
		return builder;
	}

	public static ContentProviderOperation.Builder newDeleteRawContacts(
			int localContactId) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newDelete(ContactsContract.RawContacts.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.RawContacts._ID + " = "
				+ localContactId, null);// 
		return builder;
	}

	private static ContentProviderOperation.Builder newInsterGroup(
			ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的group
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(ContactsContract.Groups.CONTENT_URI);
		// 装入数据
		builder.withValues(values);
		return builder;
	}

	private static ContentProviderOperation.Builder newUpdateGroup(
			int localGroupId, ContentValues values) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的group
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(ContactsContract.Groups.CONTENT_URI);
		// 设置条件 原来本地库的group表的ID
		builder.withSelection(ContactsContract.Groups._ID + " = "
				+ localGroupId, null);// 

		// 装入数据
		builder.withValues(values);
		return builder;
	}

	public static ContentProviderOperation.Builder newDeleteGroup(
			int localGroupId) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newDelete(ContactsContract.Groups.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.RawContacts._ID + " = "
				+ localGroupId, null);// 
		return builder;
	}

	public static ContentProviderOperation.Builder newDeleteDataInfoFromID(
			int localDataId) {
		// 如果是新建的 则在插入的时候 需要去从操作序列中取得 之前插入联系人的时候得到的row_contentID
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newDelete(ContactsContract.Data.CONTENT_URI);
		// 设置条件 原来本地库的data表的ID
		builder.withSelection(ContactsContract.Data._ID + " = " + localDataId,
				null);// 
		return builder;
	}

	/**
	 * 删除本地缓存库中缓存的全部的SIM卡账户的联系人信息（删除全部）
	 */
	public static int delProviderSimData(ContentResolver cr) {
		int error = ErrorInfo.ERROR_UNKNOW;
		try {
			ArrayList<ContentProviderOperation> ops = Lists.newArrayList();

			/*// 首先查询出属于 139缓存类型的用户 的全部的raw_countenID
			Cursor cursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.DELETED
							+ " = '0'" + " AND " + RawContacts.ACCOUNT_TYPE
							+ " = '" + Config.ACCOUNT_TYPE_SIM + "'", null,
					null);

			while (cursor.moveToNext()) {

				String rawCoutactID = cursor.getString(cursor
						.getColumnIndex(RawContacts._ID));
				// 创建删除 Data表的操作
				ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
						.withSelection(
								Data.RAW_CONTACT_ID + " = " + rawCoutactID,
								null).build());
			}

			cursor.close();

			// 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
					.withSelection(
							RawContacts.DELETED + " = '0'" + " AND "
									+ RawContacts.ACCOUNT_TYPE + " = '"
									+ Config.ACCOUNT_TYPE_SIM + "'", null)
					.build());

			// 删除用户组 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(
					ContactsContract.Groups.CONTENT_URI).withSelection(
					ContactsContract.Groups.DELETED + " = '0'" + " AND "
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
							+ Config.ACCOUNT_TYPE_SIM + "'", null).build());*/
			
			// SIM联系人删除条件
			String where = RawContacts.DELETED + " = '0'" + " AND "
			+ RawContacts.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_SIM + "'";
			// SIM删除联系人（标志位至1）
			cr.delete(RawContacts.CONTENT_URI, where, null);
			
            //SIM联系人组删除条件
			String groupwhere = ContactsContract.Groups.DELETED + " = '0'" + " AND "
			+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_SIM + "'";
			//SIM删除联系人组
			cr.delete(ContactsContract.Groups.CONTENT_URI, groupwhere, null);

			ContentProviderResult[] cprArray = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);

			error = ErrorInfo.SUCCESS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}

	/**
	 * 删除本地缓存库中缓存的全部的SIM卡账户的联系人信息
	 */
	public static int delProviderSimDataOutOfSimId(ContentResolver cr,
			String SimId) {
		int error = ErrorInfo.ERROR_UNKNOW;
		try {
			ArrayList<ContentProviderOperation> ops = Lists.newArrayList();

			/*
			 * // 首先查询出属于 139缓存类型的用户 的全部的raw_countenID Cursor cursor =
			 * cr.query(RawContacts.CONTENT_URI, new String[] { RawContacts._ID
			 * }, RawContacts.DELETED + " = '0'" + " AND " +
			 * RawContacts.ACCOUNT_TYPE + " = '" +
			 * Config.ACCOUNT_TYPE_SIM+"' AND " + RawContacts.ACCOUNT_NAME +
			 * " <> '" + SimId+"'"
			 * 
			 * , null, null);
			 * 
			 * while (cursor.moveToNext()) {
			 * 
			 * String rawCoutactID = cursor.getString(cursor
			 * .getColumnIndex(RawContacts._ID)); // 创建删除 Data表的操作
			 * ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
			 * .withSelection( Data.RAW_CONTACT_ID + " = " + rawCoutactID,
			 * null).build()); }
			 * 
			 * cursor.close();
			 * 
			 * // 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			 * ops.add(ContentProviderOperation
			 * .newDelete(RawContacts.CONTENT_URI) .withSelection(
			 * RawContacts.DELETED + " = '0'" + " AND " +
			 * RawContacts.ACCOUNT_TYPE + " = '" + Config.ACCOUNT_TYPE_SIM +
			 * "' AND " + RawContacts.ACCOUNT_NAME + " <> '" + SimId+"'" , null)
			 * .build());
			 * 
			 * 
			 * // 删除用户组 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			 * ops.add(ContentProviderOperation
			 * .newDelete(ContactsContract.Groups.CONTENT_URI) .withSelection(
			 * ContactsContract.Groups.DELETED + " = '0'" + " AND " +
			 * ContactsContract.Groups.ACCOUNT_TYPE + " = '" +
			 * Config.ACCOUNT_TYPE_SIM + "' AND " +
			 * ContactsContract.Groups.ACCOUNT_NAME + " <> '" + SimId+"'" ,
			 * null) .build());
			 */

			// SIM联系人删除条件
			String where = RawContacts.DELETED + " = '0'" + " AND "
					+ RawContacts.ACCOUNT_TYPE + " = '"
					+ Config.ACCOUNT_TYPE_SIM + "' AND "
					+ RawContacts.ACCOUNT_NAME + " <> '" + SimId + "'";
			// SIM删除联系人（标志位至1）
			cr.delete(RawContacts.CONTENT_URI, where, null);
			
            //SIM联系人组删除条件
			String groupwhere = ContactsContract.Groups.DELETED + " = '0'"
					+ " AND " + ContactsContract.Groups.ACCOUNT_TYPE + " = '"
					+ Config.ACCOUNT_TYPE_SIM + "' AND "
					+ ContactsContract.Groups.ACCOUNT_NAME + " <> '" + SimId
					+ "'";
			//SIM删除联系人组
			cr.delete(ContactsContract.Groups.CONTENT_URI, groupwhere, null);

			ContentProviderResult[] cprArray = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);

			error = ErrorInfo.SUCCESS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}

	/**
	 * 删除本地缓存库中缓存的全部的139账户的联系人信息
	 */
	public static int delProvider139Data(ContentResolver cr) {
		int error = ErrorInfo.ERROR_UNKNOW;
		try {
			ArrayList<ContentProviderOperation> ops = Lists.newArrayList();

			/*// 首先查询出属于 139缓存类型的用户 的全部的raw_countenID
			Cursor cursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.DELETED
							+ " = '0'" + " AND " + RawContacts.ACCOUNT_TYPE
							+ " = '" + Config.ACCOUNT_TYPE_139 + "'", null,
					null);

			// cursor.moveToFirst();
			while (cursor.moveToNext()) {

				String rawCoutactID = cursor.getString(cursor
						.getColumnIndex(RawContacts._ID));
				// 创建删除 Data表的操作
				ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
						.withSelection(
								Data.RAW_CONTACT_ID + " = " + rawCoutactID,
								null).build());
			}

			cursor.close();

			// 删除RawContacts表
			// 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
					.withSelection(
							RawContacts.DELETED + " = '0'" + " AND "
									+ RawContacts.ACCOUNT_TYPE + " = '"
									+ Config.ACCOUNT_TYPE_139 + "'", null)
					.build());

			// 删除Groups 表
			// 删除用户组 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(
					ContactsContract.Groups.CONTENT_URI).withSelection(
					ContactsContract.Groups.DELETED + " = '0'" + " AND "
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
							+ Config.ACCOUNT_TYPE_139 + "'", null).build());*/
			
			// 139联系人删除条件
			String where = RawContacts.DELETED + " = '0'" + " AND "
			+ RawContacts.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_139 + "'";
			// 139删除联系人（标志位至1）
			cr.delete(RawContacts.CONTENT_URI, where, null);
			
            // 139联系人组删除条件
			String groupwhere = ContactsContract.Groups.DELETED + " = '0'" + " AND "
			+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_139 + "'";
			// 139删除联系人组
			cr.delete(ContactsContract.Groups.CONTENT_URI, groupwhere, null);

			
			ContentProviderResult[] cprArray = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);

			error = ErrorInfo.SUCCESS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}

	/**
	 * 删除本地缓存库中缓存的全部的139卡账户的联系人信息
	 */
	public static int delProvider139DataOutOfId(ContentResolver cr, String Id139) {
		int error = ErrorInfo.ERROR_UNKNOW;
		try {
			ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
			/*
			// 首先查询出属于 139缓存类型的用户 的全部的raw_countenID
			Cursor cursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.DELETED
							+ " = '0'" + " AND " + RawContacts.ACCOUNT_TYPE
							+ " = '" + Config.ACCOUNT_TYPE_139 + "' AND "
							+ RawContacts.ACCOUNT_NAME + " <> '" + Id139 + "'"

					, null, null);

			if (cursor != null) {
				while (cursor.moveToNext()) {

					String rawCoutactID = cursor.getString(cursor
							.getColumnIndex(RawContacts._ID));
					// 创建删除 Data表的操作
					ops.add(ContentProviderOperation
							.newDelete(Data.CONTENT_URI).withSelection(
									Data.RAW_CONTACT_ID + " = " + rawCoutactID,
									null).build());
				}

				cursor.close();
			}

			// 删除RawContacts表
			// 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
					.withSelection(
							RawContacts.DELETED + " = '0'" + " AND "
									+ RawContacts.ACCOUNT_TYPE + " = '"
									+ Config.ACCOUNT_TYPE_139 + "' AND "
									+ RawContacts.ACCOUNT_NAME + " <> '"
									+ Id139 + "'", null).build());

			// 删除 group表
			// 删除用户组 因为已经删除数据还是保存在表里面的，此处添加删除标志判断，防止数据过多操作过慢
			ops.add(ContentProviderOperation.newDelete(
					ContactsContract.Groups.CONTENT_URI).withSelection(
					ContactsContract.Groups.DELETED + " = '0'" + " AND "
							+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
							+ Config.ACCOUNT_TYPE_139 + "' AND "
							+ ContactsContract.Groups.ACCOUNT_NAME + " <> '"
							+ Id139 + "'", null).build());*/
			
			// 139联系人删除条件
			String where = RawContacts.DELETED + " = '0'" + " AND "
			+ RawContacts.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_139 + "' AND "
			+ RawContacts.ACCOUNT_NAME + " <> '"
			+ Id139 + "'";
			// 139删除联系人（标志位至1）
			cr.delete(RawContacts.CONTENT_URI, where, null);
			
            // 139联系人组删除条件
			String groupwhere = ContactsContract.Groups.DELETED + " = '0'" + " AND "
			+ ContactsContract.Groups.ACCOUNT_TYPE + " = '"
			+ Config.ACCOUNT_TYPE_139 + "' AND "
			+ ContactsContract.Groups.ACCOUNT_NAME + " <> '"
			+ Id139 + "'";
			// 139删除联系人组
			cr.delete(ContactsContract.Groups.CONTENT_URI, groupwhere, null);

			ContentProviderResult[] cprArray = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);

			error = ErrorInfo.SUCCESS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}

	/**
	 * 负责创建一个叫做未分组的组，用来存放所有的没有分组的联系人（包括139网络联系人和sim卡联系人）
	 * 
	 * @param cr
	 * @param returnLocalGroupId
	 *            返回参数，存放用来返回的 未分组的本地组的ID
	 * @return
	 */
	public static int createProviderGroupUnMember(ContentResolver cr,
			ArrayList<String> returnLocalGroupId) {
		int error = ErrorInfo.ERROR_UNKNOW;
		try {

			// 首先查询系统有没有已经设定了这个 叫未分组的组
			Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI,
					new String[] { ContactsContract.Groups._ID },
					ContactsContract.Groups.DELETED + " = '0'" + " AND "
							+ ContactsContract.Groups.SOURCE_ID + " = '"
							+ Config.DEFAULT_GROUP_SOURCE_ID + "'"

					, null, null);

			String localGroupID = null;

			while (cursor.moveToNext()) {

				localGroupID = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Groups._ID));
			}
			cursor.close();

			// 如果没有 则说明没有这个组 则创建，否则直接返回组 ID
			if (localGroupID == null) {
				ArrayList<ContentProviderOperation> ops = Lists.newArrayList();

				// 在数据表中创建一个未分组的组
				ops.add(ContentProviderOperation.newInsert(
						ContactsContract.Groups.CONTENT_URI).withValue(
						ContactsContract.Groups.SOURCE_ID,
						Config.DEFAULT_GROUP_SOURCE_ID).withValue(
						ContactsContract.Groups.TITLE,
						Config.DEFAULT_GROUP_NAME).withValue(
						ContactsContract.Groups.SYNC4, "奡奡").build());

				ContentProviderResult[] cprArray = cr.applyBatch(
						ContactsContract.AUTHORITY, ops);

				if (cprArray != null && cprArray.length > 0) {
					long tId = ContentUris.parseId(cprArray[0].uri);
					if (tId != -1) {
						localGroupID = "" + tId;
					}

				}

			}

			if (localGroupID != null) {
				returnLocalGroupId.add(localGroupID);
			}

			error = ErrorInfo.SUCCESS;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}

	public static void testAdd139Contact(ContentResolver cr) { // 测试循环插入联系人
		try {
			for (int j = 0; j < 10; j++) {
				ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
				for (int i = 0; i < 50; i++) {

					int rawContactInsertIndex = ops.size();
					// public static final String ACCOUNT_NAME = "account_name";
					// public static final String ACCOUNT_TYPE = "account_type";
					// public static final String SOURCE_ID = "sourceid";
					// public static final String VERSION = "version";
					// public static final String DIRTY = "dirty";
					ops.add(ContentProviderOperation.newInsert(
							RawContacts.CONTENT_URI).withValue(
							RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_139)
							.withValue(RawContacts.ACCOUNT_NAME, "13714669692")
							.withValue(RawContacts.SOURCE_ID,
									"139-" + (j * 5 + i)).withValue(
									RawContacts.SYNC1,
									System.currentTimeMillis()).withValue(
									RawContacts.DIRTY, "1").build());
					// try {
					// new Object().wait(2000);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }

					ops.add(ContentProviderOperation
							.newInsert(Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID,
									rawContactInsertIndex).withValue(
									Data.MIMETYPE,
									StructuredName.CONTENT_ITEM_TYPE)
							.withValue(StructuredName.DISPLAY_NAME, "朱x" + i)
							.build());

					ops.add(ContentProviderOperation
							.newInsert(Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID,
									rawContactInsertIndex).withValue(
									Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, "1398929734" + i)
							.withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());

					ops.add(ContentProviderOperation.newAssertQuery(
							RawContacts.CONTENT_URI).withSelection(
							RawContacts.SOURCE_ID + "='139-12'" + " AND "
									+ RawContacts.DELETED + " = '0'" + " AND "
									+ RawContacts.ACCOUNT_TYPE + " = '"
									+ Config.ACCOUNT_TYPE_139 + "'" + " AND "
									+ RawContacts.ACCOUNT_NAME
									+ " = '13714669692'", null).withValue(
							RawContacts.ACCOUNT_NAME, "'13714669692'").build());

				}
				ContentProviderResult[] cpr = cr.applyBatch(
						ContactsContract.AUTHORITY, ops);
				int l = cpr.length;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void testAssert139Contact(ContentResolver cr) {
		try {
			for (int j = 0; j < 1; j++) {
				ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
				for (int i = 0; i < 1; i++) {

					ops.add(ContentProviderOperation.newAssertQuery(
							RawContacts.CONTENT_URI).withSelection(
							RawContacts.SOURCE_ID + "='139-12'" + " AND "
									+ RawContacts.DELETED + " = '0'" + " AND "
									+ RawContacts.ACCOUNT_TYPE + " = '"
									+ Config.ACCOUNT_TYPE_139 + "'" + " AND "
									+ RawContacts.ACCOUNT_NAME
									+ " = '13714669692'", null).withValue(
							RawContacts.ACCOUNT_NAME, "'13714669692'").build());

				}
				ContentProviderResult[] cpr = cr.applyBatch(
						ContactsContract.AUTHORITY, ops);
				int l = cpr.length;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void testAddSIMContact(ContentResolver cr) {
		try {
			for (int j = 0; j < 1; j++) {
				ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
				for (int i = 0; i < 1; i++) {

					int rawContactInsertIndex = ops.size();
					// public static final String ACCOUNT_NAME = "account_name";
					// public static final String ACCOUNT_TYPE = "account_type";
					// public static final String SOURCE_ID = "sourceid";
					// public static final String VERSION = "version";
					// public static final String DIRTY = "dirty";
					ops.add(ContentProviderOperation.newInsert(
							RawContacts.CONTENT_URI).withValue(
							RawContacts.ACCOUNT_TYPE, Config.ACCOUNT_TYPE_SIM)
							.withValue(RawContacts.ACCOUNT_NAME, "sim")
							.withValue(RawContacts.SOURCE_ID, i).withValue(
									RawContacts.SYNC1, "10000").withValue(
									RawContacts.DIRTY, "1").build());
					// try {
					// new Object().wait(2000);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }

					ops.add(ContentProviderOperation
							.newInsert(Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID,
									rawContactInsertIndex).withValue(
									Data.MIMETYPE,
									StructuredName.CONTENT_ITEM_TYPE)
							.withValue(StructuredName.DISPLAY_NAME, "朱x" + i)
							.build());

					ops.add(ContentProviderOperation
							.newInsert(Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID,
									rawContactInsertIndex).withValue(
									Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, "1398929734" + i)
							.withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());

				}
				cr.applyBatch(ContactsContract.AUTHORITY, ops);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
