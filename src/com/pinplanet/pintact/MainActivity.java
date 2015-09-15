package com.pinplanet.pintact;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.login.LoginActivity;
import com.pinplanet.pintact.login.LoginRegisterActivity;
import com.pinplanet.pintact.login.LoginRegisterOptionsActivity;
import com.pinplanet.pintact.slideshow.ActivitySlideShow;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.MySoundPool;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.io.InputStream;
import java.util.List;


//TODO: clean up code... we use some method's in LoginActivity too... 
public class MainActivity extends MyActivity {

    private static final String TAG = MainActivity.class.getName();

    String currentStep = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        //if(!BuildConfig.DEBUG)
        Crashlytics.start(this);
        //Crashlytics.getInstance().setDebugMode(true);
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        TextView btn = (TextView) findViewById(R.id.buttonRegister);
        btn.setFocusableInTouchMode(true);
        btn.requestFocus();

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        SingletonLoginData.getInstance().loadLoginData();

        if (SingletonLoginData.getInstance().getUserData() != null) {
            //loadPreferences();
            //AppService.checkIfThereIsAnyContacts();
            if (SingletonLoginData.getInstance().deviceRegistered == null) {
                postGetRegistrationID();
            }
            Intent it = new Intent(this, LeftDeckActivity.class);
            startActivity(it);
        } else if (!MainActivity.this.getSharedPreferences("PINTACT", Context.MODE_PRIVATE).contains("FIRST_START")) {
            SharedPreferences.Editor e = MainActivity.this.getSharedPreferences("PINTACT", Context.MODE_PRIVATE).edit();
            e.putBoolean("FIRST_START", true);
            e.apply();

            Intent intent = ActivitySlideShow.getInstance(MainActivity.this, ActivitySlideShow.TourType.SLIDE_SHOW_FIRST_START,
                    R.array.ARRAY_SLIDE_START, false, false);

            startActivity(intent);
        }

        MySoundPool.initSoundPool(this);
        AppController.getInstance().getChatData();
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "OnBackPressed()");
        moveTaskToBack(true);
        return;
    }


    public void onPostNetwork() {

        if (!currentStep.equals("loadContactUpdates") && SingletonNetworkStatus.getInstance().getCode() != 200) {
            SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);
            SingletonNetworkStatus.getInstance().getWaitDialog().dismiss();
            SingletonNetworkStatus.getInstance().setWaitDialog(null);

            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());


            return;
        }

        if (currentStep.equals("loadContactUpdate")) {
            try {
                AppService.handleGetContactResponse();
            } catch (Exception e) {
                Toast.makeText(this, "Error in loading contact updates", Toast.LENGTH_LONG);
            }
            Intent it = new Intent(this, LeftDeckActivity.class);
            startActivity(it);
        }

    }


    public void onRegister(View view) {
        Intent registerOptionsIntent = new Intent(this, LoginRegisterOptionsActivity.class);
        startActivity(registerOptionsIntent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }


    public void onHowPintactWorks(View view) {
        Intent it = ActivitySlideShow.getInstance(this, ActivitySlideShow.TourType.PINTACT,
                R.array.ARRAY_TOUR_HOW_PINTACT_WORKS, true, false);
        startActivity(it);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    public void onLogin(View view) {
        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    public void loadProfileImages() {
        List<ProfileDTO> data = SingletonLoginData.getInstance().getUserProfiles();
        for (int position = 0; position < data.size(); position++) {
            ProfileDTO item = data.get(position);
            if (item.getUserProfile().getPathToImage() != null &&
                    item.getUserProfile().getPathToImage() != "" &&
                    SingletonLoginData.getInstance().getBitmap(position) == null) {
                loadImage(position, item.getUserProfile().getPathToImage());
            }
        }
    }

    // test loading profile images
    public void loadImage(int index, String photo_url_str) {
        System.out.println("Loading image from " + photo_url_str);
        new DownloadImageTask().execute(photo_url_str, Integer.toString(index));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        int position;

        public DownloadImageTask() {
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            position = Integer.parseInt(urls[1]);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                System.out.println("Error" + e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            SingletonLoginData.getInstance().setBitmap(position, result);
        }
    }
}


