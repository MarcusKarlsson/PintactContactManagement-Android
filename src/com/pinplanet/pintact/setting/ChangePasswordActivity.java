package com.pinplanet.pintact.setting;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.util.HashMap;
import java.util.Map;


public class ChangePasswordActivity extends MyActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.change_password);

    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);
    hideRight();

    TextView tv = (TextView)findViewById(R.id.actionBar);
    tv.setText(getResources().getString(R.string.change_password));


    final EditText currentPassword = (EditText)findViewById(R.id.old_password);

    final EditText newPassword = (EditText)findViewById(R.id.new_password);
    
    View hideShowOldPassword = findViewById(R.id.hide_show_old_password);
    hideShowOldPassword.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (currentPassword.getTransformationMethod() == null) {
          currentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
          currentPassword.setTransformationMethod(null);
        }
      }
    });
    
    View hideShowNewPassword = findViewById(R.id.hide_show_new_password);
    hideShowNewPassword.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (newPassword.getTransformationMethod() == null) {
          newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
          newPassword.setTransformationMethod(null);
        }
      }
    });
    
    View changePasswordBtn = findViewById(R.id.change_password_button);
    changePasswordBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String newPasswordValue = newPassword.getText().toString();
        if (newPassword == null || newPassword.length() < 8) {
          myDialog(R.string.generic_error_dialog_title, R.string.password_too_short_error);
          return;
        }
        SingletonNetworkStatus.getInstance().setActivity(ChangePasswordActivity.this);
        Map<String, String> data = new HashMap<String, String>();
        data.put("oldpassword", currentPassword.getText().toString());
        data.put("password", newPasswordValue);
        String json = new Gson().toJson(data);
        String path = "/api/users/password/update.json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(ChangePasswordActivity.this, path, json, "POST");
      }
    });
  }

  public void onPostNetwork () {

    if (SingletonNetworkStatus.getInstance().getCode() != 200) {
      myDialog(SingletonNetworkStatus.getInstance().getMsg(),
          SingletonNetworkStatus.getInstance().getErrMsg());
      return;
    }
    finish();
  }
}
