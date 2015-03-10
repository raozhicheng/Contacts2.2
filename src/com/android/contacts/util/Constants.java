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

package com.android.contacts.util;

import android.app.Service;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts.Data;

/**
 * Background {@link Service} that is used to keep our process alive long enough
 * for background threads to finish. Started and stopped directly by specific
 * background tasks when needed.
 */
public class Constants {
    /**
     * Specific MIME-type for {@link Phone#CONTENT_ITEM_TYPE} entries that
     * distinguishes actions that should initiate a text message.
     */
    public static final String MIME_SMS_ADDRESS = "vnd.android.cursor.item/sms-address";

    public static final String SCHEME_TEL = "tel";
    public static final String SCHEME_SMSTO = "smsto";
    public static final String SCHEME_MAILTO = "mailto";
    public static final String SCHEME_IMTO = "imto";
    


	/* Add begin, custom by 139's contacts */
    /**
     * The name of shared preferences file which store the filter of contacts
     */
    public static final String FILTER_SEE_CONTACTS="com.asp.contacts.filter";
    public static final String FILTER_SEE_ONLY_PHONE="filter_see_only_phone";
    public static final String FILTER_SEE_ONLY_NET="filter_see_only_net";
   // public static final String FILTER_SEE_ONLY_NET="raw_contacts.account_type=com.139";  //查询网络通讯录上的
    public static final String FILTER_SEE_ONLY_SIM="filter_see_only_sim";
    
    /**
     * The uri of query custom contacts info list
     */
	public static final Uri URI_CUSTOM_CONTACT_LIST = Uri.withAppendedPath(
			ContactsContract.AUTHORITY_URI, "custom_contacts");

    /**
     * The uri of search custom contacts info list
     */
	public static final Uri URI_CUSTOM_CONTACT_SEARCH = Uri.withAppendedPath(
			ContactsContract.AUTHORITY_URI, "custom_contacts/search");
	
	public static final String EXTRA_KEY_SHOW_SMS = "extra_key_show_sms";

	public static final String EXTRA_KEY_MOBILE_PHONE = "extra_key_mobile_phone";

	/**
	 * The projection for query the head view info of each contact's info view
	 */
	public final static String[] HEAD_PROJECTION = new String[] {
			RawContacts.DISPLAY_NAME_PRIMARY, RawContacts.ACCOUNT_TYPE,
			android.provider.ContactsContract.Contacts.Data.DATA1,
			RawContacts._ID };

	/**
	 * @see #HEAD_PROJECTION
	 */
	public final static int VIEW_CONTACT_HEADER_COLUMN_INDEX_NAME = 0;

	/**
	 * @see #HEAD_PROJECTION
	 */
	public final static int VIEW_CONTACT_HEADER_COLUMN_INDEX_TYPE = 1;

	/**
	 * @see #HEAD_PROJECTION
	 */
	public final static int VIEW_CONTACT_HEADER_COLUMN_INDEX_NUMBER = 2;

	/**
	 * @see #HEAD_PROJECTION
	 */
	public final static int VIEW_CONTACT_HEADER_COLUMN_INDEX_ID = 3;

	public static final String FILER_SEE_RECENT_CALLS="com.asp.recent.filter";

	public static final String FILER_SEE_CALLS_TYPE="filer_see_calls_type";
	
	public static final String EXTRA_SHOW_GROUP_ITEMS="extra_show_group_items";
	/* Add End, custom by 139's contacts */
}
