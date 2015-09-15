package com.pinplanet.pintact.group;


import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.chat.ThreadsFragment;
import com.pinplanet.pintact.contact.PintactProfileActivity;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.IntentUtil;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupContactsActivity extends MyActivity {

    // request codes
    private static final int GROUP_PIN_ACTIVITY_REQUEST_CODE = 1;
    private int sendCmd = -1;
    private static int CMD_GROUP_INFO = 0;

    private boolean isModerator;

    private ThreadsFragment topicFragment = null;
    private TextView currentScreenTitle, groupMembersTextView;
    private LinearLayout topicHeader;
    private ListView pendingListView, membersListView;

    private ImageView members;
    private ImageView chat;
    private TextView membersTextView, membersButtonTextView, pendingContactsTV;
    private TextView chatTextView;

    ArrayList<GroupMember> pendingContactList;
    ArrayList<GroupMember> groupMemberList;

    List<String> userSharedProfileIds;

    FrameLayout contentFrame;

    GroupDTO group;
    GroupContactsAdapter membersAdapter;
    GroupContactsAdapter pendingAdapter;

    boolean isLoadingGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list_view);
        hideRight();
        group = SingletonLoginData.getInstance().getCurGroup();
        if(group != null){
            showGroupContactView();
        }else{
            String groupId = savedInstanceState.getString("groupId");
            if(groupId != null){

            }
        }
    }

    public void loadingGroup(String group) {
        SingletonLoginData.getInstance().loadLoginData();

        if (SingletonLoginData.getInstance().getUserData() != null) {
            isLoadingGroup = true;
            String path = "/api/group/" + Uri.encode(group, "utf-8") + "/members.json?" + SingletonLoginData.getInstance().getPostParam();
            SingletonNetworkStatus.getInstance().setActivity(this);
            new HttpConnection().access(this, path, "", "GET");
        }

    }

    private void showGroupContactView(){


        showRightImage(R.drawable.three_dots);
        addRightImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentGroupActions(SingletonLoginData.getInstance().getCurGroup(),
                    SingletonLoginData.getInstance().getGroupContacts())
                    .show(GroupContactsActivity.this.getFragmentManager(), "groupAction");
            }
        });

        pendingContactList = new ArrayList<>();
        groupMemberList = new ArrayList<>();
        userSharedProfileIds = new ArrayList<>();


        if (group.getModerators() != null) {
            if (group.getModerators().get(0).equals(Long.toString(SingletonLoginData.getInstance().getUserData().getId()))) {
                isModerator = true;
                Log.d(TAG, "isModerator");
                getPendingMembers(group);
            } else {
                isModerator = false;
                Log.d(TAG, "Isn't moderator");
            }
        }

        pendingListView = (ListView) findViewById(R.id.pendingListView);
        pendingContactsTV = (TextView) findViewById(R.id.pendingContactsTV);
        membersListView = (ListView) findViewById(R.id.membersListView);
        membersListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        membersTextView = (TextView) findViewById(R.id.groupMembersTextView);
        members = (ImageView) findViewById(R.id.members);
        chat = (ImageView) findViewById(R.id.set_chat);
        membersButtonTextView = (TextView) findViewById(R.id.membersButtonTextView);
        chatTextView = (TextView) findViewById(R.id.chatTextView);
        currentScreenTitle = (TextView) findViewById(R.id.set_contact_sync);
        topicHeader = (LinearLayout) findViewById(R.id.glv_thread_header);
        topicHeader.setVisibility(View.GONE);
        contentFrame = (FrameLayout) findViewById(R.id.content_frame);

        showLeftImage(R.drawable.actionbar_left_arrow);
        View.OnClickListener backLn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingletonLoginData.getInstance().setCurGroup(null);
                finish();
            }
        };

        openMembersFragment();
        addLeftClickListener(backLn);

        TextView create_new_topic = (TextView) findViewById(R.id.gmn_add_button);
        create_new_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateTopicActivity();
            }
        });

    }

    private void openCreateTopicActivity() {
        Intent myIntent = new Intent(this, CreateTopicActivity.class);
        startActivity(myIntent);
    }

    public void openTopicsFragment() {
        Log.d("CHAT", "Opening topic fragment");
        if (topicFragment == null) {
            topicFragment = new ThreadsFragment();
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, topicFragment).commit();
        fragmentManager.executePendingTransactions();

        //currentScreenTitle.setText(getString(R.string.lm_topics));
        membersListView.setVisibility(View.GONE);
        membersTextView.setText("Chat Discussion Topics");
        pendingContactsTV.setVisibility(View.GONE);
        pendingListView.setVisibility(View.GONE);
//        defaultHeader.setVisibility(View.GONE);
        contentFrame.setVisibility(View.VISIBLE);
        topicHeader.setVisibility(View.VISIBLE);
    }

    public void openMembersFragment() {

        setMembersContacts(SingletonLoginData.getInstance().getGroupContacts());
        contentFrame.setVisibility(View.GONE);

        if (pendingContactList.size() > 0) {
            pendingContactsTV.setVisibility(View.VISIBLE);
            pendingListView.setVisibility(View.VISIBLE);
        }
        membersListView.setVisibility(View.VISIBLE);
        membersTextView.setText("MEMBERS");
        topicHeader.setVisibility(View.GONE);
    }


    public void onProfileView(int i) {
        Intent myIntent = PintactProfileActivity.getInstanceForContactView(this);
        startActivity(myIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupDTO group = SingletonLoginData.getInstance().getCurGroup();

        long seconds = System.currentTimeMillis();
        String exp = "Exp";
        if (group.getExpiredTime() != null) {
            long diffMS = (Long.parseLong(group.getExpiredTime()) - seconds);
            long days = diffMS / (1000 * 60 * 60 * 24);
            if (days >= 0) {
                exp = days + "d";
            } else {
                exp = "Exp";
            }
        }

        showTitle(SingletonLoginData.getInstance().getCurGroup().getGroupName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GROUP_PIN_ACTIVITY_REQUEST_CODE
                && resultCode == GroupPinActivity.GROUP_EDIT_CANCEL) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void membersClicked(View v) {
        members.setImageResource(R.drawable.members_selected);
        membersButtonTextView.setTextColor(getResources().getColor(R.color.PINTACT_BLUE_COLOR));
        chat.setImageResource(R.drawable.messages);
        chatTextView.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        openMembersFragment();
    }

    public void chatClicked(View v) {
        chat.setImageResource(R.drawable.messages_selected);
        chatTextView.setTextColor(getResources().getColor(R.color.PINTACT_BLUE_COLOR));
        members.setImageResource(R.drawable.members);
        membersButtonTextView.setTextColor(getResources().getColor(R.color.PINTACT_BLACK_COLOR));
        openTopicsFragment();
    }

    private void getPendingMembers(GroupDTO group) {
        String apiPath = null;

        String groupPin = group.getGroupPin();

        apiPath = "/api/group/" + groupPin + "/pendingMembers";


        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, "", "GET");
    }

    private void setMembersContacts(List<ContactDTO> contactDTOList) {
        groupMemberList.clear();
        GroupMember groupMember;
        Boolean acontact, isUser;
        String name, companyName, pathToImage, phoneNumber = null, userEmail = null;
        String groupId = group.getId();
        String memberId;
        for (int i = 0; i < contactDTOList.size(); i++) {
            name = contactDTOList.get(i).getContactUser().getName();
            memberId = Long.toString(contactDTOList.get(i).getContactUser().getId());
            if (memberId.equals(Long.toString(SingletonLoginData.getInstance().getUserData().getId()))) {
                isUser = true;
                List<ProfileDTO> profileList = contactDTOList.get(i).getSharedProfiles();
                for (int j = 0; j < profileList.size(); j++) {
                    userSharedProfileIds.add("\"" + Long.toString(profileList.get(j).getUserProfile().getId()) + "\"");
                }
                group.setSharedProfileIds(userSharedProfileIds);
            } else {
                isUser = false;

                ProfileDTO profile = AppService.getMergedProfile(contactDTOList.get(i));
                for (UserProfileAttribute profileAttribute : profile.getUserProfileAttributes()) {
                    if (profileAttribute.getType() == AttributeType.PHONE_NUMBER
                            && profileAttribute.getLabel() != null
                            && profileAttribute.getLabel().matches(".*(?ui:mobile|iphone|cell).*")) {
                        if (profileAttribute.getValue() != null) {
                            phoneNumber = profileAttribute.getValue();
                            phoneNumber = phoneNumber.replace("+", "");
                        }
                    } else if (profileAttribute.getType() == AttributeType.EMAIL) {
                        userEmail = profileAttribute.getValue();
                    }
                }
            }
            acontact = contactDTOList.get(i).isAcontact();
            companyName = contactDTOList.get(i).getContactUser().getCompanyName();
            pathToImage = contactDTOList.get(i).getContactUser().getPathToImage();
            String initials = new StringBuilder().append(contactDTOList.get(i).getContactUser().getFirstName().charAt(0)).
                    append(contactDTOList.get(i).getLastName().charAt(0)).toString();
            groupMember = new GroupMember(memberId, groupId, name, companyName, pathToImage, false, acontact, isUser, phoneNumber, userEmail, initials);
            groupMemberList.add(groupMember);
        }
        setMembersContactsListView();
    }

    private void setMembersContactsListView() {
        membersAdapter = new GroupContactsAdapter(this, this, groupMemberList);
        membersListView.setAdapter(membersAdapter);
    }

    private void setPendingContactsList(JSONArray jsonArray) {
        pendingContactsTV.setVisibility(View.VISIBLE);
        GroupMember contact;
        JSONObject jsonObject;
        Boolean acontact;
        String name = "";
        String companyName = "";
        String pathToImage = "";
        String memberId;
        String groupId = group.getId();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i).getJSONObject("contactUser");
                Log.d(TAG, "jsonObject: " + jsonObject.toString());
                memberId = jsonObject.getString("id");
                name = jsonObject.getString("name");
                companyName = jsonObject.getString("companyName");
                pathToImage = jsonObject.getString("pathToImage");
                acontact = jsonArray.getJSONObject(i).getBoolean("acontact");
                String initials = new StringBuilder().append(jsonObject.getString("firstName").charAt(0)).
                        append(jsonObject.getString("lastName").charAt(0)).toString();
                contact = new GroupMember(memberId, groupId, name, companyName, pathToImage, true, acontact, false, null, null, initials);
                pendingContactList.add(contact);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "JSONException: " + e.toString());
            }
        }
        setPendingContacts(pendingContactList);
    }

    private void setPendingContacts(ArrayList<GroupMember> pendingContactList) {
        pendingAdapter = new GroupContactsAdapter(this, this, pendingContactList);
        pendingListView.setAdapter(pendingAdapter);
        pendingListView.setVisibility(View.VISIBLE);
    }

    private void reloadMembers() {
        for (int i = 0; i < pendingContactList.size(); i++) {
            if (pendingContactList.get(i).getStatus().equals("Approved")) {
                groupMemberList.add(pendingContactList.get(i));
                membersAdapter.notifyDataSetChanged();
                pendingContactList.remove(i);
                if (pendingContactList.size() <= 0) {
                    pendingContactsTV.setVisibility(View.GONE);
                }
            } else if (pendingContactList.get(i).getStatus().equals("Rejected")) {
                pendingContactList.remove(i);
                if (pendingContactList.size() <= 0) {
                    pendingContactsTV.setVisibility(View.GONE);
                }
            }
        }
    }

    public void emailAll(View v) {
        List<String> emailAddressList = new ArrayList<String>();
        for (GroupMember groupMember : groupMemberList) {
            emailAddressList.add(groupMember.getUserEmail());
        }

        IntentUtil.sendEmail(GroupContactsActivity.this,
                emailAddressList.toArray(new String[emailAddressList.size()]), null, null);
    }

    public void messageAll(View v) {
        if (isModerator) {
            new FragmentGroupBroadcast(SingletonLoginData.getInstance().getCurGroup())
                    .show(GroupContactsActivity.this.getFragmentManager(), "groupBroadcast");
        } else {
            Toast toast = Toast.makeText(this, "Action reserved for moderators", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public void groupShare(View v) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Join " + group.getGroupName() + " on Pintact using pin: " + group.getGroupPin());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void groupAdd(View v) {
        Intent addIntent = new Intent(this, GroupInviteActivity.class);
        startActivity(addIntent);
    }

    private void getGroupInfo() {
        String groupId = group.getId();

        String apiPath = "/api/groupPin/" + groupId + "/detail";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, "", "GET");
        sendCmd = CMD_GROUP_INFO;
    }

    @SuppressLint("NewApi")
    public void onPostNetwork() {
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            Log.d(TAG, SingletonNetworkStatus.getInstance().getMsg());
            Log.d(TAG, SingletonNetworkStatus.getInstance().getErrMsg());
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }

        Log.d(TAG, "Group Contacts Json: " + SingletonNetworkStatus.getInstance().getJson());
        if (sendCmd == CMD_GROUP_INFO) {
            Log.d(TAG, "Hereee");
            try {
                JSONObject jsonObject = new JSONObject(SingletonNetworkStatus.getInstance().getJson());
                SingletonLoginData.getInstance().getCurGroup().setGroupName(jsonObject.getString("groupName"));
                setTitle(jsonObject.getString("groupName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //openTopicsFragment();
        }else if (isLoadingGroup) {
            isLoadingGroup = false;

            // get the data
            Log.d(TAG, "LeftDeckActivityJson: " + SingletonNetworkStatus.getInstance().getJson());
            Type collectionType = new TypeToken<Collection<ContactDTO>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Collection<ContactDTO> contacts = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
            SingletonLoginData.getInstance().setGroupContacts(new ArrayList<ContactDTO>(contacts));
            showGroupContactView();

        } else {
            if (SingletonNetworkStatus.getInstance().getJson().equals("")) {
                Log.d(TAG, "json equals blank");
                reloadMembers();
            }
            try {
                JSONArray jsonArray = new JSONArray(SingletonNetworkStatus.getInstance().getJson());
                Log.d(TAG, "JSONArray length: " + jsonArray.length());
                if (jsonArray.length() > 0)
                    setPendingContactsList(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
        sendCmd = -1;
    }
}
