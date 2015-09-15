package com.pinplanet.pintact.login;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.DataLoginData;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class LoginConnectActivity extends MyActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {
    private static final String TAG = "LoginConnect";

    private static int CMD_REQUEST_FB = 0;
    private static int CMD_REQUEST_GOOGLE = 1;

    String userPin;
    CallbackManager callbackManager;
    Bundle parameters;
    ImageButton connectFacebookButton, connectGoogleButton;
    TextViewTypeFace nextButton;
    TextView facebookFriendCountTV, googleFriendCountTV;

    GoogleApiClient mGoogleApiClient;
    Person currentPerson;
    private boolean mIntentInProgress;
    private boolean mSignInClicked = false;
    private static final int RC_SIGN_IN = 0;

    ArrayList<String> fbFriendsIds, googleFriendsIds;

    private int sendCmd = -1;
    private static final int CMD_SUGGEST_PIN = 0, CMD_SIGN_UP = 2;

    Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_connect);
        FacebookSdk.sdkInitialize(getApplicationContext());

        it = new Intent(this, AccountImageUpdateActivity.class);
        Intent callingIntent = getIntent();
        it.putExtra("ProfilePictureUri", callingIntent.getStringExtra("ProfilePictureUri"));
        Log.d(TAG, "UserPin in Connect: " + callingIntent.getStringExtra("UserPin"));
        userPin = callingIntent.getStringExtra("UserPin");
        Log.d(TAG, "GoogleJsonString in Connect: " + callingIntent.getStringExtra("GoogleJsonString"));
        it.putExtra("GoogleJsonString", callingIntent.getStringExtra("GoogleJsonString"));
        Log.d(TAG, "FBJsonString in Connect: " + callingIntent.getStringExtra("FBJsonString"));
        it.putExtra("FBJsonString", callingIntent.getStringExtra("FBJsonString"));

        showTitle("CONNECT");
        hideBoth();

        nextButton = (TextViewTypeFace) findViewById(R.id.connectNextButton);
        facebookFriendCountTV = (TextView) findViewById(R.id.facebookFriendCountTextView);
        googleFriendCountTV = (TextView) findViewById(R.id.googleFriendCountTextView);

        //pinGen();
        connectFacebookButton = (ImageButton) findViewById(R.id.facebookConnectButton);
        connectGoogleButton = (ImageButton) findViewById(R.id.googleConnectButton);

        setCountTextViews();

        //Set the Facebook login button callback
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Logged in to Facebook");
                        Profile profile = Profile.getCurrentProfile();
                        //If profile was fetched
                        if (profile != null) {
                            //Attempt to get each name, if successful, pass into intent
                            Log.d(TAG, "Profile: " + profile.toString());
                            connectFacebookButton.setBackgroundResource(R.drawable.connected_facebook);
                            connectFacebookButton.setClickable(false);
                            getFacebookUser();
                        } else {
                            Log.d(TAG, "Profile is null");
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Log in cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG, "Facebook error: " + e.toString());
                    }
                });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
