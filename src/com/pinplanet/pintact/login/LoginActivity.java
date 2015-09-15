package com.pinplanet.pintact.login;

import java.io.InputStream;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;

import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.DataLogin;
import com.pinplanet.pintact.utility.DataLoginData;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

public class LoginActivity extends MyActivity {

  private static final String TAG = LoginActivity.class.getName();

  String mUserName;
  String mPassword;

  int mloginStep = 0;
  boolean isGCMRegister = true;

  // from sample code
  public static final String EXTRA_MESSAGE = "message";
  public static final String PROPERTY_REG_ID = "registration_id";

  /**
   * Substitute you own sender ID here. This is the project number you got
   * from the API Console, as described in "Getting Started."
   */
  String SENDER_ID = "159895103372";


  GoogleCloudMessaging gcm;
  Context context;

  String regid;
  // end of sample code


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_login);

      ((EditText)findViewById(R.id.passText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {

          @Override
          public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
              if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                  onLogin(textView);
                  return true;
              }

              return false;
          }
      });


    DisplayMetrics dm = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(dm);

      findViewById(R.id.linearLayoutForgot).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              onForgot(view);
          }
      });

    findViewById(R.id.linearLayoutRegister).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent it = new Intent(LoginActivity.this, LoginRegisterActivity.class);
        startActivity(it);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
      }
    });


    findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              onLogin(view);
          }
      });

    ActionBar actionBar = getActionBar();
    actionBar.hide();

  }

  public void onLogin(View view) {

    EditText userName = (EditText) findViewById(R.id.userText);
    EditText password = (EditText) findViewById(R.id.passText);

    mUserName = userName.getText().toString();
    mPassword = password.getText().toString();
    // Validation

    loginSent();
  }

  public void loginSent() {

    if (mUserName.isEmpty() || mPassword.isEmpty()) {
      // need a layout for info
      // maybe a existing one
      Log.d(TAG, "Field Empty!");

      // test dialog
      myDialog(R.string.DIALOG_TITLE_FIELD_EMPTY, R.string.DIALOG_MESSAGE_FIELD_EMPTY_LOGIN);

      return;
    }

    // Authentication
    DataLogin data = new DataLogin(mUserName, mPassword);
    Gson gson = new GsonBuilder().create();
    String params = gson.toJson(data);

    SingletonNetworkStatus.getInstance().clean();
    SingletonNetworkStatus.getInstance().setActivity(this);
    SingletonNetworkStatus.getInstance().setDoNotDismissDialog(true);
    String path = "/api/users/login.json";
    new HttpConnection().access(this, path, params, "POST");

  }
  public void onPassVisible(View view)
  {
    EditText password = (EditText) findViewById(R.id.passText);
    if(password.getTransformationMethod() instanceof HideReturnsTransformationMethod)
    {
      password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    else
    {
      password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }
  }

  public void saveLoginData(UserDTO user, Long userId, String accessToken) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(getString(R.string.login_username), userId.toString());
    editor.putString(getString(R.string.login_user),  new Gson().toJson(user));
    editor.putString(getString(R.string.access_token), accessToken);
    editor.commit();

  }

  public void onPostNetwork() {

    if (mloginStep == 0 && SingletonNetworkStatus.getInstance().getCode() != 200) {
      SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);
      if (SingletonNetworkStatus.getInstance().getWaitDialog() != null) {
       SingletonNetworkStatus.getInstance().getWaitDialog().dismiss();
       SingletonNetworkStatus.getInstance().setWaitDialog(null);
      }

      myDialog(SingletonNetworkStatus.getInstance().getMsg(),
          SingletonNetworkStatus.getInstance().getErrMsg());


      return;
    }


    if (mloginStep == 0) {
      Gson gson = new GsonBuilder().create();
      DataLoginData obj = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), DataLoginData.class);
      SingletonLoginData.getInstance().setAccessToken(obj.accessToken);
      SingletonLoginData.getInstance().setUserDTO(obj.userDTO);
      mloginStep++;
      saveLoginData(obj.userDTO, obj.userDTO.id, obj.accessToken);
        AppService.getProfilesAsync();
        AppService.getLabelsAsync();
        AppService.setUserProfileChildLabels();
      AppService.fetchContacts(this);

      postGetRegistrationID();
      return;
    }

    if (mloginStep == 1) { // getting profiles
      AppService.handleGetContactResponse();
      SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);
      // Bring up the LeftDeck
      Intent it = new Intent(this, LeftDeckActivity.class);
      startActivity(it);
    }


  }

  public void onForgot(View view) {
    Intent it = new Intent(this, LoginForgotActivity.class);
    startActivity(it);
    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
  }

  public void onRegister(View view) {
    Intent it = new Intent(this, LoginRegisterActivity.class);
    startActivity(it);
    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
  }



  // test loading profile images
  public void loadImage(int index, String photo_url_str) {
    System.out.println("Loading image from " + photo_url_str);
    new DownloadImageTask().execute(photo_url_str, Integer.toString(index));
  }

  private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    int position;

    public DownloadImageTask() {
    }

    protected Bitmap doInBackground(String... urls) {
      String urldisplay = urls[0];
      position = Integer.parseInt(urls[1]);
      Bitmap mIcon11 = null;
      try {
        InputStream in = new java.net.URL(urldisplay).openStream();
        mIcon11 = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        System.out.println("Error" + e.getMessage());
      }
      return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
      SingletonLoginData.getInstance().setBitmap(position, result);
    }
  }

    public void onRegisterClicked(View v)
    {
        Intent it = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(it);

        finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

  @Override
  public void onBackPressed() {
    Log.d(TAG,"onBackPressed");
    finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }
}
