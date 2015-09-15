package com.pinplanet.pintact.group;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.PintactProfileActivity;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.UiControllerUtil;

public class GroupProfileShareActivity extends MyActivity {

  public static String ARG_PROFILE_SHARE = "SHARE_PROFILES";
  int mArgInt = 0; // -1 : from group pin; 0 : from add contact >0: from notification accept
  int mShareStep = 0; // 0 : send see; 1 : reload notification;

  LinkedHashMap<String, Integer> hm = new LinkedHashMap<String, Integer>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_profile_share);

    if ( getIntent().getExtras() != null )
      mArgInt = getIntent().getExtras().getInt(ARG_PROFILE_SHARE);

    TextView btn = (TextView) findViewById(R.id.gps_pin_tv);
    btn.setFocusableInTouchMode(true);
    btn.requestFocus();

    TextView tv = (TextView)findViewById(R.id.actionBar);
    if(mArgInt < 0)
        tv.setText(getResources().getString(R.string.ab_join_group));
    else
        tv.setText(getResources().getString(R.string.ab_group_add));

    showLeftImage(R.drawable.actionbar_left_arrow);

    hideRight();

    View.OnClickListener backLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);

    TextView share = (TextView) findViewById(R.id.gm_share);
    share.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        onShareProfile();
      }
    });

    TextView preview = (TextView) findViewById(R.id.gm_preview);
    preview.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        onProfileView();
      }
    });

    for (int i = 0; i < SingletonLoginData.getInstance().getUserProfiles().size() ; i ++ ) {
      addProfiles(i);
    }

  }

  public Long[] getSharedProfileIds(){
    Long profId[] = new Long[hm.size()];
    List<ProfileDTO> profiles = SingletonLoginData.getInstance().getUserProfiles();
    Set<Map.Entry<String, Integer>> entries = hm.entrySet();
    int j =0;
    for(Map.Entry<String, Integer> entry : entries) {
      for (int i = 0; i < profiles.size(); i++) {
        UserProfile prof = profiles.get(i).getUserProfile();
        if(entry.getKey().equals(prof.getName()))
        {
          profId[j++] = prof.getId();
        }
      }
    }

    return profId;
  }

  public void onProfileView() {
    // set shared profile
    Long ids[] = getSharedProfileIds();


    if ( ids.length == 0 ) {
      myDialog(getString(R.string.gm_no_profile_title), getString(R.string.gm_no_profile_detail));
      return;
    }

    UiControllerUtil.openPreviewShareActivity(ids);

    Intent myIntent = PintactProfileActivity.getInstanceForShareView(GroupProfileShareActivity.this);
    startActivity(myIntent);
  }

  public void onShareProfile() {
    // set shared profile
    String notes = ((EditText)findViewById(R.id.gps_msg_cont)).getText().toString();
    Long profId[] = getSharedProfileIds();


    if ( profId.length == 0 ) {
      myDialog("No Profile Selected", "Please select at least one profile to share");
      return;
    }

    SingletonLoginData.getInstance().getContactShareRequest().setNote(notes);
    SingletonLoginData.getInstance().getContactShareRequest().setUserProfileIdsShared(profId);

    Gson gson = new GsonBuilder().create();
    String params = gson.toJson(SingletonLoginData.getInstance().getContactShareRequest());

    if ( mArgInt > 0 ) {
      SingletonNetworkStatus.getInstance().clean();
      SingletonNetworkStatus.getInstance().setDoNotDismissDialog(true);
    }

    SingletonNetworkStatus.getInstance().setActivity(this);
    String path = "/api/contacts.json?" + SingletonLoginData.getInstance().getPostParam();
    if ( mArgInt < 0 ) // from group pin
    {
      path = "/api/contacts/addByPin.json?" + SingletonLoginData.getInstance().getPostParam();
    }

    new HttpConnection().access(this, path, params, "POST");
  }

  public void onPostNetwork() {
    if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
      myDialog(SingletonNetworkStatus.getInstance().getMsg(), 
          SingletonNetworkStatus.getInstance().getErrMsg());
      return;
    }

    if ( mArgInt < 0 ) {
      Intent it = new Intent(GroupProfileShareActivity.this, LeftDeckActivity.class);
      it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      it.putExtra("DIALOG_TITLE_RES_ID", R.string.dialog_group_joined_title);
      it.putExtra("DIALOG_MESSAGE_RES_ID", R.string.dialog_group_joined_message);
      startActivity(it);
      return;
    }
    
    if (mArgInt == 0) {
      // see if contact was created
      AppService.handleUpdateContactResponse();
      
      Intent it = new Intent(GroupProfileShareActivity.this, LeftDeckActivity.class);
      it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      it.putExtra("DIALOG_TITLE_RES_ID", R.string.dialog_invite_sent_title);
      it.putExtra("DIALOG_MESSAGE_RES_ID", R.string.dialog_invite_sent_message);
      startActivity(it);
      return;
    }

    if ( mArgInt > 0 && mShareStep == 0 ) {
      String path = "/api/notifications/" +
          SingletonLoginData.getInstance().getNotifications().getData().get(mArgInt).getNotificationId() +
          "/seen.json?" + SingletonLoginData.getInstance().getPostParam();
      new HttpConnection().access(this, path, "", "POST");
      AppService.checkIfThereIsAnyContacts();
      mShareStep ++;
      return;
    }

    if ( mArgInt > 0 && mShareStep == 1 ) {

      // this should be the last one
      SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);

      String path = "/api/sortedNotifications.json?pageSize=100&" + SingletonLoginData.getInstance().getPostParam();
      new HttpConnection().access(this, path, "", "GET");
      mShareStep ++;
      return;
    }

    if ( mShareStep == 2 ) {
      // update notification
      Type collectionType = new TypeToken<PageDTO<NotificationDTO>>(){}.getType();
      Gson gson = new GsonBuilder().create();
      PageDTO<NotificationDTO> notifications = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
      SingletonLoginData.getInstance().setNotifications(notifications);
      finish();
    }
  }

    public void addProfiles(int i)
    {
        UserProfile currentProfile=  SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title=currentProfile.getName();

        LinearLayout container = (LinearLayout)findViewById(R.id.ps_share_lo);
        final View addView = getLayoutInflater().inflate(R.layout.profile_thumb, null);
        container.addView(addView);

        // change the name of the text
        TextView tv = (TextView)addView.findViewById(R.id.pt_name);
        tv.setText(title);

        Log.d(TAG, "addProfiles:" + i + " title:" + title + " image" + currentProfile.getPathToImage());

        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) addView.findViewById(R.id.pt_profile_image);
        ivPhoto.setDefaultImageResId(R.drawable.silhouette);

        if (currentProfile.getPathToImage() != null) {
            ivPhoto.setImageUrl(currentProfile.getPathToImage(), AppController.getInstance().getImageLoader());
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        final RelativeLayout lo = (RelativeLayout)addView.findViewById(R.id.pt_all);
        lo.setOnClickListener(new View.OnClickListener(){

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                TextView tv = (TextView) v.findViewById(R.id.pt_name);
                String key = tv.getText().toString();
                selectProfile(lo, key);
                //tvInfo.setText(listProfiles);

            }});

        if(SingletonLoginData.getInstance().getUserProfiles().size() == 1)
        {
            selectProfile(lo, title);
        }

    }

    private void selectProfile(RelativeLayout lo, String key)
    {
        Integer value = hm.get(key);
        lo.setBackgroundDrawable(lo.getResources().getDrawable(
                value == null ?
                        R.drawable.border_profile_thumb_sel_half :
                        R.drawable.border_profile_thumb_nosel_half
        ));
        if ( value == null ) {
            hm.put(key, 1);
        } else
            hm.remove(key);

        // we need to update tv_share
        String listProfiles = "";
        Set<Map.Entry<String, Integer>> entries = hm.entrySet();
        for ( Map.Entry<String, Integer> entry : entries ) {
            listProfiles += entry.getKey() + ",";
        }


        TextView tvInfo = (TextView) findViewById(R.id.tv_share);
        if ( listProfiles.length() > 1 )
            listProfiles = listProfiles.substring(0, listProfiles.length() - 1);
    }

  public void onDummy(View view) {
  }

}
