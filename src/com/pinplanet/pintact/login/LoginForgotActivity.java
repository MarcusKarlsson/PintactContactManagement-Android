package com.pinplanet.pintact.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import org.json.JSONObject;

public class LoginForgotActivity extends MyActivity {

	private static final String TAG=LoginForgotActivity.class.getName();
	
	private EditText email;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.login_forgot);
			
			TextView btn = (TextView) findViewById(R.id.buttonSend);
			btn.setFocusableInTouchMode(true);
			btn.requestFocus();
			
			email=(EditText) findViewById(R.id.editTextEmail);
			
			showTitle(R.string.ab_forgot);

			hideBoth();

            showLeftImage(R.drawable.actionbar_left_arrow);
            addLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
		}
		
		public void postResetPassword(View v) {
			String str_email=email.getText().toString();
			
			if ( str_email.isEmpty()  ) {
				Log.d(TAG,"Field Empty!");

				myDialog(R.string.DIALOG_TITLE_FIELD_EMPTY, R.string.DIALOG_MESSAGE_FIELD_EMPTY_LOGIN_FORGOT);
				return;
			}

			String path ="/api/users/password/forgot.json";

			SingletonNetworkStatus.getInstance().clean();
			SingletonNetworkStatus.getInstance().setActivity(this);
			SingletonNetworkStatus.getInstance().setDoNotDismissDialog(true);

            String json=null;
            try {
               json=new JSONObject().put("email",str_email).toString();
            }catch (Exception e){}


			new HttpConnection().access(this, path,json, "POST");
		}
		
		public void onPostNetwork () {
			
			if (SingletonNetworkStatus.getInstance().getCode() != 200 ) 
			{
	    		SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);
	        	SingletonNetworkStatus.getInstance().getWaitDialog().dismiss();
	        	SingletonNetworkStatus.getInstance().setWaitDialog(null);
	    		
				myDialog(SingletonNetworkStatus.getInstance().getMsg(), 
						SingletonNetworkStatus.getInstance().getErrMsg());

			}
			else
			{
	    		SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);
	        	SingletonNetworkStatus.getInstance().getWaitDialog().dismiss();
	        	SingletonNetworkStatus.getInstance().setWaitDialog(null);
	        	
				findViewById(R.id.layoutItems).setVisibility(View.GONE);
				findViewById(R.id.textViewInfo).setVisibility(View.INVISIBLE);
				findViewById(R.id.textViewSuccess).setVisibility(View.VISIBLE);

			}
		}
		
		public void onDummy(View view) {
		}
		
	    @Override
	    public void onBackPressed() {
	    	System.out.println("OnBackPressed - Activity.");
	    	finish();
			overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
	    	return;
	    }
		
		
}
