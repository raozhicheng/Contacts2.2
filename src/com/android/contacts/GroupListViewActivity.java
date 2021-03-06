package com.android.contacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import com.android.contacts.ContactsListActivity.ContactListItemCache;
import com.android.contacts.ContactsListActivity.RefreshTask;
import com.android.contacts.aspire.config.Config;
import com.android.contacts.model.Net139Source;
import com.android.contacts.ui.ContactsPreferencesActivity;
import com.android.contacts.ui.CustomContactsPrefsActivity;
import com.android.contacts.ui.GroupsActivity;
import com.android.contacts.util.Constants;

import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Intents.UI;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class GroupListViewActivity extends ExpandableListActivity
// implements View.OnCreateContextMenuListener
{

	private GroupExpandableListAdapter mAdapter = null;
	private static final String TAG = "GroupListViewActivity";

	private static final int SUBACTIVITY_SEE = 6;

	// **************************组数组，用存储每组内的联系人****************************
	public class Entity {
		String name = null;
		String phone = null;
		String type = null;
		long contactId = 0;
		String lookupKey = null;
	}

	// *********************显示分组menuItem**********************
	private MenuItem group = null;
	private TextView countView = null;
	private ExpandableListView listView = null;
	private int allcount = 0;

	private static final int INDEX_ID = 0;
	private static final int INDEX_NAME = 2;
	private static final int INDEX_PHONE = 4;
	private static final int INDEX_TYPE = 11;
	private static final int INDEX_LOOKUP = 8;
	private static final int HAS_PHONE_NUMBER = 10;

	// 查询条件
	private static final String GROUPS_WHERE = " ((" + Groups.ACCOUNT_TYPE
			+ " = '" + Config.ACCOUNT_TYPE_139 + "' ) OR ( " + Groups.SOURCE_ID
			+ " = '" + Config.DEFAULT_GROUP_SOURCE_ID + "' ))" + " AND "
			+ Groups.DELETED + " = '0'";

	// 选项设置条件
	private static final String CLAUSE_ONLY_NET = RawContacts.ACCOUNT_TYPE
			+ "= '" + Config.ACCOUNT_TYPE_139 + "'";
	private static final String CLAUSE_ONLY_SIM = RawContacts.ACCOUNT_TYPE
			+ "= '" + Config.ACCOUNT_TYPE_SIM + "'";

	private boolean mDisplayOnlyPhones = false;
	private boolean mDisplayOnlySIM = true;
	private boolean mDisplayOnlyNet = true;

	// ContextMenu
	static final int MENU_ITEM_VIEW_CONTACT = 1;
	static final int MENU_ITEM_CALL = 2;
	static final int MENU_ITEM_EDIT_BEFORE_CALL = 3;
	static final int MENU_ITEM_SEND_SMS = 4;
	static final int MENU_ITEM_SEND_IM = 5;
	static final int MENU_ITEM_EDIT = 6;
	static final int MENU_ITEM_DELETE = 7;
	static final int MENU_ITEM_TOGGLE_STAR = 8;
	
	private static final String CLAUSE_IS_NOT_DELETED = RawContacts.DELETED

	+ "=0";

	// **************************组内联系人查询**********************************
	private final String[] CUSTOM_CONTACTS_PROJECTION = new String[] {
			Contacts._ID, // 0
			Contacts.DISPLAY_NAME_PRIMARY, // 1
			Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
			Contacts.SORT_KEY_PRIMARY, // 3
			Phone.NUMBER, // 4
			Contacts.TIMES_CONTACTED, // 5
			Contacts.CONTACT_PRESENCE, // 6
			Contacts.PHOTO_ID, // 7
			Contacts.LOOKUP_KEY, // 8
			Contacts.PHONETIC_NAME, // 9
			Contacts.HAS_PHONE_NUMBER, // 10
			RawContacts.ACCOUNT_TYPE, // 11
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setTitle("显示" +"位有电话号码的联系人");
		
		// 设置选项模式
		setOptionMode();
		
		
		setContentView(R.layout.elistview_zgy_layout);
		//初始化View
		findView();
		// count = (TextView) findViewById(R.id.mycount);

		// count = new TextView(this);
		/*
		 * AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
		 * ViewGroup.LayoutParams.FILL_PARENT, 40); count.setLayoutParams(lp);
		 */
		// count.setText("显示联系人数为");

		// final ExpandableListView list = getExpandableListView();
		// final ExpandableListView list = new ExpandableListView(this);
		mAdapter = new GroupExpandableListAdapter(this);
		// 设置列表适配器IdeasExpandableListAdapter
		// list.setAdapter(mAdapter);
		listView.setAdapter(mAdapter);
		// 注册长按选项弹出莱单
		registerForContextMenu(listView);
		// 设置显示联系人数
		setAllCountView();

	}

	public void onResume() {
		super.onResume();
		// 设置选项模式
		setOptionMode();
		mAdapter = new GroupExpandableListAdapter(this);
		// 设置列表适配器IdeasExpandableListAdapter
		setListAdapter(mAdapter);
		// 设置显示联系人数
		setAllCountView();
	}

	public void findView() {
		listView = (ExpandableListView) findViewById(android.R.id.list);
		countView = (TextView) this.findViewById(R.id.list_title_text);
	}

	private void setAllCountView() {
		Uri uri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI,
				"custom_contacts");
		Cursor cursor = getContentResolver().query(uri, null, // 相应组下过滤查询
				null, null, null);
		allcount = cursor.getCount();
		cursor.close();

		String mycount = String.valueOf(allcount);
		if (mDisplayOnlyPhones) {
			countView.setText("显示" + mycount + "位有电话号码的联系人");
			//this.setTitle("显示" + mycount + "位有电话号码的联系人");
		} else {
			countView.setText("显示" + mycount + "位联系人");
			//this.setTitle("显示" + mycount + "位联系人");
		}

	}

	private void setOptionMode() {
		SharedPreferences prefs = getSharedPreferences(
				Constants.FILTER_SEE_CONTACTS, MODE_WORLD_READABLE);
		mDisplayOnlyPhones = prefs.getBoolean(Constants.FILTER_SEE_ONLY_PHONE,
				false);
		mDisplayOnlySIM = prefs.getBoolean(Constants.FILTER_SEE_ONLY_SIM, true);
		mDisplayOnlyNet = prefs.getBoolean(Constants.FILTER_SEE_ONLY_NET, true);
	}

	private String getContactSelection() {

		String rtnStr = GROUPS_WHERE; // 初始不過濾
		if (mDisplayOnlyPhones) {
			// nothing
		}
		if (mDisplayOnlyNet && mDisplayOnlySIM) {
			// has no limit if both net and SIM;
		} else if (mDisplayOnlyNet) {
			rtnStr += " AND " + CLAUSE_ONLY_NET;
		} else if (mDisplayOnlySIM) {
			rtnStr += " AND " + CLAUSE_ONLY_SIM;
		}
		return rtnStr;
	}

	// 长按行建立ContextMenu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int groupPos = 0;
		int childPos = 0;
		Entity entity = null;
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

			groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			entity = (Entity) mAdapter.getChild(groupPos, childPos);

			menu.setHeaderTitle(entity.name);
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
					entity.contactId);
			menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
					.setIntent(new Intent(Intent.ACTION_VIEW, contactUri));
			//menu.add(0, MENU_ITEM_CALL, 0, getString(R.string.menu_call));
			menu.add(0, MENU_ITEM_CALL, 0, "呼叫"+entity.phone);
			menu
					.add(0, MENU_ITEM_SEND_SMS, 0,
							getString(R.string.menu_sendSMS));
		} else {
			// nothing
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();
		int groupPos = 0;
		int childPos = 0;
		Entity entity = null;
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

			groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			entity = (Entity) mAdapter.getChild(groupPos, childPos);
		} else {
			// nothing
			return true;
		}
		String phone = entity.phone;
		switch (item.getItemId()) {

		case MENU_ITEM_CALL: {
			ContactsUtils.initiateCall(GroupListViewActivity.this, phone);
			return true;
		}

		case MENU_ITEM_SEND_SMS: {
			ContactsUtils.initiateSms(GroupListViewActivity.this, phone);
			return true;
		}
		}

		return false;
	}

	/*
	 * @Override public void onCreateContextMenu(ContextMenu menu, View view,
	 * 生成长按MENU ContextMenuInfo menuInfo) { // If Contacts was invoked by }
	 * another Activity simply as a way of // picking a contact, don't show the
	 * context menu
	 * 
	 * 
	 * AdapterView.AdapterContextMenuInfo info; try { info =
	 * (AdapterView.AdapterContextMenuInfo) menuInfo; } catch
	 * (ClassCastException e) { return; }
	 * 
	 * Cursor cursor = (Cursor) ((Menu)
	 * getExpandableListAdapter()).getItem(info.position); if (cursor == null) {
	 * // For some reason the requested item isn't available, do nothing return;
	 * } long id = info.id; Uri contactUri =
	 * ContentUris.withAppendedId(Contacts.CONTENT_URI, id); long rawContactId =
	 * ContactsUtils.queryForRawContactId( getContentResolver(), id); Uri
	 * rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
	 * rawContactId);
	 * 
	 * Setup the menu header menu.setHeaderTitle(cursor .getString(INDEX_NAME));
	 * 
	 * // View contact details menu.add(0, MENU_ITEM_VIEW_CONTACT, 0,
	 * R.string.menu_viewContact) .setIntent(new Intent(Intent.ACTION_VIEW,
	 * contactUri));
	 * 
	 * if (cursor.getInt(HAS_PHONE_NUMBER) != 0) { // Calling contact
	 * menu.add(0, MENU_ITEM_CALL, 0, getString(R.string.menu_call)); // Send
	 * item menu .add(0, MENU_ITEM_SEND_SMS, 0,
	 * getString(R.string.menu_sendSMS)); //长按，向联系人发短信 }
	 * 
	 * }
	 * 
	 * @Override public boolean onContextItemSelected(MenuItem item) {
	 * AdapterView.AdapterContextMenuInfo info; TextView v=null; try { info =
	 * (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	 * 
	 * } catch (ClassCastException e) { android.util.Log.e(TAG,
	 * "bad menuInfo",e); return false; }
	 * 
	 * //Cursor cursor = (Cursor) getExpandableListAdapter().
	 * //getItem(info.position);
	 * 
	 * v=(TextView)info.targetView.findViewById(R.id.tv_phone); //获取选中行的tv_phone
	 * String phone=v.getText().toString(); //cursor.getString(INDEX_PHONE);
	 * switch (item.getItemId()) {
	 * 
	 * case MENU_ITEM_CALL: {
	 * ContactsUtils.initiateCall(GroupListViewActivity.this, phone); return
	 * true; }
	 * 
	 * case MENU_ITEM_SEND_SMS: {
	 * ContactsUtils.initiateSms(GroupListViewActivity.this, phone); return
	 * true; } }
	 * 
	 * 
	 * return super.onContextItemSelected(item); }
	 */

	/**
	 * @author IdeasAndroid 可展开（收缩）列表示例
	 */
	public class GroupExpandableListAdapter extends BaseExpandableListAdapter // CursorAdapter
	// implements
	// ExpandableListAdapter

	{

		private String[] groups = null;

		LayoutInflater inflater = null;
		String keyStr = null; // 编辑框内容长度
		int keyStrstsrt = 0, keyStrend = 0;

		/*private static final String CLAUSE_IS_NOT_DELETED = RawContacts.DELETED

		+ "=0";

		// **************************组内联系人查询**********************************
		private final String[] CUSTOM_CONTACTS_PROJECTION = new String[] {
				Contacts._ID, // 0
				Contacts.DISPLAY_NAME_PRIMARY, // 1
				Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
				Contacts.SORT_KEY_PRIMARY, // 3
				Phone.NUMBER, // 4
				Contacts.TIMES_CONTACTED, // 5
				Contacts.CONTACT_PRESENCE, // 6
				Contacts.PHOTO_ID, // 7
				Contacts.LOOKUP_KEY, // 8
				Contacts.PHONETIC_NAME, // 9
				Contacts.HAS_PHONE_NUMBER, // 10
				RawContacts.ACCOUNT_TYPE, // 11
		};*/

		// **************************组查询************************************
		private final String[] GROUPS_PROJECTION = new String[] { Groups._ID, // 0
				Groups.SYNC1, // 1 139ID
				Groups.TITLE, // 2
				Groups.SUMMARY_COUNT, // 3
				Groups.SUMMARY_WITH_PHONES // 4
		};

		private static final int INDEX_ID_COLUMN = 0;
		// private static final int INDEX_SYNC1_COLUMN = 1;
		private static final int INDEX_TITLE_COLUMN = 2;
		private static final int INDEX_COUNT_COLUMN = 3;
		// private static final int INDEX_SUMMARY_PHONES_COLUMN = 4;

		private Context mContext = null;
		private List<String> groupList = null;
		// private List<List<Cursor>> itemList = null; // 做Cursor的链表的链表
		private List<List<Entity>> itemList = null; // 做Entity的链表的链表

		/*
		 * private Cursor cursor = getContentResolver().query(
		 * Groups.CONTENT_SUMMARY_URI, GROUPS_PROJECTION, GROUPS_WHERE, null,
		 * ContactsContract.Groups.SYNC4);
		 */
		/*
		 * //mCursor = getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
		 * GROUPS_PROJECTION, getContactSelection(), null,
		 * ContactsContract.Groups.SYNC4);
		 */
		/* 初始化组 */
		@SuppressWarnings("null")
		public void initGroup() {

			Cursor cursor = getContentResolver().query(
					Groups.CONTENT_SUMMARY_URI, GROUPS_PROJECTION,
					getContactSelection(), null, ContactsContract.Groups.SYNC4);

			// System.out.print("*********加载的组 数：*********" +
			// cursor.getCount());
			android.util.Log.i(TAG, "*********加载的组 数：*********"
					+ cursor.getCount());
			groups = new String[cursor.getCount()]; // 加上头，显示联系人个数
			// groups[0] = "显示联系人位数";

			if (cursor != null) {
				// int groupi = 0;
				cursor.moveToFirst();
				/*
				 * android.util.Log.i(TAG, "*********加载的组名：*********" +
				 * cursor.getString(2)); groups[0] =
				 * cursor.getString(INDEX_TITLE_COLUMN); cursor.moveToNext();
				 * android.util.Log.i(TAG, "*********加载的组名：*********" +
				 * cursor.getString(2)); groups[1] =
				 * cursor.getString(INDEX_TITLE_COLUMN); cursor.moveToNext();
				 * android.util.Log.i(TAG, "*********加载的组名：*********" +
				 * cursor.getString(2)); groups[2] =
				 * cursor.getString(INDEX_TITLE_COLUMN);
				 */

				for (int n = 0; n < groups.length; n++) {

					android.util.Log.i(TAG, "*********加载的组名：*********"
							+ cursor.getString(INDEX_TITLE_COLUMN));
					groups[n] = cursor.getString(INDEX_TITLE_COLUMN);
					// groups[n] =
					// cursor.getString(INDEX_TITLE_COLUMN)+"  ("+cursor.getCount()+")";
					cursor.moveToNext();
				}
				/*
				 * System.out.print("*********加载的组名：*********" +
				 * cursor.getString(3));
				 */// !!!!!!
				/*
				 * while (cursor != null) {
				 * System.out.print("*********加载的组名：*********" +
				 * cursor.getString(3)); //!!!!!! groups[groupi] =
				 * cursor.getString(INDEX_TITLE_COLUMN);
				 * System.out.print("*********加载的组名：*********" +
				 * groups[groupi]); cursor.moveToNext(); groupi++; }
				 */

				cursor.close();
			}
		}

		/* 初始化组内联系人 */
		public void initChild() {
			// itemList =new ArrayList<List<Cursor>>();

			Uri mGroupUri = null;
			Cursor childCursor = null;
			int childcount = 0;
			for (int j = 0; j < groups.length; j++) {

				List<Entity> item = new ArrayList<Entity>();

				mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI,
						groups[j]); // 组模式
				childCursor = getContentResolver().query(mGroupUri,
						CUSTOM_CONTACTS_PROJECTION, // 相应组下过滤查询
						CLAUSE_IS_NOT_DELETED, null, getSortOrder(CUSTOM_CONTACTS_PROJECTION));
				childCursor.moveToFirst();
				android.util.Log.i(TAG, "*********此组联系人 数：*********"
						+ childCursor.getCount());
				childcount = childCursor.getCount();
				// groupchildcount[j]=String.valueOf(childCursor.getCount());

				allcount = allcount + childcount;

				for (int n = 0; n < childcount; n++) {

					Entity entity = new Entity();
					entity.contactId = childCursor.getLong(INDEX_ID); //
					entity.lookupKey = childCursor.getString(INDEX_LOOKUP);
					entity.name = childCursor.getString(INDEX_NAME);

					android.util.Log.i(TAG, "*********加入联系人名：*********"
							+ childCursor.getString(INDEX_NAME));

					entity.phone = childCursor.getString(INDEX_PHONE);
					entity.type = childCursor.getString(INDEX_TYPE);
					item.add(entity);
					childCursor.moveToNext();

				}

				/*
				 * while (childCursor != null) { Entity entity = new Entity();
				 * entity.contactId = childCursor.getLong(INDEX_ID); //
				 * entity.lookupKey = childCursor.getString(INDEX_LOOKUP);
				 * entity.name = childCursor.getString(INDEX_NAME); entity.phone
				 * = childCursor.getString(INDEX_PHONE); entity.type =
				 * childCursor.getString(INDEX_TYPE); item.add(entity);
				 * childCursor.moveToNext(); }
				 */
				itemList.add(item);
				childCursor.close(); // 关游标
			}
			// String mycount=String.valueOf(allcount);
			// countView.setText(mycount);
		}

		/*
		 * public final Uri CONTENT_URI = //
		 * android.provider.CallLog.Calls.CONTENT_URI;
		 * android.provider.ContactsContract.;
		 */
		private final Uri URI_CUSTOM_CONTACT_LIST = Uri.withAppendedPath(
				android.provider.ContactsContract.AUTHORITY_URI,
				"custom_contacts");

		// ******************************cur查询列表属性参数*************************************
		final String[] CUR_C = new String[] { "display_name", "data1",
				"raw_contacts._id as _id", "account_type", "lookup" };

		/*
		 * private Cursor mCursor = null;
		 * 
		 * private String selectStr = "1";
		 */

		public GroupExpandableListAdapter(Context context) {
			// super(context, c, autoRequery);
			inflater = getLayoutInflater();
			this.mContext = context;

			/*
			 * mCursor = getContentResolver().query(
			 * Uri.withAppendedPath(Constants.URI_CUSTOM_CONTACT_SEARCH,
			 * TextUtils.isEmpty(selectStr) ? "*" : selectStr), CUR_C, null,
			 * null, null);
			 */
			groupList = new ArrayList<String>();
			itemList = new ArrayList<List<Entity>>();
			initData();

		}

		/**
		 * 初始化数据，将相关数据放到List中，方便处理
		 */
		private void initData() {

			initGroup(); // 初始化组
			for (int i = 0; i < groups.length; i++) {
				// if(groupchildcount[i]!=null){
				// groupList.add(groups[i] + "   ( " + groupchildcount[i] +
				// " )");
				// }
				// else{
				groupList.add(groups[i]);
				// }
			}

			initChild(); // 初始化组内联系人

		}

		public boolean areAllItemsEnabled() {
			return false;
		}

		/*
		 * 设置子节点对象，在事件处理时返回的对象，可存放一些数据
		 */
		public Object getChild(int groupPosition, int childPosition) {
			return itemList.get(groupPosition).get(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		/*
		 * 字节点视图，这里我们显示一行联系人
		 */
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Entity entityget = (Entity) itemList.get(groupPosition).get(
					childPosition);
			boolean newView;
			View view;
			if (convertView == null) {
				newView = true;
				// v = newView(mContext, cursor, parent);
				view = inflater.inflate(R.layout.recent_calls_list_item_group,
						null);
			} else {
				newView = false;
				view = convertView;
			}

			final String name = entityget.name;
			final String phone = entityget.phone;
			final String type = entityget.type;
			final long contactId = entityget.contactId;
			final String lookupKey = entityget.lookupKey;

			final Uri uri = Contacts.getLookupUri(contactId, lookupKey);

			final TextView tv_name = (TextView) view.findViewById(R.id.tv_Name);
			// tv_name.setText(name);

			TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
			// //将电话号码TextView去掉
			// tv_phone.setText(phone);

			// *************高亮实现****************
			if (TextUtils.isEmpty(keyStr)) {
				tv_name.setText(name);
				//tv_phone.setText(phone); // 设值电话号码
			} else {
				SpannableStringBuilder pstyle = new SpannableStringBuilder(
						phone);
				if (phone != null && phone.contains(keyStr)) {
					keyStrstsrt = phone.indexOf(keyStr);
					keyStrend = keyStrstsrt + keyStr.length();
					pstyle.setSpan(new BackgroundColorSpan(Color
							.rgb(56, 125, 0)), keyStrstsrt, keyStrend,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				tv_phone.setText(pstyle); // 设值电话号码

				SpannableStringBuilder nstyle = new SpannableStringBuilder(name);
				if (name != null && name.contains(keyStr)) {
					keyStrstsrt = name.indexOf(keyStr);
					keyStrend = keyStrstsrt + keyStr.length();
					nstyle.setSpan(new BackgroundColorSpan(Color
							.rgb(56, 125, 0)), keyStrstsrt, keyStrend,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				tv_name.setText(nstyle);
			}

			// ***************名信卡***********
			/*
			 * ImageButton card = (ImageButton)
			 * view.findViewById(R.id.card_icon); card.setOnClickListener(new
			 * Button.OnClickListener() { //
			 * ***************添加名信卡的触发事件处理*******************
			 * 
			 * public void onClick(View v) { // TODO Auto-generated method stub
			 * Intent intent = new Intent();
			 * intent.setClass(GroupListViewActivity.this,
			 * ViewContactActivity.class);
			 * 
			 * intent.setData(uri);// ******** startActivity(intent);
			 * GroupListViewActivity.this.finish(); } });
			 */

			// ***************打电话***********

			ImageButton callbutton = (ImageButton) view
					.findViewById(R.id.call_icon);
			callbutton.setOnClickListener(new Button.OnClickListener() {
				/*************** 添加电话的触发事件处理 *****************/
				public void onClick(View v) { // TODO Auto-generated method stub

					ContactsUtils.initiateCall(mContext, phone);

				}
			});

			// ***************发短信***********

			ImageButton card = (ImageButton) view.findViewById(R.id.msg_icon);
			card.setOnClickListener(new Button.OnClickListener() {
				/*************** 添加短信的触发事件处理 *******************/
				public void onClick(View v) { // TODO Auto-generated method stub

					ContactsUtils.initiateSms(mContext, phone);
				}
			});

			// **********************判断号码是com.139的还是SIM的*************************

			ImageView web_icon = (ImageView) view.findViewById(R.id.web_icon);
			if (ContactsUtils.isWebContact(type)) // if(type.equals("com.139"))
			{
				web_icon.setImageResource(R.drawable.ic_contact_web);
			} else {
				web_icon.setImageResource(R.drawable.ic_contact_sim);
			}

			// view.setTag(cursor.getLong(INDEX_ID_COLUMN));
			view
					.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu,
								View v, ContextMenuInfo menuInfo) {
							// *****************添加处理方法*************
						}
					});

			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					/*// *****************列表中选中一行，拨号*************
					Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
					intent.setData(Uri.fromParts("tel", phone, null));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					// mDigits.getText().clear(); // 清除查询框
*/					Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
		     contactId);
            Intent viewContactintent = new Intent(Intent.ACTION_VIEW, contactUri);	
					startActivity(viewContactintent);
					GroupListViewActivity.this.finish();
				}
			});

			/*
			 * view.setOnCreateContextMenuListener(new
			 * OnCreateContextMenuListener() {
			 * 
			 * @Override public void onCreateContextMenu(ContextMenu menu, View
			 * v, ContextMenuInfo menuInfo) { // TODO Auto-generated method stub
			 * 
			 * AdapterView.AdapterContextMenuInfo info; try { info =
			 * (AdapterView.AdapterContextMenuInfo) menuInfo; } catch
			 * (ClassCastException e) { return; }
			 * 
			 * // Setup the menu header menu.setHeaderTitle(name);
			 * 
			 * if (phone!=null) { // Calling contact menu.add(0, MENU_ITEM_CALL,
			 * 0, getString(R.string.menu_call)); // Send SMS item menu .add(0,
			 * MENU_ITEM_SEND_SMS, 0, getString(R.string.menu_sendSMS));
			 * //长按，向联系人发短信 } }
			 * 
			 * }); //长按，生成ContextMenu
			 */
			return view;
		}

		/*
		 * 返回当前分组的字节点个数
		 */
		public int getChildrenCount(int groupPosition) {
			return itemList.get(groupPosition).size();
		}

		/*
		 * 返回分组对象，用于一些数据传递，在事件处理时可直接取得和分组相关的数据
		 */
		public Object getGroup(int groupPosition) {
			return groupList.get(groupPosition);
		}

		/*
		 * 分组的个数
		 */
		public int getGroupCount() {
			return groupList.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		/*
		 * 分组视图，这里也是一个文本视图
		 */
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView text = null;
			String name = null;
			if (convertView == null) {
				text = new TextView(mContext);
			} else {
				text = (TextView) convertView;
			}

			name = (String) groupList.get(groupPosition);

			Uri mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI,
					name); // 组模式
			Cursor childCursor = getContentResolver().query(mGroupUri,
					CUSTOM_CONTACTS_PROJECTION, // 相应组下过滤查询
					CLAUSE_IS_NOT_DELETED, null, null);
			childCursor.moveToFirst();
			android.util.Log.i(TAG, "*********此组联系人 数：*********"
					+ childCursor.getCount());
			int childcount = childCursor.getCount();
			childCursor.close();
			name = name + " ( " + String.valueOf(childcount) + " )";

			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, 70);  //70是组栏宽度
			text.setLayoutParams(lp);
			text.setTextSize(20);
			text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			text.setPadding(60, 0, 0, 0);
			text.setText(name);
			return text;
		}

		/*
		 * 判断分组是否为空，本示例中数据是固定的，所以不会为空，我们返回false
		 * 如果数据来自数据库，网络时，可以把判断逻辑写到这个方法中，如果为空 时返回true
		 */
		public boolean isEmpty() {
			return false;
		}

		/*
		 * 收缩列表时要处理的东西都放这儿
		 */
		public void onGroupCollapsed(int groupPosition) {

		}

		/*
		 * 展开列表时要处理的东西都放这儿
		 */
		public void onGroupExpanded(int groupPosition) {

		}

		/*
		 * Indicates whether the child and group IDs are stable across changes
		 * to the underlying data.
		 */
		public boolean hasStableIds() {
			return false;
		}

		/*
		 * Whether the child at the specified position is selectable.
		 */
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// final long groupId=cursor.getLong(INDEX_ID_COLUMN);
			final String name = cursor.getString(INDEX_NAME);
			final String phone = cursor.getString(INDEX_PHONE);
			final String type = cursor.getString(INDEX_TYPE);
			final long contactId = cursor.getLong(INDEX_ID);
			final String lookupKey = cursor.getString(INDEX_LOOKUP);

			final Uri uri = Contacts.getLookupUri(contactId, lookupKey);

			final TextView tv_name = (TextView) view.findViewById(R.id.tv_Name);
			// tv_name.setText(name);

			TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
			// tv_phone.setText(phone);

			// *************高亮实现****************
			if (TextUtils.isEmpty(keyStr)) {
				tv_name.setText(name);
				tv_phone.setText(phone);
			} else {
				SpannableStringBuilder pstyle = new SpannableStringBuilder(
						phone);
				if (phone != null && phone.contains(keyStr)) {
					keyStrstsrt = phone.indexOf(keyStr);
					keyStrend = keyStrstsrt + keyStr.length();
					pstyle.setSpan(new BackgroundColorSpan(Color
							.rgb(56, 125, 0)), keyStrstsrt, keyStrend,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				// tv_phone.setText(pstyle); //不给电话号码设值

				SpannableStringBuilder nstyle = new SpannableStringBuilder(name);
				if (name != null && name.contains(keyStr)) {
					keyStrstsrt = name.indexOf(keyStr);
					keyStrend = keyStrstsrt + keyStr.length();
					nstyle.setSpan(new BackgroundColorSpan(Color
							.rgb(56, 125, 0)), keyStrstsrt, keyStrend,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				tv_name.setText(nstyle);
			}

			// ***************名信卡***********
			/*
			 * ImageButton card = (ImageButton)
			 * view.findViewById(R.id.card_icon); card.setOnClickListener(new
			 * Button.OnClickListener() { //
			 * ***************添加名信卡的触发事件处理*******************
			 * 
			 * public void onClick(View v) { // TODO Auto-generated method stub
			 * Intent intent = new Intent();
			 * intent.setClass(GroupListViewActivity.this,
			 * ViewContactActivity.class);
			 * 
			 * intent.setData(uri);// ******** startActivity(intent);
			 * GroupListViewActivity.this.finish(); } });
			 */

			// ***************打电话***********

			ImageButton callbutton = (ImageButton) view
					.findViewById(R.id.call_icon);
			callbutton.setOnClickListener(new Button.OnClickListener() {
				/*************** 添加电话的触发事件处理 *****************/
				public void onClick(View v) { // TODO Auto-generated method stub

					/*
					 * if (!TextUtils.isEmpty(query)) { Intent newIntent = new
					 * Intent( Intent.ACTION_CALL_PRIVILEGED,
					 * Uri.fromParts("tel", query, null));
					 * startActivity(newIntent); } finish(); return;
					 */
					ContactsUtils.initiateCall(mContext, phone);

				}
			});

			// ***************发短信***********

			ImageButton card = (ImageButton) view.findViewById(R.id.msg_icon);
			card.setOnClickListener(new Button.OnClickListener() {
				/*************** 添加短信的触发事件处理 *******************/
				public void onClick(View v) { // TODO Auto-generated method stub
					/*
					 * Intent intent = new Intent();
					 * intent.setClass(GroupListViewActivity.this,
					 * ViewContactActivity.class);
					 * 
					 * intent.setData(uri);// ******** startActivity(intent);
					 * GroupListViewActivity.this.finish();
					 */
					ContactsUtils.initiateSms(mContext, phone);
				}
			});

			// **********************判断号码是com.139的还是SIM的*************************

			ImageView web_icon = (ImageView) view.findViewById(R.id.web_icon);
			if (ContactsUtils.isWebContact(type)) // if(type.equals("com.139"))
			{
				web_icon.setImageResource(R.drawable.web_ico);
			} else {
				web_icon.setImageResource(R.drawable.sim_ico);
			}

			// view.setTag(cursor.getLong(INDEX_ID_COLUMN));
			view
					.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu,
								View v, ContextMenuInfo menuInfo) {
							// *****************添加处理方法*************
						}
					});

			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// *****************列表中选中一行，拨号*************
					Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
					intent.setData(Uri.fromParts("tel", phone, null));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					// mDigits.getText().clear(); // 清除查询框
					finish();
				}
			});
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = inflater.inflate(R.layout.recent_calls_list_item_group,
					null);
			return v;
		}

		@Override
		public long getCombinedChildId(long groupId, long childId) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getCombinedGroupId(long groupId) {
			// TODO Auto-generated method stub
			return 0;
		}

		private String getSortOrder(String[] projectionType) {
			if (true) {
				return Contacts.SORT_KEY_PRIMARY;
			} else {
				return Contacts.SORT_KEY_ALTERNATIVE;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list, menu);

		group = menu.findItem(R.id.menu_group);
		group.setTitle("字母排列显示");
        group.setIcon(R.drawable.ic_menu_sort_alphabetically);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_group:
			Intent intentgroup = new Intent(this, DialtactsActivity.class);
			intentgroup.putExtra("groupMode", false);
			intentgroup.setAction("Group");
			startActivity(intentgroup);
			return true;

		case R.id.menu_see:
			final Intent intent = new Intent(this,
					ContactsPreferencesActivity.class);
			startActivityForResult(intent, SUBACTIVITY_SEE);
			return true;
			// 测试 begin--

		case R.id.menu_refresh:
			// 执行一个刷新任务
			new RefreshTask(this).execute(new Object());

			return true;

		case R.id.menu_search:

			Intent intentsearch = new Intent(this, ContactsListActivity.class);
			intentsearch.setAction(UI.FILTER_CONTACTS_ACTION);
			startActivity(intentsearch);
			// onSearchRequested();
			return true;

		}
		return false;
	}

}
