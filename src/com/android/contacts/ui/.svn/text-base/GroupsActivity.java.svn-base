package com.android.contacts.ui;
//张国友修改过
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Intents.UI;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.contacts.ContactsListActivity;
import com.android.contacts.ContactsSearchManager;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.aspire.config.Config;
import com.android.contacts.util.Constants;

public class GroupsActivity extends Activity implements OnClickListener {

	private static final int INDEX_ID_COLUMN = 0;
	// private static final int INDEX_SYNC1_COLUMN = 1;
	private static final int INDEX_TITLE_COLUMN = 2;
	private static final int INDEX_COUNT_COLUMN = 3;
	// private static final int INDEX_SUMMARY_PHONES_COLUMN = 4;

	private static final int MENU_ITEM_SHOW_CONTACTS = 1;
	
	//有选择的查看
	private boolean mDisplayOnlyPhones;
	private boolean mDisplayOnlySIM;
	private boolean mDisplayOnlyNet;
	private static final String CLAUSE_ONLY_PHONES = "";   //默认全部
    private static final String CLAUSE_ONLY_NET = "((" +RawContacts.ACCOUNT_TYPE
	+ "= '" + Config.ACCOUNT_TYPE_139 +"')OR ( " + Groups.SOURCE_ID
			+ " = '" + Config.DEFAULT_GROUP_SOURCE_ID + "' ))";   //添加选择或未分组
    private static final String CLAUSE_ONLY_SIM = "(("+RawContacts.ACCOUNT_TYPE
	+ "= '" + Config.ACCOUNT_TYPE_SIM +"')OR ( " + Groups.SOURCE_ID
			+ " = '" + Config.DEFAULT_GROUP_SOURCE_ID + "' ))";   //添加选择或未分组

	private static final String[] GROUPS_PROJECTION = new String[] {
			Groups._ID, // 0
			Groups.SYNC1, // 1 139ID
			Groups.TITLE, // 2
			Groups.SUMMARY_COUNT, // 3
			Groups.SUMMARY_WITH_PHONES // 4
	};

	Cursor mCursor = null;

	private static final String GROUPS_WHERE = " ((" + Groups.ACCOUNT_TYPE
			+ " = '" + Config.ACCOUNT_TYPE_139 + "' ) OR ( " + Groups.SOURCE_ID
			+ " = '" + Config.DEFAULT_GROUP_SOURCE_ID + "' ))" + " AND "
			+ Groups.DELETED + " = '0'";
	
	GroupsListAdapter mAdapter = null;

	final private TempStorge tempStorage = new TempStorge();;

	private ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			if (mCursor != null && !mCursor.isClosed()) {
				onResume();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // hide title bar
		setContentView(R.layout.groups_list_content);
		initBtns();

		ContactsUtils.initBottomBtns(this);

		ContactsUtils.initToSearchView(this);
	}

	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {

		if (globalSearch) {
			super.startSearch(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		} else {
			ContactsSearchManager.startSearch(this, initialQuery);
		}
	}

	private void setDefaultMode() {
		SharedPreferences prefs=getSharedPreferences(Constants.FILTER_SEE_CONTACTS, MODE_WORLD_READABLE);
		mDisplayOnlyPhones=prefs.getBoolean(Constants.FILTER_SEE_ONLY_PHONE, false);
		mDisplayOnlySIM=prefs.getBoolean(Constants.FILTER_SEE_ONLY_SIM, true);
		mDisplayOnlyNet=prefs.getBoolean(Constants.FILTER_SEE_ONLY_NET, true);
	}
	
	private String getContactSelection() {

		String rtnStr = GROUPS_WHERE ; //初始不過濾
		if (mDisplayOnlyPhones) {
		//	nothing
		}
		if (mDisplayOnlyNet && mDisplayOnlySIM) {
			//has no limit if both net and SIM;
		} else if (mDisplayOnlyNet) {
			rtnStr += " AND " + CLAUSE_ONLY_NET;
		} else if (mDisplayOnlySIM) {
			rtnStr += " AND " + CLAUSE_ONLY_SIM;
		}
		return rtnStr;
	}
	
	public void onResume() {
		super.onResume();
		closeCursor();
		setDefaultMode();
//		mCursor = getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
//				GROUPS_PROJECTION, GROUPS_WHERE, null, null);
		mCursor = getContentResolver().query(Groups.CONTENT_SUMMARY_URI,
				GROUPS_PROJECTION, getContactSelection(), null, ContactsContract.Groups.SYNC4);
		if(mCursor!=null)
		{
			mCursor.registerContentObserver(mObserver);
			startManagingCursor(mCursor);
			ListView mGroupsList = (ListView) findViewById(R.id.lv_groups);
			mAdapter = new GroupsListAdapter(this, mCursor, true);
			mGroupsList.setAdapter(mAdapter);
		}
		
	}

	private void closeCursor() {
		if (mCursor != null) {
			mCursor.unregisterContentObserver(mObserver);
			mCursor.close();
			mCursor = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeCursor();
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeCursor();
	}

	private void initBtns() {
		/*Button btn = (Button) findViewById(R.id.btn_show_all);
		if (btn != null) {
			btn.setOnClickListener(this);
		}*/
	}

	public void onClick(View v) {
		/*switch (v.getId()) {
		case R.id.btn_show_all:
			Intent intent = new Intent(this, ContactsListActivity.class);
			startActivity(intent);
			break;
		}*/
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_ITEM_SHOW_CONTACTS:
			showGroupItems();
			return true;
		}
		return false;
	}

	public class GroupsListAdapter extends CursorAdapter {
		LayoutInflater inflater = null;

		public GroupsListAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			inflater = getLayoutInflater();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// final long groupId=cursor.getLong(INDEX_ID_COLUMN);
			final String groupName = cursor.getString(INDEX_TITLE_COLUMN);
			final int groupCount = cursor.getInt(INDEX_COUNT_COLUMN);
			final TextView tv_GroupName = (TextView) view
					.findViewById(R.id.tv_groupname);
			tv_GroupName.setText(groupName);
			TextView tv_GroupCount = (TextView) view
					.findViewById(R.id.tv_groupcount);
			tv_GroupCount.setText(getString(R.string.group_summary_conunt,
					new Object[] { groupCount }));
			view.setTag(cursor.getLong(INDEX_ID_COLUMN));
			view
					.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu,
								View v, ContextMenuInfo menuInfo) {
							if (groupCount <= 0) {
								return;
							}
							menu.setHeaderTitle(groupName);
							menu.add(0, MENU_ITEM_SHOW_CONTACTS, 1,
									R.string.group_menu_show_contacts);
						}
					});

			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (groupCount <= 0) {
						return;
					}
					tempStorage.groupName = groupName;
					showGroupItems();
				}
			});

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = inflater.inflate(R.layout.group_item, null);
			return v;
		}

	}

	private void showGroupItems() {
		Intent intent = new Intent(GroupsActivity.this,
				ContactsListActivity.class);
		intent.setAction(UI.LIST_GROUP_ACTION);
		intent.putExtra(UI.TITLE_EXTRA_KEY, tempStorage.groupName);
		intent.putExtra(UI.GROUP_NAME_EXTRA_KEY, tempStorage.groupName);
		intent.putExtra(Constants.EXTRA_SHOW_GROUP_ITEMS, true);
		startActivity(intent);
	}

	/**
	 * thread not safe
	 * 
	 * @author Tom
	 * 
	 */
	private class TempStorge {
		// public long groupId=0l;
		public String groupName = "";
	}

}
