package com.pinplanet.pintact.notification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.PintactProfileActivity;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.EventType;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;
import com.pinplanet.pintact.list.ListViewEditTextItem;
import com.pinplanet.pintact.list.ListViewTextItem;
import com.pinplanet.pintact.list.ListViewUserItem;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.MyFragment;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.late.gui.ExpandableListViewAdvanced;
import de.late.widget.ExpandableViewListAdapter;
import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 03.10.2014.
 *
 */
public class FragmentNotificationDetails extends MyFragment {

    private static final String TAG = FragmentNotificationDetails.class.getName();

    private int ADD_GROUP_ID_FROM = 0, ADD_GROUP_ID_INTRODUCED_USER = 0, ADD_GROUP_ID_MESSAGE = 0,ADD_GROUP_ID_WHEN = 0,ADD_GROUP_ID_PERSONAL_MESSAGE = 0;

    private static final String PARAM_NOTIFICATION="notification";
    private static final String PARAM_NOTIFICATION_INDEX="notification_index";

    private LinearLayout linearLayoutShareProfileItems,layoutShareProfiles;
    private TextView textViewShareSelected;

    private ExpandableViewListAdapter adapterNotificationDetails;
    private ExpandableListViewAdvanced listViewNotificationDetails;

    private NotificationDTO notificationDTO;
    private int notificationIndex;
    private ProfileDTO mProfile;
    private UserDTO userDTO;
    private ListViewEditTextItem notestText;


    public FragmentNotificationDetails() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationDTO = (NotificationDTO)getArguments().getSerializable(PARAM_NOTIFICATION);
        notificationIndex = getArguments().getInt(PARAM_NOTIFICATION_INDEX,-1);
        mProfile = SingletonLoginData.getInstance().getMergedProfile();
        userDTO = notificationDTO.getData().sender;
    }

    public static final FragmentNotificationDetails getInstance(int notificationIndex,NotificationDTO notificaion) {
        FragmentNotificationDetails fragment = new FragmentNotificationDetails();
        Bundle args = new Bundle();
        args.putInt(PARAM_NOTIFICATION_INDEX,notificationIndex);
        args.putSerializable(PARAM_NOTIFICATION,notificaion);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification_details, container, false);

        ((MyActivity)getActivity()).showLeftImage(R.drawable.actionbar_left_arrow);
        ((MyActivity)getActivity()).addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        listViewNotificationDetails = (ExpandableListViewAdvanced) v.findViewById(R.id.listViewNotificationDetails);

        adapterNotificationDetails = new ExpandableViewListAdapter(getActivity(), new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
            }

            @Override
            public void initChild(ExpandableViewListAdapter expandableViewListAdapter, int i, int i2, View convertView) {
                ViewListItem item=adapterNotificationDetails.getChild(i,i2);
                if(item instanceof ListViewEditTextItem)
                {
                    final EditText editTextView = (EditText) convertView.findViewById(R.id.editTextView);
                    editTextView.setTag(item);
                    editTextView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
                        @Override
                        public void afterTextChanged(Editable editable) {
                            Log.d(TAG,"afterTextChanged");
                            ListViewEditTextItem led=(ListViewEditTextItem)editTextView.getTag();
                            led.setText(editTextView.getText().toString());
                        }
                    });

                    //TODO: fix later
                    //not works, we cant clear focus...damn Android
