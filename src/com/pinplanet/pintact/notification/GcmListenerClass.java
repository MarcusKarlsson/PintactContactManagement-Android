package com.pinplanet.pintact.notification;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Andy on 6/20/2015.
 */
public class GcmListenerClass extends GcmListenerService {
    private final static String TAG = "Debugging";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
    }
}
