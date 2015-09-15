package com.pinplanet.pintact.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.ContactInviteActivity;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.ImageUploadActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

public class SearchSettingActivity extends ImageUploadActivity {

  public static final String IS_REGISTRATION_FLOW = "IS_REGISTRATION_FLOW";

  private TextView pin;
  private EditText firstName;
  private EditText lastName;
  private EditText title;
  private EditText companyName;
  private EditText city;
  private EditText state;
  private boolean isSaveClicked;
  Switch activateSwitch;
  boolean isRegistrationFlow = false;
  private ImageView profileImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_setting_main);

    SingletonNetworkStatus.getInstance().setActivity(this);
    String path = "/api/searchProfiles.json?" + SingletonLoginData.getInstance().getPostParam();
    new HttpConnection().access(this, path, "", "GET");

    if ( getIntent().getExtras() != null && getIntent().getExtras().containsKey(IS_REGISTRATION_FLOW))
      isRegistrationFlow = getIntent().getExtras().getBoolean(IS_REGISTRATION_FLOW);

    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);
    showRightText(getResources().getString(R.string.set_search_setting_save));
    View.OnClickListener saveLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        isSaveClicked = true;
        boolean flag = uploadImage();
        if(!flag){
          saveSearchProfile();
        }
      }
    };
    addRightTextClickListener(saveLn);
    TextView tv = (TextView)findViewById(R.id.actionBar);
    tv.setText(getResources().getString(R.string.set_search_setting).toUpperCase());

    pin = (TextView)findViewById(R.id.set_search_setting_search_field_pin_text);
    firstName = (EditText)findViewById(R.id.set_search_setting_search_field_fname_text);
    lastName = (EditText)findViewById(R.id.set_search_setting_search_field_lname_text);
    title = (EditText)findViewById(R.id.set_search_setting_search_field_title_text);
    companyName = (EditText)findViewById(R.id.set_search_setting_search_field_company_text);
    city = (EditText)findViewById(R.id.set_search_setting_search_field_city_text);
    state = (EditText)findViewById(R.id.set_search_setting_search_field_state_text);
    activateSwitch = (Switch) findViewById(R.id.set_search_setting_privacy_switch);
    profileImage = (ImageView)findViewById(R.id.pcn_add_image);
    init();
  }
  private void saveSearchProfile(){
    SingletonNetworkStatus.getInstance().setActivity(SearchSettingActivity.this);
    SearchProfileDto profileDto = getProfileData();
    String json = new Gson().toJson(profileDto);
    String path = "/api/searchProfiles.json?" + SingletonLoginData.getInstance().getPostParam();
    new HttpConnection().access(SearchSettingActivity.this, path, json, "POST");
  }

  public void onPostNetwork () {

    if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
      myDialog(SingletonNetworkStatus.getInstance().getMsg(),
          SingletonNetworkStatus.getInstance().getErrMsg());
      finish();
      return;
    }else {
      if(isUploadingImage) {
        super.onPostNetwork();
        saveSearchProfile();
      }else
      if (isSaveClicked) {
        if(isRegistrationFlow){
          onInvite();
        }else {
          finish();
        }
      } else {
        String json = SingletonNetworkStatus.getInstance().getJson();
        SearchProfileDto profileDto = new Gson().fromJson(json, SearchProfileDto.class);
        setTextFields(profileDto);
      }
    }
  }

  public void onInvite() {
    Intent it = new Intent(this, ContactInviteActivity.class);
    it.putExtra(ContactInviteActivity.ARG_INVITE_ACTIVITY, 0);
    startActivity(it);
  }

  private SearchProfileDto getProfileData(){
    SearchProfileDto profileDto = new SearchProfileDto();
    profileDto.firstName = firstName.getText().toString();
    profileDto.lastName = lastName.getText().toString();
    profileDto.title = title.getText().toString();
    profileDto.companyName = companyName.getText().toString();
    profileDto.city = city.getText().toString();
    profileDto.state = state.getText().toString();
    profileDto.pathToImage = (imInfo != null)? imInfo.thumbnailPath: null;

    if(activateSwitch.isChecked()){
      profileDto.status = 1;
    }else{
      profileDto.status = 0;
    }
    return profileDto;
  }



  private void setTextFields(SearchProfileDto profileDto){
    pin.setText(SingletonLoginData.getInstance().getUserData().pin);
    firstName.setText(profileDto.firstName);
    lastName.setText(profileDto.lastName);
    title.setText(profileDto.title);
    companyName.setText(profileDto.companyName);
    city.setText(profileDto.city);
    state.setText(profileDto.state);
    if(profileDto.status == 0){
      activateSwitch.setChecked(false);
    }else{
      activateSwitch.setChecked(true);
    }
//    if(profileDto.pathToImage != null)
//    {
      initImagePath(profileDto.pathToImage);
//    }
  }

  class SearchProfileDto{
    String firstName;

    String lastName;
    String pathToImage;
    String title;
    String companyName;
    String city;
    String state;
    Long userId;
    byte status;
  }
}
