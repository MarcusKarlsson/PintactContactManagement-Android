package com.pinplanet.pintact.group;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.login.NetworkContact;
import com.pinplanet.pintact.login.NetworkContactAdapter;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pinplanet.pintact.utility.UiControllerUtil.myDialog;

public class GroupInviteActivity extends MyActivity {
    ListView groupInviteList;
    NetworkContactAdapter adapter;
    ArrayList<NetworkContact> networkContactArrayList;
    List<ContactDTO> contactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invite);
        setTitle("ADD MEMBERS");
        showRightText("ADD");
        addRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClickListener();
                finish();
            }
        });
        showLeftImage(R.drawable.actionbar_left_arrow);
        addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        networkContactArrayList = new ArrayList<>();
        getContacts();

        groupInviteList = (ListView) findViewById(R.id.groupInviteList);
        adapter = new NetworkContactAdapter(this, this, networkContactArrayList);
        groupInviteList.setAdapter(adapter);
    }

    private void getContacts() {
        NetworkContact networkContact;
        contactList = SingletonLoginData.getInstance().getContactList();
        for (ContactDTO contact : contactList) {
            if(!contact.isLocalContact) {
                Log.d(TAG, "pathToImage: " + contact.getContactUser().getPathToImage());
                Log.d(TAG, "UserId: " + contact.getUserId());
                networkContact = new NetworkContact(Long.toString(contact.getUserId()), contact.getName(), contact.getContactUser().getPathToImage(), true);
                if (networkContact != null)
                    networkContactArrayList.add(networkContact);
            }
        }
    }

    private void addClickListener() {
        ArrayList<String> idArrayList = new ArrayList<>();
        for (NetworkContact networkContact : networkContactArrayList) {
            if (networkContact.getInvited())
                idArrayList.add("\"" + networkContact.getPintactId() + "\"");
        }
        String[] idArray = new String[idArrayList.size()];
        idArray = idArrayList.toArray(idArray);
        addUsers(idArray);
    }

    private void addUsers(String[] idArray) {
        Gson gson = new GsonBuilder().create();

        String params = "{\"groupId\":\"" + SingletonLoginData.getInstance().getCurGroup().getId() +
                "\",\"sourceUserIdArray\":" + Arrays.toString(idArray) + "}";

        SingletonNetworkStatus.getInstance().clean();
        SingletonNetworkStatus.getInstance().setActivity(this);

        String path = "/api/group/addPintact.json";

        new HttpConnection().access(this, path, params, "POST");
    }

    public void onPostNetwork() {
        Log.d(TAG, "PostNetwork NetworkContactAdapter");
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            Log.d(TAG, SingletonNetworkStatus.getInstance().getMsg());
            Log.d(TAG, SingletonNetworkStatus.getInstance().getErrMsg());
            myDialog(SingletonNetworkStatus.getInstance().getMsg(), SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }
        Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
    }
}
