package com.pinplanet.pintact.contact;


import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;


public class ContactIntroduceActivity extends MyActivity {
	
		String mFirstName1, mFirstName2, mFirstName3;
		EditText mETMsg;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.contact_introduce);
			
			showTitle(R.string.ab_pintroduce_message);

			mFirstName1 = "";
			if (SingletonLoginData.getInstance().getIntroducedProfile().getUserProfile() != null) {
			  mFirstName1 = SingletonLoginData.getInstance().getIntroducedProfile().getUserProfile().getFirstName();
			} else {
			  List<ContactDTO> contactList = SingletonLoginData.getInstance().getContactList();
			  for (ContactDTO contact : contactList) {
			    if (!contact.isLocalContact
			        && contact.getUserId().equals(SingletonLoginData.getInstance().getIntroducedProfile().getUserId())) {
			      mFirstName1 = contact.getFirstName();
			      break;
			    }
			  }
			}
			mFirstName2 = SingletonLoginData.getInstance().getMergedProfile().getUserProfile().getFirstName();
			mFirstName3 = SingletonLoginData.getInstance().getUserData().firstName;
			
			mETMsg = (EditText)findViewById(R.id.gps_msg_cont);
			mETMsg.setText(getResources().getString(R.string.pintroduction_message, mFirstName1, mFirstName2, mFirstName3));
			
			hideRight();
			
			showLeftImage(R.drawable.actionbar_left_arrow);
			View.OnClickListener backLn = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			};
			addLeftClickListener(backLn);
			
			View.OnClickListener sendLn = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onSend(v);
					ContactIntroduceActivity.this.hideSoftKeyboard(ContactIntroduceActivity.this);
				}
			};
			
			TextView tvSend = (TextView)findViewById(R.id.gm_preview);
			tvSend.setOnClickListener(sendLn);
		}
		
		public void onSend(View view) {
			// Json: userId1: mFirstName2, userId2: mFirstName1, message:
			// /api/users/introduce.json + params
			introRequest ir = new introRequest();
			ir.userId1 = SingletonLoginData.getInstance().getMergedProfile().getUserId();
			ir.userId2 = SingletonLoginData.getInstance().getIntroducedProfile().getUserId();
			ir.message = mETMsg.getText().toString();
			
			Gson gson = new GsonBuilder().create();
			String params = gson.toJson(ir);

			SingletonNetworkStatus.getInstance().setActivity(this);
			String path = "/api/users/introduce.json?" + SingletonLoginData.getInstance().getPostParam();
			new HttpConnection().access(this, path, params, "POST");
			
		}
		
		public void onPostNetwork () {
			if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
				myDialog(SingletonNetworkStatus.getInstance().getMsg(), 
						SingletonNetworkStatus.getInstance().getErrMsg());
				
				return;
			}
			
			Intent it = new Intent(ContactIntroduceActivity.this, LeftDeckActivity.class);
			it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(it);
		}
		
		public class introRequest {
			public Long userId1;
			public Long userId2;
			public String message;
		}
		
}
