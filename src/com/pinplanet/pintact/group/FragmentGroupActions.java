package com.pinplanet.pintact.group;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.IntentUtil;
import com.pinplanet.pintact.utility.SingletonLoginData;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentGroupActions extends DialogFragment {

  // request codes
  private static final int GROUP_PIN_ACTIVITY_REQUEST_CODE = 1;
  
  private GroupDTO groupDTO;
  private List<ContactDTO> groupContactList;
  
  public FragmentGroupActions() {}
  
  public FragmentGroupActions(GroupDTO groupDTO, List<ContactDTO> groupContactList) {
    this.groupDTO = groupDTO;
    this.groupContactList = groupContactList;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    getDialog().setTitle(R.string.group_actions_dialog_title);
    
    View v = inflater.inflate(R.layout.group_action_selector, container, false);
    
    final String dateString = groupDTO.getExpiredTime() != null ?
        SimpleDateFormat.getDateInstance()
        .format(new Date(Long.parseLong(groupDTO.getExpiredTime())))
        : "N/A";
    
    // check whether this group is created by the user
    boolean isModerator = (groupDTO.getModerated() == null || ! groupDTO.getModerated())
        ? SingletonLoginData.getInstance().getUserData().id.toString().equals(groupDTO.getCreatedBy())
            : groupDTO.isModerator();
    View groupEditButton = v.findViewById(R.id.group_edit);
    if ( isModerator ) {
      groupEditButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent myIntent = new Intent(v.getContext(), GroupPinActivity.class);
          startActivityForResult(myIntent, GROUP_PIN_ACTIVITY_REQUEST_CODE); 
        }
      });
    } else {
      groupEditButton.setVisibility(View.GONE);
    }
    
    v.findViewById(R.id.group_sms).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        List<String> numberList = new ArrayList<String>();
        for (ContactDTO contact : groupContactList) {
          ProfileDTO profile = AppService.getMergedProfile(contact);
          for (UserProfileAttribute profileAttribute : profile.getUserProfileAttributes()) {
            if (profileAttribute.getType() == AttributeType.PHONE_NUMBER
                && profileAttribute.getLabel() != null
                && profileAttribute.getLabel().matches(".*(?ui:mobile|iphone|cell).*")) {
              if (profileAttribute.getValue() != null) {
                numberList.add(Uri.encode(profileAttribute.getValue()));
                break;
              }
            }
          }
        }
        
        IntentUtil.sendSms(FragmentGroupActions.this, numberList, null);
      }
    });
    
    if (isModerator) {
      v.findViewById(R.id.group_broadcast).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          new FragmentGroupBroadcast(SingletonLoginData.getInstance().getCurGroup(), FragmentGroupActions.this)
          .show(FragmentGroupActions.this.getFragmentManager(), "groupBroadcast");
        }
      });
    } else {
      v.findViewById(R.id.group_broadcast).setVisibility(View.GONE);
    }
    
    v.findViewById(R.id.group_share_email).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String emailSubject = getResources().getString(R.string.group_share_email_subject);
        String emailBody = getResources().getString(R.string.group_share_email_body,
            groupDTO.getGroupPin(), groupDTO.getGroupName(),
            dateString);
        IntentUtil.sendEmail(FragmentGroupActions.this, null, emailSubject, emailBody);
      }
    });
    
    v.findViewById(R.id.group_share_sms).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String smsMessage = getResources().getString(R.string.group_share_sms_body,
            groupDTO.getGroupPin(), groupDTO.getGroupName(),
            dateString);
        IntentUtil.sendSms(FragmentGroupActions.this, (String)null, smsMessage);
      }
    });

    return v;
  }
  
}
