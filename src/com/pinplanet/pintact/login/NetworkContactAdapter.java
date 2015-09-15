package com.pinplanet.pintact.login;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.EventType;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pinplanet.pintact.utility.UiControllerUtil.myDialog;

/**
 * Created by wildcat on 5/31/2015.
 */
public class NetworkContactAdapter extends BaseAdapter {
    private final static String TAG = "Debugging";
    private ArrayList<NetworkContact> networkContactList;
    private final LayoutInflater mInflater;
    private Activity activity;

    private int sendCmd = -1;
    private static int CMD_REQUEST_CONNECT = 0;

    public NetworkContactAdapter(Activity activity, Context context, ArrayList<NetworkContact> networkContactList) {
        mInflater = LayoutInflater.from(context);
        this.networkContactList = networkContactList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return networkContactList.size();
    }

    @Override
    public NetworkContact getItem(int position) {
        return networkContactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView nameView;
        CustomNetworkImageView imageView;
        final ImageView checkCircle;
        ViewHolder viewHolder = null;

        if (v == null) {
            Log.d(TAG, "v null");
            v = mInflater.inflate(R.layout.login_network_contact, parent, false);
//            v.setTag(R.id.networkNameView, v.findViewById(R.id.networkNameView));
//            v.setTag(R.id.networkImageView, v.findViewById(R.id.networkImageView));
//            v.setTag(R.id.networkCheckCircle, v.findViewById(R.id.networkCheckCircle));

            viewHolder = new ViewHolder();
            viewHolder.checkCircle = (ImageView) v.findViewById(R.id.networkCheckCircle);
            viewHolder.imageView = (CustomNetworkImageView) v.findViewById(R.id.networkImageView);
            viewHolder.nameView = (TextView) v.findViewById(R.id.networkNameView);

            v.setTag(viewHolder);
        } else {
            Log.d(TAG, "v not null");
            viewHolder = (ViewHolder) v.getTag();
        }

//        nameView = (TextView) v.getTag(R.id.networkNameView);
//        imageView = (CustomNetworkImageView) v.getTag(R.id.networkImageView);
//        checkCircle = (ImageView) v.getTag(R.id.networkCheckCircle);


        final NetworkContact networkContact;
        final ViewHolder finalViewHolder = viewHolder;
        networkContact = getItem(position);

        if (networkContact.getConnected() || networkContact.getInvited()) {
            viewHolder.checkCircle.setImageResource(R.drawable.circle_check_orange);
        } else {
            viewHolder.checkCircle.setImageResource(R.drawable.circle);
        }

        viewHolder.checkCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkContact.setConnected(true);
                String userId = networkContact.getPintactId();
                finalViewHolder.checkCircle.setImageResource(R.drawable.circle_check_orange);
                if (!networkContact.getGroupMember()) {
                    onConnectClicked(userId, activity);
                } else {
                    if (networkContact.getInvited()) {
                        networkContact.setInvited(false);
                        finalViewHolder.checkCircle.setImageResource(R.drawable.circle);
                    }
                    else {
                        networkContact.setInvited(true);
                    }
                }
            }
        });

        if (networkContact != null)

        {
            if (viewHolder.nameView != null && networkContact.getContactName() != null) {
                viewHolder.nameView.setText(networkContact.getContactName());
            }
            if (viewHolder.imageView != null) {
                if (networkContact.getPathToImage() != null && !networkContact.getPathToImage().equals("null")) {
                    Log.d(TAG, "Path to image: " + networkContact.getPathToImage());
                    viewHolder.imageView.setImageUrl(networkContact.getPathToImage(), AppController.getInstance().getImageLoader());
                    viewHolder.imageView.setBackgroundResource(R.color.PINTACT_WHITE_COLOR);
                } else {
                    viewHolder.imageView.setImageResource(R.drawable.silhouette);
                }
            }
        }

        notifyDataSetChanged();

        return v;
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

    public static class ViewHolder {
        public TextView nameView;
        public ImageView checkCircle;
        public CustomNetworkImageView imageView;
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
        sendCmd = -1;
    }
}
