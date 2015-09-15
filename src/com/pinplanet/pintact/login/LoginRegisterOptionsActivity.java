package com.pinplanet.pintact.login;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.facebook.login.widget.LoginButton;

//import com.linkedin.platform.LISession;
//import com.linkedin.platform.LISessionManager;
//import com.linkedin.platform.errors.LIAuthError;
//import com.linkedin.platform.listeners.AuthListener;
//import com.linkedin.platform.utils.Scope;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static com.pinplanet.pintact.utility.UiControllerUtil.myDialog;

//Activity to give user option of registering through
//Facebook or manually
public class LoginRegisterOptionsActivity extends MyActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {
    private static final String TAG = LoginRegisterOptionsActivity.class.getName();
    private static final int RC_SIGN_IN = 0;

    private int sendCmd = -1;
    private static int CMD_REQUEST_FB = 0;
    private static int CMD_REQUEST_GOOGLE = 1;

    CallbackManager callbackManager;
    Intent loginRegisterIntent;
    GoogleApiClient mGoogleApiClient;
    Bundle parameters;

    int googleFriendCount, fbFriendCount;

    /**
     * True if the sign-in button was clicked.  When true, we know to resolve all
     * issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked = false;

    /**
     * True if we are in the process of resolving a ConnectionResult
     */
    private boolean mIntentInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showTitle("REGISTER");
        hideBoth();
        FacebookSdk.sdkInitialize(getApplicationContext());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        setContentView(R.layout.login_register_options);

//        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
//            @Override
//            public void onAuthSuccess() {
//                // Authentication was successful.  You can now do
//                // other calls with the SDK.
//                Log.d(TAG, "LI Auth Success");
//            }
//
//            @Override
//            public void onAuthError(LIAuthError error) {
//                // Handle authentication errors
//                Log.d(TAG, "LI Error: " + error.toString());
//            }
//        }, true);


//        LoginButton authButton = (LoginButton) findViewById(R.id.facebook_login_button);
//        authButton.setReadPermissions(Arrays.asList("friends"));

        //If we need to get the currently logged in user, this is how to
//        Profile profile2 = Profile.getCurrentProfile();
//        if (profile2 != null) {
//            Log.d(TAG, profile2.getName());
//        } else {
//            Log.d(TAG, "profile2 null");
//        }

        //Set the Facebook login button callback
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Logged in to Facebook");
                        getFacebookFriends();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Log in cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG, e.toString());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
        // LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    public void facebookLoginClicked(View view) {
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_friends"));
    }

    public void googleLoginClicked(View view) {
        Log.d(TAG, "Google Login clicked");
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
            mSignInClicked = true;
        }
    }

    public void registerManuallyClicked(View view) {
        loginRegisterIntent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
        startActivity(loginRegisterIntent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Signed into GooglePlus Here");
        Plus.PeopleApi.loadConnected(mGoogleApiClient)
                .setResultCallback(this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended here");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed with result: " + connectionResult);
        if (!mIntentInProgress && connectionResult.hasResolution()) {
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

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        ArrayList<String> tempArray = new ArrayList<>();
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                googleFriendCount = personBuffer.getCount();
                for (int i = 0; i < googleFriendCount; i++) {
                    Log.d(TAG, "i: " + i + " count: " + googleFriendCount);
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
            facebookStartActivity(SingletonNetworkStatus.getInstance().getJson().toString());
        }
        if (sendCmd == CMD_REQUEST_GOOGLE) {
            Log.d(TAG, "CMD_REQUEST_GOOGLE");
            Log.d(TAG, "TESTJSON: " + SingletonNetworkStatus.getInstance().getJson().toString());
            googleStartActvity(SingletonNetworkStatus.getInstance().getJson().toString());
        }
        sendCmd = -1;
    }

    private void googleStartActvity(String jsonString) {
        String personFirstName, personLastName = null;
        char personMiddleInitial = 0;
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String userId = currentPerson.getId();
            String personName = currentPerson.getDisplayName();
            String[] personNameArray = personName.split(" ");
            personFirstName = personNameArray[0];
            if (personNameArray.length == 2) {
                personLastName = personNameArray[1];
            } else if (personNameArray.length >= 2) {
                personLastName = personNameArray[2];
                personMiddleInitial = personNameArray[1].charAt(0);
            }
            String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Log.d(TAG, personName + " " + personEmail);
            //Attempt to get each name, if successful, pass into intent
            loginRegisterIntent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
            if (!userId.equals(""))
                loginRegisterIntent.putExtra("GoogleId", userId);
            if (!personFirstName.equals(""))
                loginRegisterIntent.putExtra("FirstName", personFirstName);
            if (personMiddleInitial != 0)
                loginRegisterIntent.putExtra("MiddleInitial", personMiddleInitial);
            if (personLastName != null)
                loginRegisterIntent.putExtra("LastName", personLastName);
            if (personEmail != null) {
                loginRegisterIntent.putExtra("Email", personEmail);
            }
            if (jsonString != null) {
                loginRegisterIntent.putExtra("GoogleJsonString", jsonString);
            }
            loginRegisterIntent.putExtra("GoogleFriendCount", googleFriendCount);

            String profilePictureUri = Uri.parse(currentPerson.getImage().getUrl()).toString();
            StringBuilder sb = new StringBuilder(profilePictureUri);
            sb.delete(sb.length() - 2, sb.length());
            sb.append("200");
            profilePictureUri = sb.toString();
            Log.d(TAG, "GOOGLEURL: " + profilePictureUri);
            if (!profilePictureUri.equals(""))
                loginRegisterIntent.putExtra("ProfilePictureUri", profilePictureUri);
            startActivity(loginRegisterIntent);
        }
    }

    private void getFacebookFriends() {
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
                                    JSONArray friendsArray = object.getJSONObject("friends").getJSONArray("data");
                                    fbFriendCount = friendsArray.length();
                                    requestInfo("FB", getFacebookFriendsIds(friendsArray));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                });
        parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture,friends");
        request.setParameters(parameters);
        request.executeAsync();

    }

    public ArrayList<String> getFacebookFriendsIds(JSONArray friendsArray) {
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

    private void facebookStartActivity(String jsonString) {
        Profile profile = Profile.getCurrentProfile();
        //If profile was fetched
        if (profile != null) {
            //Attempt to get each name, if successful, pass into intent
            loginRegisterIntent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
            if (!profile.getFirstName().equals(""))
                loginRegisterIntent.putExtra("FirstName", profile.getFirstName());
            if (!profile.getMiddleName().equals(""))
                loginRegisterIntent.putExtra("MiddleInitial", profile.getMiddleName().charAt(0));
            if (!profile.getLastName().equals(""))
                loginRegisterIntent.putExtra("LastName", profile.getLastName());
            if (!profile.getId().equals(""))
                loginRegisterIntent.putExtra("FacebookId", profile.getId());
            String profilePictureUri = profile.getProfilePictureUri(220, 220).toString();
            if (!profilePictureUri.equals(""))
                loginRegisterIntent.putExtra("ProfilePictureUri", profilePictureUri);
            if (jsonString != null) {
                loginRegisterIntent.putExtra("FBJsonString", jsonString);
            }
            loginRegisterIntent.putExtra("FBFriendCount", fbFriendCount);
            startActivity(loginRegisterIntent);
        } else {
            Log.d(TAG, "Profile is null");
        }
    }
    // Build the list of member permissions our LinkedIn session requires
//    private static Scope buildScope() {
//        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
//    }
}
