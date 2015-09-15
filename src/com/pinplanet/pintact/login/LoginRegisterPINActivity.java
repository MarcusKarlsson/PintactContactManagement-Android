package com.pinplanet.pintact.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.DataLoginData;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.util.Map;
import java.util.regex.Pattern;

public class LoginRegisterPINActivity extends MyActivity {

        private static final String TAG = LoginRegisterPINActivity.class.getName();
        private static final int CMD_SUGGEST_PIN=0,CMD_CHECK_PIN=1,CMD_SIGN_IN=2;

        private enum ICON {ICON_EMPTY,ICON_SUCCESS,ICON_FAIL};

		private EditText editTextEnterPin;
        private TextView textViewSuggest,textViewJoin;

		private int sendCmd = -1;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.login_register_pin);

            hideRight();

            showTitle(R.string.ab_pin);

            showLeftImage(R.drawable.actionbar_left_arrow);
            addLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            textViewSuggest=(TextView)findViewById(R.id.buttonSuggest);
            textViewJoin=(TextView)findViewById(R.id.buttonJoin);

            activateSuggest(true);
            activateJoin(false);

			String fn = SingletonLoginData.getInstance().getSignupRequest().getFirstName().substring(0,1).toLowerCase();
			String ln = SingletonLoginData.getInstance().getSignupRequest().getLastName().substring(0,1).toLowerCase();
			String initStr;
			if ( SingletonLoginData.getInstance().getSignupRequest().getMiddleName().length() > 0 ) {
				String mn = SingletonLoginData.getInstance().getSignupRequest().getMiddleName().substring(0,1).toLowerCase();
				initStr = fn+mn+ln;
			} else
				initStr = fn + ln;

            final String name = initStr;
            StringBuilder sb=new StringBuilder(initStr);
            while(sb.length()<7)
            {sb.append('#');}

			editTextEnterPin = (EditText)findViewById(R.id.editTextEnterPin);
			editTextEnterPin.setHint(sb.toString());


            editTextEnterPin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3){}

                @Override
                public void afterTextChanged(Editable editable) {

                    Log.d(TAG,"afterTextChanged sendCmd:"+sendCmd);
                    String text=editTextEnterPin.getText().toString();

                    if(text.length()==7)
                    {
                        if(check.matcher(text).matches())
                        {
                            if(sendCmd!=CMD_SUGGEST_PIN)
                            checkPin(text);
                        }
                        else
                        {
                            setPinIcon(ICON.ICON_FAIL);
                            activateSuggest(true);
                        }
                    }
                    else
                    {
                        setPinIcon(ICON.ICON_EMPTY);
                        //TODO: for now we activate it always
                        //activateSuggest(false);
                        activateJoin(false);
                        if(text.length() >= name.length())
                            editTextEnterPin.setInputType(InputType.TYPE_CLASS_NUMBER);
                        else
                            editTextEnterPin.setInputType(InputType.TYPE_CLASS_TEXT);
                    }

                }
            });
		}


        public void activateSuggest(boolean activate)
        {
            textViewSuggest.setEnabled(activate);
            textViewSuggest.setBackgroundResource(activate ? R.color.PINTACT_BLUE_COLOR:R.color.PINTACT_BLUE_ALPHA_COLOR);
        }

        public void activateJoin(boolean activate)
        {
            textViewJoin.setEnabled(activate);
            textViewJoin.setBackgroundResource(activate ? R.color.PINTACT_ORANGE_COLOR:R.color.PINTACT_ORANGE_ALPHA_COLOR);

        }

        public void setPinIcon(ICON icon)
        {
            int resID=R.drawable.pin_check_empty;

            if(icon==ICON.ICON_SUCCESS)
                resID=R.drawable.pin_check_ok;
            else if(icon==ICON.ICON_FAIL)
                resID=R.drawable.pin_check_fail;

            editTextEnterPin.setCompoundDrawablesWithIntrinsicBounds(0,0,resID,0);
        }

	    @Override
	    public void onBackPressed() {
	    	Log.d(TAG, "OnBackPressed.");
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	    }

		
		public void onPinGen(View v) {
			String params = "{\"firstName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getFirstName() + 
						 "\",\"middleName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getMiddleName() +
						 "\",\"lastName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getLastName() +
						 "\"}";
			SingletonNetworkStatus.getInstance().setActivity(this);
			String path = "/api/pins/suggestPin.json";
			new HttpConnection().access(this, path, params, "POST");
			
			sendCmd = CMD_SUGGEST_PIN;
		}


        public void join(View v)
        {
            String pin=editTextEnterPin.getText().toString();
            SingletonLoginData.getInstance().getSignupRequest().setPin(pin);

            Gson gson = new GsonBuilder().create();
            String params = gson.toJson(SingletonLoginData.getInstance().getSignupRequest());

            SingletonNetworkStatus.getInstance().setActivity(this);
            String path = "/api/users/signUp.json";
            new HttpConnection().access(this, path, params, "POST");

            sendCmd = CMD_SIGN_IN;
        }


        public void checkPin(String pinStr) {
            String params  = "{\"pin\":\"" + pinStr + "\"}";

            SingletonNetworkStatus.getInstance().setActivity(this);
            String path = "/api/pins/pinAvailability.json";
            new HttpConnection().access(this, path, params, "POST");

            sendCmd = CMD_CHECK_PIN;
        }
		
		public void onPostNetwork() {
			
			if ( SingletonNetworkStatus.getInstance().getCode() != 200 )
            {
                myDialog(SingletonNetworkStatus.getInstance().getMsg(),  SingletonNetworkStatus.getInstance().getErrMsg());
				return;
			}
			
			if ( sendCmd == CMD_SUGGEST_PIN )
            {
				Gson gson = new GsonBuilder().create();
				String[] pinStr = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), String[].class);

                activateJoin(true);
                setPinIcon(ICON.ICON_SUCCESS);

				editTextEnterPin.setText(pinStr[0]);
			}
            else if ( sendCmd == CMD_CHECK_PIN )
            {
              Gson gson = new GsonBuilder().create();
              if(SingletonNetworkStatus.getInstance().getJson() != null) {
                  Map<String, Boolean> data = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), Map.class);
                  Boolean isAvilable = data.get("available");
                  if (isAvilable) {
                      setPinIcon(ICON.ICON_SUCCESS);
                      activateJoin(true);
                  } else {
                      setPinIcon(ICON.ICON_FAIL);
                      activateJoin(false);
                      activateSuggest(true);
                  }
              }
            }
            else if( sendCmd == CMD_SIGN_IN)
            {
                Gson gson = new GsonBuilder().create();
                DataLoginData obj = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), DataLoginData.class);
                SingletonLoginData.getInstance().setAccessToken(obj.accessToken);
                SingletonLoginData.getInstance().setUserDTO(obj.userDTO);
                saveLoginData(obj);

                Intent it = new Intent(this, LoginRegisterSuccessActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);
                overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
            }

            sendCmd=-1;
		}

  public void saveLoginData(DataLoginData dataLoginData) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(getString(R.string.login_username), SingletonLoginData.getInstance().getSignupRequest().getEmailId());
    editor.putString(getString(R.string.access_token), dataLoginData.accessToken);
    editor.putInt(getString(R.string.set_show_native_contacts), 1);
    editor.putString(getString(R.string.login_user), new Gson().toJson(SingletonLoginData.getInstance().getUserData()));
    editor.commit();

    AppService.reInit();

  }

  private static final Pattern check = Pattern.compile("^[A-Za-z]{2}[0-9]{5}$|^[A-Za-z]{3}[0-9]{4}$",Pattern.CASE_INSENSITIVE);

		
}
