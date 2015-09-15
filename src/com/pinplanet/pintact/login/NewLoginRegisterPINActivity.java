package com.pinplanet.pintact.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class NewLoginRegisterPINActivity extends MyActivity implements View.OnFocusChangeListener,View.OnKeyListener{

        private static final String TAG = NewLoginRegisterPINActivity.class.getName();
        private static final int CMD_SUGGEST_PIN=0,CMD_CHECK_PIN=1,CMD_SIGN_IN=2;


    private enum HINT_TYPE {EMPTY, SUCCESS, FAIL};

        private EditText[] editTextPin;
        private TextView textViewSuggest,textViewJoin,textViewDescHint;

		private int sendCmd = -1;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.new_login_register_pin);

            hideRight();

            getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            showTitle(R.string.ab_pin);

            showLeftImage(R.drawable.actionbar_left_arrow);
            addLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            textViewDescHint=(TextView)findViewById(R.id.textViewDescHint);
            textViewSuggest=(TextView)findViewById(R.id.buttonSuggest);
            textViewJoin=(TextView)findViewById(R.id.buttonJoin);
            activateSuggest(false);
            activateJoin(false);

			String fn = SingletonLoginData.getInstance().getSignupRequest().getFirstName().substring(0,1).toLowerCase();
			String ln = SingletonLoginData.getInstance().getSignupRequest().getLastName().substring(0,1).toLowerCase();

            StringBuffer sbPin=new StringBuffer();
            sbPin.append(fn);
            if ( SingletonLoginData.getInstance().getSignupRequest().getMiddleName().length() > 0 )
            {
                String mn = SingletonLoginData.getInstance().getSignupRequest().getMiddleName().substring(0,1).toLowerCase();
                sbPin.append(mn);
            }
            sbPin.append(ln);

            editTextPin=new EditText[7];
            editTextPin[0]=(EditText)findViewById(R.id.editText1);
            editTextPin[1]=(EditText)findViewById(R.id.editText2);
            editTextPin[2]=(EditText)findViewById(R.id.editText3);
            editTextPin[3]=(EditText)findViewById(R.id.editText4);
            editTextPin[4]=(EditText)findViewById(R.id.editText5);
            editTextPin[5]=(EditText)findViewById(R.id.editText6);
            editTextPin[6]=(EditText)findViewById(R.id.editText7);

            setPinDescHint(HINT_TYPE.EMPTY);

            final TextWatcher textWatcherFinal=new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    View focusCurrent=getWindow().getCurrentFocus();
                    if( focusCurrent==null) return;

                    View focusNew = focusCurrent.focusSearch(View.FOCUS_RIGHT);

                    if(focusNew!=null)
                    {
                        Log.d(TAG,"afterTextChanged focusNew tag:"+focusNew.getTag());
                        focusNew.requestFocus();
                    }
                    else
                    {
                        String text=getCurrentPIN();

                        if(text.length()==7)
                        {
                            if(check.matcher(text).matches())
                            {
                                if(sendCmd!=CMD_SUGGEST_PIN)
                                    checkPin(text);
                            }
                            else
                            {
                                setPinDescHint(HINT_TYPE.EMPTY);
                                activateSuggest(true);
                            }
                        }
                        else
                        {
                            setPinDescHint(HINT_TYPE.EMPTY);
                            activateSuggest(false);
                            activateJoin(false);
                        }
                    }
                }
            };



            int cnt=0,length=sbPin.length();
            for(EditText ed:editTextPin)
            {
                if(cnt<length)
                {
                    ed.setText("" + sbPin.charAt(cnt));
                    ed.setHint("" + sbPin.charAt(cnt));
                }

                ed.setTag("" + cnt);
                //ed.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ed.setOnKeyListener(this);
                ed.setOnFocusChangeListener(this);
                //ed.setInputType(InputType.TYPE_NULL);
                ed.addTextChangedListener(textWatcherFinal);
                cnt++;
            }
            editTextPin[sbPin.length()].requestFocus();

		}


        public void activateSuggest(boolean activate)
        {
            textViewSuggest.setEnabled(activate);
            textViewSuggest.setBackgroundResource(activate ? R.color.PINTACT_ORANGE_COLOR:R.color.PINTACT_ORANGE_ALPHA_COLOR);
        }

        public void activateJoin(boolean activate)
        {
            textViewJoin.setEnabled(activate);
            textViewJoin.setBackgroundResource(activate ? R.color.PINTACT_BLUE_COLOR:R.color.PINTACT_BLUE_ALPHA_COLOR);
        }

        public void setPinDescHint(HINT_TYPE type)
        {
            if(type== HINT_TYPE.SUCCESS)
            {
                int resID=R.string.lrp_desc_success;
                int colorId=R.color.PINTACT_GREEN_COLOR;
                textViewDescHint.setTextColor(getResources().getColor(colorId));
                textViewDescHint.setText(resID);
                textViewDescHint.setVisibility(View.VISIBLE);
            }
            else if(type== HINT_TYPE.FAIL)
            {
                int resID=R.string.lrp_desc_fail;
                int colorId=R.color.PINTACT_RED_COLOR;
                textViewDescHint.setTextColor(getResources().getColor(colorId));
                textViewDescHint.setText(resID);
                textViewDescHint.setVisibility(View.VISIBLE);
            }
            else
            textViewDescHint.setVisibility(View.INVISIBLE);
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
            String pin=getCurrentPIN();
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
                setPinDescHint(HINT_TYPE.SUCCESS);
                setCurrentPIN(pinStr[0]);
			}
            else if ( sendCmd == CMD_CHECK_PIN )
            {
              Gson gson = new GsonBuilder().create();
              Map<String, Boolean> data= gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), Map.class);
              Boolean isAvilable = data.get("available");
              if(isAvilable) {
                  setPinDescHint(HINT_TYPE.SUCCESS);
                  activateJoin(true);
              }else{
                  setPinDescHint(HINT_TYPE.FAIL);
                  activateJoin(false);
                  activateSuggest(true);
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
    editor.putString(getString(R.string.login_user), new Gson().toJson(SingletonLoginData.getInstance().getUserData()));
    editor.commit();

    AppService.reInit();

  }

  private static final Pattern check = Pattern.compile("^[A-Za-z]{2}[0-9]{5}$|^[A-Za-z]{3}[0-9]{4}$",Pattern.CASE_INSENSITIVE);


    private String getCurrentPIN()
    {
        StringBuffer pin=new StringBuffer();
        for(EditText ed:editTextPin)
        {
            pin.append(ed.getText().toString());
        }

        return pin.toString();
    }

    private void setCurrentPIN(String pin)
    {
        int cnt=0;
        for(EditText ed:editTextPin)
        {
            ed.setText(""+pin.charAt(cnt++));
        }
    }

    private void setNextFocusRight(View v)
    {
        View foundView;
        View lastView=v;
        while( (foundView=lastView.focusSearch(View.FOCUS_RIGHT)) !=null)
        {
            if(((EditText)foundView).getText().toString().isEmpty())
            {
                foundView.requestFocus();
                return;
            }

            lastView=foundView;
        }

        lastView.requestFocus();
    }

    private void setNextFocusLeft(View v)
    {
        View foundView;
        View lastView=v;
        while( (foundView=lastView.focusSearch(View.FOCUS_LEFT)) !=null)
        {
            if(!((EditText)foundView).getText().toString().isEmpty())
            {
                lastView.requestFocus();
                return;
            }

            lastView=foundView;
        }

        lastView.requestFocus();
    }


    @Override
    public void onFocusChange(View v, boolean b) {
        if(!b)return;

        if(((EditText)v).getText().toString().isEmpty())
        {
            setNextFocusLeft(v);
        }
        else
        {
            setNextFocusRight(v);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
        Toast.makeText(this,"onKey KeyCode:"+keyEvent.getKeyCode(),Toast.LENGTH_LONG).show();
        if(keyEvent.getAction()==KeyEvent.ACTION_UP &&  (keyEvent.getKeyCode()==KeyEvent.KEYCODE_DEL || keyEvent.getKeyCode()==KeyEvent.KEYCODE_BACK)) {
            View ed=v.focusSearch(View.FOCUS_LEFT);
               if(ed!=null)
               {
                   ((EditText) ed).setText("");
                   ed.requestFocus();
               }
        }
        return false;
    }

}
