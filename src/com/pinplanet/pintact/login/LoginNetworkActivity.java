package com.pinplanet.pintact.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.utility.DataLoginData;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class LoginNetworkActivity extends MyActivity {
    private final static String TAG = "LoginNetworkDebugging";
    private ArrayList<NetworkContact> networkContactArrayList;
    private Map<String, NetworkContact> networkContactMap;
    PriorityQueue<NetworkContact> queue;

    private int sendCmd = -1;
    private static int CMD_REQUEST_FB = 0;
    private static int CMD_REQUEST_GOOGLE = 1;
    private static int CMD_REQUEST_CONNECTALL = 0;


    private ListView contactListView;
    private Intent callingIntent;
    private ImageView connectAll;

    private NetworkContactAdapter networkContactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_network);
        setTitle("NETWORK");
        hideLeft();

        addRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }
        });

        callingIntent = getIntent();
        Log.d(TAG, "GoogleJsonString in Network: " + callingIntent.getStringExtra("GoogleJsonString"));
        Log.d(TAG, "FBJsonString in Network: " + callingIntent.getStringExtra("FBJsonString"));

        networkContactMap = new HashMap<String, NetworkContact>();
        contactListView = (ListView) findViewById(R.id.networkContactListView);

        Comparator<NetworkContact> comparator = new Comparator<NetworkContact>() {
            @Override
            public int compare(NetworkContact lhs, NetworkContact rhs) {
                if (lhs.getContactName().compareTo(rhs.getContactName()) > 0) {
                    return 1;
                }
                return -1;
            }
        };
        queue = new PriorityQueue<NetworkContact>(10, comparator);

        if (callingIntent.getStringExtra("GoogleJsonString") != null)
            addJsonToMap(callingIntent.getStringExtra("GoogleJsonString"));
        if (callingIntent.getStringExtra("FBJsonString") != null)
            addJsonToMap(callingIntent.getStringExtra("FBJsonString"));
        //networkContactArrayList = new ArrayList<>(networkContactMap.values());
        networkContactArrayList = new ArrayList<>(queue);
        networkContactAdapter = new NetworkContactAdapter(this, this, networkContactArrayList);
        contactListView.setAdapter(networkContactAdapter);

        connectAll = (ImageView) findViewById(R.id.networkConnectAllCircle);
        connectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectAllClicked();
            }
        });

