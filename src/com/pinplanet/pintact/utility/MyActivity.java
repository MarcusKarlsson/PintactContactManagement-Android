package com.pinplanet.pintact.utility;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.DeviceRegistrationRequest;
import com.pinplanet.pintact.data.DeviceRegistrationRequest.DeviceType;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MyActivity extends Activity {

    protected final String TAG = this.getClass().getName();

    private boolean isActive;

    TextView title, rightText, leftText;
    SearchView search;
    ImageView imgLeft, imgRight;

    private boolean searchClosed = true;

    // adapted from sample GCM code
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "344518771652";


    GoogleCloudMessaging gcm;
    Context context;

    String regid;
    // end of sample code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //helps to detect which Activity is currently open
        Log.i(TAG, "onCreate()");

        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.action_bar_all);

        //hide the drawer icon...
        actionBar.setDisplayHomeAsUpEnabled(false);

        title = (TextView) findViewById(R.id.actionBar);
        imgLeft = (ImageView) findViewById(R.id.actionBarMenu);
        imgRight = (ImageView) findViewById(R.id.actionBarRightImage);
        rightText = (TextView) findViewById(R.id.actionBarRightText);
        leftText = (TextView) findViewById(R.id.actionBarLeftText);
        search = (SearchView) findViewById(R.id.actionBarSearch);

        AutoCompleteTextView search_text = (AutoCompleteTextView) search.findViewById(search.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        //search_text.setTextColor(Color.WHITE);
        search_text.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        search_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.TEXT_SIZE_NORMAL));

        search.setVisibility(View.INVISIBLE);
    }

    public void showSearch(int resHint, boolean focus) {
        title.setVisibility(View.INVISIBLE);
        search.setVisibility(View.VISIBLE);

        searchClosed = false;

        if (resHint != -1)
            search.setQueryHint(getString(resHint));

        if (focus)
            search.requestFocus();
    }

    public void refreshSearch() {
        search.setQuery(search.getQuery(), true);
    }

    public void showSearch() {
        searchClosed = false;
        search.setVisibility(View.VISIBLE);
    }

    public void hideSearch() {
        search.setVisibility(View.INVISIBLE);
    }

    public void hideAndCloseSearch() {
        searchClosed = true;
        search.setVisibility(View.VISIBLE);
    }

    public boolean isSearchClosed() {
        return searchClosed;
    }

    public void setSearchTextQueryListener(SearchView.OnQueryTextListener tl) {
        search.setOnQueryTextListener(tl);
    }

    public void setSearchText(String txt) {
        search.setQuery(txt, true);
    }

    public void hideLeft() {
        imgLeft.setVisibility(View.GONE);
    }

    public void showTitle(String str) {
        searchClosed = true;
        title.setVisibility(View.VISIBLE);
        search.setVisibility(View.INVISIBLE);
        title.setText(str);
    }

    public void showTitle(int txtRes) {
        searchClosed = true;
        title.setVisibility(View.VISIBLE);
        search.setVisibility(View.INVISIBLE);
        title.setText(txtRes);
    }

    public void hideTitle() {
        title.setVisibility(View.INVISIBLE);
        search.setVisibility(View.INVISIBLE);
    }

    public boolean isRightImgVisible() {
        return View.VISIBLE == imgRight.getVisibility();
    }

    public boolean isRightTextVisible() {
        return View.VISIBLE == rightText.getVisibility();
    }

    public void hideRight() {
        rightText.setVisibility(View.GONE);
        imgRight.setVisibility(View.GONE);
    }

    //Set one right to gone and one to invisible so formatting remains the same
    //Used for chat title
    public void hideOneRight() {
        rightText.setVisibility(View.GONE);
        imgRight.setVisibility(View.INVISIBLE);
    }

    public void showRightText() {
        rightText.setVisibility(View.VISIBLE);
    }

    public void showLeftText() {
        leftText.setVisibility(View.VISIBLE);
    }

    public void showRightText(String txt) {
        rightText.setText(txt);
        showRightText();
    }

    public void showLeftText(int txtRes) {
        leftText.setText(txtRes);
        showLeftText();
    }

    public void showRightText(int txtRes) {
        rightText.setText(txtRes);
        showRightText();
        ;
    }

    public void showLeftImage(int resId) {
        imgLeft.setImageResource(resId);
        imgLeft.setVisibility(View.VISIBLE);
    }

    public void showRightImage(int resId) {
        if (resId != 0)
            imgRight.setImageResource(resId);
        imgRight.setVisibility(View.VISIBLE);
    }

    public String getRightText() {
        return rightText.getText().toString();
    }


    public void addRightTextClickListener(View.OnClickListener ln) {
        rightText.setOnClickListener(ln);
    }

    public void addLeftTextClickListener(View.OnClickListener ln) {
        leftText.setOnClickListener(ln);
    }

    public void addRightImageClickListener(View.OnClickListener ln) {
        imgRight.setOnClickListener(ln);
    }

    public void addLeftClickListener(View.OnClickListener ln) {
        imgLeft.setOnClickListener(ln);
    }

    public void hideBoth() {
        hideLeft();
        hideRight();
    }

    public void onPostNetwork() {

    }

    public void myDialog(int title, int info) {
        myDialog(getString(title), getString(info));
    }

    public void myDialog(String title, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(info);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.generic_error_dialog_dismiss,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void updatePreferencesSort(int value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.set_sort_setting), value);
        editor.commit();
        SingletonLoginData.getInstance().getUserSettings().sort = value;

    }

    public void updatePreferencesLocal(int value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.set_show_native_contacts), value);
        editor.commit();
        SingletonLoginData.getInstance().getUserSettings().local = value;

        System.out.println("local:" + value);

        // clear local contact info
        if (value == 0) {
            SingletonLoginData.getInstance().setLocalContactList(new ArrayList<ContactDTO>());
            AppService.initContactList(true);
        } else {
            new LoadContactAsyncTask(this).execute();
        }

    }

    public void loadPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int sort = sharedPref.getInt(getString(R.string.set_sort_setting), 0);
        int local = sharedPref.getInt(getString(R.string.set_show_native_contacts), 1);
        int push = sharedPref.getInt(getString(R.string.set_push_notifications), 1);
        SingletonLoginData.getInstance().getUserSettings().sort = sort;
        SingletonLoginData.getInstance().getUserSettings().local = local;

        System.out.println("sort, local, push:" + sort + " " + local + " " + push);
        if (local == 1) {
            loadLocalContactsSummary();
            new LoadContactAsyncTask().execute();
        }

    }

    public class LoadContactAsyncTask extends AsyncTask {

        private ProgressDialog dialog;
        private MyActivity activity;

        public LoadContactAsyncTask() {
        }

        public LoadContactAsyncTask(MyActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            if (this.activity != null) {
                dialog = new ProgressDialog(this.activity);
                dialog.setMessage(AppController.getInstance().getString(R.string.DIALOG_MESSAGE_PLEASE_WAIT));
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            if (this.activity != null && dialog != null && dialog.isShowing() && this.activity.isActive()) {
                dialog.dismiss();
            }
            if (SingletonLoginData.getInstance().getContactListChangeListner() != null) {
                SingletonLoginData.getInstance().getContactListChangeListner().contactListChanged();
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            loadLocalContacts();
            AppService.checkIfLocalContactsRegistered();
            AppService.setLabels();
            AppService.setUserProfileChildLabels();
            return null;
        }
    }

    public void loadLocalContactsSummary() {
        System.out.println("Load local contact started");
        Set<String> pintactNativeIds = ContactTableDbInterface.getInstance().getAllPintactNativeContacts();
        Set<String> pintactNativeContactIds = new HashSet<String>();
        for (String rawId : pintactNativeIds) {
            pintactNativeContactIds.add(AppService.getContactIdOfRawContactid(rawId));
        }
        System.out.println("Native loaded : ");
        Cursor cur = this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Data.IN_VISIBLE_GROUP + " != 0", new String[]{}, null);
        System.out.println("Count : ");
        ContactDTO contact;

        HashMap<String, ContactDTO> allContacts = new HashMap<String, ContactDTO>(100);

        //Log.i(TAG,"loadLocalContacts() count:"+cur.getCount());

        if (cur.getCount() > 0) {

            int contactidIndex = cur.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int photoUriIndex = cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);

            while (cur.moveToNext()) {

                String id = cur.getString(contactidIndex);
                //String groupType = cur.getString(visibleGroupIndex);
                if (!pintactNativeContactIds.contains(id)) {
                    // String mimeType = cur.getString(mimeIndex);
                    List<UserProfileAttribute> attributes = null;
                    UserProfile userProfile = null;
                    UserDTO user = null;
                    if (allContacts.containsKey(id)) {
                        // update contact
                        contact = allContacts.get(id);
                        ProfileDTO profileDTO = contact.getSharedProfiles().get(0);
                        attributes = profileDTO.getUserProfileAttributes();
                        userProfile = profileDTO.getUserProfile();
                        user = contact.getContactUser();
                    } else {
                        contact = new ContactDTO();
                        contact.localContactId = id;
                        contact.isLocalContact = true;
                        List<ProfileDTO> profileDTOs = new ArrayList<ProfileDTO>();
                        ProfileDTO profileDTO = new ProfileDTO();
                        attributes = new ArrayList<UserProfileAttribute>();
                        userProfile = new UserProfile();
                        user = new UserDTO();
                        profileDTO.setUserProfile(userProfile);
                        profileDTO.setUserProfileAttributes(attributes);
                        profileDTO.setUserProfileAddresses(new ArrayList<UserProfileAddress>());
                        profileDTOs.add(profileDTO);

                        userProfile.setPathToImage(cur.getString(photoUriIndex));
                        contact.setSharedProfiles(profileDTOs);
                        contact.setContactUser(user);
                        allContacts.put(id, contact);
                    }


                    String dName = cur.getString(displayNameIndex);
                    if (dName == null)
                        dName = "";

                    int pos = dName.lastIndexOf(' ');
                    if (pos == -1) {
                        userProfile.setFirstName(dName);
                        user.setFirstName(dName);
                    } else {
                        String fn = dName.substring(0, pos);
                        String ln = "";
                        // in case the last name was followed by an extra ' ';
                        if (pos != dName.length() - 1)
                            ln = dName.substring(pos + 1, dName.length());
                        // set name
                        user.setFirstName(fn);
                        user.setLastName(ln);
                        userProfile.setFirstName(fn);
                        userProfile.setLastName(ln);
                    }
                }

            }

        }
        List<ContactDTO> list = new ArrayList<ContactDTO>(allContacts.values());
        Iterator<ContactDTO> it = list.iterator();
        while (it.hasNext()) {
            ContactDTO _contact = it.next();
            if (_contact.getContactUser().getFirstName() == null || _contact.getContactUser().getFirstName().length() == 0) {
                it.remove();
            }
        }

        cur.close();
        SingletonLoginData.getInstance().setLocalContactList(list);
        AppService.initContactList(true);
        System.out.println("Total local contacts loaded: " + list.size());
    }

    public void loadLocalContacts() {

        System.out.println("Load local contact started");
        Set<String> pintactNativeIds = ContactTableDbInterface.getInstance().getAllPintactNativeContacts();
        Set<String> pintactNativeContactIds = new HashSet<String>();
        for (String rawId : pintactNativeIds) {
            pintactNativeContactIds.add(AppService.getContactIdOfRawContactid(rawId));
        }

        Cursor cur = this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.LABEL,
                ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.LABEL, ContactsContract.CommonDataKinds.Photo.PHOTO_URI
                , ContactsContract.CommonDataKinds.Note.NOTE, ContactsContract.CommonDataKinds.Im.DATA,
                ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, ContactsContract.Data.IN_VISIBLE_GROUP}, ContactsContract.Data.IN_VISIBLE_GROUP + " != 0", new String[]{}, null);

        ContactDTO contact;

        HashMap<String, ContactDTO> allContacts = new HashMap<String, ContactDTO>(100);
        System.out.println("Count : " + cur.getCount());
        //Log.i(TAG,"loadLocalContacts() count:"+cur.getCount());

        if (cur.getCount() > 0) {

            int contactidIndex = cur.getColumnIndex(ContactsContract.Data.CONTACT_ID);
            int visibleGroupIndex = cur.getColumnIndex(ContactsContract.Data.IN_VISIBLE_GROUP);
            int mimeIndex = cur.getColumnIndex(ContactsContract.Data.MIMETYPE);
            int displayNameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int phoneLabelIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
            int phoneNumberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int emailLabelIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
            int emailDataIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            int noteColumnIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE);
            int protocolIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL);
            int customProtocolIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
            int imDataIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
            int photoUriIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI);

            while (cur.moveToNext()) {

                String id = cur.getString(contactidIndex);
                String groupType = cur.getString(visibleGroupIndex);
                if (!pintactNativeContactIds.contains(id) && groupType.equals("1")) {
                    String mimeType = cur.getString(mimeIndex);
                    List<UserProfileAttribute> attributes = null;
                    UserProfile userProfile = null;
                    UserDTO user = null;
                    if (allContacts.containsKey(id)) {
                        // update contact
                        contact = allContacts.get(id);
                        ProfileDTO profileDTO = contact.getSharedProfiles().get(0);
                        attributes = profileDTO.getUserProfileAttributes();
                        userProfile = profileDTO.getUserProfile();
                        user = contact.getContactUser();
                    } else {
                        contact = new ContactDTO();
                        contact.localContactId = id;
                        contact.isLocalContact = true;
                        List<ProfileDTO> profileDTOs = new ArrayList<ProfileDTO>();
                        ProfileDTO profileDTO = new ProfileDTO();
                        attributes = new ArrayList<UserProfileAttribute>();
                        userProfile = new UserProfile();
                        user = new UserDTO();
                        profileDTO.setUserProfile(userProfile);
                        profileDTO.setUserProfileAttributes(attributes);
                        profileDTO.setUserProfileAddresses(new ArrayList<UserProfileAddress>());
                        profileDTOs.add(profileDTO);
                        contact.setSharedProfiles(profileDTOs);
                        contact.setContactUser(user);
                        allContacts.put(id, contact);
                    }

                    if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        String dName = cur.getString(displayNameIndex);
                        if (dName == null)
                            dName = "";

                        int pos = dName.lastIndexOf(' ');
                        if (pos == -1) {
                            userProfile.setFirstName(dName);
                            user.setFirstName(dName);
                        } else {
                            String fn = dName.substring(0, pos);
                            String ln = "";
                            // in case the last name was followed by an extra ' ';
                            if (pos != dName.length() - 1)
                                ln = dName.substring(pos + 1, dName.length());
                            // set name
                            user.setFirstName(fn);
                            user.setLastName(ln);
                            userProfile.setFirstName(fn);
                            userProfile.setLastName(ln);
                        }
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) && cur.getString(phoneNumberIndex) != null) {
                        // set phone munber
                        attributes.add(new UserProfileAttribute(AttributeType.PHONE_NUMBER, cur.getString(phoneLabelIndex), cur.getString(phoneNumberIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) && cur.getString(emailDataIndex) != null) {
                        // set email
                        attributes.add(new UserProfileAttribute(AttributeType.EMAIL, cur.getString(emailLabelIndex), cur.getString(emailDataIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE) && cur.getString(photoUriIndex) != null) {
                        userProfile.setPathToImage(cur.getString(photoUriIndex));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE) && cur.getString(noteColumnIndex) != null) {
                        attributes.add(new UserProfileAttribute(AttributeType.PRIVATE_NOTE, null, cur.getString(noteColumnIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) && cur.getString(imDataIndex) != null) {
                        attributes.add(new UserProfileAttribute(AttributeType.SERVICE_ID, ContactsContract.CommonDataKinds.Im.getProtocolLabel(getResources(), cur.getInt(protocolIndex), cur.getString(customProtocolIndex)).toString(), cur.getString(imDataIndex)));
                    }

                }
            }
        }

        Collection<ContactDTO> list = allContacts.values();
        Iterator<ContactDTO> it = list.iterator();
        while (it.hasNext()) {
            ContactDTO _contact = it.next();
            if (_contact.getContactUser().getFirstName() == null || _contact.getContactUser().getFirstName().length() == 0) {
                it.remove();
            }
        }

        SingletonLoginData.getInstance().setLocalContactList(list);
        SingletonLoginData.getInstance().setLocalContactSearchMap(ContactTableDbInterface.getInstance().loadIsPintact());
        AppService.initContactList(true);
        cur.close();
        SingletonLoginData.getInstance().setAllLocalContacts(allContacts);
        System.out.println("Total local contacts loaded: " + allContacts.size());
    }


    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isActive = false;
    }

    public void postGetRegistrationID() {
        // sample code testing
        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            // need to always send as may be for new account - let server figure that out
            registerInBackground();
        } else {
            System.out.println("No valid Google Play Services APK found.");
            return;
        }
        // end of testing
    }

    public void postLoginGetRegister() {

        if (regid == null || regid.isEmpty()) {
            // wait for regid;
            System.out.println("Something is wrong with regid!!!!");
            // don't try to register without ID
            return;
        }

        // Device information
        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        String info = "device_id:" + deviceId + ",made:" +
                Build.MANUFACTURER + ",model:" + Build.MODEL + ",sdk:" +
                Build.VERSION.SDK_INT + ",release:" + Build.VERSION.RELEASE + ",density:" +
                getResources().getDisplayMetrics().density + ",dpi:" +
                getResources().getDisplayMetrics().densityDpi + ",width:" +
                getResources().getDisplayMetrics().widthPixels + ",height:" +
                getResources().getDisplayMetrics().heightPixels;

        DeviceRegistrationRequest data = new DeviceRegistrationRequest();
        data.setDeviceId(regid);
        data.setClientInfo(info);
        data.setStaticId(deviceId);
        data.setDeviceType(DeviceType.ANDROID);
        data.setUserPreference(true);
        data.setUserId(SingletonLoginData.getInstance().getUserData().getId());
        data.setProduction(true);
        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(data);

        String path = "/api/device/register.json?" + SingletonLoginData.getInstance().getPostParam();
        new RestServiceAsync(null).execute(path, params, "POST");
    }

    // copy from sample code

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        //TODO: enable later again...

//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                System.out.println("This device is not supported.");
//                finish();
//            }
//            return false;
//        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        System.out.println("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);

        editor.commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();
        editor.putString("deviceRegistered", "true");
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            System.out.println("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            System.out.println("App version changed.");
            return "";
        }
        System.out.println("Registration ID is [" + registrationId + "]");
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                        regid = gcm.register(SENDER_ID);
                    }
                    //regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    postLoginGetRegister();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                System.out.println("My Registration ID: [" + msg + "]");
            }
        }).start();
    }

}
