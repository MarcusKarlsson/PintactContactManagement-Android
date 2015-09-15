package com.pinplanet.pintact.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.ImageUploadActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;

public class AccountImageUpdateActivity extends ImageUploadActivity {

    private static final String TAG = "AccountImageUpdate";

    private boolean isNextButtonPressed = false;

    private TextViewTypeFace nextTextView;
    private LinearLayout imageButtonsHolder;

    String profilePictureUrl;
    Intent callingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_image_update_activity);
        showTitle(R.string.REGISTER_ADD_AN_IMAGE);

        hideBoth();

        callingIntent = getIntent();
        profilePictureUrl = callingIntent.getStringExtra("ProfilePictureUri");

        imageButtonsHolder = (LinearLayout) findViewById(R.id.imageButtonsHolder);
        nextTextView = (TextViewTypeFace) findViewById(R.id.imageNextTextView);

//    showRightText(R.string.action_bar_option);
//
//    addRightTextClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        onNext(v);
//      }
//    });

        init();

        if (profilePictureUrl != null) {
            imageView.setImageUrl(profilePictureUrl, AppController.getInstance().getImageLoader());
            //imagePath1 = profilePictureUri;
        } else {
            imageButtonsHolder.setVisibility(View.GONE);
            nextTextView.setText("SKIP");
        }
    }

    public void onDummy(View view) {
    }

    @Override
    public void removeSearchImage(View view) {
        imInfo = null;
        imageView.setLocalImageBitmap(null);
        imageView.setImageUrl(null, AppController.getInstance().getImageLoader());
        imageButtonsHolder.setVisibility(View.GONE);
        nextTextView.setText("SKIP");
        imagePath1 = null;

    }

    public void alertDialog(String title, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
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
                        Intent it = new Intent(AccountImageUpdateActivity.this, LoginConnectActivity.class);
                        startActivity(it);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

                    }
                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        isNextButtonPressed = false;
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onNext(View view) {

        if (imagePath1 == null && profilePictureUrl == null) {
            alertDialog("Continue?", "Do you want to continue without an image?");
        } else {
            if (imagePath1 != null) {
                uploadImage();
            } else if (profilePictureUrl != null) {
                isNextButtonPressed = true;
                setProfilePicture();
            }
//            Intent connectIntent = new Intent(this, LoginConnectActivity.class);
//            connectIntent.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
//            connectIntent.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));
//            connectIntent.putExtra("GoogleFriendCount", callingIntent.getIntExtra("GoogleFriendCount", -1));
//            connectIntent.putExtra("FBFriendCount", callingIntent.getIntExtra("FBFriendCount", -1));
//            connectIntent.putExtra("UserPin", callingIntent.getStringExtra("UserPin"));
//            connectIntent.putExtra("GoogleId", callingIntent.getStringExtra("GoogleId"));
//            connectIntent.putExtra("FacebookId", callingIntent.getStringExtra("FacebookId"));
//            startActivity(connectIntent);
        }

    }

    @Override
    public void onBackPressed() {
    }

    public void onPostNetwork() {

        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            Log.d(TAG, "Network Error");
            Log.d(TAG, SingletonNetworkStatus.getInstance().getMsg());
            Log.d(TAG, SingletonNetworkStatus.getInstance().getErrMsg());
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            //finish();
            return;
        } else {
            if (isUploadingImage) {
                super.onPostNetwork();
                isNextButtonPressed = true;
                String params = "{\"pathToImage\":\"" + imInfo.thumbnailPath + "\"}";

                SingletonNetworkStatus.getInstance().setActivity(this);
                String path = "/api/users/addAccountImage.json?" + SingletonLoginData.getInstance().getPostParam();
                new HttpConnection().access(this, path, params, "POST");
            } else if (isNextButtonPressed) {
                AppService.getProfilesAsync();

                Intent connectIntent = new Intent(this, LoginNetworkActivity.class);
                connectIntent.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
                connectIntent.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));
                connectIntent.putExtra("GoogleFriendCount", callingIntent.getIntExtra("GoogleFriendCount", -1));
                connectIntent.putExtra("FBFriendCount", callingIntent.getIntExtra("FBFriendCount", -1));
                connectIntent.putExtra("UserPin", callingIntent.getStringExtra("UserPin"));
                connectIntent.putExtra("GoogleId", callingIntent.getStringExtra("GoogleId"));
                connectIntent.putExtra("FacebookId", callingIntent.getStringExtra("FacebookId"));
                startActivity(connectIntent);

//                Intent it = new Intent(this, MainActivity.class);
//                //it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(it);
//                finish();
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }

    }

    public void changeImage(View view) {
        createAttachDialog(view);
    }

    private void setProfilePicture() {
        String params = "{\"pathToImage\":\"" + profilePictureUrl + "\"}";

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = "/api/users/addAccountImage.json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST");
    }
}