//        ArrayList<String> facebookFriendsIds = callingIntent.getStringArrayListExtra("fbFriendsIds");
//        if (facebookFriendsIds != null) {
//            Log.d(TAG, "friendsIds: " + facebookFriendsIds.toString());
//            requestFbInfo(facebookFriendsIds);
//        } else {
//            ArrayList<String> googleFriendsIds = callingIntent.getStringArrayListExtra("googleFriendsIds");
//            if (googleFriendsIds != null) {
//                requestGoogleInfo(googleFriendsIds);
//            }
//        }
    }

    private void requestFbInfo(ArrayList<String> facebookFriendsIds) {
        JSONObject fbRequestObject = new JSONObject();
        try {
            fbRequestObject.put("socialMediaType", "FB");
            fbRequestObject.put("friendIds", facebookFriendsIds);
        } catch (JSONException e) {
            Log.d(TAG, "requestFbInfo exception: " + e.toString());
            e.printStackTrace();
        }
//        String params = fbRequestObject.;
//        params = params.replace("\\", "");
        String params = "{\"socialMediaType\":\"FB\",\"friendIds\":" + facebookFriendsIds.toString() + "}";
        Log.d(TAG, "Params: " + params);
        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = "/api/users/searchFriends.json";
        new HttpConnection().access(this, path, params, "POST");

        sendCmd = CMD_REQUEST_FB;
    }

    private void requestGoogleInfo(ArrayList<String> facebookFriendsIds) {
        JSONObject fbRequestObject = new JSONObject();
        try {
            fbRequestObject.put("socialMediaType", "GOOGLE");
            fbRequestObject.put("friendIds", facebookFriendsIds);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }
//        String params = fbRequestObject.;
//        params = params.replace("\\", "");
        String params = "{\"socialMediaType\":\"GOOGLE\",\"friendIds\":" + facebookFriendsIds.toString() + "}";
        Log.d(TAG, "Params: " + params);
        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = "/api/users/searchFriends.json";
        new HttpConnection().access(this, path, params, "POST");

        sendCmd = CMD_REQUEST_GOOGLE;
    }

    private void addJsonToMap(String jsonString) {
        String contactName, pathToImage, userId;
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                contactName = jsonObject.getString("firstName") + " " + jsonObject.getString("lastName");
                pathToImage = jsonObject.getString("pathToImage");
                userId = jsonObject.getString("id");
                NetworkContact networkContact = new NetworkContact(userId, contactName, pathToImage);
                if (userId != null && networkContact != null && queue != null) {
                    queue.add(networkContact);
                } else {
                    Log.d(TAG, "HereNOw");
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString());
            e.printStackTrace();
        }
    }

    private void onConnectAllClicked() {
        Log.d(TAG, "onConnectAllClicked");
        ContactShareRequest req = new ContactShareRequest();
        req.setDestinationUserIdArray(getFriendsIds());
        //req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        SingletonLoginData.getInstance().setContactShareRequest(req);

        onShareProfile();
    }

    private Long[] getFriendsIds() {
        ArrayList<Long> arrayList = new ArrayList<>();
        for (int i = 0; i < networkContactArrayList.size(); i++) {
            arrayList.add(Long.parseLong(networkContactArrayList.get(i).getPintactId()));
        }
        Long[] friendsIds = new Long[arrayList.size()];
        friendsIds = arrayList.toArray(friendsIds);
        Log.d(TAG, "friendsIds: " + arrayList.toString());
        return friendsIds;
    }

    public void onShareProfile() {
        Log.d(TAG, "onShareProfile");

        Long[] profId = getSharedProfileIds();
        Log.d(TAG, "profId: " + profId);
        SingletonLoginData.getInstance().getContactShareRequest().setUserProfileIdsShared(profId);

        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(SingletonLoginData.getInstance().getContactShareRequest());

        SingletonNetworkStatus.getInstance().clean();
        SingletonNetworkStatus.getInstance().setActivity(this);

        String path = "/api/contactsList.json?" + SingletonLoginData.getInstance().getPostParam();

        new HttpConnection().access(this, path, params, "POST");
        sendCmd = CMD_REQUEST_CONNECTALL;
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

    public void onPostNetwork() {
        Log.d(TAG, "networkonPostNetwork");
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(), SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }
        if (sendCmd == CMD_REQUEST_CONNECTALL) {
            Log.d(TAG, "CMD_REQUEST_CONNECT");
            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
            checkAll();
        }
        Log.d(TAG, "TESTJSON2: " + SingletonNetworkStatus.getInstance().getJson().toString());

//        if (sendCmd == CMD_REQUEST_FB) {
//            Log.d(TAG, "CMD_REQUEST_FB");
//            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
//            addJsonToMap(SingletonNetworkStatus.getInstance().getJson().toString());
//            ArrayList<String> googleFriendsIds = callingIntent.getStringArrayListExtra("googleFriendsIds");
//            if (googleFriendsIds != null) {
//                requestGoogleInfo(googleFriendsIds);
//            } else {
//                networkContactArrayList = new ArrayList<>(networkContactMap.values());
//                NetworkContactAdapter networkContactAdapter = new NetworkContactAdapter(this, networkContactArrayList);
//                contactListView.setAdapter(networkContactAdapter);
//            }
//        }
//        if (sendCmd == CMD_REQUEST_GOOGLE) {
//            Log.d(TAG, "CMD_REQUEST_GOOGLE");
//            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
//            addJsonToMap(SingletonNetworkStatus.getInstance().getJson().toString());
//            networkContactArrayList = new ArrayList<>(networkContactMap.values());
//            NetworkContactAdapter networkContactAdapter = new NetworkContactAdapter(this, networkContactArrayList);
//            contactListView.setAdapter(networkContactAdapter);
//        }
        sendCmd = -1;
    }

    private void checkAll(){
        Log.d(TAG, "inCheckAll");
        connectAll.setImageResource(R.drawable.circle_check_orange);
        for (int i=0; i<networkContactArrayList.size(); i++) {
            networkContactArrayList.get(i).setConnected(true);
        }
        networkContactAdapter.notifyDataSetChanged();
    }

}
