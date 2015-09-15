package com.pinplanet.pintact.group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.DateInterval;
import com.pinplanet.pintact.utility.EditTextTypeFace;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateGroupActivity extends MyActivity {

    private EditText groupNameEditText;
    private TextView automaticTV, manualTV, yesTV, noTV, createGroupButton;
    private ImageView automaticImageView, manualImageView, yesImageView, noImageView, automaticIcon, manualIcon, yesIcon, noIcon;
    private Spinner timeOptionsSpinner;

    private boolean connectSelected, moderateSelected;
    private boolean autoSelectBool, manualSelectBool, yesSelectBool, noSelectBool;

    HashMap<String, Integer> hm = new HashMap<String, Integer>();
    int mArgInt = -1; // -1 : from group pin; 0 : from add contact; >0: from notification accept


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        showTitle("ADD GROUP");
        hideRight();

        groupNameEditText = (EditText) findViewById(R.id.groupNameEditText);

        automaticTV = (TextView) findViewById(R.id.automaticTV);
        manualTV = (TextView) findViewById(R.id.manualTV);
        yesTV = (TextView) findViewById(R.id.moderateYesTV);
        noTV = (TextView) findViewById(R.id.moderateNoTV);
        createGroupButton = (TextView) findViewById(R.id.createGroupButton);

        automaticImageView = (ImageView) findViewById(R.id.automaticCircle);
        automaticIcon = (ImageView) findViewById(R.id.automaticIcon);
        manualImageView = (ImageView) findViewById(R.id.manualCircle);
        manualIcon = (ImageView) findViewById(R.id.manualIcon);
        yesImageView = (ImageView) findViewById(R.id.moderateYesCircle);
        yesIcon = (ImageView) findViewById(R.id.moderateYesIcon);
        noImageView = (ImageView) findViewById(R.id.moderateNoCircle);
        noIcon = (ImageView) findViewById(R.id.moderateNoIcon);

        timeOptionsSpinner = (Spinner) findViewById(R.id.timeOptionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.group_time_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeOptionsSpinner.setAdapter(adapter);

        for (int i = 0; i < SingletonLoginData.getInstance().getUserProfiles().size(); i++) {
            addProfiles(i);
        }

        groupNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int groupNameLength = groupNameEditText.getText().toString().length();
                if (groupNameLength > 50) {
                    groupNameEditText.setError("Value cannot exeed 50 characters");
                } else if (groupNameLength == 0) {
                    groupNameEditText.setError("Value is required");
                } else {
                    groupNameEditText.setError(null);
                }
            }
        });
    }

    public void addProfiles(int i) {
        UserProfile currentProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = currentProfile.getName();

        LinearLayout container = (LinearLayout) findViewById(R.id.ps_share_lo);
        final View addView = getLayoutInflater().inflate(R.layout.profile_thumb_half, null);
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

            }
        });

    }

    public void automaticSelected(View v) {
        automaticTV.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
        automaticImageView.setImageResource(R.drawable.circle_check_orange);
        automaticIcon.setImageResource(R.drawable.automatic_icon_highlighted);
        manualTV.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        manualImageView.setImageResource(R.drawable.circle);
        manualIcon.setImageResource(R.drawable.manual_icon);

        autoSelectBool = true;
        manualSelectBool = false;
        connectSelected = true;
        if (moderateSelected && (!groupNameEditText.getText().equals(""))) {
            Log.d(TAG, "editText:" + groupNameEditText.getText() + "here");
            createGroupButton.setBackgroundColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
            createGroupButton.setClickable(true);
        }
    }

    public void manualSelected(View v) {
        manualTV.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
        manualImageView.setImageResource(R.drawable.circle_check_orange);
        manualIcon.setImageResource(R.drawable.manual_icon_highlighted);
        automaticTV.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        automaticImageView.setImageResource(R.drawable.circle);
        automaticIcon.setImageResource(R.drawable.automatic_icon);

        manualSelectBool = true;
        autoSelectBool = false;
        connectSelected = true;
        if (moderateSelected && (!groupNameEditText.getText().equals(""))) {
            createGroupButton.setBackgroundColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
            createGroupButton.setClickable(true);
        }
    }

    public void yesSelected(View v) {
        yesTV.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
        yesImageView.setImageResource(R.drawable.circle_check_orange);
        yesIcon.setImageResource(R.drawable.moderate_yes_icon_highlighted);
        noTV.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        noImageView.setImageResource(R.drawable.circle);
        noIcon.setImageResource(R.drawable.moderate_no_icon);

        timeOptionsSpinner.setVisibility(View.GONE);

        yesSelectBool = true;
        noSelectBool = false;
        moderateSelected = true;
        if (connectSelected && (!groupNameEditText.getText().equals(""))) {
            createGroupButton.setBackgroundColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
            createGroupButton.setClickable(true);
        }
    }

    public void noSelected(View v) {
        noTV.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
        noImageView.setImageResource(R.drawable.circle_check_orange);
        noIcon.setImageResource(R.drawable.moderate_no_icon_highlighted);
        yesTV.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        yesImageView.setImageResource(R.drawable.circle);
        yesIcon.setImageResource(R.drawable.moderate_yes_icon);

        timeOptionsSpinner.setVisibility(View.VISIBLE);

        noSelectBool = true;
        yesSelectBool = false;
        moderateSelected = true;
        if (connectSelected && (!groupNameEditText.getText().equals(""))) {
            createGroupButton.setBackgroundColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
            createGroupButton.setClickable(true);
        }
    }

    public void createGroupSelected(View v) {
        Log.d(TAG, "createGroupSelected");
        if (autoSelectBool && noSelectBool)
            createUnmoderatedGroup();
        if (autoSelectBool && yesSelectBool)
            createModeratedGroup();
        if (manualSelectBool && noSelectBool)
            createUnmoderatedList();
        if (manualSelectBool && yesSelectBool)
            createModeratedList();

    }

    private void createUnmoderatedGroup() {
        Log.d(TAG, "createUnmoderatedGroup");

        // do validation on exp date
        long seconds = System.currentTimeMillis();
        long expTime = seconds;
        int daysToMillis = 24 * 60 * 60 * 1000;
        int timeOptionSelected = timeOptionsSpinner.getSelectedItemPosition();
        switch (timeOptionSelected) {
            case 0:
                expTime += 1 * daysToMillis;
                break;
            case 1:
                expTime += 2 * daysToMillis;
                break;
            case 2:
                expTime += 3 * daysToMillis;
                break;
            case 3:
                expTime += 7 * daysToMillis;
                break;
            case 4:
                expTime += 2 * 7 * daysToMillis;
                break;
            case 5:
                expTime += 3 * 7 * daysToMillis;
                break;
            case 6:
                expTime += 30 * daysToMillis;
                break;
            default:
                expTime += 30 * daysToMillis;
        }
        // validate group name
        System.out.println("cur : " + seconds + " == exp : " + expTime);

        String groupName = groupNameEditText.getText().toString();

        if (groupName == null || groupName.length() < 1) {
            groupNameEditText.setError("Value is required");
        }
        if (groupNameEditText.getError() != null) {
            myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
            return;
        }

        Gson gson = new GsonBuilder().create();
        String params = null;
        String apiPath = null;

        // get profiles to share
        Long profId[] = getSharedProfileIds();

        if (profId.length == 0) {
            myDialog("No Profile Selected", "Please select at least one profile to share");
            return;
        }


        ContactShareRequest contactShareRequest = new ContactShareRequest();
        contactShareRequest.setGroupName(groupName);
        contactShareRequest.setExpiryTimeInUTC(Long.toString(expTime));
        contactShareRequest.setUserProfileIdsShared(profId);
        contactShareRequest.setPurpose("GROUP");
        contactShareRequest.setGroupVisibility("MEMBERS");
        params = gson.toJson(contactShareRequest);
        apiPath = "/api/groupPins";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
    }

    private void createModeratedGroup() {
        Log.d(TAG, "createModeratedGroup");

        // validate group name
        String groupName = groupNameEditText.getText().toString();

        if (groupName == null || groupName.length() < 1) {
            groupNameEditText.setError("Value is required");
        }
        if (groupNameEditText.getError() != null) {
            myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
            return;
        }

        Gson gson = new GsonBuilder().create();
        String params = null;
        String apiPath = null;

        // get profiles to share
        Long profId[] = getSharedProfileIds();

        if (profId.length == 0) {
            myDialog("No Profile Selected", "Please select at least one profile to share");
            return;
        }

        Long[] moderators = {SingletonLoginData.getInstance().getUserData().getId()};

        ContactShareRequest contactShareRequest = new ContactShareRequest();
        contactShareRequest.setGroupName(groupName);
        contactShareRequest.setUserProfileIdsShared(profId);
        contactShareRequest.setPurpose("GROUP");
        contactShareRequest.setGroupVisibility("MEMBERS");
        params = gson.toJson(contactShareRequest);
        apiPath = "/api/moderatedGroupPins";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
    }

    private void createUnmoderatedList() {
        Log.d(TAG, "createUnmoderatedList");


        // do validation on exp date
        long seconds = System.currentTimeMillis();
        long expTime = seconds;
        long daysToMillis = 24 * 60 * 60 * 1000;
        int timeOptionSelected = timeOptionsSpinner.getSelectedItemPosition();
        switch (timeOptionSelected) {
            case 0:
                expTime += 1 * daysToMillis;
                break;
            case 1:
                expTime += 2 * daysToMillis;
                break;
            case 2:
                expTime += 3 * daysToMillis;
                break;
            case 3:
                expTime += 7 * daysToMillis;
                break;
            case 4:
                expTime += 2 * 7 * daysToMillis;
                break;
            case 5:
                expTime += 3 * 7 * daysToMillis;
                break;
            case 6:
                expTime += 30 * daysToMillis;
                break;
            default:
                expTime += 30 * daysToMillis;
        }
        // validate group name
        System.out.println("cur : " + seconds + " == exp : " + expTime);

        String groupName = groupNameEditText.getText().toString();

        if (groupName == null || groupName.length() < 1) {
            groupNameEditText.setError("Value is required");
        }
        if (groupNameEditText.getError() != null) {
            myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
            return;
        }

        Gson gson = new GsonBuilder().create();
        String params = null;
        String apiPath = null;

        // get profiles to share
        Long profId[] = getSharedProfileIds();

        if (profId.length == 0) {
            myDialog("No Profile Selected", "Please select at least one profile to share");
            return;
        }


        ContactShareRequest contactShareRequest = new ContactShareRequest();
        contactShareRequest.setGroupName(groupName);
        contactShareRequest.setExpiryTimeInUTC(Long.toString(expTime));
        contactShareRequest.setUserProfileIdsShared(profId);
        contactShareRequest.setPurpose("LIST");
        contactShareRequest.setGroupVisibility("MEMBERS");
        params = gson.toJson(contactShareRequest);
        apiPath = "/api/groupPins";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
    }

    private void createModeratedList() {

        // validate group name
        String groupName = groupNameEditText.getText().toString();

        if (groupName == null || groupName.length() < 1) {
            groupNameEditText.setError("Value is required");
        }
        if (groupNameEditText.getError() != null) {
            myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
            return;
        }

        Gson gson = new GsonBuilder().create();
        String params = null;
        String apiPath = null;

        // get profiles to share
        Long profId[] = getSharedProfileIds();

        if (profId.length == 0) {
            myDialog("No Profile Selected", "Please select at least one profile to share");
            return;
        }

        Long[] moderators = {SingletonLoginData.getInstance().getUserData().getId()};

        ContactShareRequest contactShareRequest = new ContactShareRequest();
        contactShareRequest.setGroupName(groupName);
        contactShareRequest.setUserProfileIdsShared(profId);
        contactShareRequest.setPurpose("LIST");
        contactShareRequest.setGroupVisibility("MEMBERS");

        params = gson.toJson(contactShareRequest);
        apiPath = "/api/moderatedGroupPins";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
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


    @SuppressLint("NewApi")
    public void onPostNetwork() {
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            Log.d(TAG, SingletonNetworkStatus.getInstance().getMsg());
            Log.d(TAG, SingletonNetworkStatus.getInstance().getErrMsg());
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
        finish();
    }
}
