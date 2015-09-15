package com.pinplanet.pintact.contact;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.SearchDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.UiControllerUtil;

public class ContactFindActivity extends MyActivity {

  ListView lvContact;
  Cursor people;
  PageDTO<SearchDTO> suggests;
  boolean isSelectAll = false;
  FragmentPintactsList fragment;
  private Long contactId = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);


    setContentView(R.layout.contact_search_view);

    hideBoth();

    View.OnClickListener finClkLn = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    };

    //showRightImage(R.drawable.actionbar_x);
    //addRightImageClickListener(finClkLn);

    fragment = new FragmentPintactsList(new ArrayList<SearchDTO>(), false,
        ActionButtonType.ADD,
        PintactActionType.VIEW_PROFILE,
        EmptyViewType.EMPTY);
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    fragmentManager.executePendingTransactions();
    
    // set search behavior
    final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() { 
      @Override 
      public boolean onQueryTextChange(String newText) {
        onKeyInput(newText);
        return true; 
      } 

      @Override 
      public boolean onQueryTextSubmit(String query) {
        System.out.println("Query Text is " + query);
        return true; 
      } 
    };
    setSearchTextQueryListener(queryTextListener);

    showSearch(R.string.ADD_CONTACT,true);
  }

  public Long getContactId() {
    return contactId;
  }

  public void setContactId(Long contactId) {
    this.contactId = contactId;
  }

  public void onKeyInput(String str) 
  {
    String path = null;
    try {
      path = "/api/profiles/suggest.json?" + SingletonLoginData.getInstance().getPostParam() +
          "&query=" + URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      // this really shouldn't happen; choke if it does
      throw new RuntimeException(uee);
    }

      RestServiceAsync rs=new RestServiceAsync(new PostServiceExecuteTask() {
      @Override
      public void run(int statusCode, String result) {
        if(statusCode == 200) {
          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());
          if (getContactId() != null) {
            AppService.handleGetContactResponse();
            ContactDTO contactDTO = null;
            for (ContactDTO contact : SingletonLoginData.getInstance().getContactList()) {
              if (!contact.isLocalContact && getContactId().equals(contact.getUserId())) {
                contactDTO = contact;
                break;
              }
            }
            setContactId(null);
            UiControllerUtil.openContactDetail(contactDTO);

            Runnable myRunnable = new Runnable(){
              public void run() {
                Intent myIntent = PintactProfileActivity.getInstanceForContactView(ContactFindActivity.this);
                startActivity(myIntent);
                ContactFindActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
              }};
            mainHandler.post(myRunnable);
            return;
          }

          Type collectionType = new TypeToken<PageDTO<SearchDTO>>(){}.getType();
          Gson gson = new GsonBuilder().create();
          suggests = gson.fromJson(result, collectionType);

            mainHandler.post(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(false);
                }
            });


          if(suggests != null) {
            // search result
            // list view operation
            Runnable myRunnable = new Runnable() {
              public void run() {
                fragment.updateListContents(suggests.getData());
                System.out.println("Received1");
              }
            };
            mainHandler.post(myRunnable);
          }
        }
      }
    });


      setProgressBarIndeterminateVisibility(true);
    rs.execute(path, "", "GET");

  }

}
