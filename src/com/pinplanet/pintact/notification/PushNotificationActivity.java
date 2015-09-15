package com.pinplanet.pintact.notification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.group.GroupContactsActivity;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class PushNotificationActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        loadingGroup(getIntent().getStringExtra("groupId"));
    }

    public void loadingGroup(String group) {
//        isLoadingGroup = true;
        String path = "/api/group/" + Uri.encode(group, "utf-8") + "/members.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, "", "GET");
    }

    @SuppressLint("NewApi")
    public void onPostNetwork() {

        SingletonNetworkStatus.getInstance().setActivity(this);

        if (SingletonNetworkStatus.getInstance().getCode() != 0 && SingletonNetworkStatus.getInstance().getCode() != 200) {
            Log.d("Debugging", SingletonNetworkStatus.getInstance().getMsg());
            Log.d("Debugging", SingletonNetworkStatus.getInstance().getErrMsg());
            SingletonNetworkStatus.getInstance().setCode(0);
        }


        // get the data
        Log.d("Debugging", "PushNotificationActivityJson: " + SingletonNetworkStatus.getInstance().getJson());
        Type collectionType = new TypeToken<Collection<ContactDTO>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        Collection<ContactDTO> contacts = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
        SingletonLoginData.getInstance().setGroupContacts(new ArrayList<ContactDTO>(contacts));

        Intent myIntent = new Intent(this, GroupContactsActivity.class);
        startActivity(myIntent);

    }
}
