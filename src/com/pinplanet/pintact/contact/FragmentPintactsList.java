package com.pinplanet.pintact.contact;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.ListableEntity;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.SearchDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;
import com.pinplanet.pintact.group.GroupProfileShareActivity;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.late.gui.ListViewAdvanced;
import de.late.widget.SimpleViewListAdapter;
import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 10.10.2014.
 */
public class FragmentPintactsList  extends Fragment  implements ContactListChangeListner{

  private static final String TAG = FragmentPintactsList.class.getName();
  ListViewAdvanced lv;
  SectionAdapter adapter;
  List<? extends ListableEntity> entities;
  private boolean enableFilter;
  private ActionButtonType actionButtonType;
  private PintactActionType pintactActionType;
  private EmptyViewType emptyViewType;
  
  public FragmentPintactsList() {}

  public FragmentPintactsList(List<? extends ListableEntity> entities, boolean enableFilter,
                              ActionButtonType actionButtonType, PintactActionType pintactActionType,
                              EmptyViewType emptyViewType) {
    this.entities = entities;
    this.enableFilter = enableFilter;
    this.actionButtonType = actionButtonType;
    this.pintactActionType = pintactActionType;
    this.emptyViewType = emptyViewType;
  }

    @Override
    public void contactListChanged() {

        if(SingletonLoginData.getInstance().getIsStatusChanged() && adapter != null) {
            SingletonLoginData.getInstance().setIsStatusChanged(false);
            entities = AppService.initContactList(false);
            adapter.setEntities(entities);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View v=inflater.inflate(R.layout.fragment_pintacts_list, container, false);

      Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

      Runnable myRunnable = new Runnable(){
          public void run(){
              lv=(ListViewAdvanced)v.findViewById(R.id.listViewContacts);
              if(emptyViewType == null || FragmentPintactsList.this.getActivity() == null)
              {
                  UiControllerUtil.goToMainActivity(FragmentPintactsList.this.getActivity());

              }
              lv.setEmptyView(v.findViewById(emptyViewType.getViewId()));
              if(emptyViewType.getViewId() == R.id.emptyPintactLayout) {
                  v.findViewById(R.id.buttonFindSome).setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          Intent myIntent = new Intent(getActivity(), ContactFindActivity.class);
                          startActivity(myIntent);
                      }
                  });
              }
    adapter=new SectionAdapter(getActivity(),entities,new SimpleViewListAdapter.initView() {
        @Override
        public void init(SimpleViewListAdapter simpleViewListAdapter, int index, View convertView) {

            if(adapter.getItem(index) instanceof ListViewPintactsGroup)return;
            //if(convertView.getId()==R.layout.list_view_pintact_group_header)return;

            TextView textActionView = (TextView)convertView.findViewById(R.id.textViewAction);
            LinearLayout textActionViewLayout = (LinearLayout)convertView.findViewById(R.id.textViewActionLayout);
            textActionViewLayout.setTag(simpleViewListAdapter.getItem(index));

            if (actionButtonType != null) {
                textActionViewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListViewPintactItem itemTag = (ListViewPintactItem)view.getTag();
                        final ListableEntity entity = itemTag.getEntity();
                        switch (actionButtonType) {
                            case INVITE:

                                ContactDTO contactDTO = (ContactDTO)entity;
                                Map<String, Long> userMap = SingletonLoginData.getInstance().getLocalContactSearchMap();
                                if(contactDTO.isLocalContact && userMap.containsKey(contactDTO.localContactId))
                                {
                                    ContactShareRequest req = new ContactShareRequest();
                                    Intent it = new Intent(FragmentPintactsList.this.getMyActivity(), GroupProfileShareActivity.class);
                                    req.setDestinationUserId(userMap.get(contactDTO.localContactId));
                                    req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
                                    it.putExtra(GroupProfileShareActivity.ARG_PROFILE_SHARE, 0);
                                    SingletonLoginData.getInstance().setContactShareRequest(req);
                                    startActivity(it);
                                }else {
                                    new FragmentNativeCommunication(R.string.invite_dialog_header,
                                            getResources().getString(R.string.invite_sms,
                                                    ((ContactDTO) entity).getContactUser().getName(),
                                                    SingletonLoginData.getInstance().getUserData().getPin(),
                                                    SingletonLoginData.getInstance().getUserData().getName()),
                                            getResources().getString(R.string.invite_email_subject),
                                            Html.fromHtml(getResources().getString(R.string.invite_email_body,
                                                    ((ContactDTO) entity).getContactUser().getName(),
                                                    SingletonLoginData.getInstance().getUserData().getPin(),
                                                    SingletonLoginData.getInstance().getUserData().getName())),
                                            (ContactDTO) entity).show(FragmentPintactsList.this.getFragmentManager(), "invite");
                                }
                                break;
                            case ADD:
                                ContactShareRequest req = new ContactShareRequest();
                                Intent it = new Intent(FragmentPintactsList.this.getMyActivity(), GroupProfileShareActivity.class);
                                SearchDTO searchDTO = (SearchDTO)entity;

                                // if this is group pin, do sth else.
                                if (searchDTO.getGroupPin() == null) {
                                    req.setDestinationUserId(Long.parseLong(searchDTO.getUserId()));
                                    req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
                                    it.putExtra(GroupProfileShareActivity.ARG_PROFILE_SHARE, 0);
                                } else {
                                    req.setDestinationPin(searchDTO.getGroupPin());
                                    it.putExtra(GroupProfileShareActivity.ARG_PROFILE_SHARE, -1);
                                }

                                SingletonLoginData.getInstance().setContactShareRequest(req);
                                startActivity(it);
                                break;
                            default: // nothing to do here
                        }
                    }
                });
            }
        }
    });

    lv.setAdapter(adapter);

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
            ListViewPintactItem item = (ListViewPintactItem) adapter.getItem(index);
            ListableEntity entity = item.getEntity();
            switch (pintactActionType) {
                case VIEW_PROFILE:
                    ContactDTO contactDTO = null;
                    if (entity instanceof SearchDTO) {
                      if (!((SearchDTO)entity).isInContactList()) {
                        return;
                      }
                  }
              }}});

              lv.setAdapter(adapter);

              lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                      ListViewPintactItem item = (ListViewPintactItem) adapter.getItem(index);
                      ListableEntity entity = item.getEntity();
                      switch (pintactActionType) {
                          case VIEW_PROFILE:
                              ContactDTO contactDTO = null;
                              if (entity instanceof SearchDTO) {
                                  if (!((SearchDTO)entity).isInContactList()) {
                                      return;
                                  }
                                  for (ContactDTO contact : SingletonLoginData.getInstance().getContactList()) {
                                      if (!contact.isLocalContact
                                              && contact.getUserId() == Long.parseLong(((SearchDTO)entity).getUserId())) {
                                          contactDTO = contact;
                                          break;
                                      }
                                  }
                                  if (contactDTO == null) {
                                      if (getActivity() instanceof ContactFindActivity) {
                                          ((ContactFindActivity)getActivity()).setContactId(Long.parseLong(((SearchDTO) entity).getUserId()));
                                      }
                                      return;
                                  }
                              } else {
                                  contactDTO = (ContactDTO)entity;
                                  if(contactDTO.isLocalContact)
                                  {
                                      contactDTO = SingletonLoginData.getInstance().getLocalContactDetail(contactDTO.localContactId, FragmentPintactsList.this.getActivity());
                                  }
                              }
                              UiControllerUtil.openContactDetail(contactDTO);
                              Intent myIntent = PintactProfileActivity.getInstanceForContactView(getActivity());
                              startActivity(myIntent);
                              getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                              break;
                          case PINTRODUCE:
                              ProfileDTO introduced = new ProfileDTO();
                              introduced.setUserProfileAttributes(new ArrayList<UserProfileAttribute>());
                              introduced.setUserProfileAddresses(new ArrayList<UserProfileAddress>());

                              ContactDTO selectedItem = (ContactDTO) entity;
                              if (selectedItem.getSharedProfiles() != null && !selectedItem.getSharedProfiles().isEmpty()) {
                                  introduced.setUserProfile(selectedItem.getSharedProfiles().get(0).getUserProfile());
                              }
                              introduced.setUserId(selectedItem.getUserId());

                              SingletonLoginData.getInstance().setIntroducedProfile(introduced);

                              Intent it = new Intent(FragmentPintactsList.this.getActivity(), ContactIntroduceActivity.class);
                              startActivity(it);
                              break;
                          default:
                              // take no action
                      }

                      return;
                  }
              });

              // add filter for search view
              if (enableFilter) {
                  final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                      @Override
                      public boolean onQueryTextChange(final String cs) {
                          adapter.getFilter().filter(cs,new Filter.FilterListener() {
                              @Override
                              public void onFilterComplete(int i) {

                              }
                          });
                          return true;
                      }

                      @Override
                      public boolean onQueryTextSubmit(String query) {
                          return true;
                      }
                  };

                  getMyActivity().setSearchTextQueryListener(queryTextListener);
              }

              lv.requestFocus();

          }
      };
      mainHandler.post(myRunnable);
    


    return v;
  }
  
  public void updateListContents(List<? extends ListableEntity> entities) {
    adapter.setEntities(entities);
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onResume() {
    super.onResume();

    if(SingletonLoginData.getInstance().getIsStatusChanged() && adapter != null) {
      SingletonLoginData.getInstance().setIsStatusChanged(false);
      entities = AppService.initContactList(false);
      adapter.setEntities(entities);
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    Log.d(TAG,"onAttach()");
  }

  @Override
  public void onDetach() {
    super.onDetach();
    Log.d(TAG,"onDetach()");
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG,"onPause()");
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d(TAG, "onStart()");
  }

    public MyActivity getMyActivity() {
    return (MyActivity)getActivity();
  }


  class InviteLocalContact implements PostServiceExecuteTask {
    ViewListItem item;
    ContactDTO contactDTO;

    InviteLocalContact(ContactDTO contactDTO,ViewListItem item) {
      this.item=item;
      this.contactDTO=contactDTO;
    }
    
    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          item.setEditMode(false);
          contactDTO.setAlreadyInvited(true);
          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

          Runnable myRunnable = new Runnable(){
            public void run(){
              lv.invalidateViews();
            }
          };
          mainHandler.post(myRunnable);
        }
      } catch (Exception e) {
        System.out.println("Error fetching contact updates " +e.getMessage());
        e.printStackTrace();
      }
    }
  }

}
