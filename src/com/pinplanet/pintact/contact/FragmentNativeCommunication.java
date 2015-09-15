package com.pinplanet.pintact.contact;


import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.utility.IntentUtil;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentNativeCommunication extends DialogFragment {
  
  private int titleRes;
  private String smsMessage;
  private String emailSubject;
  private CharSequence emailBody;
  private ContactDTO contactDTO;
  
  public FragmentNativeCommunication() {}
  
  public FragmentNativeCommunication(int titleRes, String smsMessage,
      String emailSubject, CharSequence emailBody, ContactDTO contactDTO) {
    this.titleRes = titleRes;
    this.smsMessage = smsMessage;
    this.emailSubject = emailSubject;
    this.emailBody = emailBody;
    this.contactDTO = contactDTO;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.native_comminication_selector, container, false);
    
    String phoneNumber = null;
    String email = null;
    if (contactDTO != null && contactDTO.getSharedProfiles() != null && contactDTO.getSharedProfiles().size() > 0
        && contactDTO.getSharedProfiles().get(0).getUserProfileAttributes() != null) {
      for (UserProfileAttribute userProfileAttribute : contactDTO.getSharedProfiles().get(0).getUserProfileAttributes()) {
        if (phoneNumber == null && userProfileAttribute.getType()== AttributeType.PHONE_NUMBER) {
          phoneNumber = userProfileAttribute.getValue();
        } else if (email == null && userProfileAttribute.getType()== AttributeType.EMAIL) {
          email = userProfileAttribute.getValue();
        }
        if (phoneNumber != null && email != null) {
          break;
        }
      }
    }

    Log.d(FragmentNativeCommunication.class.getName(), " email:" + email);

    getDialog().setTitle(titleRes);
    
    // SMS
    View smsButton = v.findViewById(R.id.native_communication_sms);
    if (contactDTO != null && (phoneNumber == null || phoneNumber.length() == 0)) {
      smsButton.setVisibility(View.GONE);
    } else {
      final String finalPhoneNumber = phoneNumber;
      smsButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          IntentUtil.sendSms(FragmentNativeCommunication.this, finalPhoneNumber, smsMessage);
        }
      });
    }
    
    // e-mail
    View emailButton = v.findViewById(R.id.native_communication_email);
    if (contactDTO != null && (email == null || email.length() == 0)) {
      emailButton.setVisibility(View.GONE);
    } else {
      final String finalEmail = email;
      emailButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          IntentUtil.sendEmail(FragmentNativeCommunication.this, new String[]{finalEmail},
              emailSubject, emailBody.toString());
        }
      });
    }
    
    // error dialog if no phone or email
    if (smsButton.getVisibility() == View.GONE && emailButton.getVisibility() == View.GONE) {
      getDialog().setTitle(R.string.no_comm_available_error_title);
      v.findViewById(R.id.native_communication_error).setVisibility(View.VISIBLE);
    } else {
      v.findViewById(R.id.native_communication_error).setVisibility(View.GONE);
    }

    return v;
  }
  
}
