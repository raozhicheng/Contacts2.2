package com.android.contacts.ui;
//张国友修改过
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.mid.service.MIDServiceManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.contacts.R;
import com.android.contacts.aspire.datasync.icontact139.IContact139AccountManager;
import com.android.contacts.aspire.datasync.icontact139.MidPhysicsAccountManager;
import com.android.contacts.util.Constants;

public class CustomContactsPrefsActivity extends Activity implements
		OnClickListener {

	private SharedPreferences filter = null;

	private CheckBox cb1, cb2, cb3;
	
	private TextView tv;
	
	private static MIDServiceManager midMgr = null;


	private Button okButton; // 新建完成键

	private boolean checked1, checked2, checked3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // hide title bar
		setContentView(R.layout.see_preference);
		filter = getSharedPreferences(Constants.FILTER_SEE_CONTACTS,
				MODE_WORLD_WRITEABLE);

		intiBtns();
		/***************设置网络联系人提示*************/
		try {
			// MIDServiceManager a = null;
			midMgr = MIDServiceManager.getInstance(getApplicationContext());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String account_name = MidPhysicsAccountManager.get139AccountFromMid(midMgr).accountName;
		tv = (TextView)findViewById(R.id.see_only_net_tv);
		tv.setText("显示业务通行证号码("+account_name+")的网络联系人");

		/**************** 取原有设置值 ***************/
		checked1 = filter.getBoolean(Constants.FILTER_SEE_ONLY_PHONE, false);
		checked2 = filter.getBoolean(Constants.FILTER_SEE_ONLY_SIM, true);
		checked3 = filter.getBoolean(Constants.FILTER_SEE_ONLY_NET, true);

		/**************** 设置多选框 ******************/
		cb1 = (CheckBox) findViewById(R.id.key_see_only_phone);
		cb1.setChecked(checked1);
		// cb1.setChecked(filter.getBoolean(Constants.FILTER_SEE_ONLY_PHONE,true));
		cb1.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!(cb1.isChecked() == checked1
						&& cb2.isChecked() == checked2 && cb3.isChecked() == checked3)) {

					okButton.setEnabled(true); // 将完成键解除禁用
					okButton.setTextColor(Color.rgb(255, 255, 255));

				} else {

					okButton.setTextColor(Color.rgb(150, 150, 150));
					okButton.setEnabled(false); // 将完成键禁用掉
				}
			}
		});
		cb2 = (CheckBox) findViewById(R.id.key_see_only_sim);
		cb2.setChecked(checked2);
		// cb2.setChecked(filter.getBoolean(Constants.FILTER_SEE_ONLY_SIM,true));
		cb2.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!(cb1.isChecked() == checked1
						&& cb2.isChecked() == checked2 && cb3.isChecked() == checked3)) {

					okButton.setEnabled(true); // 将完成键解除禁用
					okButton.setTextColor(Color.rgb(255, 255, 255));

				} else {

					okButton.setTextColor(Color.rgb(150, 150, 150));
					okButton.setEnabled(false); // 将完成键禁用掉
				}
			}
		});
		cb3 = (CheckBox) findViewById(R.id.key_see_only_net);
		cb3.setChecked(checked3);
		// cb3.setChecked(filter.getBoolean(Constants.FILTER_SEE_ONLY_NET,
		// true));
		cb3.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!(cb1.isChecked() == checked1
						&& cb2.isChecked() == checked2 && cb3.isChecked() == checked3)) {

					okButton.setEnabled(true); // 将完成键解除禁用
					okButton.setTextColor(Color.rgb(255, 255, 255));
					// okButton.setTextColor(R.color.textColorIconOverlay);
				} else {

					okButton.setTextColor(Color.rgb(150, 150, 150));
					okButton.setEnabled(false); // 将完成键禁用掉
					// okButton.setTextColor(R.color.textColorIconOverlayShadow);
				}
			}
		});

		// if (cb1 != null) {
		// editor.putBoolean(Constants.FILTER_SEE_ONLY_PHONE,
		// cb1.isChecked());
		// okButton.setEnabled(true); //将完成键解除禁用
		// }else{
		// okButton.setEnabled(false); //将完成键禁用掉
		// }
		// if (cb2 != null) {
		// editor.putBoolean(Constants.FILTER_SEE_ONLY_SIM, cb2
		// .isChecked());
		// okButton.setEnabled(true); //将完成键解除禁用
		// }else{
		// okButton.setEnabled(false); //将完成键禁用掉
		// }
		// if (cb3 != null) {
		// editor.putBoolean(Constants.FILTER_SEE_ONLY_NET, cb3
		// .isChecked());
		// okButton.setEnabled(true); //将完成键解除禁用
		// }else{
		// okButton.setEnabled(false); //将完成键禁用掉
		// }
	}

	private void intiBtns() {
		okButton = (Button) findViewById(R.id.btn_setting_done);
		// findViewById(R.id.btn_setting_done).setOnClickListener(this);
		okButton.setOnClickListener(this);
		okButton.setTextColor(Color.rgb(150, 150, 150));
		okButton.setEnabled(false); // 将完成键禁用掉
		// okButton.setTextColor(R.color.textColorIconOverlayShadow);
		findViewById(R.id.btn_setting_doNotSave).setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v == null) {
			return;
		}
		int id = v.getId();

		switch (id) {
		case R.id.btn_setting_done:
			Editor editor = filter.edit();
			if (cb1 != null) {
				editor.putBoolean(Constants.FILTER_SEE_ONLY_PHONE, cb1
						.isChecked());
			}
			if (cb2 != null) {
				editor.putBoolean(Constants.FILTER_SEE_ONLY_SIM, cb2
						.isChecked());
			}
			if (cb3 != null) {
				editor.putBoolean(Constants.FILTER_SEE_ONLY_NET, cb3
						.isChecked());
			}
			editor.commit();
			setResult(RESULT_OK);
			this.finish();
			return;
		case R.id.btn_setting_doNotSave:
			setResult(RESULT_CANCELED);
			this.finish();
			return;
		}

	}

}
