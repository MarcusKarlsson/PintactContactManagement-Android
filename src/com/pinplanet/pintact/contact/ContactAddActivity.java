package com.pinplanet.pintact.contact;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.MyActivity;

public class ContactAddActivity extends MyActivity {

  FragmentConnectContact fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_contact);

    hideBoth();

//    View.OnClickListener finClkLn = new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        finish();
//      }
//    };
    //showRightImage(R.drawable.actionbar_x);
    //addRightImageClickListener(finClkLn);

    fragment = new FragmentConnectContact();
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    fragmentManager.executePendingTransactions();

  }

}
