package com.pinplanet.pintact.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.SignupRequest;
import com.pinplanet.pintact.utility.DataLoginData;
import com.pinplanet.pintact.utility.EditTextTypeFace;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;
import com.pinplanet.pintact.utility.UiControllerUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pranab on 10/23/14.
 */
public class LoginEmailActivity extends MyActivity {
    private static final String TAG = "LoginEmailActivity";
    private int sendCmd = -1;
    private static final int CMD_SUGGEST_PIN = 0;

    String profilePictureUri, userEmail;
    Intent callingIntent;
    EditText emailEditText;
    EditTextTypeFace phoneNumberEditText, countryCodeEditText;

    String userPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register_email);
        showTitle(R.string.ab_register);
        hideBoth();

        callingIntent = getIntent();
        profilePictureUri = callingIntent.getStringExtra("ProfilePictureUri");
        userEmail = callingIntent.getStringExtra("Email");

        FacebookSdk.sdkInitialize(getApplicationContext());
        emailEditText = (EditText) findViewById(R.id.editTextEmail);
        if (userEmail != null) {
            Log.d(TAG, userEmail);
            emailEditText.setText(userEmail);
        }
        else {
            Log.d(TAG, "Email is null");
            getFacebookEmail();
        }

        showLeftImage(R.drawable.actionbar_left_arrow);
        addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext(v);
            }
        });

        ((EditText) findViewById(R.id.editTextCountryCode)).setText(UiControllerUtil.getCountryZipCode(this));

        final EditText editText = (EditText) findViewById(R.id.editTextPassword);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onNext(textView);
                }
                return false;
            }
        });

        View hideShowPassword = findViewById(R.id.hide_show_password);
        hideShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getTransformationMethod() == null) {
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    editText.setTransformationMethod(null);
                }
            }
        });

        final TextView phoneView = (TextView) findViewById(R.id.editTextMobile);

        //TODO_ maybe we can combine this as default watcher with our string processing code
        //phoneView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        phoneView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                text = text.replaceAll("[-() ]", "");
                if (text.length() > 3 && text.length() <= 10) {
                    StringBuilder formattedPhone = new StringBuilder();
                    formattedPhone.append("(" + text.substring(0, 3) + ") ");
                    if (text.length() > 6) {
                        formattedPhone.append(text.substring(3, 6) + "-" + text.substring(6));
                    } else {
                        formattedPhone.append(text.substring(3));
                    }
                    text = formattedPhone.toString();
                }
                phoneView.removeTextChangedListener(this);
                editable.replace(0, editable.toString().length(), text);
                phoneView.addTextChangedListener(this);
            }
        });
        phoneNumberEditText = (EditTextTypeFace) findViewById(R.id.editTextMobile);
        countryCodeEditText = (EditTextTypeFace) findViewById(R.id.editTextCountryCode);
        getUsersPhoneNumber();
    }

    public void onDummy(View view) {
    }

    public void onNext(View view) {

        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        EditText editTextMobile = (EditText) findViewById(R.id.editTextMobile);
        EditText editTextCountryCode = ((EditText) findViewById(R.id.editTextCountryCode));
        EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);


        boolean invalid = false;

        if (TextUtils.isEmpty(editTextEmail.getText()) || !UiControllerUtil.isEmailValid(editTextEmail.getText().toString())) {
            editTextEmail.setError(getString(R.string.ERROR_INPUT_INVALID));
            if (!invalid) editTextEmail.requestFocus();
            invalid = true;
        }

        if (TextUtils.isEmpty(editTextCountryCode.getText()) || !editTextCountryCode.getText().toString().startsWith("+") || editTextCountryCode.getText().toString().length() < 2) {
            editTextCountryCode.setError(getString(R.string.ERROR_INPUT_INVALID));
            if (!invalid) editTextCountryCode.requestFocus();
            invalid = true;
        }

        if (TextUtils.isEmpty(editTextMobile.getText())) {
            editTextMobile.setError(getString(R.string.ERROR_INPUT_INVALID));
            if (!invalid) editTextMobile.requestFocus();
            invalid = true;
        }

        if (TextUtils.isEmpty(editTextPassword.getText()) || editTextPassword.getText().length() < 8) {
            editTextPassword.setError(getString(R.string.ERROR_INPUT_INVALID_PASSWORD));
            if (!invalid) editTextPassword.requestFocus();
            invalid = true;
        }

        if (invalid) return;

//    // Do validation
//    String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();
//    String mobileNumber = ((EditText) findViewById(R.id.editTextMobile)).getText().toString();
//    String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
//
//    if ( !UiControllerUtil.validField(this, email, "Email") ||
//        !UiControllerUtil.validField(this, mobileNumber, "Mobile Number") ||
//        !UiControllerUtil.validField(this, password, "Password"))
//      return;
//
//    // check if valid email address
//    if ( !UiControllerUtil.isEmailValid(email)) {
//      myDialog("Invalid Input", "Please enter a valid email address ");
//      return;
//    }
//
//    // check if password long enough
//    if (password == null || password.length() < 8) {
//      myDialog(R.string.generic_error_dialog_title, R.string.password_too_short_error);
//      return;
//    }


        SignupRequest req = SingletonLoginData.getInstance().getSignupRequest();
        if (req != null) {
            req.setEmailId(editTextEmail.getText().toString());

            req.setCountryCode(editTextCountryCode.getText().toString().substring(1));
            req.setMobileNumber(editTextCountryCode.getText().toString() + editTextMobile.getText().toString());
            req.setPassword(editTextPassword.getText().toString());

            // check if email is still available
            String params = "{\"email\":\"" + editTextEmail.getText().toString() + "\"}";

            SingletonNetworkStatus.getInstance().setActivity(this);
            String path = "/api/users/emailAvailability.json";
            new HttpConnection().access(this, path, params, "POST");
        }

    }


    public void onPostNetwork() {
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }

        if (SingletonNetworkStatus.getInstance().getJson().contains("false")) {
            myDialog("Email Already In Use", "A Pintact user already exists with the specified email, please choose a different e-mail or go to Forgot Password.");
            return;
        }

        Intent it = new Intent(this, LoginConnectActivity.class);
        it.putExtra("ProfilePictureUri", profilePictureUri);
        it.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
        it.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));
        it.putExtra("GoogleFriendCount", callingIntent.getIntExtra("GoogleFriendCount", -1));
        it.putExtra("FBFriendCount", callingIntent.getIntExtra("FBFriendCount", -1));
        it.putExtra("UserPin", callingIntent.getStringExtra("UserPin"));
        it.putExtra("GoogleId", callingIntent.getStringExtra("GoogleId"));
        it.putExtra("FacebookId", callingIntent.getStringExtra("FacebookId"));
        startActivity(it);
        //finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    private void getFacebookEmail() {
        Bundle parameters;
        if (AccessToken.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            if (object != null) {
                                try {
                                    emailEditText.setText(object.getString("email"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (response.getError() != null)
                                    Log.d(TAG, "Facebook error: " + response.getError().getErrorMessage());
                            }
                        }
                    });
            parameters = new Bundle();
            parameters.putString("fields", "email");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    private void getUsersPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        if(mPhoneNumber != null) {
            StringBuilder sb = new StringBuilder(mPhoneNumber);
            countryCodeEditText.setText("+" + Character.toString(mPhoneNumber.charAt(0)));
            sb.deleteCharAt(0);
            phoneNumberEditText.setText(sb.toString());
        }else{
            countryCodeEditText.setText("+1");
        }
    }
}
