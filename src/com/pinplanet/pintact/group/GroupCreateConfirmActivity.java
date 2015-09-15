package com.pinplanet.pintact.group;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.FragmentNativeCommunication;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

public class GroupCreateConfirmActivity extends MyActivity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_create_confirm);
    
    showTitle(R.string.ab_group_created);
    hideRight();
    
    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent it = new Intent(GroupCreateConfirmActivity.this, LeftDeckActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(it);
      }
    };
    addLeftClickListener(backLn);
    
    TextView pinTextView = (TextView)findViewById(R.id.group_create_confirm_pin);
    pinTextView.setText(SingletonLoginData.getInstance().getCurGroup().getGroupPin());
    pinTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String dateString = SingletonLoginData.getInstance().getCurGroup().getExpiredTime() != null ?
            SimpleDateFormat.getDateInstance()
            .format(new Date(Long.parseLong(SingletonLoginData.getInstance().getCurGroup().getExpiredTime())))
            : "N/A";
        new FragmentNativeCommunication(R.string.group_share_dialog_title,
            getResources().getString(R.string.group_share_sms_body,
                SingletonLoginData.getInstance().getCurGroup().getGroupPin(),
                SingletonLoginData.getInstance().getCurGroup().getGroupName(),
                dateString),
            getResources().getString(R.string.group_share_email_subject),
            getResources().getString(R.string.group_share_email_body,
                SingletonLoginData.getInstance().getCurGroup().getGroupPin(),
                SingletonLoginData.getInstance().getCurGroup().getGroupName(),
                dateString), null)
        .show(GroupCreateConfirmActivity.this.getFragmentManager(), "group_share");
      }
    });
  }
  
}