//                    editTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                        @Override
//                        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                            if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
//                            {
//                                textView.clearFocus();
//                                textViewAccept.requestFocus();
//                                ViewUtil.hideKeyboard(editTextView,getActivity());
//
//                                return true;
//                            }
//                            return false;
//                        }
//                    });
                }
            }
        });

        listViewNotificationDetails.setAdapter(adapterNotificationDetails);
        listViewNotificationDetails.setIgnoreGroupClicks(true);
        listViewNotificationDetails.setAllowAutosizing(true);


        listViewNotificationDetails.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int group, int child, long l) {
              try {
                Object obj = adapterNotificationDetails.getChild(group, child);
                if (obj instanceof ListViewNotificationItem) {
                  ListViewNotificationItem item = (ListViewNotificationItem) adapterNotificationDetails.getChild(group, child);
                  if (item != null) {
                    NotificationDTO notificationDTO = item.getNotification();
                    Long userId = null;
                    if (notificationDTO.getEventType().equals(EventType.CONTACT_INTRODUCE) && group == 2) {
                      userId = notificationDTO.getData().introducingUser.id;
                    } else {
                      userId = notificationDTO.getData().sender.id;
                    }
                    ContactDTO contactDTO = ContactTableDbInterface.getInstance().getContact(userId);

                    if (contactDTO == null) {
                      contactDTO = notificationDTO.getData().contactDto;
                    }

                    if (contactDTO != null) {
                      openUserDetail(contactDTO);
                    }
                    return true;
                  }
                }
                return false;
              }catch (Exception e)
              {
                throw new RuntimeException(e);
              }
            }
        });


        //########### profile share stuff ##############
        linearLayoutShareProfileItems = (LinearLayout) v.findViewById(R.id.share_layout);
        layoutShareProfiles = (LinearLayout) v.findViewById(R.id.layoutShareProfiles);

        ((TextView)layoutShareProfiles.findViewById(R.id.textViewLeftGroupText)).setText(R.string.PROFILE_SELECT_PROFILES_TO_SHARED);

        textViewShareSelected = (TextView) v.findViewById(R.id.tv_share);

        v.findViewById(R.id.tv_accept_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAcceptInvite();
            }
        });

        v.findViewById(R.id.tv_hide_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRejectInvite();
            }
        });

        v.findViewById(R.id.tv_preview_share_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Long ids[] = getSharedProfileIds();
                if (ids.length == 0) {
                    ((MyActivity) getActivity()).myDialog(getString(R.string.gm_no_profile_title), getString(R.string.gm_no_profile_detail));
                    return;
                }

                UiControllerUtil.openPreviewShareActivity(ids);
                Intent myIntent = PintactProfileActivity.getInstanceForShareView(getActivity());
                startActivity(myIntent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        EventType eventType = notificationDTO.getEventType();

        if(eventType==EventType.CONTACT_INVITE || eventType== EventType.CONTACT_INTRODUCE)
        {
            if(eventType==EventType.CONTACT_INVITE)
                ((MyActivity)getActivity()).showTitle(R.string.noti_title_pinivite);
            else ((MyActivity)getActivity()).showTitle(R.string.noti_title_pintroduction);

            layoutShareProfiles.setVisibility(View.VISIBLE);
            updateSharedProfileIcons();
        }
        else
        {
            ((MyActivity)getActivity()).showTitle(R.string.noti_title_update);
            layoutShareProfiles.setVisibility(View.GONE);

            if(!notificationDTO.isSeen()) {
                AppService.markSeenNotification(notificationDTO);
            }
        }

        //########### profile share stuff end ##############

        updateList();

        return v;
    }



    public void updateList() {
        Log.d(TAG, "updateList()");

        adapterNotificationDetails.clear();

        if(notificationDTO==null)return;

        ADD_GROUP_ID_FROM = -1;
        ADD_GROUP_ID_INTRODUCED_USER = -1;
        ADD_GROUP_ID_MESSAGE = -1;
        ADD_GROUP_ID_WHEN = -1;
        ADD_GROUP_ID_PERSONAL_MESSAGE = -1;

        ListViewGroupNotificationsItem groupFrom;
        ListViewGroupNotificationsItem groupIntroducedUser=null;
        ListViewGroupNotificationsItem groupIntroducedPersonalMessage=null;
        ListViewGroupNotificationsItem groupMessage;
        ListViewGroupNotificationsItem groupWhen = new ListViewGroupNotificationsItem(R.string.noti_detail_when);

        String fName = notificationDTO.getData().sender.firstName;
        String lName = notificationDTO.getData().sender.lastName;
        String senderName =  UiControllerUtil.getName(fName, lName);

        EventType eventType = notificationDTO.getEventType();

        Log.d(TAG,"eventType:"+eventType+" decription:"+eventType.getSimpleDescription());

        //invitation
        if(eventType==EventType.CONTACT_INVITE)
        {
            groupFrom = new ListViewGroupNotificationsItem(R.string.noti_detail_invitation_from);
            String message=getString(R.string.noti_detail_message_from)+" "+senderName.toUpperCase();
            groupMessage = new ListViewGroupNotificationsItem(message);

        }
        else if(eventType==EventType.CONTACT_INTRODUCE)
        {
            groupFrom = new ListViewGroupNotificationsItem(R.string.noti_detail_introduced_by);
            groupIntroducedUser=new ListViewGroupNotificationsItem(R.string.noti_detail_introduced_to);

            String message=getString(R.string.noti_detail_message_from)+" "+senderName.toUpperCase();
            groupMessage = new ListViewGroupNotificationsItem(message);
        }
        else
        {
            groupFrom = new ListViewGroupNotificationsItem(R.string.noti_detail_update_from);
            groupMessage = new ListViewGroupNotificationsItem(R.string.noti_detail_update_details);
        }

        ADD_GROUP_ID_FROM= adapterNotificationDetails.addGroup(groupFrom);
        ListViewNotificationItem item=new ListViewNotificationItem(true,notificationDTO);
        item.setShowIsSeen(false);
        groupFrom.addChild(item);

        if(eventType==EventType.CONTACT_INTRODUCE)
        {
            ADD_GROUP_ID_PERSONAL_MESSAGE=adapterNotificationDetails.addGroup(groupIntroducedUser);
            ListViewUserItem user=new ListViewUserItem(notificationDTO.getData().introducingUser);
            user.setEnabled(false);
            groupIntroducedUser.addChild(user);

        }

        ADD_GROUP_ID_MESSAGE=adapterNotificationDetails.addGroup(groupMessage);
        groupMessage.addChild(new ListViewTextItem(notificationDTO.getDetailedText()));

        ADD_GROUP_ID_WHEN=adapterNotificationDetails.addGroup(groupWhen);
        Date d = notificationDTO.getParsedDate();
        groupWhen.addChild(new ListViewTextItem(""+DateUtils.getRelativeTimeSpanString(d.getTime())));

        if(eventType==EventType.CONTACT_INTRODUCE || eventType == EventType.CONTACT_INVITE)
        {
            groupIntroducedPersonalMessage=new ListViewGroupNotificationsItem(R.string.noti_detail_personal_message);
            ADD_GROUP_ID_PERSONAL_MESSAGE=adapterNotificationDetails.addGroup(groupIntroducedPersonalMessage);
          notestText = new ListViewEditTextItem( getString(R.string.noti_join_me_text));
            groupIntroducedPersonalMessage.addChild( notestText);

        }



        adapterNotificationDetails.notifyDataSetChanged();
        listViewNotificationDetails.expandAllGroups();
      listViewNotificationDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
          ListViewGroupNotificationsItem item = (ListViewGroupNotificationsItem) adapterView.getItemAtPosition(index);
        }
      });
        listViewNotificationDetails.invalidateViews();

    }


    //########### profile share stuff ##############
    LinkedHashMap<String, Integer> hm = new LinkedHashMap<String, Integer>();

  public Long[] getSharedProfileIds(){
    Long profId[] = new Long[hm.size()];
    List<ProfileDTO> profiles = SingletonLoginData.getInstance().getUserProfiles();
    Set<Map.Entry<String, Integer>> entries = hm.entrySet();
    int j =0;
    for(Map.Entry<String, Integer> entry : entries) {
      for (int i = 0; i < profiles.size(); i++) {
        UserProfile prof = profiles.get(i).getUserProfile();
        if(entry.getKey().equals(prof.getName()))
        {
          profId[j++] = prof.getId();
        }
      }
    }

    return profId;
  }

    public void addProfiles(int i) {
        UserProfile currentProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = currentProfile.getName();

        final View addView = getActivity().getLayoutInflater().inflate(R.layout.profile_thumb_half, null);
        linearLayoutShareProfileItems.addView(addView);

        // change the name of the text
        TextView tv = (TextView) addView.findViewById(R.id.pt_name);
        tv.setText(title);

        Log.d(TAG, "addProfiles:" + i + " title:" + title + " image" + currentProfile.getPathToImage());

        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) addView.findViewById(R.id.pt_profile_image);
        ivPhoto.setDefaultImageResId(R.drawable.silhouette);


        if (currentProfile.getPathToImage() != null) {
            ivPhoto.setImageUrl(currentProfile.getPathToImage(), AppController.getInstance().getImageLoader());
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        final RelativeLayout lo = (RelativeLayout) addView.findViewById(R.id.pt_all);
        lo.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                TextView tv = (TextView) v.findViewById(R.id.pt_name);
                String key = tv.getText().toString();
                selectProfile(lo, key);
            }
        });

        if(SingletonLoginData.getInstance().getUserProfiles().size() == 1)
        {
            selectProfile(lo, title);
        }
    }

    private void selectProfile(RelativeLayout lo, String key)
    {
        Integer value = hm.get(key);
        lo.setBackgroundDrawable(lo.getResources().getDrawable(
                value == null ?
                        R.drawable.border_profile_thumb_sel_half :
                        R.drawable.border_profile_thumb_nosel_half
        ));
        if ( value == null ) {
            hm.put(key, 1);
        } else
            hm.remove(key);

        // we need to update tv_share
        String listProfiles = "";
        Set<Map.Entry<String, Integer>> entries = hm.entrySet();
        for ( Map.Entry<String, Integer> entry : entries ) {
            listProfiles += entry.getKey() + ",";
        }


        if (listProfiles.length() > 1)
            listProfiles = listProfiles.substring(0, listProfiles.length() - 1);

        textViewShareSelected.setText(listProfiles);
    }


    public void updateSharedProfileIcons() {

        try {
            // show shared profiles
            for (int i = 0; i < SingletonLoginData.getInstance().getUserProfiles().size(); i++) {
                addProfiles(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    //######### code taken from the old activity ################
    boolean isAccepted=false;

    public void onAcceptInvite() {
        ContactShareRequest req = new ContactShareRequest();
        Long destId = notificationDTO.getData().sender.id;
        if(notificationDTO.getEventType().equals(EventType.CONTACT_INTRODUCE))
        {
          destId = notificationDTO.getData().introducingUser.id;
        }
        req.setDestinationUserId(destId);
        req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        SingletonLoginData.getInstance().setContactShareRequest(req);

        onShareProfile();
    }

  boolean isAcceptingInvite = false;

  public void onShareProfile() {
    // set shared profile
    String notes = notestText.getText().toString();
    Long profId[] = getSharedProfileIds();


    if ( profId.length == 0 ) {
      myDialog("No Profile Selected", "Please select at least one profile to share");
      return;
    }

    SingletonLoginData.getInstance().getContactShareRequest().setNote(notes);
    SingletonLoginData.getInstance().getContactShareRequest().setUserProfileIdsShared(profId);

    Gson gson = new GsonBuilder().create();
    String params = gson.toJson(SingletonLoginData.getInstance().getContactShareRequest());

    SingletonNetworkStatus.getInstance().clean();
    SingletonNetworkStatus.getInstance().setActivity(getActivity());
    SingletonNetworkStatus.getInstance().setFragment(this);

    String path = "/api/contacts.json?" + SingletonLoginData.getInstance().getPostParam();

    isAcceptingInvite = true;
    new HttpConnection().access(this.getActivity(), path, params, "POST");

  }

    public void onRejectInvite() {
      if(notificationDTO.getEventType().equals(EventType.CONTACT_INTRODUCE))
      {
        alertDialog(getString(R.string.DIALOG_TITLE_HIDE_INTRO), getString(R.string.DIALOG_MESSAGE_HIDE_INTRO));
      }else {
        alertDialog(getString(R.string.DIALOG_TITLE_REJECT_INVITE), getString(R.string.DIALOG_MESSAGE_REJECT_INVITE));
      }
    }

    public void alertDialog(String title, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(info);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        rejectInvite();
                    }
                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

  boolean isReject = false;
  boolean isIntroduceHidden = false;

    public void rejectInvite() {

        Long destId = notificationDTO.getData().sender.id;
        if(notificationDTO.getEventType().equals(EventType.CONTACT_INTRODUCE))
        {
          destId = notificationDTO.getData().introducingUser.id;
          String path = "/api/notifications/"+notificationDTO.getNotificationId()+"/seen.json?" + SingletonLoginData.getInstance().getPostParam();

          SingletonNetworkStatus.getInstance().clean();
          SingletonNetworkStatus.getInstance().setActivity(getActivity());
          SingletonNetworkStatus.getInstance().setFragment(this);
          isIntroduceHidden = true;
          isReject = true;
          new HttpConnection().access(getActivity(), path, "", "POST");
        }else {
          String path = "/api/contacts/" + destId + "/reject.json?" + SingletonLoginData.getInstance().getPostParam();

          SingletonNetworkStatus.getInstance().clean();
          SingletonNetworkStatus.getInstance().setActivity(getActivity());
          SingletonNetworkStatus.getInstance().setFragment(this);
          isReject = true;
          new HttpConnection().access(getActivity(), path, "", "POST");
        }
    }

    public void onPostNetwork () {

        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            getActivity().onBackPressed();
            return;
        }
      if(isIntroduceHidden)
      {
        notificationDTO.setSeen(true);
        int count = SingletonLoginData.getInstance().getTotalUnseenNoti();
        SingletonLoginData.getInstance().setTotalUnseenNoti((count > 1)? count - 1 : 0);

      }
      if(isAcceptingInvite)
      {
        AppService.handleUpdateContactResponse();

      }
      if(isAcceptingInvite || isReject)
      {
        PageDTO<NotificationDTO> notificationDTOs = SingletonLoginData.getInstance().getNotifications();
        for(int i=0;i<notificationDTOs.getData().size();i++)
        {
          if(notificationDTOs.getData().get(i).getNotificationId() == notificationDTO.getNotificationId())
          {
            notificationDTOs.getData().remove(i);
            break;
          }
        }
      }else{
        PageDTO<NotificationDTO> notificationDTOs = SingletonLoginData.getInstance().getNotifications();
        for(int i=0;i<notificationDTOs.getData().size();i++)
        {
          if(notificationDTOs.getData().get(i).getNotificationId() == notificationDTO.getNotificationId())
          {
            notificationDTOs.getData().get(i).setSeen(true);
            break;
          }
        }
      }
      if(! isIntroduceHidden) {
        AppService.markSeenNotification(notificationDTO);
      }
      //SingletonNetworkStatus.getInstance().setActivity(null);
      //SingletonNetworkStatus.getInstance().setFragment(null);
      if(getActivity()!=null)
         getActivity().onBackPressed();

    }

  private void openUserDetail(ContactDTO contactDTO){
    UiControllerUtil.openContactDetail(contactDTO);
    Intent myIntent = PintactProfileActivity.getInstanceForShareView(getActivity());
    startActivity(myIntent);
    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
  }


}