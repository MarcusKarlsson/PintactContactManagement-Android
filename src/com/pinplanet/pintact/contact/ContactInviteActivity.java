package com.pinplanet.pintact.contact;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactInviteActivity extends MyActivity {

  public static final String ARG_INVITE_ACTIVITY = "invite_activity_view";

  int mArgInt = 0;

  ListView lvContact;
  Cursor people;
  ContactInviteLVAdapter adapter;
  boolean isSelectAll = false;
  List<InviteContactDTO> contactDTOs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_invite_main);

    TextView tv = (TextView)findViewById(R.id.actionBar);
    tv.setText(getResources().getString(R.string.ab_invite_all));
    this.showRightText(R.string.im_invite);

    if ( getIntent().getExtras() != null )
      mArgInt = getIntent().getExtras().getInt(ARG_INVITE_ACTIVITY);

    if ( mArgInt == 0) {
      this.showLeftText(R.string.im_skip);
      this.hideLeft();
      addLeftTextClickListener(new View.OnClickListener(){

        @Override
        public void onClick(View v) {
          Intent it = new Intent(ContactInviteActivity.this, LeftDeckActivity.class);
          startActivity(it);
        }

      });
    }
    else {
      showLeftImage(R.drawable.actionbar_left_arrow);
      View.OnClickListener backLn = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          finish();
        }
      };
      addLeftClickListener(backLn);
    }

    View.OnClickListener finClkLn = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        inviteContactList(v);
      }
    };

    View.OnClickListener addClkLn = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showContactList(v);
      }
    };

    addRightTextClickListener(finClkLn);

    // set invite all button
    ImageView ivAll = (ImageView) findViewById(R.id.im_title);
    ivAll.setOnClickListener(addClkLn);

    // get list view
    lvContact = (ListView) findViewById(R.id.im_list);
    new LoadEmailContactAsyncTask(this).execute();
  }

    public class LoadEmailContactAsyncTask extends AsyncTask {

        private ProgressDialog dialog;
        private MyActivity activity;

        public LoadEmailContactAsyncTask()
        {
        }

        public LoadEmailContactAsyncTask(MyActivity activity)
        {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            if(this.activity != null) {
                dialog = new ProgressDialog(this.activity);
                dialog.setMessage(AppController.getInstance().getString(R.string.DIALOG_MESSAGE_PLEASE_WAIT));
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            // Create the Adapter and set
            adapter = new ContactInviteLVAdapter(ContactInviteActivity.this, contactDTOs);
            lvContact.setAdapter(adapter);

            // to handle click event on listView item
            lvContact.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3) {
                    // when user clicks on ListView Item , onItemClick is called
                    // with position and View of the item which is clicked
                    // we can use the position parameter to get index of clicked item

                    ImageView ivSelect = (ImageView) v.findViewById(R.id.im_check);
                    if (lvContact.isItemChecked(position)) {
                        ivSelect.setImageResource(R.drawable.circle_check_orange);
                    } else {
                        ivSelect.setImageResource(R.drawable.circle);
                    }

                }

            });
            if(this.activity != null && dialog != null && dialog.isShowing()&& this.activity.isActive()) {
                dialog.dismiss();
            }

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            contactDTOs = getContactEmails(ContactInviteActivity.this.getApplicationContext());
            return null;
        }
    }

  public List<InviteContactDTO> getContactEmails(Context context) {

    Set<String> alreadyInvitedEmails = ContactTableDbInterface.getInstance().getAllInvitedContacts();
    Set<String> pintactNativeIds = ContactTableDbInterface.getInstance().getAllPintactNativeContacts();
    Set<String> pintactNativeContactIds = new HashSet<String>();
    for(String rawId : pintactNativeIds) {
      pintactNativeContactIds.add(AppService.getContactIdOfRawContactid(rawId));
    }

    ContentResolver cr = context.getContentResolver();


      Cursor cur = this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {
              ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.DATA,ContactsContract.CommonDataKinds.Email.LABEL,
              ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
              ContactsContract.CommonDataKinds.Phone.LABEL, ContactsContract.CommonDataKinds.Photo.PHOTO_URI
              , ContactsContract.CommonDataKinds.Note.NOTE, ContactsContract.CommonDataKinds.Im.DATA,
              ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, ContactsContract.Data.IN_VISIBLE_GROUP}, null, null, ContactsContract.Contacts.DISPLAY_NAME);

      InviteContactDTO contact;

      HashMap<String, InviteContactDTO> allContacts = new HashMap<String, InviteContactDTO>();

      //Log.i(TAG,"loadLocalContacts() count:"+cur.getCount());

      if (cur.getCount() > 0) {

          while (cur.moveToNext()) {

              String id = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
              String groupName = cur.getString(cur.getColumnIndex(ContactsContract.Data.IN_VISIBLE_GROUP));
              if (!pintactNativeContactIds.contains(id) && groupName.equals("1")) {
                  String mimeType = cur.getString(cur.getColumnIndex(ContactsContract.Data.MIMETYPE));
                  if (allContacts.containsKey(id)) {
                      // update contact
                      contact = allContacts.get(id);
                  } else {
                      contact = new InviteContactDTO();
                      allContacts.put(id, contact);
                  }

                  if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                      String dName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                      if(dName == null)
                          dName = "";
                      int pos = dName.lastIndexOf(' ');
                      if (pos == -1) {
                          contact.name = dName;
                      } else {
                          String fn = dName.substring(0, pos);
                          String ln = "";
                          // in case the last name was followed by an extra ' ';
                          if (pos != dName.length() - 1)
                              ln = dName.substring(pos + 1, dName.length());
                          contact.name = fn+" "+ln;
                      }
                  }  else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                      // set email
                      contact.email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                      if (alreadyInvitedEmails.contains(contact.email)) {
                          contact.alreadyInvited = true;
                      }
                  }

              }
          }
      }
      List<InviteContactDTO> list = new ArrayList<InviteContactDTO>(allContacts.values());
      Iterator<InviteContactDTO> it = list.iterator();
      while (it.hasNext()) {
            InviteContactDTO _contact = it.next();
          if (_contact.name == null || _contact.email == null || _contact.email.length() == 0 ) {
              it.remove();
          }

      }

      cur.close();


    return list;
  }

  public static class InviteContactDTO {
    public String name;
    public String email;
    public String phone;
    public transient boolean alreadyInvited = false;
  }

  public void onLogin(View view) {
    Intent it = new Intent(this, LeftDeckActivity.class);

    startActivity(it);
  }		

  public void showContactList(View v) {
    isSelectAll = !isSelectAll;
    ImageView tmp = (ImageView) v;
    tmp.setImageResource(isSelectAll ? R.drawable.circle_check_orange : R.drawable.circle);
    for ( int i=0; i< adapter.getCount(); i++ ) {
      lvContact.setItemChecked(i, isSelectAll);
    }
    TextView selectAllLabel = (TextView)findViewById(R.id.im_select_all_label);
    selectAllLabel.setText(isSelectAll ? R.string.im_none : R.string.im_all);
  }

  public List<InviteContactDTO> getCheckedItems() {
    List<InviteContactDTO> selectedContacts = new ArrayList<InviteContactDTO>();
    for ( int i=0; i< adapter.getCount(); i++ ) {
      if(lvContact.isItemChecked(i))
      {
        selectedContacts.add(contactDTOs.get(i));
      }
    }
    return selectedContacts;
  }

  public void inviteContactList(View v) {
    List<InviteContactDTO> selectedContacts = getCheckedItems();
    if(selectedContacts.size() > 0) {
      SingletonNetworkStatus.getInstance().setActivity(this);
      Map<String, List<InviteContactDTO>> data = new HashMap<String, List<InviteContactDTO>>();
      data.put("data", selectedContacts);
      String json = new Gson().toJson(data);
      String path = "/api/users/invite.json?" + SingletonLoginData.getInstance().getPostParam();
      new HttpConnection().access(this, path, json, "POST");
    }

  }

  public void onPostNetwork () {
    if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
      myDialog(SingletonNetworkStatus.getInstance().getMsg(),
          SingletonNetworkStatus.getInstance().getErrMsg());
      finish();
      return;
    }else {
      List<InviteContactDTO> selectedContacts = getCheckedItems();
      List<String>emailsInvited = new ArrayList<String>();
      for(InviteContactDTO inviteContactDTO : selectedContacts)
      {
        emailsInvited.add(inviteContactDTO.email);
      }
      ContactTableDbInterface.getInstance().addEmailsToInvitedList(emailsInvited);

      if ( mArgInt == 0){
        Intent it = new Intent(this, LeftDeckActivity.class);
        startActivity(it);
      }
      finish();
    }
  }

}