//        mGoogleApiClient.connect();
//        if (mGoogleApiClient.isConnected()) {
//            Log.d(TAG, "Google Api Client connected");
//            currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//            if (currentPerson.hasId()) {
//                Log.d(TAG, "Logged into Google");
//                connectGoogleButton.setBackgroundResource(R.drawable.connected_google);
//                connectGoogleButton.setClickable(false);
//            }
//        } else {
//            Log.d(TAG, "Google Api Client not connected");
//        }

    }

    private void getFacebookUser() {
        Log.d(TAG, "In getFacebookUser");
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        if (response.getError() != null) {
                            Log.d(TAG, "Facebook error: " + response.getError().getErrorMessage());
                        } else {
                            if (object != null) {
                                Log.d(TAG, response.toString());
                                try {
                                    SingletonLoginData.getInstance().getSignupRequest().setFacebookId(object.get("id").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    JSONArray friendsArray = object.getJSONObject("friends").getJSONArray("data");
                                    if (friendsArray.length() == 1) {
                                        facebookFriendCountTV.setText(Integer.toString(friendsArray.length()) + " friend on Facebook");
                                    } else {
                                        facebookFriendCountTV.setText(Integer.toString(friendsArray.length()) + " friends on Facebook");
                                    }
                                    facebookFriendCountTV.setVisibility(View.VISIBLE);
                                    fbFriendsIds = getFriendsIds(friendsArray);
                                    requestInfo("FB", fbFriendsIds);
                                } catch (JSONException e) {
                                    Log.d(TAG, e.toString());
                                    e.printStackTrace();
                                }
                                connectFacebookButton.setBackgroundResource(R.drawable.connected_facebook);
                                connectFacebookButton.setClickable(false);
                            }
                        }

                    }
                });
        parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture,friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void connectFacebookClicked(View view) {
        Log.d(TAG, "Facebook connect clicked");
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_friends"));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    public void connectNextClicked(View view) {
        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(SingletonLoginData.getInstance().getSignupRequest());
        Log.d(TAG, "Params: " + params);
        join();
//        if (fbFriendsIds != null)
//            it.putStringArrayListExtra("fbFriendsIds", fbFriendsIds);
//        if (googleFriendsIds != null)
//            it.putStringArrayListExtra("googleFriendsIds", googleFriendsIds);
//        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(it);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

//    public void pinGen() {
//        if (SingletonLoginData.getInstance().getSignupRequest() != null) {
//            Log.d(TAG, SingletonLoginData.getInstance().getSignupRequest().getFirstName());
//            String params = "{\"firstName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getFirstName() +
//                    "\",\"middleName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getMiddleName() +
//                    "\",\"lastName\":\"" + SingletonLoginData.getInstance().getSignupRequest().getLastName() +
//                    "\"}";
//            SingletonNetworkStatus.getInstance().setActivity(this);
//            String path = "/api/pins/suggestPin.json";
//            new HttpConnection().access(this, path, params, "POST");
//
//            sendCmd = CMD_SUGGEST_PIN;
//        } else {
//            Log.d(TAG, "SignupRequest is null");
//        }
//    }

    public void join() {
        String pin = userPin;
        SingletonLoginData.getInstance().getSignupRequest().setPin(pin);

        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(SingletonLoginData.getInstance().getSignupRequest());
        Log.d(TAG, "Params: " + params);

        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = "/api/users/signUp.json";
        new HttpConnection().access(this, path, params, "POST");

        sendCmd = CMD_SIGN_UP;
        //nextButton.setClickable(true); //TAKE OUT WHEN YOU GO BACK TO REGISTERING USERS
    }

    public void saveLoginData(DataLoginData dataLoginData) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.login_username), SingletonLoginData.getInstance().getSignupRequest().getEmailId());
        editor.putString(getString(R.string.access_token), dataLoginData.accessToken);
        editor.putString(getString(R.string.login_user), new Gson().toJson(SingletonLoginData.getInstance().getUserData()));
        editor.commit();

        AppService.reInit();
    }


    public ArrayList<String> getFriendsIds(JSONArray friendsArray) {
        ArrayList<String> friendsList = new ArrayList<>();
        String temp;
        for (int i = 0; i < friendsArray.length(); i++) {
            try {
                temp = "\"" + friendsArray.getJSONObject(i).getString("id") + "\"";
                friendsList.add(temp);
            } catch (JSONException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
        }
        return friendsList;
    }

    public void connectGoogleClicked(View view) {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google Api Client connected");
        currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (currentPerson.hasId()) {
            Log.d(TAG, "Logged into Google");
            SingletonLoginData.getInstance().getSignupRequest().setGoogleId(currentPerson.getId());
            connectGoogleButton.setBackgroundResource(R.drawable.connected_google);
            connectGoogleButton.setClickable(false);
            Plus.PeopleApi.loadConnected(mGoogleApiClient)
                    .setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed with result: " + connectionResult);
        if (!mIntentInProgress && connectionResult.hasResolution()) {
            if (mSignInClicked) {
                try {
                    mIntentInProgress = true;
                    startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                            RC_SIGN_IN, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        ArrayList<String> tempArray = new ArrayList<>();
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                if (count == 1) {
                    googleFriendCountTV.setText("1 friend on Google+");
                } else {
                    googleFriendCountTV.setText(Integer.toString(count) + " friends on Google+");
                }
                googleFriendCountTV.setVisibility(View.VISIBLE);
                for (int i = 0; i < count; i++) {
                    Log.d(TAG, "i: " + i + " count: " + count);
                    Log.d(TAG, "Google Display name: " + personBuffer.get(i).getDisplayName());
                    Log.d(TAG, "Google ID: " + personBuffer.get(i).getId());
                    tempArray.add("\"" + personBuffer.get(i).getId() + "\"");
                }
            } finally {
                personBuffer.close();
            }
            requestInfo("GOOGLE", tempArray);
        } else {
            Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }
    }

    private void requestInfo(String mediaType, ArrayList<String> friendsIds) {
        String params = "{\"socialMediaType\":\"" + mediaType + "\",\"friendIds\":" + friendsIds.toString() + "}";
        Log.d(TAG, "Params: " + params);
        SingletonNetworkStatus.getInstance().setActivity(this);
        ProfileDTO mProfile = new ProfileDTO();
        mProfile = new ProfileDTO();
        mProfile.setUserProfile(new UserProfile());
        mProfile.setUserProfileAttributes(new ArrayList<UserProfileAttribute>());
        mProfile.setUserProfileAddresses(new ArrayList<UserProfileAddress>());
        String path = "/api/users/searchFriends.json";
        if (mediaType.equals("GOOGLE"))
            sendCmd = CMD_REQUEST_GOOGLE;
        if (mediaType.equals("FB"))
            sendCmd = CMD_REQUEST_FB;
        new HttpConnection().access(this, path, params, "POST");
    }

    public void onPostNetwork() {

        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(), SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }
        if (sendCmd == CMD_REQUEST_FB) {
            Log.d(TAG, "CMD_REQUEST_FB");
            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
            it.putExtra("FBJsonString", SingletonNetworkStatus.getInstance().getJson().toString());

        }
        if (sendCmd == CMD_REQUEST_GOOGLE) {
            Log.d(TAG, "CMD_REQUEST_GOOGLE");
            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
            it.putExtra("GoogleJsonString", SingletonNetworkStatus.getInstance().getJson().toString());
        }

//        if (sendCmd == CMD_SUGGEST_PIN) {
//            Log.d(TAG, "CMD_SUGGEST_PIN");
//            Gson gson = new GsonBuilder().create();
//            //String[] pinStr = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), String[].class);
//            String jsonString = SingletonNetworkStatus.getInstance().getJson().toString();
//            jsonString = jsonString.replace("[", "");
//            jsonString = jsonString.replace("]", "");
//            jsonString = jsonString.replace("\"", "");
//            jsonString = jsonString.replace(" ", "");
//            String[] array = jsonString.split(",");
//            Log.d(TAG, "Pin: " + array[0]);
//            userPin = array[0];
//
//            nextButton.setClickable(true);
        //} else if (sendCmd == CMD_SIGN_IN) {
        if (sendCmd == CMD_SIGN_UP) {
            Log.d(TAG, "CMD_SIGN_UP");
            Gson gson = new GsonBuilder().create();
            DataLoginData obj = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), DataLoginData.class);
            SingletonLoginData.getInstance().setAccessToken(obj.accessToken);
            SingletonLoginData.getInstance().setUserDTO(obj.userDTO);
            if (obj.defaultProfile != null) {
                Log.d(TAG, "defaultProfile: " + obj.defaultProfile.toString());
                Log.d(TAG, "defaultProfileId: " + obj.defaultProfile.getProfileId());
                SingletonLoginData.getInstance().setUserProfiles(Arrays.asList(obj.defaultProfile));
            } else {
                Log.d(TAG, "defaultProfile is null");
            }
            saveLoginData(obj);

            if (fbFriendsIds != null)
                it.putStringArrayListExtra("fbFriendsIds", fbFriendsIds);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (it.getStringExtra("FBJsonString") == null && it.getStringExtra("GoogleJsonString") == null) {
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
            } else {
                Log.d(TAG, "FBJsonString: " + it.getStringExtra("FBJSonString"));
                Log.d(TAG, "GoogleJsonString: " + it.getStringExtra("GoogleJsonString"));
                startActivity(it);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }

        sendCmd = -1;
    }

    private void setCountTextViews() {
        Intent callingIntent = getIntent();
        int googleFriendCount = callingIntent.getIntExtra("GoogleFriendCount", -1);
        if (googleFriendCount == 1) {
            SingletonLoginData.getInstance().getSignupRequest().setGoogleId(getIntent().getStringExtra("GoogleId"));
            googleFriendCountTV.setText("1 friend on Google+");
            googleFriendCountTV.setVisibility(View.VISIBLE);
            connectGoogleButton.setBackgroundResource(R.drawable.connected_google);
            connectGoogleButton.setClickable(false);
        } else if (googleFriendCount != -1) {
            SingletonLoginData.getInstance().getSignupRequest().setGoogleId(getIntent().getStringExtra("GoogleId"));
            googleFriendCountTV.setText(Integer.toString(googleFriendCount) + " friends on Google+");
            googleFriendCountTV.setVisibility(View.VISIBLE);
            connectGoogleButton.setBackgroundResource(R.drawable.connected_google);
            connectGoogleButton.setClickable(false);
        }

        int fbFriendCount = callingIntent.getIntExtra("FBFriendCount", -1);
        if (fbFriendCount == 1) {
            SingletonLoginData.getInstance().getSignupRequest().setFacebookId(getIntent().getStringExtra("FacebookId"));
            facebookFriendCountTV.setText("1 friend on Facebook");
            facebookFriendCountTV.setVisibility(View.VISIBLE);
            connectFacebookButton.setBackgroundResource(R.drawable.connected_facebook);
            connectFacebookButton.setClickable(false);
        } else if (fbFriendCount != -1) {
            SingletonLoginData.getInstance().getSignupRequest().setFacebookId(getIntent().getStringExtra("FacebookId"));
            facebookFriendCountTV.setText(Integer.toString(fbFriendCount) + " friends on Facebook");
            facebookFriendCountTV.setVisibility(View.VISIBLE);
            connectFacebookButton.setBackgroundResource(R.drawable.connected_facebook);
            connectFacebookButton.setClickable(false);
        }
    }

}
