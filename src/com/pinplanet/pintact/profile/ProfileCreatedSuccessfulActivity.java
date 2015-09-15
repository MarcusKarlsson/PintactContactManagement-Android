package com.pinplanet.pintact.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.ContactInviteActivity;
import com.pinplanet.pintact.setting.SearchSettingActivity;
import com.pinplanet.pintact.utility.MyActivity;

public class ProfileCreatedSuccessfulActivity extends MyActivity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.profile_create_success);
			
            hideBoth();

            showTitle(R.string.ab_congratulations);

		}

          public void updateSearchProfile(View view) {
            Intent it = new Intent(this, SearchSettingActivity.class);
            it.putExtra(SearchSettingActivity.IS_REGISTRATION_FLOW, true);
            startActivity(it);
          }
		
		public void onInvite(View view) {
			Intent it = new Intent(this, ContactInviteActivity.class);
			startActivity(it);
		}
		
		
}
