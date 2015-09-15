package com.pinplanet.pintact;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.contact.ActionButtonType;
import com.pinplanet.pintact.contact.ContactAddActivity;
import com.pinplanet.pintact.contact.ContactFindActivity;
import com.pinplanet.pintact.contact.ContactInviteActivity;
import com.pinplanet.pintact.contact.EmptyViewType;
import com.pinplanet.pintact.contact.FragmentPintactsList;
import com.pinplanet.pintact.contact.PintactActionType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.group.GroupContactsActivity;
import com.pinplanet.pintact.group.GroupProfileShareActivity;
import com.pinplanet.pintact.label.LabelContactsActivity;
import com.pinplanet.pintact.leftdeck.searchResult;
import com.pinplanet.pintact.leftdeck.searchResultAdapter;
import com.pinplanet.pintact.notification.FragmentNotifications;
import com.pinplanet.pintact.setting.AboutPintactActivity;
import com.pinplanet.pintact.setting.ChangePasswordActivity;
import com.pinplanet.pintact.setting.SearchSettingActivity;
import com.pinplanet.pintact.setting.SettingSortActivity;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class LeftDeckActivity extends MyActivity {

    private static final String TAG = LeftDeckActivity.class.getName();


    static final int OPTION_PROFILE = 0;
    static final int OPTION_SETTING = 1;
    static final int OPTION_CONTACT = 2;
    static final int OPTION_GROUP = 3;
    static final int OPTION_LABEL = 4;
    static final int OPTION_NOTIFY = 5;
    public static String SELECTED_OPTIONS = "leftdeck_selected_option";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;

    View tableView, searchView;
    int tableIndex;
    boolean isFirstTime = true;
    boolean isSearchPage = false;
    boolean hadRightImg = false;
    boolean hadRightText = false;
    boolean restoreActionBar = false;
    int labelOp = 0;
    ArrayList<View> mSubLabelItems;
    View mSubLabelView;
    View mAddView; // for label
    View leftDeckView;

    // for loading
    int mSelectedItem = OPTION_CONTACT;
    boolean isLoggingOut = false;
    boolean isLoadingGroup = false;
    boolean isQueryJoinedGroup = false;

    TextViewTypeFace textViewNotificationCounter;
    RelativeLayout notificationLayout;

    //private final Handler mDrawerHandler = new Handler();


    public void setNotificationCountOrDisable() {
        if (textViewNotificationCounter != null) {
            final int notifications = SingletonLoginData.getInstance().getTotalUnseenNoti();

            textViewNotificationCounter.setText("" + notifications);
            notificationLayout.setVisibility((notifications > 0) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // for reject
    int mRejectStep = 0;
    int mNotifyIndex = 0;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.leftdeck_navigation_main);
        hideRight();



        //showDialog();
        loadPreferences();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setFocusableInTouchMode(false);

        leftDeckView = findViewById(R.id.left_drawer);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);

        notificationLayout = (RelativeLayout) findViewById(R.id.notificationView);
        textViewNotificationCounter = (TextViewTypeFace) findViewById(R.id.textViewNotificationCounter);

        setNotificationCountOrDisable();
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.transparent_point,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                Log.d(TAG, "onDrawerClosed()");

                if (restoreActionBar) {
                    //we check if the search was active before we opened the drawer
                    if (!isSearchClosed()) {
                        showSearch();
                        showRightImage(0);
                    } else if (mTitle != null)//set the title if no search is needed
                    {
                        showTitle(mTitle.toString());
                        if (hadRightImg) {
                            showRightImage(0);
                        }
                        if (hadRightText) {
                            showRightText();
                        }
                    }
                    restoreActionBar = false;
                }

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                Log.d(TAG, "onDrawerOpened()");
                hadRightImg = isRightImgVisible();
                hadRightText = isRightTextVisible();
                hideRight();
                ((MyActivity) drawerView.getContext()).hideSoftKeyboard((MyActivity) drawerView.getContext());
                hideTitle();
                hideSearch();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                setNotificationCountOrDisable();
                restoreActionBar = true;
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // go back to login page if not register yet
        String accessToken = SingletonLoginData.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty() || accessToken.length() < 1) {
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selectItem(bundle.getInt(SELECTED_OPTIONS));

            // cancel all notifications if any
            if (SingletonLoginData.getInstance().mNotificationManager != null) {
                SingletonLoginData.getInstance().mNotificationManager.cancelAll();
                SingletonLoginData.getInstance().mNotificationManager = null;
            }
        } else if (savedInstanceState == null) {
            selectItem(OPTION_CONTACT);
        }

        ImageView abIV = (ImageView) findViewById(R.id.actionBarMenu);
        abIV.setClickable(true);
        abIV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tvAdd = (TextView) findViewById(R.id.ldm_add_pintact);
        tvAdd.setClickable(true);
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddContact();
            }
        });

        //closeDialog();
        if (getIntent().getStringExtra("NotificationGroupId") != null) {
            //loadingGroup(getIntent().getStringExtra("NotificationGroupId"));
            selectItem(OPTION_GROUP);
        }
    }

    public void toggleDrawer() {
        if (mDrawerLayout.isDrawerOpen(leftDeckView)) {
            mDrawerLayout.closeDrawer(leftDeckView);
        } else {
            mDrawerLayout.openDrawer(leftDeckView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setNotificationCountOrDisable();
        System.out.println("Onresume - Activity.");
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().containsKey("DIALOG_TITLE_RES_ID")) {
            this.myDialog((Integer) intent.getExtras().get("DIALOG_TITLE_RES_ID"),
                    (Integer) intent.getExtras().get("DIALOG_MESSAGE_RES_ID"));
        }
    }

    @Override
    public void onBackPressed() {
        if (isFinishing()) return;

        Log.d(TAG, "OnBackPressed - getBackStackEntryCount():" + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            Log.d(TAG, "OnBackPressed - isDrawerOpen():" + mDrawerLayout.isDrawerOpen(leftDeckView));
            if (!mDrawerLayout.isDrawerOpen(leftDeckView)) {
                toggleDrawer();
                return;
            }

            moveTaskToBack(true);
        }
//    moveTaskToBack(true);

        return;
    }

    public void onAddContact() {
        Intent myIntent = new Intent(this, ContactAddActivity.class);
        startActivity(myIntent);
    }

    public void setLabelOpPost(int op, View v, View subV, ArrayList<View> list) {
        labelOp = op;
        mSubLabelView = v;
        mAddView = subV;
        mSubLabelItems = list;
    }

    public void onAcceptInvite(View v) {

        String value = ((TextView) ((View) v.getParent()).findViewById(R.id.for_data)).getText().toString();
        System.out.println("Activity Accept clicked - value " + value);

        int index = Integer.valueOf(value);
        ContactShareRequest req = new ContactShareRequest();
        Long destId = SingletonLoginData.getInstance().getNotifications().getData().get(index).getData().sender.id;
        req.setDestinationUserId(destId);
        req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        SingletonLoginData.getInstance().setContactShareRequest(req);

        Intent myIntent = new Intent(this, GroupProfileShareActivity.class);
        myIntent.putExtra(GroupProfileShareActivity.ARG_PROFILE_SHARE, index);
        startActivity(myIntent);
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
                        rejectInvite();
                    }
                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    public void onRejectInvite(View v) {

        String value = ((TextView) ((View) v.getParent()).findViewById(R.id.for_data1)).getText().toString();
        System.out.println("Activity Rejected clicked - value " + value);
        mNotifyIndex = Integer.valueOf(value);

        alertDialog("Confirm Rejection", "Are you sure you want to reject this invitation?");
    }

    public void rejectInvite() {

        int index = mNotifyIndex;
        mRejectStep = 1;
        Long destId = SingletonLoginData.getInstance().getNotifications().getData().get(index).getData().sender.id;
        String path = "/api/contacts/" + destId + "/reject.json?" + SingletonLoginData.getInstance().getPostParam();

        SingletonNetworkStatus.getInstance().clean();
        SingletonNetworkStatus.getInstance().setDoNotDismissDialog(true);
        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, "", "POST");

    }

    public void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent it = new Intent(this, LeftDeckActivity.class);
        it.putExtra(LeftDeckActivity.SELECTED_OPTIONS, OPTION_NOTIFY);
        // add the following line would show Pintact to the preview page.
        // it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Pintact Update")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public void logout() {
        isLoggingOut = true;
        String path = "/api/users/logout.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, "", "POST");
    }

    public void loadLabel(String label) {
        SingletonLoginData.getInstance().setCurrentLabel(label);
        Intent myIntent = new Intent(this, LabelContactsActivity.class);
        startActivity(myIntent);
    }

    public void loadingGroup(String group) {
        isLoadingGroup = true;
        String path = "/api/group/" + Uri.encode(group, "utf-8") + "/members.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, "", "GET");
    }

    private List<searchResult> getData() {
        List<searchResult> list = new ArrayList<searchResult>();
        list.add(new searchResult("Maria Stenson", "Brand Evangelist, Center"));
        list.add(new searchResult("Morgan Stanley", "Investment Banking, NYC"));
        list.add(new searchResult("Maria Stenson", "Brand Evangelist, Center"));
        list.add(new searchResult("Morgan Stanley", "Investment Banking, NYC"));
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        /// COMMENT  boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(leftDeckView);

        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
    /*
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
     */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

  /* The click listner for ListView in the navigation drawer */
  /*
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
   */

    @SuppressLint("NewApi")
    private void selectItem(int position) {

        mSelectedItem = position;

        // do network access
        // construct URL
        String path = "";
        SingletonNetworkStatus.getInstance().setActivity(this);
        switch (position) {
            case OPTION_CONTACT:
                AppService.checkIfThereIsAnyContacts();
                onPostNetwork();
                break;

            case OPTION_PROFILE:
                setFragment(position);

                mDrawerLayout.closeDrawer(leftDeckView);
                break;

            case OPTION_NOTIFY:

                setFragment(position);
                mDrawerLayout.closeDrawer(leftDeckView);
                break;

            case OPTION_LABEL:
                setFragment(position);
                mDrawerLayout.closeDrawer(leftDeckView);

                break;

            case OPTION_GROUP:
                setFragment(position);
                mDrawerLayout.closeDrawer(leftDeckView);
                break;

            default:
                onPostNetwork();
                break;
        }

    }

    @SuppressLint("NewApi")
    public void onPostNetwork() {

        SingletonNetworkStatus.getInstance().setActivity(this);

        final int position = mSelectedItem;

        if (SingletonNetworkStatus.getInstance().getCode() != 0 && SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            SingletonNetworkStatus.getInstance().setCode(0);

            if (labelOp > 0)
                labelOp = 0;

            if (mRejectStep > 0)
                mRejectStep = 0;

            if (isLoadingGroup) {
                isLoadingGroup = false;
            }

            return;
        }

        if (isLoadingGroup) {
            isLoadingGroup = false;

            // get the data
            Log.d(TAG, "LeftDeckActivityJson: " + SingletonNetworkStatus.getInstance().getJson());
            Type collectionType = new TypeToken<Collection<ContactDTO>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Collection<ContactDTO> contacts = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
            SingletonLoginData.getInstance().setGroupContacts(new ArrayList<ContactDTO>(contacts));

            Intent myIntent = new Intent(this, GroupContactsActivity.class);
            startActivity(myIntent);

            return;
        }

        if (isQueryJoinedGroup) {
            isQueryJoinedGroup = false;

            Type collectionType = new TypeToken<Collection<GroupDTO>>() {
            }.getType();
            Gson gson = new GsonBuilder().create();
            Collection<GroupDTO> groups = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
            if (groups == null) {
                groups = new ArrayList<GroupDTO>();
            }
            SingletonLoginData.getInstance().setJoinedGroups(new ArrayList<GroupDTO>(groups));
        }

        if (position == OPTION_SETTING && isLoggingOut) {
            // clear data
            SingletonLoginData.getInstance().clean();
            SingletonNetworkStatus.getInstance().clean();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(getString(R.string.login_username));
            editor.remove(getString(R.string.access_token));
            editor.commit();

            AppService.reInit();
            // return to login page
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        // for Add/delete labels
        if (labelOp > 0) {

            if (labelOp == 1) // Add label
            {
                EditText inputET = (EditText) mAddView.findViewById(R.id.label_input);
                TextView labelTV = (TextView) mAddView.findViewById(R.id.label_view);
                TextView addTV = (TextView) mAddView.findViewById(R.id.view_add);
                String label = inputET.getText().toString();
                labelTV.setText(label);
                inputET.setVisibility(View.GONE);
                labelTV.setVisibility(View.VISIBLE);
                addTV.setVisibility(View.INVISIBLE);

                float dimen = mAddView.getContext().getResources().getDimension(R.dimen.label_left_margin_expand);
                View tvLabel = mAddView.findViewById(R.id.label_text_container);
                RelativeLayout.LayoutParams layoutParams =
                        (RelativeLayout.LayoutParams) tvLabel.getLayoutParams();
                layoutParams.setMargins(Math.round(dimen), 0, 0, 0);
                tvLabel.setLayoutParams(layoutParams);

                RelativeLayout rlo = (RelativeLayout) mSubLabelView.findViewById(R.id.lm_clkLO);
                rlo.setClickable(true);
                mSubLabelItems.add(mAddView);
                AppService.addLabels(label);
            }

            if (labelOp == 2) // remove label
            {
                LinearLayout container = (LinearLayout) mSubLabelView.findViewById(R.id.lm_llo);
                TextView labelTV = (TextView) mAddView.findViewById(R.id.label_view);
                String label = labelTV.getText().toString();
                AppService.removeLabel(label);

                container.removeView(mAddView);
                mSubLabelItems.remove(mAddView);
            }

            labelOp = 0;
            return;
        }

        // for reject INVITES/INTRODUCES
        if (mRejectStep > 0) {
            if (mRejectStep == 1) {
                String path = "/api/notifications/" +
                        SingletonLoginData.getInstance().getNotifications().getData().get(mNotifyIndex).getNotificationId() +
                        "/seen.json?" + SingletonLoginData.getInstance().getPostParam();
                new HttpConnection().access(this, path, "", "POST");
                mRejectStep++;
                return;
            }

            if (mRejectStep == 2) {

                // this should be the last one
                SingletonNetworkStatus.getInstance().setDoNotDismissDialog(false);

                String path = "/api/sortedNotifications.json?pageSize=100&" + SingletonLoginData.getInstance().getPostParam();
                new HttpConnection().access(this, path, "", "GET");
                mRejectStep++;
                return;
            }

            if (mRejectStep == 3) {
                // update notification
                Type collectionType = new TypeToken<PageDTO<NotificationDTO>>() {
                }.getType();
                Gson gson = new GsonBuilder().create();
                PageDTO<NotificationDTO> notifications = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), collectionType);
                SingletonLoginData.getInstance().setNotifications(notifications);
            }

            mRejectStep = 0;
        }

        setFragment(position);

        mDrawerLayout.closeDrawer(leftDeckView);
    }

    private void setFragment(int position) {
        SingletonLoginData.getInstance().setContactListChangeListner(null);
        try {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }


        showLeftImage(R.drawable.actionbar_menu);
        addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer();
            }
        });

        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        Fragment fragment = null;
        switch (position) {
            case OPTION_PROFILE:
                setTitle(getText(R.string.ab_profile));
                fragment = new FragmentProfile();
                fragment.setArguments(args);
                break;
            case OPTION_SETTING:
                setTitle(getText(R.string.ab_setting));
                fragment = new PlanetFragment();
                fragment.setArguments(args);
                break;
            case OPTION_CONTACT:
                setTitle(getText(R.string.ab_contact_all));

                showSearch(R.string.HINT_SEARCH_MY_PINTACTS, true);

                showRightImage(R.drawable.actionbar_plus_orange);
                final MyActivity THIS = LeftDeckActivity.this;
                addRightImageClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(THIS, ContactAddActivity.class);
                        startActivity(myIntent);
                    }
                });

                FragmentPintactsList fragmentList = new FragmentPintactsList(AppService.initContactList(false), true,
                        ActionButtonType.INVITE, PintactActionType.VIEW_PROFILE,
                        EmptyViewType.PINTACT);
                fragment = fragmentList;

                SingletonLoginData.getInstance().setContactListChangeListner(fragmentList);

                break;
            case OPTION_GROUP:
                setTitle(getText(R.string.ab_group_pin));
                fragment = new FragmentGroup();
                fragment.setArguments(args);
                break;
            case OPTION_LABEL:
                setTitle(getText(R.string.ab_label));
                fragment = new FragmentLabel();
                fragment.setArguments(args);
                break;
            case OPTION_NOTIFY:
                setTitle(getText(R.string.ab_notify));
                hideOneRight();
                fragment = new FragmentNotifications();
                break;

        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
    }

    @SuppressLint("NewApi")
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        showTitle(mTitle.toString());
    }


    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onIconClicked(View v) {
        return;
    }

    public void onNoteClicked(View v) {

        return;
    }

    public void onPhoneClicked(View v) {
        return;
    }

    public void onLabelClicked(View v) {
        return;
    }

    public void onFeedBackClicked(View v) {
        //TextView tv = (TextView)findViewById(R.id.set_feedback);
        //tv.setText(getResources().getString(R.string.android_res_qualifier));
        //return;
    }

    //TODO: add missing items...
    public void onMenuClicked(View v) {
        restoreActionBar = false;

        final int id = v.getId();
        switch (id) {
            case R.id.ldm_contacts:
                selectItem(OPTION_CONTACT);
                break;

            case R.id.ldm_notifications:
                selectItem(OPTION_NOTIFY);
                break;

            case R.id.ldm_group_pin:
                selectItem(OPTION_GROUP);
                break;

            case R.id.ldm_labels:
                selectItem(OPTION_LABEL);
                break;

            case R.id.ldm_profile:
                selectItem(OPTION_PROFILE);
                break;

            case R.id.ldm_settings:
                selectItem(OPTION_SETTING);
                break;

            default:
                break;

        }

        return;
    }

    public void searchButtonClicked(View v) {
        EditText et = (EditText) findViewById(R.id.TopsearchEdit);

        //String testStr = "Quick Test";
        boolean isLeft = isSearchPage;

        ViewGroup parent;
        if (isFirstTime) {
            isFirstTime = false;
            tableView = findViewById(R.id.leftTableLayout);
            parent = (ViewGroup) tableView.getParent();
            tableIndex = parent.indexOfChild(tableView);

            searchView = getLayoutInflater().inflate(R.layout.search_left_deck, parent, false);

            // init search result panel
            ListView lv = (ListView) searchView.findViewById(R.id.searchListView);
            searchResultAdapter adapter = new searchResultAdapter(this, getData());
            lv.setAdapter(adapter);

        }

        if (isLeft) {
            parent = (ViewGroup) searchView.getParent();
            parent.removeView(searchView);
            parent.addView(tableView, tableIndex);
            et.setText("");
            et.setHint("search your Pintacts");
        } else {
            parent = (ViewGroup) tableView.getParent();
            parent.removeView(tableView);
            parent.addView(searchView, tableIndex);
        }

        isSearchPage = !isSearchPage;

    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    @SuppressLint("NewApi")
    public static class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";
        ExpandableListView expListView;
        View mLabelView;
        ArrayList<View> mLabelItems = new ArrayList<View>();
        MyActivity mActivity;
        View mRootView;

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        public View gen_setting(LayoutInflater inflater, ViewGroup container) {
            View rootView = inflater.inflate(R.layout.setting_main, container, false);
            TextView logout = (TextView) rootView.findViewById(R.id.set_logout);
            logout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    System.out.println("Logging out...");
                    ((LeftDeckActivity) getActivity()).logout();
                }
            });

            // set default value for some settings;
            Switch stLocal = (Switch) rootView.findViewById(R.id.set_broadcast_switch);
            stLocal.setChecked(SingletonLoginData.getInstance().getUserSettings().local == 1);

            stLocal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MyActivity) buttonView.getContext()).updatePreferencesLocal(isChecked ? 1 : 0);
                }
            });

            // invite a friend
            View.OnClickListener lnInvite = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent it = new Intent(v.getContext(), ContactInviteActivity.class);
                    it.putExtra(ContactInviteActivity.ARG_INVITE_ACTIVITY, 1);
                    startActivity(it);
                }
            };

            TextView pinText = (TextView) rootView.findViewById(R.id.set_pin_text);
            pinText.setText(SingletonLoginData.getInstance().getUserData().pin);

            View viewInvite = rootView.findViewById(R.id.invite_wrapper);
            viewInvite.setOnClickListener(lnInvite);

            // sort settings
            View.OnClickListener lnSort = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent it = new Intent(v.getContext(), SettingSortActivity.class);
                    startActivity(it);
                }
            };
            View viewSort = rootView.findViewById(R.id.sort_setting_wrapper);
            viewSort.setOnClickListener(lnSort);


            View.OnClickListener lnSearch = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent it = new Intent(v.getContext(), SearchSettingActivity.class);
                    startActivity(it);
                }
            };

            View viewSearch = rootView.findViewById(R.id.search_setting_wrapper);
            viewSearch.setOnClickListener(lnSearch);

            View.OnClickListener changePassword = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent it = new Intent(v.getContext(), ChangePasswordActivity.class);
                    startActivity(it);
                }
            };

            View viewChangePassword = rootView.findViewById(R.id.change_password_wrapper);
            viewChangePassword.setOnClickListener(changePassword);

            View.OnClickListener lnAbout = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent it = new Intent(v.getContext(), AboutPintactActivity.class);
                    startActivity(it);
                }
            };

            View viewAbout = rootView.findViewById(R.id.about_wrapper);
            viewAbout.setOnClickListener(lnAbout);

            return rootView;
        }


        @SuppressLint("NewApi")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            View result = null;
            mActivity = (MyActivity) this.getActivity();

            switch (i) {
                case OPTION_PROFILE:
                    break;
                case OPTION_SETTING:
                    result = gen_setting(inflater, container);
                    break;
                case OPTION_GROUP:
                    break;
                case OPTION_LABEL:
                    break;
                case OPTION_NOTIFY:
                    //result = gen_notifcation(inflater, container);
                    break;
                default:
                    break;
            }

            mRootView = result;

            return result;
        }


        @Override
        public void onResume() {
            super.onResume();
            System.out.println("Onresume - Fragment.");

            int i = getArguments().getInt(ARG_PLANET_NUMBER);
        }

        // Convert pixel to dip
        @SuppressLint("NewApi")
        public int getDipsFromPixel(float pixels) {
            // Get the screen's density scale
            final float scale = getResources().getDisplayMetrics().density;
            // Convert the dps to pixels, based on density scale
            return (int) (pixels * scale + 0.5f);
        }

        @SuppressLint("NewApi")
        private void setGroupIndicatorToRight() {
      /* Get the screen width */
            DisplayMetrics dm = new DisplayMetrics();
            this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = dm.widthPixels;

            int p1 = getDipsFromPixel(45);
            int p2 = getDipsFromPixel(5);

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                expListView.setIndicatorBounds(width - p1, width - p2);
            } else {
                expListView.setIndicatorBoundsRelative(width - p1, width - p2);
            }
        }
    }
}
