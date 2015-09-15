package com.pinplanet.pintact.contact;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.EventType;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.EditTextTypeFace;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.MyFragment;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dennis on 15.01.2015.
 */
public class FragmentConnectAddContactManually extends MyFragment {

    //TODO: implement all needed click listeners, some code can be used from other layouts (re-used IDs)

    private static final String TAG = FragmentConnectAddContactManually.class.getName();

    private LinearLayout linearLayoutShareProfileItems;
    private EditTextTypeFace notesText;
    private TextView textViewShareSelected;

    private EditTextTypeFace nameText, emailText, mobileText, countryText;


    public FragmentConnectAddContactManually() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_connect_add_contact_manually, container, false);

        getMyActivity().showTitle(R.string.TITLE_ADD_CONTACT_MANUALLY);

        linearLayoutShareProfileItems = (LinearLayout) v.findViewById(R.id.share_layout);

        notesText = (EditTextTypeFace)v.findViewById(R.id.contactMessageEntry);
        textViewShareSelected = (TextView)v.findViewById(R.id.tv_share);

        updateSharedProfileIcons();

        v.findViewById(R.id.add_contact_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAcceptInvite();
            }
        });

        v.findViewById(R.id.tv_preview_share_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Long ids[] = getSharedProfileIds();
                if (ids.length == 0) {
                    ((MyActivity) getActivity()).myDialog(getString(R.string.gm_no_profile_title), getString(R.string.gm_no_profile_detail));
                    return;
                }

                UiControllerUtil.openPreviewShareActivity(ids);
                Intent myIntent = PintactProfileActivity.getInstanceForShareView(getActivity());
                startActivity(myIntent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        nameText = (EditTextTypeFace)v.findViewById(R.id.editTextFirstName);
        emailText = (EditTextTypeFace)v.findViewById(R.id.editTextEmail);
        mobileText = (EditTextTypeFace)v.findViewById(R.id.editTextMobile);
        countryText = (EditTextTypeFace)v.findViewById(R.id.editTextCountryCode);
        countryText.setText(UiControllerUtil.getCountryZipCode(this.getActivity()));

        return v;
    }

    public void updateSharedProfileIcons() {

        try {
            // show shared profiles
            for (int i = 0; i < SingletonLoginData.getInstance().getUserProfiles().size(); i++) {
                addProfiles(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    //######### code taken from the old activity ################
    boolean isAccepted=false;

    public void onAcceptInvite() {
        ContactShareRequest req = new ContactShareRequest();
        UserDTO destinationUserInfo = new UserDTO();
        String email = emailText.getText().toString();
        String name = nameText.getText().toString();
        String mobile = mobileText.getText().toString();
        String countryCode = countryText.getText().toString();

        if ( email.length() == 0 && mobile.length() == 0) {
            myDialog("Insufficient data.", "Please provide either email or mobile.");
            return;
        }
        if ( name.length() == 0) {
            myDialog("Insufficient data.", "Please provide name.");
            return;
        }

        if(mobile.length() > 0 && (TextUtils.isEmpty(countryCode) || !countryCode.startsWith("+") ||  countryCode.length()  <2))
        {
            myDialog("Insufficient data.", "Please provide correct country code.");
            return;
        }

        destinationUserInfo.setFirstName(name);
        destinationUserInfo.setLastName("");
        if(email != null && email.length() > 0) {
            destinationUserInfo.setEmailId(email);
        }

        if(mobile != null && mobile.length() > 0) {
            destinationUserInfo.setMobileNumber(countryCode + mobile);
        }

        req.setDestinationUserInfo(destinationUserInfo);
        req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        SingletonLoginData.getInstance().setContactShareRequest(req);

        onShareProfile();
    }

    LinkedHashMap<String, Integer> hm = new LinkedHashMap<String, Integer>();

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

    public void addProfiles(int i) {
        UserProfile currentProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = currentProfile.getName();

        final View addView = getActivity().getLayoutInflater().inflate(R.layout.profile_thumb_half, null);
        linearLayoutShareProfileItems.addView(addView);

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
                selectProfile(lo, key);
            }
        });

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


        if (listProfiles.length() > 1)
            listProfiles = listProfiles.substring(0, listProfiles.length() - 1);

        textViewShareSelected.setText(listProfiles);
    }

    boolean isAcceptingInvite = false;

    public void onShareProfile() {
        // set shared profile
        String notes =  notesText.getText().toString();
        Long profId[] = getSharedProfileIds();


        if ( profId.length == 0 ) {
            myDialog("No Profile Selected", "Please select at least one profile to share");
            return;
        }

        SingletonLoginData.getInstance().getContactShareRequest().setNote(notes);
        SingletonLoginData.getInstance().getContactShareRequest().setUserProfileIdsShared(profId);

        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(SingletonLoginData.getInstance().getContactShareRequest());

        SingletonNetworkStatus.getInstance().clean();
        SingletonNetworkStatus.getInstance().setActivity(getActivity());
        SingletonNetworkStatus.getInstance().setFragment(this);

        String path = "/api/contacts/addManual.json?" + SingletonLoginData.getInstance().getPostParam();

        isAcceptingInvite = true;
        new HttpConnection().access(this.getActivity(), path, params, "POST");

    }


    public void onPostNetwork () {

        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            getActivity().onBackPressed();
            return;
        }

        AppService.handleUpdateContactResponse();

        if(getActivity()!=null)
            getActivity().onBackPressed();

    }

    public MyActivity getMyActivity()
    {
        return (MyActivity)getActivity();
    }
}

