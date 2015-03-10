package com.android.contacts;

//张国友修改过
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.model.Net139Source;
import com.android.contacts.util.Constants;
import com.android.internal.widget.ContactHeaderWidget;

public class ViewContactSecondaryActivity extends Activity implements
		OnClickListener {

	private final static String TAG = "ViewContactSecondaryActivity";

	private boolean isSmsMode = false;
	private Uri mLookupUri = null;
	private Cursor mCursor = null;
	private Cursor mHeadCursor;
	private String mNumber = ""; // the contact's mobile phone number
	private String phone = null;

	private final static String[] PROJECTION_SMS = new String[] { Sms.BODY,
			Sms.DATE, Sms.TYPE, Sms._ID };
	private final static String[] PROJECTION_CALLLOG = new String[] {
			Calls.NUMBER, Calls.DATE, Calls.TYPE, Calls.DURATION, Calls._ID };

	private final static int COLUMN_INDEX_MAIN_DATA = 0;
	private final static int COLUMN_INDEX_DATE = 1;
	private final static int COLUMN_INDEX_TYPE = 2;
	private final static int COLUMN_INDEX_DURATION = 3;
	
    protected ContactHeaderWidget mContactHeaderWidget;
    private TextView mContactType;
	

	private final static String WHERE_SMS = Sms.ADDRESS + " = ?";
	private final static String WHERE_CALLLOG = Calls.NUMBER + " = ?";

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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		mLookupUri = intent.getData();
		if (mLookupUri == null) {
			finish();
		}
		isSmsMode = intent.getBooleanExtra(Constants.EXTRA_KEY_SHOW_SMS, true);
		mNumber = intent.getStringExtra(Constants.EXTRA_KEY_MOBILE_PHONE);
		if (TextUtils.isEmpty(mNumber)) {
			finish();
		}
		setContentView(R.layout.contact_card_layout);
		
		 mContactHeaderWidget = (ContactHeaderWidget) findViewById(R.id.contact_header_widget);
	        //mContactHeaderWidget.showStar(true);
	        mContactHeaderWidget.setExcludeMimes(new String[] {
	            Contacts.CONTENT_ITEM_TYPE
	        });
	}

	private String[] getProjection() {
		return isSmsMode ? PROJECTION_SMS : PROJECTION_CALLLOG;
	}

	private Uri getUri() {
		return isSmsMode ? Sms.CONTENT_URI : Uri.withAppendedPath(
				CallLog.CONTENT_URI, "calls");
	}

	private String getWhere() {
		return isSmsMode ? WHERE_SMS : WHERE_CALLLOG;
	}

	private synchronized void initHeaderAndBottom() {
		closeHeadCursor();
		String name = "";
		String type = "";
		final long rawContactId = ContentUris.parseId(mLookupUri);
		mHeadCursor = getContentResolver().query(
				Constants.URI_CUSTOM_CONTACT_LIST, Constants.HEAD_PROJECTION,
				RawContacts._ID + " = " + rawContactId, null, null);

		if (mHeadCursor != null && mHeadCursor.moveToFirst()) {
			mHeadCursor.registerContentObserver(mObserver);
			startManagingCursor(mHeadCursor);
			name = mHeadCursor
					.getString(Constants.VIEW_CONTACT_HEADER_COLUMN_INDEX_NAME);
			type = mHeadCursor
					.getString(Constants.VIEW_CONTACT_HEADER_COLUMN_INDEX_TYPE);
			mNumber = mHeadCursor
					.getString(Constants.VIEW_CONTACT_HEADER_COLUMN_INDEX_NUMBER);
		} else {
			Toast.makeText(this, R.string.invalidContactMessage,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "invalid contact uri: " + mLookupUri);
			finish();
			return;
		}
		//((TextView) findViewById(R.id.tv_contacts_item_name)).setText(name);
		boolean isWebContacts = ContactsUtils.isWebContact(type);
		/*((ImageView) findViewById(R.id.iv_contacts_item_icon))
				.setImageResource(isWebContacts ? R.drawable.web_book
						: R.drawable.sim_book);*/
		mContactType = ((TextView) findViewById(R.id.tv_contacts_item_type));
		mContactType.setText(isWebContacts ? R.string.netContacts_title
						: R.string.simContacts_title);
		int x = this.getWindowManager().getDefaultDisplay().getWidth()-180;
		 AbsoluteLayout.LayoutParams view_para = new AbsoluteLayout.LayoutParams(180,70,x,32);                
		 mContactType.setLayoutParams(view_para);
		/*((TextView) findViewById(R.id.tv_contacts_item_type))
		.setText("");

		TextView tv_seconderyTitle = (TextView) findViewById(R.id.tv_secondery_title);
		tv_seconderyTitle.setText(isSmsMode ? R.string.recentMsgsIconLabel
				: R.string.recentCallsIconLabel);*/

		/*View v_detail = findViewById(R.id.ll_btn_show_book);
		v_detail.setBackgroundResource(0);
		v_detail.setOnClickListener(this);
		ImageView iv = (ImageView) findViewById(R.id.iv_book);
		iv.setImageResource(R.drawable.ic_book_unselected);
		View v_msg = findViewById(R.id.ll_btn_show_msg);*/
		/*View v_call = findViewById(R.id.ll_btn_show_call);*/
	/*	if (isSmsMode) {
			v_msg.setBackgroundResource(R.drawable.bottom_btn);
			v_call.setOnClickListener(this);
			v_call.setBackgroundResource(0);
			iv = (ImageView) v_msg.findViewById(R.id.iv_msg);
			iv.setImageResource(R.drawable.ic_msg);
			iv = (ImageView) v_call.findViewById(R.id.iv_call);
			iv.setImageResource(R.drawable.ic_call_unselected);
		} else {
			v_call.setBackgroundResource(R.drawable.bottom_btn);
			v_msg.setOnClickListener(this);
			v_msg.setBackgroundResource(0);
			iv = (ImageView) v_call.findViewById(R.id.iv_call);
			iv.setImageResource(R.drawable.ic_call);
			iv = (ImageView) v_msg.findViewById(R.id.iv_msg);
			iv.setImageResource(R.drawable.ic_msg_unselected);
		}*/
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContactHeaderWidget.bindFromContactLookupUri(mLookupUri);
		initHeaderAndBottom();
		closeCursor();
		Log.i(TAG, "============uri========>" + getUri());
		mCursor = getContentResolver().query(getUri(), getProjection(),
				getWhere(), new String[] { mNumber }, Calls.DATE + " desc "); // 设置按时间降序
		
		//phone=mCursor.getColumnName(COLUMN_INDEX_MAIN_DATA);  //联系人电话号码
		if (mCursor == null) {
			// TODO when the cursor is null;
		}
		mCursor.registerContentObserver(mObserver);
		startManagingCursor(mCursor);
		ListView mListView = (ListView) findViewById(R.id.contact_data);
		mListView.setAdapter(new MyCursor(this, mCursor));
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
		closeHeadCursor();
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeCursor();
		closeHeadCursor();
	}

	private void closeHeadCursor() {
		if (mHeadCursor != null) {
			mHeadCursor.unregisterContentObserver(mObserver);
			mHeadCursor.close();
			mHeadCursor = null;
		}
	}

	private class MyCursor extends CursorAdapter {
		LayoutInflater inflater = null;
		Context context = null;

		public MyCursor(Context context, Cursor c) {
			super(context, c);
			this.context = context;
			inflater = getLayoutInflater();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ImageView iv_icon = (ImageView) view
					.findViewById(R.id.iv_item_icon);
			TextView tv_mainData = (TextView) view
					.findViewById(R.id.tv_main_data);
			TextView tv_secondaryData = (TextView) view
					.findViewById(R.id.tv_secondery_data);
			TextView tv_date = (TextView) view.findViewById(R.id.tv_date);

			final String mMainData = cursor.getString(COLUMN_INDEX_MAIN_DATA);
			final long mDate = cursor.getLong(COLUMN_INDEX_DATE);
			final int mType = cursor.getInt(COLUMN_INDEX_TYPE);
			long mDuration = -1l; // sms
			if (cursor.getColumnCount() > 4) { // 修改--原因在sms模式下Cursor有4个元素，还是call模式下Cursor有5个元素
				mDuration = cursor.getLong(COLUMN_INDEX_DURATION);
			}

			setIcon(iv_icon, mType);
			tv_mainData.setText(mMainData);
			if (mDuration == -1l || mType == Calls.MISSED_TYPE) {
				tv_secondaryData.setVisibility(View.GONE);
			} else {
				tv_secondaryData.setText(ContactsUtils.formatDuration(context,
						mDuration));
				tv_secondaryData.setVisibility(View.VISIBLE);
			}

			/*CharSequence dateClause = DateUtils.formatDateRange(context, mDate,
					mDate, DateUtils.FORMAT_SHOW_TIME
							| DateUtils.FORMAT_SHOW_DATE
							| DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_YEAR);
			tv_date.setText(dateClause);*/
			long date = cursor.getLong(COLUMN_INDEX_DATE);

	        // Set the date/time field by mixing relative and absolute times.
	        int flags = DateUtils.FORMAT_ABBREV_RELATIVE;

	        tv_date.setText(DateUtils.getRelativeTimeSpanString(date,
	                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, flags));	
			
			if (isSmsMode) {
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					// //*****************查看短信详情*************
					/*** new一个Intent对象 ***/
					// Intent intent = new Intent();
					// intent.setClass(ViewContactSecondaryActivity.this,
					// SMSInformationActivity.class);
					// /***new一个Bundle对象，并将要传递的数据导入***/
					// Bundle bundle = new Bundle();
					// bundle.putString("body",mMainData);
					// bundle.putLong("date",mDate);
					// bundle.putInt("type",mType);
					// // bundle.putLong("duration",mDuration);
					// /***将Bundle对象bundle给Intent***/
					// intent.putExtras(bundle);
					// /***调用SMSInformationActivity***/
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
							.fromParts(Constants.SCHEME_SMSTO, mNumber, null)); // 短信详情

					startActivity(intent);
				}

			});
			}
			else{
				view.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						// //**************拨打联系人号码*************
						Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
						intent.setData(Uri.fromParts("tel", mNumber, null));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    startActivity(intent);
						
//						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
//								.fromParts(Constants.SCHEME_SMSTO, mNumber, null)); // 短信详情
//
//						startActivity(intent);
					}

				});
			}
		}

		private void setIcon(ImageView iv, int type) {
			if (isSmsMode) {
				iv.setImageResource(R.drawable.icon_msg);
			} else {
				switch (type) {
				case Calls.INCOMING_TYPE:
					iv
							.setImageResource(R.drawable.ic_call_log_list_incoming_call);
					break;
				case Calls.OUTGOING_TYPE:
					iv
							.setImageResource(R.drawable.ic_call_log_list_outgoing_call);
					break;
				case Calls.MISSED_TYPE:
					iv
							.setImageResource(R.drawable.ic_call_log_list_missed_call);
					break;
				}
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = inflater.inflate(R.layout.contact_secondery_item, null);
			return v;
		}

	}

	public void onClick(View v) {
		/*switch (v.getId()) {
		case R.id.ll_btn_show_book: {
			Intent intent = new Intent(this, ViewContactActivity.class);
			intent.setData(mLookupUri);
			startActivity(intent);
			break;
		}
		case R.id.ll_btn_show_call: {
			this.isSmsMode = false;
			onResume();
			break;
		}
		case R.id.ll_btn_show_msg: {
			this.isSmsMode = true;
			onResume();
			break;
		}
		}*/
	}

	

}
