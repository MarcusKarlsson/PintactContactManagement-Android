package com.pinplanet.pintact.label;


import java.util.Locale;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.EmptyViewType;
import com.pinplanet.pintact.contact.FragmentPintactsList;
import com.pinplanet.pintact.contact.PintactActionType;
import com.pinplanet.pintact.contact.PintactProfileActivity;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LabelContactsActivity extends MyActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.label_list_view);
	  hideRight();
    showRightImage(R.drawable.three_dots);
    addRightImageClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new FragmentLabelActions(
            SingletonLoginData.getInstance().getLabelContactMap().get(SingletonLoginData.getInstance().getCurrentLabel()))
        .show(LabelContactsActivity.this.getFragmentManager(), "labelAction");  
      }
    });
	  
	  showTitle(SingletonLoginData.getInstance().getCurrentLabel().toUpperCase(Locale.US));
	  
	  showLeftImage(R.drawable.actionbar_left_arrow);
	  View.OnClickListener backLn = new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      finish();
	    }
	  };
	  addLeftClickListener(backLn);
	  
	  Fragment fragment = new FragmentPintactsList(SingletonLoginData.getInstance().getLabelContactMap()
	      .get(SingletonLoginData.getInstance().getCurrentLabel()), false, null,
	      PintactActionType.VIEW_PROFILE,
	      EmptyViewType.EMPTY);
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    fragmentManager.executePendingTransactions();
	}

	public void onProfileView(int i) {
	  Intent myIntent = PintactProfileActivity.getInstanceForContactView(this);
    startActivity(myIntent);
	}

}
