package com.pinplanet.pintact.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.SignupRequest;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Policy;

public class LoginRegisterActivity extends MyActivity {
    private int sendCmd = -1;
    private static final int CMD_SUGGEST_PIN = 0;

    String userPin;

    String firstName, lastName, middleInitial, profilePictureUri;
    String facebookId = "";
    Intent callingIntent;
    EditText editTextFirstName, editTextMiddleInitial, editTextLastName, editTextTitle, editTextOrganization;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register);
        hideBoth();
        showTitle(R.string.ab_register);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextMiddleInitial = (EditText) findViewById(R.id.editTextMiddleInitial);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextOrganization = (EditText) findViewById(R.id.editTextOrganization);

        callingIntent = getIntent();
        firstName = callingIntent.getStringExtra("FirstName");
        lastName = callingIntent.getStringExtra("LastName");
        middleInitial = callingIntent.getStringExtra("MiddleInitial");
        profilePictureUri = callingIntent.getStringExtra("ProfilePictureUri");
        facebookId = callingIntent.getStringExtra("FacebookId");
        Log.d(TAG, "GOOGLEJSONHERE: " + callingIntent.getStringExtra("GoogleJsonString"));
        Log.d(TAG, "FBJSONHERE: " + callingIntent.getStringExtra("FBJsonString"));

        if (firstName != null) {
            editTextFirstName.setText(firstName);
        }
        if (lastName != null) {
            editTextLastName.setText(lastName);
        }
        if (middleInitial != null) {
            editTextMiddleInitial.setText(middleInitial);
        }

        //showRightText(R.string.action_bar_option);
//            addRightTextClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					onNext(v);
//				}
//			});

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


        EditText editTextOrganization = (EditText) findViewById(R.id.editTextOrganization);
        editTextOrganization.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onNext(v);
                }
                return false;
            }
        });


        //TODO: this is test code comment for release

        //###### test code starts here

//            SignupRequest req = new SignupRequest();
//            req.setFirstName("Dennis");
//            req.setMiddleName("R");
//            req.setLastName("Android");
//            req.setTitle("Dr");
//            req.setOrganization("Organization");
//
//            req.setEmailId("dennis@dennis.de");
//            req.setMobileNumber("1234567890");
//            req.setPassword("testtest");
//
//            SingletonLoginData.getInstance().setSignupRequest(req);
//
//            Intent it = new Intent(this, LoginRegisterPINActivity.class);
//            startActivity(it);
//            overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );

        //###### test code ends here
    }

    public void onDummy(View view) {
    }

    public void onNext(View view) {

        boolean invalid = false;

        if (TextUtils.isEmpty(editTextFirstName.getText())) {
            editTextFirstName.setError(getString(R.string.ERROR_INPUT_INVALID));
            if (!invalid) editTextFirstName.requestFocus();
            invalid = true;
        }

        if (TextUtils.isEmpty(editTextLastName.getText())) {
            editTextLastName.setError(getString(R.string.ERROR_INPUT_INVALID));
            if (!invalid) editTextLastName.requestFocus();
            invalid = true;
        }

        if (invalid) return;

//			// Do validation
//			String fn = ((EditText) findViewById(R.id.editTextMobile)).getText().toString();
//			String mn = ((EditText) findViewById(R.id.editTextMiddleInitial)).getText().toString();
//			String ln = ((EditText) findViewById(R.id.editTextLastName)).getText().toString();
//			String title = ((EditText) findViewById(R.id.editTextTitle)).getText().toString();
//			String organization = ((EditText) findViewById(R.id.editTextOrganization)).getText().toString();
//
//			if ( !UiControllerUtil.validField(this, fn, "First Name") ||
//				 !UiControllerUtil.validField(this, ln, "Last Name") ||
//				 !UiControllerUtil.validField(this, title, "Title") ||
//				 !UiControllerUtil.validField(this, organization, "Organization"))
//				return;


        SignupRequest req = new SignupRequest();
        req.setFirstName(editTextFirstName.getText().toString());
        req.setMiddleName(editTextMiddleInitial.getText().toString());
        req.setLastName(editTextLastName.getText().toString());
        req.setTitle(editTextTitle.getText().toString());
        req.setOrganization(editTextOrganization.getText().toString());
        SingletonLoginData.getInstance().setSignupRequest(req);

        pinGen();

//        Intent it = new Intent(this, LoginEmailActivity.class);
//        it.putExtra("ProfilePictureUri", profilePictureUri);
//        it.putExtra("Email", callingIntent.getStringExtra("Email"));
//        it.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
//        Log.d(TAG, "GoogleJsonString: " + callingIntent.getStringExtra("GoogleJsonString"));
//        it.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));
//        it.putExtra("GoogleFriendCount", callingIntent.getIntExtra("GoogleFriendCount", -1));
//        it.putExtra("FBFriendCount", callingIntent.getIntExtra("FBFriendCount", -1));
//        Log.d(TAG, "UserPin in LoginRegister: " + userPin);
//        it.putExtra("UserPin", userPin);
//        startActivity(it);
//        //finish();
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }


    public void pinGen() {
        if (SingletonLoginData.getInstance().getSignupRequest() != null) {
            Log.d(TAG, SingletonLoginData.getInstance().getSignupRequest().getFirstName());
            String params = "{\"firstName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getFirstName() +
                    "\",\"middleName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getMiddleName() +
                    "\",\"lastName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getLastName() +
                    "\"}";
            SingletonNetworkStatus.getInstance().setActivity(this);
            String path = "/api/pins/suggestPin.json";
            new HttpConnection().access(this, path, params, "POST");

            sendCmd = CMD_SUGGEST_PIN;
        } else {
            Log.d(TAG, "SignupRequest is null");
        }
    }

    public void onPostNetwork() {

        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(), SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }

        if (sendCmd == CMD_SUGGEST_PIN) {
            Log.d(TAG, "CMD_SUGGEST_PIN");
            Gson gson = new GsonBuilder().create();
            //String[] pinStr = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), String[].class);
            String jsonString = SingletonNetworkStatus.getInstance().getJson().toString();
            jsonString = jsonString.replace("[", "");
            jsonString = jsonString.replace("]", "");
            jsonString = jsonString.replace("\"", "");
            jsonString = jsonString.replace(" ", "");
            String[] array = jsonString.split(",");
            Log.d(TAG, "Pin: " + array[0]);
            userPin = array[0];

            Intent it = new Intent(this, LoginEmailActivity.class);
            it.putExtra("ProfilePictureUri", profilePictureUri);
            it.putExtra("Email", callingIntent.getStringExtra("Email"));
            it.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
            Log.d(TAG, "GoogleJsonString: " + callingIntent.getStringExtra("GoogleJsonString"));
            it.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));
            it.putExtra("GoogleFriendCount", callingIntent.getIntExtra("GoogleFriendCount", -1));
            it.putExtra("FBFriendCount", callingIntent.getIntExtra("FBFriendCount", -1));
            it.putExtra("GoogleId", callingIntent.getStringExtra("GoogleId"));
            it.putExtra("FacebookId", callingIntent.getStringExtra("FacebookId"));
            Log.d(TAG, "UserPin in LoginRegister: " + userPin);
            it.putExtra("UserPin", userPin);
            startActivity(it);
            //finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

//            findViewById(R.id.buttonNext).setClickable(true);
//            findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onNext(v);
                //}
            //});
        }

        sendCmd = -1;
    }
}
