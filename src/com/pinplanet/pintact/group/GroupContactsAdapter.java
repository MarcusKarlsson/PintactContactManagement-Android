package com.pinplanet.pintact.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.PintactProfileActivity;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.profile.ProfileShowActivity;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pinplanet.pintact.utility.UiControllerUtil.myDialog;

/**
 * Created by wildcat on 6/14/2015.
 */
public class GroupContactsAdapter extends BaseAdapter {
    private final static String TAG = "Debugging";
    private ArrayList<GroupMember> memberList;
    private final LayoutInflater mInflater;
    private Activity activity;

    private int sendCmd = -1;
    private static int CMD_REQUEST_CONNECT = 0;

    public GroupContactsAdapter(Activity activity, Context context, ArrayList<GroupMember> memberList) {
        mInflater = LayoutInflater.from(context);
        this.memberList = memberList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public GroupMember getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView nameView, companyView;
        final FrameLayout groupContactApprove, groupContactReject, groupContactConnect;
        CustomNetworkImageView imageView;

        ViewHolder viewHolder = null;

        if (v == null) {
            v = mInflater.inflate(R.layout.group_contact_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.nameView = (TextView) v.findViewById(R.id.groupContactName);
            viewHolder.companyView = (TextView) v.findViewById(R.id.groupContactCompany);
            viewHolder.imageView = (CustomNetworkImageView) v.findViewById(R.id.groupContactImage);
            viewHolder.groupContactApprove = (FrameLayout) v.findViewById(R.id.groupContactApprove);
            viewHolder.groupContactReject = (FrameLayout) v.findViewById(R.id.groupContactReject);
            viewHolder.groupContactConnect = (FrameLayout) v.findViewById(R.id.groupContactConnect);
            viewHolder.groupInitialTV = (TextView) v.findViewById(R.id.groupInitialTV);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setBackgroundResource(R.color.PINTACT_BLUE_COLOR);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundResource(R.color.PINTACT_WHITE_COLOR);
                }

                return false;
            }
        });

        final GroupMember groupMember;
        final ViewHolder finalViewHolder = viewHolder;
        groupMember = getItem(position);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupMember.isUser()) {
                    Intent myIntent = ProfileShowActivity.getInstance(activity, 0);
                    activity.startActivity(myIntent);
                    activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                } else {
                    onProfileView(activity, Long.parseLong(groupMember.getMemberId()));
                }
            }
        });

        if (groupMember != null) {
            if (groupMember.getPending() == true) {
                viewHolder.groupContactApprove.setVisibility(View.VISIBLE);
                viewHolder.groupContactApprove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupMember.setStatus("Approved");
                        groupMember.setPending(false);
                        approveUser(activity, groupMember.getGroupId(), groupMember.getMemberId());
                    }
                });
                viewHolder.groupContactReject.setVisibility(View.VISIBLE);
                viewHolder.groupContactReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groupMember.setStatus("Rejected");
                        groupMember.setPending(false);
                        rejectUser(activity, groupMember.getGroupId(), groupMember.getMemberId());
                    }
                });
            } else if (!groupMember.getConnected() && !groupMember.isUser()) {
                viewHolder.groupContactConnect.setVisibility(View.VISIBLE);
                viewHolder.groupContactConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectToUser(activity, groupMember.getMemberId());
                        finalViewHolder.groupContactConnect.setVisibility(View.GONE);
                    }
                });
            } else {
                viewHolder.groupContactConnect.setVisibility(View.GONE);
            }
            Log.d(TAG, "groupMember not null");
            if (viewHolder.nameView != null && groupMember.getName() != null) {
                Log.d(TAG, "GroupMember name: " + groupMember.getName());
                viewHolder.nameView.setText(groupMember.getName());
            }
            if (viewHolder.companyView != null) {
                if (groupMember.getCompany() != null && !groupMember.getCompany().equals("null")) {
                    viewHolder.companyView.setText(groupMember.getCompany());
                    viewHolder.companyView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.companyView.setText("");
                }
            }
            if (viewHolder.imageView != null) {
                if (groupMember.getPathToImage() != null && !groupMember.getPathToImage().equals("null")) {
                    Log.d(TAG, "Path to image: " + groupMember.getPathToImage());
                    viewHolder.imageView.setImageUrl(groupMember.getPathToImage(), AppController.getInstance().getImageLoader());
                    viewHolder.imageView.setVisibility(View.VISIBLE);
                    viewHolder.groupInitialTV.setVisibility(View.GONE);
                } else {
                    viewHolder.imageView.setVisibility(View.GONE);
                    viewHolder.groupInitialTV.setText(groupMember.getInitials());
                    viewHolder.groupInitialTV.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Path to image null");
                }
            }
        }

        notifyDataSetChanged();

        return v;
    }

    public void onProfileView(Activity activity, Long userId) {
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setUserId(userId);
        UiControllerUtil.openContactDetail(contactDTO);
        if (SingletonLoginData.getInstance().getContactUser() != null) {
            Intent myIntent = PintactProfileActivity.getInstanceForShareView(activity);
            activity.startActivity(myIntent);
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            Log.d(TAG, "null contact profile view");
        }
    }

    public void onConnectClicked(String userId, Activity activity) {
        Log.d(TAG, "onConnectClicked");
        ContactShareRequest req = new ContactShareRequest();
        Long destId = Long.parseLong(userId);
        Log.d(TAG, "destId: " + destId);
        req.setDestinationUserId(destId);
        //req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        SingletonLoginData.getInstance().setContactShareRequest(req);

        onShareProfile(activity);
    }

    public void onShareProfile(Activity activity) {
        Log.d(TAG, "onShareProfile");

        Long[] profId = getSharedProfileIds();
        Log.d(TAG, "profId: " + profId);
        SingletonLoginData.getInstance().getContactShareRequest().setUserProfileIdsShared(profId);

        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(SingletonLoginData.getInstance().getContactShareRequest());

        SingletonNetworkStatus.getInstance().clean();
        SingletonNetworkStatus.getInstance().setActivity(activity);

        String path = "/api/contacts.json?" + SingletonLoginData.getInstance().getPostParam();

        new HttpConnection().access(activity, path, params, "POST");
        sendCmd = CMD_REQUEST_CONNECT;
    }


    public Long[] getSharedProfileIds() {
        ArrayList<Long> arrayList = new ArrayList<>();
        //Long profId[] = new Long[5];
        List<ProfileDTO> profiles = SingletonLoginData.getInstance().getUserProfiles();
        //Log.d(TAG, "UserData: " + SingletonLoginData.getInstance().getUserData().toString());
        int j = 0;
        Log.d(TAG, "ProfilesSize: " + profiles.size());
        for (int i = 0; i < profiles.size(); i++) {
            UserProfile prof = profiles.get(i).getUserProfile();
            //profId[j++] = prof.getId();
            arrayList.add(prof.getId());
        }
        Long[] profId = new Long[arrayList.size()];
        profId = arrayList.toArray(profId);
        return profId;
    }

    private void connectToContact(String userId, Activity activity) {
        String params = "{\"connect\":\"false\",\"destinationUserId\":\"" + userId + "\"}";
        Log.d(TAG, "Params: " + params);
        SingletonNetworkStatus.getInstance().setActivity(activity);
        String path = "/api/contacts.json";
        if (activity != null) {
            new HttpConnection().access(activity, path, params, "POST");
        } else {
            Log.d(TAG, "activity is null");
        }

        sendCmd = CMD_REQUEST_CONNECT;
    }

    private void approveUser(Activity activity, String groupId, String userId) {
        SingletonNetworkStatus.getInstance().setActivity(activity);
        String path = "/api/group/" + groupId + "/member/" + userId + "/approve.json";
        new HttpConnection().access(activity, path, "", "POST");
    }

    private void rejectUser(Activity activity, String groupId, String userId) {
        SingletonNetworkStatus.getInstance().setActivity(activity);
        String path = "/api/group/" + groupId + "/member/" + userId + "/reject.json";
        new HttpConnection().access(activity, path, "", "POST");
    }

    private void connectToUser(Activity activity, String userId) {
        String path = "/api/contacts/addFromList.json?" + SingletonLoginData.getInstance().getPostParam();

        String groupId = SingletonLoginData.getInstance().getCurGroup().getId();
        List<String> profileIds = SingletonLoginData.getInstance().getCurGroup().getSharedProfileIds();
        Log.d(TAG, "profileIds: " + profileIds);
        Log.d(TAG, "profileId1: " + profileIds.get(0));
        String sourceUserId = Long.toString(SingletonLoginData.getInstance().getUserData().getId());

        String profileIdsString = profileIds.toString();
//        profileIdsString = profileIdsString.replace("\"", "");
        Log.d(TAG, "profileIdsString: " + profileIdsString);

//        String params = "{\"sourceUserId\":" + sourceUserId + ",\"destinationUserId\":" + "\"" + userId +
//                "\",\"userProfileIdsShared\":" + profileIds + ",\"groupId\":" + groupId + "}";
        String params = "{\"sourceUserId\":" + sourceUserId + ",\"destinationUserId\":" + "\"" + userId +
                "\",\"userProfileIdsShared\":" + profileIdsString + ",\"groupId\":" + groupId + "}";


        Map data = new HashMap();
        data.put("sourceUserId", sourceUserId);
        data.put("destinationUserId", userId);
        data.put("groupId", groupId);
        data.put("userProfileIdsShared", profileIdsString);
        String json = new Gson().toJson(data);

        new HttpConnection().access(activity, path, params, "POST");
    }

    public static class ViewHolder {
        TextView nameView, companyView, groupInitialTV;
        FrameLayout groupContactApprove, groupContactReject, groupContactConnect;
        CustomNetworkImageView imageView;
    }

    public void onPostNetwork() {
        Log.d(TAG, "PostNetwork NetworkContactAdapter");
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(activity, SingletonNetworkStatus.getInstance().getMsg(), SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }
        if (sendCmd == CMD_REQUEST_CONNECT) {
            Log.d(TAG, "CMD_REQUEST_CONNECT");
            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
        }
        Log.d(TAG, "TESTJSON2: " + SingletonNetworkStatus.getInstance().getJson().toString());
        sendCmd = -1;
    }
}