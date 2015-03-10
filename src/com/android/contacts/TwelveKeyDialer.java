/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.contacts;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.Contacts.Intents.Insert;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.contacts.util.Constants;
import com.android.internal.telephony.ITelephony;

/**
 * Dialer activity that displays the typical twelve key interface.
 */
@SuppressWarnings( { "deprecation", "restriction" })
public class TwelveKeyDialer extends Activity implements View.OnClickListener,
		View.OnLongClickListener, View.OnKeyListener,
		AdapterView.OnItemClickListener, TextWatcher, OnEditorActionListener {
	private static final String EMPTY_NUMBER = "";
	private static final String TAG = "TwelveKeyDialer";

	/** The length of DTMF tones in milliseconds */
	private static final int TONE_LENGTH_MS = 150;

	/** The DTMF tone volume relative to other sounds in the stream */
	private static final int TONE_RELATIVE_VOLUME = 80;

	/**
	 * Stream type used to play the DTMF tones off call, and mapped to the
	 * volume control keys
	 */
	private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;
	private static final Uri URI_CUSTOM_CONTACT_LIST = Uri.withAppendedPath(
			ContactsContract.AUTHORITY_URI, "custom_contacts");
	private View mDelete;
	private MenuItem mAddToContactMenuItem;
	private ToneGenerator mToneGenerator;
	private Object mToneGeneratorLock = new Object();
	private Drawable mDigitsBackground;
	private Drawable mDigitsEmptyBackground;
	private View mDialpad;
	private View mVoicemailDialAndDeleteRow;
	private View keyVisibleButton;
	private View mDialButton;
	private ListView mDialpadChooser;
	private DialpadChooserAdapter mDialpadChooserAdapter;
	// Member variables for dialpad options
	private MenuItem m2SecPauseMenuItem;
	private MenuItem mWaitMenuItem;
	private static final int MENU_ADD_CONTACTS = 1;
	private static final int MENU_2S_PAUSE = 2;
	private static final int MENU_WAIT = 3;

	// *************************添加查询输入框******************************
	private SearchEditText mDigits;
	private String mInitialFilter = "";
	private TwelveListAdapter mAdapter;
	private InputMethodManager inputkey = null;
	// ***************************添加按钮*********************************
	private View indentation;
	private View mSearch_edit_frame;
	// ***************************添加ListView************************************
	private ListView list;
	private Cursor cur;
	private ContentObserver mObserver = new ContentObserver(new Handler()) {

		public boolean deliverSelfNotifications() {
			return true;
		}

		public void onChange(boolean selfChange) {
			if (cur != null && !cur.isClosed()) {
				onResume();
			}
		}
	};
	// ******************************cur查询列表属性参数*************************************
	static final String[] CUR_C = new String[] { "display_name", "data1",
			"raw_contacts._id as _id", "account_type", "lookup" };

	// Last number dialed, retrieved asynchronously from the call DB
	// in onCreate. This number is displayed when the user hits the
	// send key and cleared in onPause.
	// CallLogAsync mCallLog = new CallLogAsync();
	private String mLastNumberDialed = EMPTY_NUMBER;

	// determines if we want to playback local DTMF tones.
	private boolean mDTMFToneEnabled;

	// Vibration (haptic feedback) for dialer key presses.
	// private HapticFeedback mHaptic = new HapticFeedback();

	/** Identifier for the "Add Call" intent extra. */
	static final String ADD_CALL_MODE_KEY = "add_call_mode";

	/**
	 * Identifier for intent extra for sending an empty Flash message for CDMA
	 * networks. This message is used by the network to simulate a press/depress
	 * of the "hookswitch" of a landline phone. Aka "empty flash".
	 * 
	 * TODO: Using an intent extra to tell the phone to send this flash is a
	 * temporary measure. To be replaced with an ITelephony call in the future.
	 * TODO: Keep in sync with the string defined in
	 * OutgoingCallBroadcaster.java in Phone app until this is replaced with the
	 * ITelephony API.
	 */
	static final String EXTRA_SEND_EMPTY_FLASH = "com.android.phone.extra.SEND_EMPTY_FLASH";

	/**
	 * Indicates if we are opening this dialer to add a call from the
	 * InCallScreen.
	 */
	private boolean mIsAddCallMode;

	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		/**
		 * Listen for phone state changes so that we can take down the
		 * "dialpad chooser" if the phone becomes idle while the chooser UI is
		 * visible.
		 */

		public void onCallStateChanged(int state, String incomingNumber) {
			// Log.i(TAG, "PhoneStateListener.onCallStateChanged: "
			// + state + ", '" + incomingNumber + "'");
			if ((state == TelephonyManager.CALL_STATE_IDLE)
					&& dialpadChooserVisible()) {
				// Log.i(TAG,
				// "Call ended with dialpad chooser visible!  Taking it down...");
				// Note there's a race condition in the UI here: the
				// dialpad chooser could conceivably disappear (on its
				// own) at the exact moment the user was trying to select
				// one of the choices, which would be confusing. (But at
				// least that's better than leaving the dialpad chooser
				// onscreen, but useless...)
				showDialpadChooser(false);
			}
		}
	};

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// Do nothing
	}

	public void onTextChanged(CharSequence input, int start, int before,
			int changeCount) {
		// Do nothing
		// DTMF Tones do not need to be played here any longer -
		// the DTMF dialer handles that functionality now.
	}

	public void afterTextChanged(Editable input) { // **实时刷新**
		if (mAdapter == null) {
			return;
		}
		// 触发搜索事件并刷新ListView
		String selectStr = mDigits.getText().toString();
		cur = getContentResolver().query(
				Uri.withAppendedPath(Constants.URI_CUSTOM_CONTACT_SEARCH,
						TextUtils.isEmpty(selectStr) ? "*" : selectStr), CUR_C, // ***包含查询***
				null, null, Contacts.SORT_KEY_PRIMARY);
		cur.registerContentObserver(mObserver);
		mAdapter.setSelectedText(selectStr);
		mAdapter.changeCursor(cur);
		startManagingCursor(cur);
	}

	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeCursor();
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		closeCursor();
	}

	private void closeCursor() {
		if (cur != null) {
			cur.unregisterContentObserver(mObserver);
			cur.close();
			cur = null;
		}
	}

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Resources r = getResources();
		// Do not show title in the case the device is in carmode.
		if ((r.getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_CAR) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		// Set the content view
		setContentView(getContentViewResource());

		// Load up the resources for the text field.
		// *************************添加查询输入框******************************
		mDigits = (SearchEditText) findViewById(R.id.digits);
		mDigitsBackground = r.getDrawable(R.drawable.noboard_bg);
		mDigitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield);
		mDigits.addTextChangedListener(this);
		mDigits.setOnEditorActionListener(this);
		mDigits.setText(mInitialFilter);
		mDigits.setKeyListener(DialerKeyListener.getInstance());
		mDigits.setOnClickListener(this);
		mDigits.setOnKeyListener(this);
		mDigits.setVisibility(EditText.GONE);
		mDigits.setHint("请输入电话号码");

		// ****************************编辑框边的按钮********************************
		mSearch_edit_frame = findViewById(R.id.search_edit_frame);
		indentation = mSearch_edit_frame.findViewById(R.id.indentationButton);
		indentation.setOnClickListener(this);
		indentation.setOnLongClickListener(this);
		mDelete = indentation;
		// ***********************************************************************

		maybeAddNumberFormatting();

		// Check for the presence of the keypad
		View view = findViewById(R.id.one);
		if (view != null) {
			setupKeypad();
		}

		mVoicemailDialAndDeleteRow = findViewById(R.id.voicemailAndDialAndDelete);

		keyVisibleButton = mVoicemailDialAndDeleteRow
				.findViewById(R.id.keyVisibleButton);
		keyVisibleButton.setOnClickListener(this);

		// Check whether we should show the onscreen "Dial" button.
		mDialButton = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton);
		mDialButton.setOnClickListener(this);

		view = mVoicemailDialAndDeleteRow.findViewById(R.id.keyGoneButton);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);

		mDialpad = findViewById(R.id.dialpad); // This is null in landscape
												// mode.

		// In landscape we put the keyboard in phone mode.
		// In portrait we prevent the soft keyboard to show since the
		// dialpad acts as one already.
		if (null == mDialpad) {
			mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
		} else {
			mDigits.setInputType(android.text.InputType.TYPE_NULL);
		}

		// Set up the "dialpad chooser" UI; see showDialpadChooser().
		mDialpadChooser = (ListView) findViewById(R.id.dialpadChooser);
		mDialpadChooser.setOnItemClickListener(this);

		if (!resolveIntent() && icicle != null) {
			super.onRestoreInstanceState(icicle);
		}

		// ********建ListView*****
		list = (ListView) findViewById(R.id.key_dialer_phonelist);
		mAdapter = new TwelveListAdapter(this, null, true);
		list.setAdapter(mAdapter);

		inputkey = (InputMethodManager) mDigits.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE); // 软键盘
	}

	protected void onRestoreInstanceState(Bundle icicle) {
		// Do nothing, state is restored in onCreate() if needed
	}

	protected void maybeAddNumberFormatting() {
		mDigits.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
	}

	/**
	 * Overridden by subclasses to control the resource used by the content
	 * view.
	 */
	protected int getContentViewResource() {
		return R.layout.twelve_key_dialer;
	}

	private boolean resolveIntent() {
		boolean ignoreState = false;

		// Find the proper intent
		final Intent intent;
		if (isChild()) {
			intent = getParent().getIntent();
			ignoreState = intent.getBooleanExtra(
					DialtactsActivity.EXTRA_IGNORE_STATE, false);
		} else {
			intent = getIntent();
		}
		// Log.i(TAG, "==> resolveIntent(): intent: " + intent);

		// by default we are not adding a call.
		mIsAddCallMode = false;

		// By default we don't show the "dialpad chooser" UI.
		boolean needToShowDialpadChooser = false;

		// Resolve the intent
		final String action = intent.getAction();
		if (Intent.ACTION_DIAL.equals(action)
				|| Intent.ACTION_VIEW.equals(action)) {
			// see if we are "adding a call" from the InCallScreen; false by
			// default.
			mIsAddCallMode = intent.getBooleanExtra(ADD_CALL_MODE_KEY, false);
			Uri uri = intent.getData();
			if (uri != null) {
				if ("tel".equals(uri.getScheme())) {
					// Put the requested number into the input area
					String data = uri.getSchemeSpecificPart();
					setFormattedDigits(data);
				} else {
					String type = intent.getType();
					if (People.CONTENT_ITEM_TYPE.equals(type)
							|| Phones.CONTENT_ITEM_TYPE.equals(type)) {
						// Query the phone number
						Cursor c = getContentResolver().query(intent.getData(),
								new String[] { PhonesColumns.NUMBER }, null,
								null, null);
						if (c != null) {
							if (c.moveToFirst()) {
								// Put the number into the input area
								setFormattedDigits(c.getString(0));
							}
							c.close();
						}
					}
				}
			}
		} else if (Intent.ACTION_MAIN.equals(action)) {
			// The MAIN action means we're bringing up a blank dialer
			// (e.g. by selecting the Home shortcut, or tabbing over from
			// Contacts or Call log.)
			//
			// At this point, IF there's already an active call, there's a
			// good chance that the user got here accidentally (but really
			// wanted the in-call dialpad instead). So we bring up an
			// intermediate UI to make the user confirm what they really
			// want to do.
			if (phoneIsInUse()) {
				// Log.i(TAG,
				// "resolveIntent(): phone is in use; showing dialpad chooser!");
				needToShowDialpadChooser = true;
			}
		}

		// Bring up the "dialpad chooser" IFF we need to make the user
		// confirm which dialpad they really want.
		showDialpadChooser(needToShowDialpadChooser);

		return ignoreState;
	}

	protected void setFormattedDigits(String data) {
		// strip the non-dialable numbers out of the data string.
		String dialString = PhoneNumberUtils.extractNetworkPortion(data);
		dialString = PhoneNumberUtils.formatNumber(dialString);
		if (!TextUtils.isEmpty(dialString)) {
			Editable digits = mDigits.getText();
			digits.replace(0, digits.length(), dialString);
			// for some reason this isn't getting called in the digits.replace
			// call above..
			// but in any case, this will make sure the background drawable
			// looks right
			afterTextChanged(digits);
		}
	}

	protected void onNewIntent(Intent newIntent) {
		setIntent(newIntent);
		resolveIntent();
	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// This can't be done in onCreate(), since the auto-restoring of the
		// digits
		// will play DTMF tones for all the old digits if it is when
		// onRestoreSavedInstanceState()
		// is called. This method will be called every time the activity is
		// created, and
		// will always happen after onRestoreSavedInstanceState().
		mDigits.addTextChangedListener(this);
	}

	private void setupKeypad() {
		// Setup the listeners for the buttons
		View view = findViewById(R.id.one);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);

		findViewById(R.id.two).setOnClickListener(this);
		findViewById(R.id.three).setOnClickListener(this);
		findViewById(R.id.four).setOnClickListener(this);
		findViewById(R.id.five).setOnClickListener(this);
		findViewById(R.id.six).setOnClickListener(this);
		findViewById(R.id.seven).setOnClickListener(this);
		findViewById(R.id.eight).setOnClickListener(this);
		findViewById(R.id.nine).setOnClickListener(this);
		findViewById(R.id.star).setOnClickListener(this);

		view = findViewById(R.id.zero);
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);

		findViewById(R.id.pound).setOnClickListener(this);
	}

	protected void onResume() {
		super.onResume();

		// retrieve the DTMF tone play back setting.
		mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
				Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;

		// Retrieve the haptic feedback setting.
		// mHaptic.checkSystemSetting();

		// if the mToneGenerator creation fails, just continue without it. It is
		// a local audio signal, and is not as important as the dtmf tone
		// itself.
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					// we want the user to be able to control the volume of the
					// dial tones
					// outside of a call, so we use the stream type that is also
					// mapped to the
					// volume control keys for this activity
					mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE,
							TONE_RELATIVE_VOLUME);
					setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
				} catch (RuntimeException e) {
					Log.w(TAG,
							"Exception caught while creating local tone generator: "
									+ e);
					mToneGenerator = null;
				}
			}
		}

		Activity parent = getParent();
		// See if we were invoked with a DIAL intent. If we were, fill in the
		// appropriate
		// digits in the dialer field.
		if (parent != null && parent instanceof DialtactsActivity) {
			Uri dialUri = ((DialtactsActivity) parent).getAndClearDialUri();
			if (dialUri != null) {
				resolveIntent();
			}
		}

		// While we're in the foreground, listen for phone state changes,
		// purely so that we can take down the "dialpad chooser" if the
		// phone becomes idle while the chooser UI is visible.
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		// Potentially show hint text in the mDigits field when the user
		// hasn't typed any digits yet. (If there's already an active call,
		// this hint text will remind the user that he's about to add a new
		// call.)
		//
		// TODO: consider adding better UI for the case where *both* lines
		// are currently in use. (Right now we let the user try to add
		// another call, but that call is guaranteed to fail. Perhaps the
		// entire dialer UI should be disabled instead.)
		if (phoneIsInUse()) {
			mDigits.setHint(R.string.dialerDialpadHintText);
		} else {
			// Common case; no hint necessary.
			// mDigits.setHint(null); //为了显示“请输入电话号码”

			// Also, a sanity-check: the "dialpad chooser" UI should NEVER
			// be visible if the phone is idle!
			showDialpadChooser(false);
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			// Hide soft keyboard, if visible (it's fugly over button dialer).
			// The only known case where this will be true is when launching the
			// dialer with
			// ACTION_DIAL via a soft keyboard. we dismiss it here because we
			// don't
			// have a window token yet in onCreate / onNewIntent
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(
					mDigits.getWindowToken(), 0);
		}
	}

	protected void onPause() {
		super.onPause();

		// Stop listening for phone state changes.
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_NONE);

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
		// TODO: I wonder if we should not check if the AsyncTask that
		// lookup the last dialed number has completed.
		mLastNumberDialed = EMPTY_NUMBER; // Since we are going to query again,
											// free stale number.
	}

	/******* 创建menu *******/
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.twelvekey, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();
		switch (item_id) {
		case R.id.book:
			Intent bintent = new Intent();
			bintent.setClass(TwelveKeyDialer.this, ContactsListActivity.class);
			startActivity(bintent);
			TwelveKeyDialer.this.finish();
			break;
		case R.id.call:
			Intent cintent = new Intent();
			cintent.setClass(TwelveKeyDialer.this,
					RecentCallsListActivity.class);
			startActivity(cintent);
			TwelveKeyDialer.this.finish();
			break;
		}
		return true;

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL: {
			long callPressDiff = SystemClock.uptimeMillis()
					- event.getDownTime();
			if (callPressDiff >= ViewConfiguration.getLongPressTimeout()) {
				// Launch voice dialer
				Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
			}
			return true;
		}
		case KeyEvent.KEYCODE_1: {
			long timeDiff = SystemClock.uptimeMillis() - event.getDownTime();
			if (timeDiff >= ViewConfiguration.getLongPressTimeout()) {
				// Long press detected, call voice mail
				callVoicemail();
			}
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL: {
			// TODO: In dialButtonPressed we do some of these
			// tests again. We should try to consolidate them in
			// one place.
			if (!phoneIsCdma() && mIsAddCallMode && isDigitsEmpty()) {
				// For CDMA phones, we always call
				// dialButtonPressed() because we may need to send
				// an empty flash command to the network.
				// Otherwise, if we are adding a call from the
				// InCallScreen and the phone number entered is
				// empty, we just close the dialer to expose the
				// InCallScreen under it.
				finish();
			}

			// If we're CDMA, regardless of where we are adding a call from
			// (either
			// InCallScreen or Dialtacts), the user may need to send an empty
			// flash command to the network. So let's call dialButtonPressed()
			// regardless
			// and dialButtonPressed will handle this functionality for us.
			// otherwise, we place the call.
			dialButtonPressed();
			return true;
		}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void keyPressed(int keyCode) {
		// mHaptic.vibrate();
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		mDigits.onKeyDown(keyCode, event);
	}

	public boolean onKey(View view, int keyCode, KeyEvent event) {
		switch (view.getId()) {
		case R.id.digits:
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				dialButtonPressed();
				return true;
			}
			break;
		}
		return false;
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.one: {
			playTone(ToneGenerator.TONE_DTMF_1);
			keyPressed(KeyEvent.KEYCODE_1);
			return;
		}
		case R.id.two: {
			playTone(ToneGenerator.TONE_DTMF_2);
			keyPressed(KeyEvent.KEYCODE_2);
			return;
		}
		case R.id.three: {
			playTone(ToneGenerator.TONE_DTMF_3);
			keyPressed(KeyEvent.KEYCODE_3);
			return;
		}
		case R.id.four: {
			playTone(ToneGenerator.TONE_DTMF_4);
			keyPressed(KeyEvent.KEYCODE_4);
			return;
		}
		case R.id.five: {
			playTone(ToneGenerator.TONE_DTMF_5);
			keyPressed(KeyEvent.KEYCODE_5);
			return;
		}
		case R.id.six: {
			playTone(ToneGenerator.TONE_DTMF_6);
			keyPressed(KeyEvent.KEYCODE_6);
			return;
		}
		case R.id.seven: {
			playTone(ToneGenerator.TONE_DTMF_7);
			keyPressed(KeyEvent.KEYCODE_7);
			return;
		}
		case R.id.eight: {
			playTone(ToneGenerator.TONE_DTMF_8);
			keyPressed(KeyEvent.KEYCODE_8);
			return;
		}
		case R.id.nine: {
			playTone(ToneGenerator.TONE_DTMF_9);
			keyPressed(KeyEvent.KEYCODE_9);
			return;
		}
		case R.id.zero: {
			playTone(ToneGenerator.TONE_DTMF_0);
			keyPressed(KeyEvent.KEYCODE_0);
			return;
		}
		case R.id.pound: {
			playTone(ToneGenerator.TONE_DTMF_P);
			keyPressed(KeyEvent.KEYCODE_POUND);
			return;
		}
		case R.id.star: {
			playTone(ToneGenerator.TONE_DTMF_S);
			keyPressed(KeyEvent.KEYCODE_STAR);
			return;
		}

		case R.id.keyGoneButton: {
			Log.i(TAG, "=============DELETEBUTTON============");
			findViewById(R.id.dialpad).setVisibility(View.GONE);
			mDigits.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);

			inputkey.showSoftInput(mDigits, InputMethodManager.SHOW_IMPLICIT);
			// inputkey.restartInput(mDigits);
			mDigits.getText().clear();
			mDigits.setHint("请输入联系人");
			return;
			// 调用输入法弹出、收缩方法，设置的两个值为显示时的flag和隐藏时的flag
		}
			// **********增加编辑框边的按钮事件处理**********
		case R.id.indentationButton: {
			keyPressed(KeyEvent.KEYCODE_DEL);
			return;
		}
		case R.id.dialButton: {

			dialButtonPressed();
			return;
		}
		case R.id.keyVisibleButton: {
			// findViewById(R.id.dialpad).setVisibility(View.INVISIBLE);
			findViewById(R.id.dialpad).setVisibility(View.VISIBLE);
			// mHaptic.vibrate();
			// inputkey.hideSoftInputFromWindow(mDigits,
			// InputMethodManager.HIDE_NOT_ALWAYS); // 隐藏软键盘
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(TwelveKeyDialer.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			mDigits.getText().clear();
			mDigits.setHint("请输入电话号码");
			return;
		}
		case R.id.digits: {
			if (!isDigitsEmpty()) {
				mDigits.setCursorVisible(true);
			}
			return;
		}
		}
	}

	public boolean onLongClick(View view) {
		final Editable digits = mDigits.getText();
		int id = view.getId();
		switch (id) {
		case R.id.indentationButton: {
			digits.clear();
			// TODO: The framework forgets to clear the pressed
			// status of disabled button. Until this is fixed,
			// clear manually the pressed status. b/2133127
			mDelete.setPressed(false);
			return true;
		}
		case R.id.one: {
			if (isDigitsEmpty()) {
				callVoicemail();
				return true;
			}
			return false;
		}
		case R.id.zero: {
			keyPressed(KeyEvent.KEYCODE_PLUS);
			return true;
		}
		}
		return false;
	}

	void callVoicemail() {
		Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri
				.fromParts("voicemail", EMPTY_NUMBER, null));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mDigits.getText().clear();
		finish();
	}

	void dialButtonPressed() {
		String number = mDigits.getText().toString();
		int tlength = number.length();
		char t[] = new char[tlength];
		number.getChars(0, tlength, t, 0);
		if (TextUtils.isEmpty(mDigits.getText().toString())) {
			return;
		}
		for (int i = 0; i < tlength; i++) {
			if (!(t[i] == '0' || t[i] == '1' || t[i] == '2' || t[i] == '3'
					|| t[i] == '4' || t[i] == '5' || t[i] == '6' || t[i] == '7'
					|| t[i] == '8' || t[i] == '9' || t[i] == '#' || t[i] == '*' || t[i] == '-')) {
				View view = (View) list.getItemAtPosition(1);
				if (view != null) {
					view.performClick();
				}
				break;
			}
		}

		Intent dialintent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
		dialintent.setData(Uri.fromParts("tel", number, null));
		dialintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(dialintent);
	}

	/**
	 * Plays the specified tone for TONE_LENGTH_MS milliseconds.
	 * 
	 * The tone is played locally, using the audio stream for phone calls. Tones
	 * are played only if the "Audible touch tones" user preference is checked,
	 * and are NOT played if the device is in silent mode.
	 * 
	 * @param tone
	 *            a tone code from {@link ToneGenerator}
	 */
	void playTone(int tone) {
		// if local tone playback is disabled, just return.
		if (!mDTMFToneEnabled) {
			return;
		}

		// Also do nothing if the phone is in silent mode.
		// We need to re-check the ringer mode for *every* playTone()
		// call, rather than keeping a local flag that's updated in
		// onResume(), since it's possible to toggle silent mode without
		// leaving the current activity (via the ENDCALL-longpress menu.)
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
				return;
			}

			// Start the new tone (will stop any playing tone)
			mToneGenerator.startTone(tone, TONE_LENGTH_MS);
		}
	}

	/**
	 * Brings up the "dialpad chooser" UI in place of the usual Dialer elements
	 * (the textfield/button and the dialpad underneath).
	 * 
	 * We show this UI if the user brings up the Dialer while a call is already
	 * in progress, since there's a good chance we got here accidentally (and
	 * the user really wanted the in-call dialpad instead). So in this situation
	 * we display an intermediate UI that lets the user explicitly choose
	 * between the in-call dialpad ("Use touch tone
	 * keypad") and the regular Dialer ("Add call").  (Or, the option "Return to
	 * call in progress" just goes back to the in-call UI with no dialpad at
	 * all.)
	 * 
	 * @param enabled
	 *            If true, show the "dialpad chooser" instead of the regular
	 *            Dialer UI
	 */
	private void showDialpadChooser(boolean enabled) {
		if (enabled) {
			// Log.i(TAG, "Showing dialpad chooser!");
			mDigits.setVisibility(View.GONE);
			if (mDialpad != null)
				mDialpad.setVisibility(View.GONE);
			mVoicemailDialAndDeleteRow.setVisibility(View.GONE);
			mDialpadChooser.setVisibility(View.VISIBLE);

			// Instantiate the DialpadChooserAdapter and hook it up to the
			// ListView. We do this only once.
			if (mDialpadChooserAdapter == null) {
				mDialpadChooserAdapter = new DialpadChooserAdapter(this);
				mDialpadChooser.setAdapter(mDialpadChooserAdapter);
			}
		} else {
			// Log.i(TAG, "Displaying normal Dialer UI.");
			mDigits.setVisibility(View.VISIBLE);
			if (mDialpad != null)
				mDialpad.setVisibility(View.VISIBLE);
			mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
			mDialpadChooser.setVisibility(View.GONE);
		}
	}

	/**
	 * @return true if we're currently showing the "dialpad chooser" UI.
	 */
	private boolean dialpadChooserVisible() {
		return mDialpadChooser.getVisibility() == View.VISIBLE;
	}

	/**
	 * Simple list adapter, binding to an icon + text label for each item in the
	 * "dialpad chooser" list.
	 */
	private static class DialpadChooserAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		// Simple struct for a single "choice" item.
		static class ChoiceItem {
			String text;
			Bitmap icon;
			int id;

			public ChoiceItem(String s, Bitmap b, int i) {
				text = s;
				icon = b;
				id = i;
			}
		}

		// IDs for the possible "choices":
		static final int DIALPAD_CHOICE_USE_DTMF_DIALPAD = 101;
		static final int DIALPAD_CHOICE_RETURN_TO_CALL = 102;
		static final int DIALPAD_CHOICE_ADD_NEW_CALL = 103;

		private static final int NUM_ITEMS = 3;
		private ChoiceItem mChoiceItems[] = new ChoiceItem[NUM_ITEMS];

		public DialpadChooserAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);

			// Initialize the possible choices.
			// TODO: could this be specified entirely in XML?

			// - "Use touch tone keypad"
			mChoiceItems[0] = new ChoiceItem(context
					.getString(R.string.dialer_useDtmfDialpad), BitmapFactory
					.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_tt_keypad),
					DIALPAD_CHOICE_USE_DTMF_DIALPAD);

			// - "Return to call in progress"
			mChoiceItems[1] = new ChoiceItem(context
					.getString(R.string.dialer_returnToInCallScreen),
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_current_call),
					DIALPAD_CHOICE_RETURN_TO_CALL);

			// - "Add call"
			mChoiceItems[2] = new ChoiceItem(context
					.getString(R.string.dialer_addAnotherCall), BitmapFactory
					.decodeResource(context.getResources(),
							R.drawable.ic_dialer_fork_add_call),
					DIALPAD_CHOICE_ADD_NEW_CALL);
		}

		public int getCount() {
			return NUM_ITEMS;
		}

		/**
		 * Return the ChoiceItem for a given position.
		 */
		public Object getItem(int position) {
			return mChoiceItems[position];
		}

		/**
		 * Return a unique ID for each possible choice.
		 */
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a view for each row.
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			// When convertView is non-null, we can reuse it (there's no need
			// to reinflate it.)
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.dialpad_chooser_list_item, null);
			}

			TextView text = (TextView) convertView.findViewById(R.id.text);
			text.setText(mChoiceItems[position].text);

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			icon.setImageBitmap(mChoiceItems[position].icon);

			return convertView;
		}
	}

	/**
	 * Handle clicks from the dialpad chooser.
	 */
	public void onItemClick(AdapterView parent, View v, int position, long id) {
		DialpadChooserAdapter.ChoiceItem item = (DialpadChooserAdapter.ChoiceItem) parent
				.getItemAtPosition(position);
		int itemId = item.id;
		switch (itemId) {
		case DialpadChooserAdapter.DIALPAD_CHOICE_USE_DTMF_DIALPAD:
			// Log.i(TAG, "DIALPAD_CHOICE_USE_DTMF_DIALPAD");
			// Fire off an intent to go back to the in-call UI
			// with the dialpad visible.
			returnToInCallScreen(true);
			break;

		case DialpadChooserAdapter.DIALPAD_CHOICE_RETURN_TO_CALL:
			// Log.i(TAG, "DIALPAD_CHOICE_RETURN_TO_CALL");
			// Fire off an intent to go back to the in-call UI
			// (with the dialpad hidden).
			returnToInCallScreen(false);
			break;

		case DialpadChooserAdapter.DIALPAD_CHOICE_ADD_NEW_CALL:
			// Log.i(TAG, "DIALPAD_CHOICE_ADD_NEW_CALL");
			// Ok, guess the user really did want to be here (in the
			// regular Dialer) after all. Bring back the normal Dialer UI.
			showDialpadChooser(false);
			break;

		default:
			Log.w(TAG, "onItemClick: unexpected itemId: " + itemId);
			break;
		}
	}

	/**
	 * Returns to the in-call UI (where there's presumably a call in progress)
	 * in response to the user selecting "use touch tone keypad" or
	 * "return to call" from the dialpad chooser.
	 */
	private void returnToInCallScreen(boolean showDialpad) {
		try {
			ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
					.checkService("phone"));
			if (phone != null)
				phone.showCallScreenWithDialpad(showDialpad);
		} catch (RemoteException e) {
			Log.w(TAG, "phone.showCallScreenWithDialpad() failed", e);
		}

		// Finally, finish() ourselves so that we don't stay on the
		// activity stack.
		// Note that we do this whether or not the showCallScreenWithDialpad()
		// call above had any effect or not! (That call is a no-op if the
		// phone is idle, which can happen if the current call ends while
		// the dialpad chooser is up. In this case we can't show the
		// InCallScreen, and there's no point staying here in the Dialer,
		// so we just take the user back where he came from...)
		finish();
	}

	/**
	 * @return true if the phone is "in use", meaning that at least one line is
	 *         active (ie. off hook or ringing or dialing).
	 */
	private boolean phoneIsInUse() {
		boolean phoneInUse = false;
		try {
			ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
					.checkService("phone"));
			if (phone != null)
				phoneInUse = !phone.isIdle();
		} catch (RemoteException e) {
			Log.w(TAG, "phone.isIdle() failed", e);
		}
		return phoneInUse;
	}

	/**
	 * @return true if the phone is a CDMA phone type
	 */
	private boolean phoneIsCdma() {
		boolean isCdma = false;
		try {
			ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
					.checkService("phone"));
			if (phone != null) {
				isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
			}
		} catch (RemoteException e) {
			Log.w(TAG, "phone.getActivePhoneType() failed", e);
		}
		return isCdma;
	}

	/**
	 * @return true if the phone state is OFFHOOK
	 */
	private boolean phoneIsOffhook() {
		boolean phoneOffhook = false;
		try {
			ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
					.checkService("phone"));
			if (phone != null)
				phoneOffhook = phone.isOffhook();
		} catch (RemoteException e) {
			Log.w(TAG, "phone.isOffhook() failed", e);
		}
		return phoneOffhook;
	}

	/**
	 * Returns true whenever any one of the options from the menu is selected.
	 * Code changes to support dialpad options
	 */
	private void updateDialString(String newDigits) {
		int selectionStart;
		int selectionEnd;

		// SpannableStringBuilder editable_text = new
		// SpannableStringBuilder(mDigits.getText());
		int anchor = mDigits.getSelectionStart();
		int point = mDigits.getSelectionEnd();

		selectionStart = Math.min(anchor, point);
		selectionEnd = Math.max(anchor, point);

		Editable digits = mDigits.getText();
		if (selectionStart != -1) {
			if (selectionStart == selectionEnd) {
				// then there is no selection. So insert the pause at this
				// position and update the mDigits.
				digits.replace(selectionStart, selectionStart, newDigits);
			} else {
				digits.replace(selectionStart, selectionEnd, newDigits);
				// Unselect: back to a regular cursor, just pass the character
				// inserted.
				mDigits.setSelection(selectionStart + 1);
			}
		} else {
			int len = mDigits.length();
			digits.replace(len, len, newDigits);
		}
	}

	/**
	 * This function return true if Wait menu item can be shown otherwise
	 * returns false. Assumes the passed string is non-empty and the 0th index
	 * check is not required.
	 */
	private boolean showWait(int start, int end, String digits) {
		if (start == end) {
			// visible false in this case
			if (start > digits.length())
				return false;

			// preceding char is ';', so visible should be false
			if (digits.charAt(start - 1) == ';')
				return false;

			// next char is ';', so visible should be false
			if ((digits.length() > start) && (digits.charAt(start) == ';'))
				return false;
		} else {
			// visible false in this case
			if (start > digits.length() || end > digits.length())
				return false;

			// In this case we need to just check for ';' preceding to start
			// or next to end
			if (digits.charAt(start - 1) == ';')
				return false;
		}
		return true;
	}

	/**
	 * @return true if the widget with the phone number digits is empty.
	 */
	private boolean isDigitsEmpty() {
		return mDigits.length() == 0;
	}

	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		if (globalSearch) {
			super.startSearch(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		} else {
			ContactsSearchManager.startSearch(this, initialQuery);
		}
	}

	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	private class TwelveListAdapter extends CursorAdapter {
		LayoutInflater inflater = null;
		String keyStr = null; // 编辑框内容长度
		int keyStrstsrt = 0, keyStrend = 0;
		private static final int INDEX_NAME = 0;
		private static final int INDEX_PHONE = 1;
		private static final int INDEX_ID = 2;
		private static final int INDEX_TYPE = 3;
		private static final int INDEX_LOOKUP = 4;

		public TwelveListAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			inflater = getLayoutInflater();
		}

		public void setSelectedText(String keyStr) {
			this.keyStr = keyStr;
		}

		public void bindView(View view, Context context, Cursor cursor) {
			// final long groupId=cursor.getLong(INDEX_ID_COLUMN);
			final String name = cursor.getString(INDEX_NAME);
			final String phone = cursor.getString(INDEX_PHONE);
			final String type = cursor.getString(INDEX_TYPE);
			final long contactId = cur.getLong(INDEX_ID);
			final String lookupKey = cur.getString(INDEX_LOOKUP);

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
				tv_phone.setText(pstyle);

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
			ImageButton card = (ImageButton) view.findViewById(R.id.card_icon);
			card.setOnClickListener(new Button.OnClickListener() {
				// ***************添加名信卡的触发事件处理*******************

				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setClass(TwelveKeyDialer.this,
							ViewContactActivity.class);

					intent.setData(uri);// ********
					startActivity(intent);
					TwelveKeyDialer.this.finish();
				}
			});

			// **********************判断号码是com.139的还是SIM的*************************
			ImageView web_icon = (ImageView) view.findViewById(R.id.web_icon);
			if (ContactsUtils.isWebContact(type))
			// if(type.equals("com.139"))
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
					mDigits.getText().clear(); // 清除查询框
					finish();
				}
			});
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = inflater.inflate(R.layout.recent_calls_list_item_twelve,
					null);
			return v;
		}
	}

}
