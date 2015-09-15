package com.pinplanet.pintact.group;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UpdateGroupPinRequest;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.profile.DownloadProfileImageTask;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.DateInterval;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

public class GroupPinActivity extends MyActivity {

    public static String ARG_PROFILE_SHARE = "SHARE_PROFILES";
    int mArgInt = 0; // -1 : from group pin; 0 : from add contact; >0: from notification accept
    int mShareStep = 0; // 0 : send see; 1 : reload notification;
    int currentExpireIndex = 0;

    // result codes
    public static final int GROUP_EDIT_CANCEL = 1;

    HashMap<String, Integer> hm = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_pin_edit);

        GroupDTO currentGroup = SingletonLoginData.getInstance().getCurGroup();

        TextView tv = (TextView) findViewById(R.id.actionBar);
        tv.setText(getResources().getString(R.string.
                ab_group_info));
        hideRight();
        showLeftImage(R.drawable.actionbar_left_arrow);

        View.OnClickListener backLn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        addLeftClickListener(backLn);

        TextView create = (TextView) findViewById(R.id.gm_create);
        TextView edit = (TextView) findViewById(R.id.gm_edit);
        TextView cancel = (TextView) findViewById(R.id.gm_cancel);

        if (getIntent().getExtras() != null) {
            mArgInt = getIntent().getExtras().getInt(ARG_PROFILE_SHARE);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(GROUP_EDIT_CANCEL);
                finish();
            }
        });

        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.gpn_date);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(DateInterval.getLabels().length - 1);
        numberPicker.setDisplayedValues(DateInterval.getLabels());
        numberPicker.setWrapSelectorWheel(false);

        final EditText groupNameTextView = (EditText) findViewById(R.id.gm_name);

        groupNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int groupNameLength = groupNameTextView.getText().toString().length();
                if (groupNameLength > 50) {
                    groupNameTextView.setError("Value cannot exeed 50 characters");
                } else if (groupNameLength == 0) {
                    groupNameTextView.setError("Value is required");
                } else {
                    groupNameTextView.setError(null);
                }
            }
        });

        if (currentGroup.getId() == null) {
            tv.setText(getResources().getString(R.string.ab_group_info));
            edit.setVisibility(View.GONE);

            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditGroup();
                }
            });

            for (int i = 0; i < SingletonLoginData.getInstance().getUserProfiles().size(); i++) {
                addProfiles(i);
            }
        } else {
            tv.setText(getResources().getString(R.string.ab_group_edit));
            findViewById(R.id.gps_pin_tv).setVisibility(View.GONE);
            findViewById(R.id.ps_share_lo).setVisibility(View.GONE);
            create.setVisibility(View.GONE);

            groupNameTextView.setText(currentGroup.getGroupName());

            if (currentGroup.getExpiredTime() != null) {
                // find value for expiration (round up)
                int index = DateInterval.getIndexForTimeInMillis(Long.parseLong(currentGroup.getExpiredTime()));

                currentExpireIndex = index;
                numberPicker.setValue(index);
            }

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditGroup();
                }
            });
        }
    }

    public Long[] getSharedProfileIds() {
        Long profId[] = new Long[hm.size()];
        List<ProfileDTO> profiles = SingletonLoginData.getInstance().getUserProfiles();
        Set<Map.Entry<String, Integer>> entries = hm.entrySet();
        int j = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            for (int i = 0; i < profiles.size(); i++) {
                UserProfile prof = profiles.get(i).getUserProfile();
                if (entry.getKey().equals(prof.getName())) {
                    profId[j++] = prof.getId();
                }
            }
        }

        return profId;
    }

    public void onEditGroup() {
        GroupDTO currentGroup = SingletonLoginData.getInstance().getCurGroup();

        // do validation on exp date
        long seconds = System.currentTimeMillis();
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.gpn_date);
        int newExpireIndex = numberPicker.getValue();
        String expTime = currentGroup.getExpiredTime();
        if (expTime == null || newExpireIndex != currentExpireIndex) {
            Long expTimeInMillis = DateInterval.getTimeInMillisForIndex(newExpireIndex);
            if (expTimeInMillis != null) {
                expTime = Long.toString(expTimeInMillis);
            }
        }

        // validate group name
        System.out.println("cur : " + seconds + " == exp : " + expTime);
        EditText gnTV = (EditText) findViewById(R.id.gm_name);

        String gn = gnTV.getText().toString();
        if (gn == null || gn.length() < 1) {
            gnTV.setError("Value is required");
        }
        if (gnTV.getError() != null) {
            myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
            return;
        }

        Gson gson = new GsonBuilder().create();
        String params = null;
        String apiPath = null;
        if (currentGroup.getId() == null) {
            // get profiles to share
            Long profId[] = getSharedProfileIds();

            if (profId.length == 0) {
                myDialog("No Profile Selected", "Please select at least one profile to share");
                return;
            }


            ContactShareRequest contactShareRequest = new ContactShareRequest();
            contactShareRequest.setGroupName(gn);
            contactShareRequest.setExpiryTimeInUTC(expTime);
            contactShareRequest.setUserProfileIdsShared(profId);
            params = gson.toJson(contactShareRequest);
            apiPath = "/api/groupPins";
        } else {
            UpdateGroupPinRequest updateGroupPinRequest = new UpdateGroupPinRequest();
            updateGroupPinRequest.setGroupPin(currentGroup.getGroupPin());
            updateGroupPinRequest.setName(gn);
            updateGroupPinRequest.setExpiryTimeInUTC(expTime);
            params = gson.toJson(updateGroupPinRequest);
            apiPath = "/api/groupPins/update";
        }

        if (mArgInt > 0) {
            SingletonNetworkStatus.getInstance().clean();
            SingletonNetworkStatus.getInstance().setDoNotDismissDialog(true);
        }

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
    }

    public void onDummy(View view) {
    }

    @SuppressLint("NewApi")
    public void onPostNetwork() {
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }

        Gson gson = new GsonBuilder().create();
        GroupDTO mGroup = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), GroupDTO.class);
        if (SingletonLoginData.getInstance().getCurGroup().getId() == null) {
            SingletonLoginData.getInstance().setCurGroup(mGroup);
            SingletonLoginData.getInstance().getGroups().add(mGroup);
        } else {
            SingletonLoginData.getInstance().getCurGroup().setGroupName(mGroup.getGroupName());
            SingletonLoginData.getInstance().getCurGroup().setExpiredTime(mGroup.getExpiredTime());
            for (int i = 0; i < SingletonLoginData.getInstance().getGroups().size(); i++) {
                if (mGroup.getId().equals(SingletonLoginData.getInstance().getGroups().get(i).getId())) {
                    SingletonLoginData.getInstance().getGroups().set(i, SingletonLoginData.getInstance().getCurGroup());
                    break;
                }
            }
        }

        if (mArgInt <= 0) {
            Intent myIntent = new Intent(this, GroupCreateConfirmActivity.class);
            startActivity(myIntent);
            return;
        }

        if (mArgInt > 0 && mShareStep == 0) {
            String path = "/api/notifications/" +
                    SingletonLoginData.getInstance().getNotifications().getData().get(mArgInt).getNotificationId() +
                    "/seen.json?" + SingletonLoginData.getInstance().getPostParam();
            new HttpConnection().access(this, path, "", "POST");
            mShareStep++;
            return;
        }

        if (mArgInt > 0 && mShareStep == 1) {
            // this should be the last one
            SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);

            String path = "/api/sortedNotifications.json?pageSize=100&" + SingletonLoginData.getInstance().getPostParam();
            new HttpConnection().access(this, path, "", "GET");
            mShareStep++;
            return;
        }

        if (mShareStep == 2) {
            // update notification
            Type collectionType = new TypeToken<PageDTO<NotificationDTO>>() {
            }.getType();
            PageDTO<NotificationDTO> notifications = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
            SingletonLoginData.getInstance().setNotifications(notifications);
            finish();
        }

        finish();
    }

    public void addProfiles(int i) {
        UserProfile currentProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = currentProfile.getName();

        LinearLayout container = (LinearLayout) findViewById(R.id.ps_share_lo);
        final View addView = getLayoutInflater().inflate(R.layout.profile_thumb, null);
        container.addView(addView);

        // change the name of the text
        TextView tv = (TextView) addView.findViewById(R.id.pt_name);
        tv.setText(title);

        Log.d(TAG, "addProfiles:" + i + " title:" + title + " image" + currentProfile.getPathToImage());

        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) addView.findViewById(R.id.pt_profile_image);
        ivPhoto.setDefaultImageResId(R.drawable.silhouette);

        if (currentProfile.getPathToImage() != null) {
            ivPhoto.setImageUrl(currentProfile.getPathToImage(), AppController.getInstance().getImageLoader());
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        final RelativeLayout lo = (RelativeLayout) addView.findViewById(R.id.pt_all);
        lo.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                TextView tv = (TextView) v.findViewById(R.id.pt_name);
                String key = tv.getText().toString();
                Integer value = hm.get(key);
                lo.setBackgroundDrawable(v.getResources().getDrawable(
                        value == null ?
                                R.drawable.border_profile_thumb_sel_half :
                                R.drawable.border_profile_thumb_nosel_half
                ));
                if (value == null) {
                    hm.put(key, 1);
                } else
                    hm.remove(key);

                // we need to update tv_share
                String listProfiles = "";
                Set<Map.Entry<String, Integer>> entries = hm.entrySet();
                for (Map.Entry<String, Integer> entry : entries) {
                    listProfiles += entry.getKey() + ",";
                }


                TextView tvInfo = (TextView) findViewById(R.id.tv_share);
                if (listProfiles.length() > 1)
                    listProfiles = listProfiles.substring(0, listProfiles.length() - 1);

                //tvInfo.setText(listProfiles);

            }
        });

    }

    public void addProfiles1(int i) {
        UserProfile userProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = userProfile.getName();

        LinearLayout container = (LinearLayout) findViewById(R.id.ps_share_lo);
        final View addView = getLayoutInflater().inflate(R.layout.profile_thumb, container, false);
        container.addView(addView);

        // change the name of the text
        TextView tv = (TextView) addView.findViewById(R.id.pt_name);
        tv.setText(title);

        ImageView ivPhoto = (ImageView) addView.findViewById(R.id.pt_profile_image);
        Bitmap bm = SingletonLoginData.getInstance().getBitmap(i);
        if (bm != null) {
            ivPhoto.setImageBitmap(bm);
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            if (userProfile.getPathToImage() != null) {
                new DownloadProfileImageTask(ivPhoto).execute(userProfile.getPathToImage(), Integer.toString(i));
            } else {
                ivPhoto.setImageResource(R.drawable.silhouette);
            }
        }


        final RelativeLayout lo = (RelativeLayout) addView.findViewById(R.id.pt_all);
        lo.setOnClickListener(new View.OnClickListener() {
                                  @SuppressLint("NewApi")
                                  @Override
                                  public void onClick(View v) {
                                      TextView tv = (TextView) v.findViewById(R.id.pt_name);
                                      String key = tv.getText().toString();
                                      Integer value = hm.get(key);
                                      lo.setBackgroundDrawable(v.getResources().getDrawable(
                                              value == null ?
                                                      R.drawable.border_profile_thumb_sel_half :
                                                      R.drawable.border_profile_thumb_nosel_half
                                      ));
                                      if (value == null) {
                                          hm.put(key, 1);
                                      } else
                                          hm.remove(key);

                                      // we need to update tv_share
                                      String listProfiles = "";
                                      Set<Map.Entry<String, Integer>> entries = hm.entrySet();
                                      for (Map.Entry<String, Integer> entry : entries) {
                                          listProfiles += entry.getKey() + ",";
                                      }
                                  }
                              }
        );
    }
}
