package com.pinplanet.pintact.setting;

import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingMainActivity extends MyActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		//////////////////////////////////////////
		/// NOT USED ANYMORE //////////////////// 
		//////////////////////////////////////////
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		hideRight();
		
		loadPreferences();

		TextView tv = (TextView)findViewById(R.id.actionBar);
		tv.setText(getResources().getString(R.string.ab_setting));

		findViewById(R.id.actionBarRightText).setVisibility(View.INVISIBLE);
		
		TextView logout = (TextView) findViewById(R.id.set_logout);
		logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("Logging out...");
				logout();
			}
		});
		
		// set default value for some settings;
		Switch stLocal = (Switch) findViewById(R.id.set_broadcast_switch);
		stLocal.setChecked(SingletonLoginData.getInstance().getUserSettings().local == 1);
		
		stLocal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	updatePreferencesLocal( isChecked ? 1 : 0);
			}
		});
		
		System.out.println("Finished setting....");
		
	}

	public void logout() {
		String path = "/api/users/logout.json?" + SingletonLoginData.getInstance().getPostParam();
		SingletonNetworkStatus.getInstance().setActivity(this);
		new HttpConnection().access(this, path, "", "POST");
	}

	public void onPostNetwork() {
		// clear data
		SingletonLoginData.getInstance().clean();

    saveLoginData();
		// return to login page
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

  public void saveLoginData() {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.remove(getString(R.string.login_username));
    editor.remove(getString(R.string.access_token));
    editor.commit();

    AppService.reInit();

  }
	
	public void onDummy(View view) {
	}


}
