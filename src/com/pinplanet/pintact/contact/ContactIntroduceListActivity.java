package com.pinplanet.pintact.contact;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.MyActivity;


public class ContactIntroduceListActivity extends MyActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_search_view);

    hideRight();

    showTitle(R.string.ab_pintroduce);

    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);

    Fragment fragment = new FragmentPintactsList(AppService.initContactList(false), true, null,
        PintactActionType.PINTRODUCE,
        EmptyViewType.EMPTY);
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    fragmentManager.executePendingTransactions();

    showSearch(R.string.ab_pintroduce,true);
  }

}
