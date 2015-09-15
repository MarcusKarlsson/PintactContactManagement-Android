package com.pinplanet.pintact.label;


import java.util.ArrayList;
import java.util.List;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;
import com.pinplanet.pintact.utility.IntentUtil;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentLabelActions extends DialogFragment {
  
  private List<ContactDTO> labelContactList;
  
  public FragmentLabelActions() {}
  
  public FragmentLabelActions(List<ContactDTO> labelContactList) {
    this.labelContactList = labelContactList;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    getDialog().setTitle(R.string.label_actions_dialog_title);
    
    View v = inflater.inflate(R.layout.label_action_selector, container, false);
    
    v.findViewById(R.id.label_email).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        List<String> emailAddressList = new ArrayList<String>();
        for (ContactDTO contact : labelContactList) {
          ContactDTO fullContact = ContactTableDbInterface.getInstance().getContact(contact.getUserId());
          ProfileDTO profile = AppService.getMergedProfile(fullContact);
          for (UserProfileAttribute profileAttribute : profile.getUserProfileAttributes()) {
            if (profileAttribute.getType() == AttributeType.EMAIL) {
              if (profileAttribute.getValue() != null) {
                emailAddressList.add(profileAttribute.getValue());
                break;
              }
            }
          }
        }

        IntentUtil.sendEmail(FragmentLabelActions.this,
            emailAddressList.toArray(new String[emailAddressList.size()]), null, null);
      }
    });
    
    v.findViewById(R.id.label_sms).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        List<String> numberList = new ArrayList<String>();
        for (ContactDTO contact : labelContactList) {
          ContactDTO fullContact = ContactTableDbInterface.getInstance().getContact(contact.getUserId());
          ProfileDTO profile = AppService.getMergedProfile(fullContact);
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
        
        IntentUtil.sendSms(FragmentLabelActions.this, numberList, null);
      }
    });

    return v;
  }
  
}
