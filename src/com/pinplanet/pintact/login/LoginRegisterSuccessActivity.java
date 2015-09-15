package com.pinplanet.pintact.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

public class LoginRegisterSuccessActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register_success);
        hideBoth();

        showTitle(R.string.ab_register_success);
      hideLeft();
      hideRight();
			TextView pin = (TextView) findViewById(R.id.textViewPin);
			pin.setText(SingletonLoginData.getInstance().getUserData().pin);
			
      AppService.getProfilesAsync();
      postGetRegistrationID();
		}


		public void uploadAccountImage(View view) {

            Intent it = new Intent(this,MainActivity.class);
            startActivity(it);
			overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
            finish();
		}

    @Override
    public void onBackPressed() {

        //super.onBackPressed();
    }
}
