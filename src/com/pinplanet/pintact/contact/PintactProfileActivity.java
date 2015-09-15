package com.pinplanet.pintact.contact;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ProfileTableDbInterface;
import com.pinplanet.pintact.label.LabelMainActivity;
import com.pinplanet.pintact.profile.ListViewGroupItem;
import com.pinplanet.pintact.profile.ListViewItemEditAddress;
import com.pinplanet.pintact.profile.ListViewItemShowDefault;
import com.pinplanet.pintact.profile.ListViewItemShowAddress;
import com.pinplanet.pintact.profile.ListViewItemType;
import com.pinplanet.pintact.profile.ProfileCreatedSuccessfulActivity;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.TextViewTypeFace;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.late.gui.ExpandableListViewAdvanced;
import de.late.utils.PackageIntentUtil;
import de.late.widget.ExpandableViewListAdapter;
import de.late.widget.ViewListItemGroup;
import de.late.widget.ViewListItem;


public class PintactProfileActivity extends MyActivity {

    private static final String TAG = PintactProfileActivity.class.getName();

    public static final String ARG_PROFILE_VIEW = "profile_view";
    public static final int ARG_PROFILE_NEW = -1, ARG_LOGIN_REGISTER_NEW = -2, ARG_SHOW_PROFILE_VIEW = 1000, ARG_SHOW_SHARE_VIEW = 10000, ARG_SHOW_CONTACT_VIEW = 100000;

    public int mArgInt;  // -2: signup; -1: profile new; 0-1000:profile view; >10K: share view; >100K: contact view
    public ProfileDTO mProfile;
    public UserDTO userDTO;

    boolean mIsContactView = false;
    boolean mIsLocalContact = false;
    boolean mIsSharedView = false;
    boolean isUploadingImage = false;
    boolean isDeleteContact = false;

    boolean isDeleteContactLabel = false;
    String deleteContactLabel;
    int deleteContactLabelIndex = -1;

    boolean isDeleteProfile = false;
    boolean isUpdatedPNotes = false;
    boolean isQuerySharedProfile = false;
    boolean isUpdateSharedProfile = false;

    LinkedHashMap<String, Integer> hm = new LinkedHashMap<String, Integer>();

    public static final String CONTACT_VIEW_MODE = "CONTACT_VIEW_MODE";

    public static Intent getInstanceForContactView(Context context) {
        Bundle b = new Bundle();
        b.putInt(CONTACT_VIEW_MODE, ARG_SHOW_CONTACT_VIEW);
        Intent i = new Intent(context, PintactProfileActivity.class);
        i.putExtras(b);
        return i;
    }

    public static Intent getInstanceForShareView(Context context) {
        Bundle b = new Bundle();
        b.putInt(CONTACT_VIEW_MODE, ARG_SHOW_SHARE_VIEW);
        Intent i = new Intent(context, PintactProfileActivity.class);
        i.putExtras(b);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UiControllerUtil.checkStaticDataPresent(this)) {
            setContentView(R.layout.activity_pintact_profile);
            mArgInt = getIntent().getIntExtra(CONTACT_VIEW_MODE, ARG_SHOW_CONTACT_VIEW);

            fromProfileView();
            initViews(false);

            fillUserData();
            fillAddressData();

            List<ViewListItem> emailItemList = getAttributeData(ListViewItemType.ITEM_TYPE.EMAIL, AttributeType.EMAIL);
            if (emailItemList.size() > 0)
                fillListData(expAdapterMail, emailItemList);
            else
                lvMail.setVisibility(View.GONE);

            List<ViewListItem> phoneItemList = getAttributeData(ListViewItemType.ITEM_TYPE.PHONE, AttributeType.PHONE_NUMBER);
            if (phoneItemList.size() > 0)
                fillListData(expAdapterPhone, phoneItemList);
            else
                lvPhone.setVisibility(View.GONE);

            List<ViewListItem> socialItemList = getAttributeData(ListViewItemType.ITEM_TYPE.SOCIAL, AttributeType.SERVICE_ID);
            if (socialItemList.size() > 0)
                fillListData(expAdapterSocial, socialItemList);
            else
                lvSocial.setVisibility(View.GONE);


            UserProfileAttribute userProfileAttribute = getAttributeData(AttributeType.PRIVATE_NOTE);
            if (userProfileAttribute != null && userProfileAttribute.getValue() != null && userProfileAttribute.getValue().trim().length() > 0) {
                TextViewTypeFace textViewTypeFace = (TextViewTypeFace) this.findViewById(R.id.contactNotesTextEntry);
                textViewTypeFace.setText(userProfileAttribute.getValue());
            } else {
                LinearLayout notesLayout = (LinearLayout) this.findViewById(R.id.layoutContactNotes);
                notesLayout.setVisibility(View.GONE);
            }
            if (mIsContactView && !mIsLocalContact && !userDTO.isManualContact()) {
                updateSharedProfileIcons();
            } else //if this is a share preview view...
            {
                findViewById(R.id.layoutShareProfiles).setVisibility(View.GONE);
                if (!userDTO.isManualContact()) {
                    findViewById(R.id.layoutShareProfileButtons).setVisibility(View.GONE);
                }
                findViewById(R.id.layoutPersonalNotes).setVisibility(View.GONE);
                findViewById(R.id.listViewLabels).setVisibility(View.GONE);
            }
        }

    }


    public void fromProfileView() {

        if (mArgInt > ARG_SHOW_SHARE_VIEW)// && !contactDTO.isLocalContact)
        {
            mProfile = SingletonLoginData.getInstance().getMergedProfile();
            userDTO = SingletonLoginData.getInstance().getContactUser();
            mIsContactView = true;
            ContactDTO contactDTO = SingletonLoginData.getInstance().getCurrentContactDto();
            mIsLocalContact = contactDTO.isLocalContact;
        } else {
            mProfile = SingletonLoginData.getInstance().getMergedProfile();
            userDTO = SingletonLoginData.getInstance().getContactUser();
            mIsSharedView = true;
        }

        TextView tv = (TextView) findViewById(R.id.actionBar);
        tv.setFocusableInTouchMode(true);
        tv.requestFocus();

        tv.setText(getName(userDTO));

        showLeftImage(R.drawable.actionbar_left_arrow);
        View.OnClickListener backLn = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goBack();
            }
        };
        addLeftClickListener(backLn);

        hideRight();
//    if ( mIsSharedView ) {
//      hideRight();
//      showRightImage(R.drawable.white_cross);
//      addRightImageClickListener(backLn);
//    }

        View.OnClickListener exportLn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIntroduce();
            }
        };

        if (mIsContactView) {
            if (!mIsLocalContact && !userDTO.isManualContact()) {
                hideRight();
                showRightImage(0);
                addRightImageClickListener(exportLn);
            }
            showContactView();
        }
    }

    private String getName(UserDTO userDTO) {
        String name = "";
        if (userDTO != null) {
            name = addString(name, userDTO.getFirstName());
            name = addString(name, userDTO.getLastName());
        }

        return name;

    }

    private List<ViewListItem> getAttributeData(ListViewItemType.ITEM_TYPE listItemType, AttributeType attributeTypeToFind) {
        List<UserProfileAttribute> attributes = mProfile.getUserProfileAttributes();
        List<ViewListItem> itemList = new ArrayList<ViewListItem>();

        for (UserProfileAttribute attribute : attributes) {
            if (attribute.getType() == attributeTypeToFind && attribute.getValue() != null
                    && attribute.getValue().length() > 0) {
                itemList.add(new ListViewItemShowDefault(attribute.getLabel(), attribute.getValue(), listItemType, attribute));
            }
        }

        return itemList;
    }


    private UserProfileAttribute getAttributeData(AttributeType attributeTypeToFind) {
        List<UserProfileAttribute> attributes = mProfile.getUserProfileAttributes();

        for (UserProfileAttribute attribute : attributes) {
            if (attribute.getType() == attributeTypeToFind) {
                return attribute;
            }
        }

        return null;
    }

    private void fillListData(ExpandableViewListAdapter adapter, List<ViewListItem> itemList) {
        ArrayList<ViewListItem> list = adapter.getGroup(0).getChilds();
        list.clear();
        list.addAll(itemList);
        adapter.notifyDataSetChanged();
    }

    private void fillAddressData() {

        List<UserProfileAddress> list = mProfile.getUserProfileAddresses();
        if (list.size() > 0) {
            ArrayList<ViewListItem> listItems = expAdapterAddress.getGroups().get(0).getChilds();
            listItems.clear();

            for (UserProfileAddress address : list) {
                ListViewItemShowAddress addr = new ListViewItemShowAddress(address.getLabel(), address.getAddressLine1(),
                        address.getCity(), address.getState(), address.getPostalCode(), address);
                listItems.add(addr);
            }
            expAdapterAddress.notifyDataSetChanged();
        } else {
            lvAddress.setVisibility(View.GONE);
        }
    }

    public void setText(int id, String value) {
        if (value != null && value.trim().length() > 0) {
            ((TextView) findViewById(id)).setText(value);
        } else
            ((TextView) findViewById(id)).setVisibility(View.GONE);

    }

    private void fillUserData() {
        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) findViewById(R.id.imageViewAdd);
        ImageView defaultImage = (ImageView) findViewById(R.id.imageViewDefault);

        if (mProfile.getUserProfile().getPathToImage() != null) {
            if (!mIsLocalContact) {
                ivPhoto.setImageUrl(mProfile.getUserProfile().getPathToImage(), AppController.getInstance().getImageLoader());
                ivPhoto.setVisibility(View.VISIBLE);
                defaultImage.setVisibility(View.GONE);
            } else {
                ivPhoto.setVisibility(View.GONE);
                defaultImage.setImageURI(Uri.parse(mProfile.getUserProfile().getPathToImage()));
                defaultImage.setVisibility(View.VISIBLE);
            }
        } else {
            ivPhoto.setVisibility(View.GONE);
            defaultImage.setVisibility(View.VISIBLE);
        }

        setText(R.id.pcn_first_name, getName(mProfile.getUserProfile()));
        setText(R.id.pcn_title, mProfile.getUserProfile().getTitle());
        setText(R.id.pcn_company, mProfile.getUserProfile().getCompanyName());
        if (userDTO != null)
            setText(R.id.pin, userDTO.getPin());
    }

    private String getName(UserProfile userProfile) {
        String firstName = userProfile.getFirstName();
        String middleName = userProfile.getMiddleName();
        String lastName = userProfile.getLastName();

        String name = "";
        name = addString(name, firstName).trim();
        name = addString(name, middleName);
        name = addString(name, lastName);

        return name;
    }

    private String addString(String st, String newSt) {
        if (newSt != null && newSt.trim().length() > 0) {
            return st + " " + newSt;
        }
        return st;
    }

    private ExpandableViewListAdapter expAdapterPhone, expAdapterMail, expAdapterSocial, expAdapterAddress, expAdapterLabel;
    private ExpandableListView lvPhone, lvMail, lvAddress, lvSocial, lvLabel;

    private void itemDeleteClicked(ExpandableViewListAdapter adapter, ViewListItem item) {
        //Log.i(TAG, "itemDeleteClicked" + item.getPosition());
        adapter.getGroups().get(0).getChilds().remove(item.getPosition());
        adapter.notifyDataSetChanged();
    }

    public void initViews(boolean editMode) {
        //phone
        lvPhone = (ExpandableListView) findViewById(R.id.listViewPhone);

        final SparseArray<ViewListItemGroup> groupsPhone = new SparseArray<ViewListItemGroup>();
        groupsPhone.put(0, new ListViewGroupItem(R.drawable.profile_phone));

        expAdapterPhone = new ExpandableViewListAdapter(this, groupsPhone, new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
            }

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, final int childPosition, View convertView) {
                if (mIsContactView) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.imageViewActionOne);
                    iv.setVisibility(View.VISIBLE);
                    iv.setTag(ExpandableViewListAdapter.getChild(groupPosition, childPosition));
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListViewItemShowDefault itemTag = (ListViewItemShowDefault) view.getTag();
                            PackageIntentUtil.openPhoneNumberChooserIntent(PintactProfileActivity.this, null, itemTag.getText());
                        }
                    });

                    ListViewItemShowDefault item = (ListViewItemShowDefault) ExpandableViewListAdapter.getChild(groupPosition, childPosition);
                    if (item.isMobileNumber()) {
                        iv = (ImageView) convertView.findViewById(R.id.imageViewActionTwo);
                        iv.setVisibility(View.VISIBLE);
                        iv.setTag(ExpandableViewListAdapter.getChild(groupPosition, childPosition));
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ListViewItemShowDefault itemTag = (ListViewItemShowDefault) view.getTag();
                                PackageIntentUtil.openSmsChooserIntent(PintactProfileActivity.this, null, itemTag.getText(), null);
                            }
                        });
                    }
                }
            }
        });
        lvPhone.setAdapter(expAdapterPhone);

        //mail
        lvMail = (ExpandableListView) findViewById(R.id.listViewEmail);

        final SparseArray<ViewListItemGroup> groupsMail = new SparseArray<ViewListItemGroup>();
        groupsMail.put(0, new ListViewGroupItem(R.drawable.profile_mail));

        expAdapterMail = new ExpandableViewListAdapter(this, groupsMail, new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
            }

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, final int childPosition, View convertView) {

                if (mIsContactView) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.imageViewActionOne);
                    iv.setVisibility(View.VISIBLE);
                    iv.setImageResource(R.drawable.profile_icon_email);
                    iv.setTag(ExpandableViewListAdapter.getChild(groupPosition, childPosition));
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListViewItemShowDefault itemTag = (ListViewItemShowDefault) view.getTag();
                            PackageIntentUtil.openEmailChooserIntent(PintactProfileActivity.this, null, itemTag.getText(), null, null, null);
                        }
                    });
                }

            }
        });


        lvMail.setAdapter(expAdapterMail);
        expAdapterMail.setEditMode(editMode);

        //address
        lvAddress = (ExpandableListView) findViewById(R.id.listViewAddress);

        final SparseArray<ViewListItemGroup> groupsAddress = new SparseArray<ViewListItemGroup>();
        groupsAddress.put(0, new ListViewGroupItem(R.drawable.profile_address));

        expAdapterAddress = new ExpandableViewListAdapter(this, groupsAddress, new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
            }

            @Override
            public void initChild(final ExpandableViewListAdapter expandableViewListAdapter, int groupPosition, final int childPosition, View convertView) {

                if (mIsContactView) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.imageViewActionOne);
                    iv.setVisibility(View.VISIBLE);
                    iv.setTag(expandableViewListAdapter.getChild(groupPosition, childPosition));
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListViewItemShowAddress itemTag = (ListViewItemShowAddress) view.getTag();

                            String suffix = itemTag.getStreet() + " " + itemTag.getCity() + " " + itemTag.getZip();
                            String prefix = "http://maps.google.co.in/maps?q=";
                            PackageIntentUtil.openGeneralUriChooser(PintactProfileActivity.this, null, prefix, suffix);
                        }
                    });
                }

            }
        });

        lvAddress.setAdapter(expAdapterAddress);
        expAdapterAddress.setEditMode(editMode);

        //social
        lvSocial = (ExpandableListView) findViewById(R.id.listViewSozial);

        final SparseArray<ViewListItemGroup> groupsSocial = new SparseArray<ViewListItemGroup>();
        groupsSocial.put(0, new ListViewGroupItem(R.drawable.profile_url));

        expAdapterSocial = new ExpandableViewListAdapter(this, groupsSocial, new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
            }

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, final int childPosition, View convertView) {

                if (mIsContactView) {
                    ImageView iv = (ImageView) convertView.findViewById(R.id.imageViewActionOne);
                    iv.setVisibility(View.VISIBLE);
                    iv.setImageResource(R.drawable.profile_icon_export);
                    iv.setTag(ExpandableViewListAdapter.getChild(groupPosition, childPosition));
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListViewItemShowDefault itemTag = (ListViewItemShowDefault) view.getTag();

                            String prefix = itemTag.getUriPrefix();
                            if (prefix != null) {
                                //openIntent(prefix+itemTag.getText());
                                PackageIntentUtil.openGeneralUriChooser(PintactProfileActivity.this, null, prefix, itemTag.getText());
                            }

                        }
                    });
                }

            }
        });

        lvSocial.setAdapter(expAdapterSocial);
        expAdapterSocial.setEditMode(editMode);

        //labels
        lvLabel = (ExpandableListView) findViewById(R.id.listViewLabels);

        final View footerLayout = findViewById(R.id.listViewLabelsFooterLayout);
        TextView textViewFooter = (TextView) footerLayout.findViewById(R.id.textViewFooter);
        textViewFooter.setText(R.string.pcn_add_label);

        final SparseArray<ViewListItemGroup> groupsLabels = new SparseArray<ViewListItemGroup>();
        groupsLabels.put(0, new ListViewGroupItem(R.drawable.profile_label));

        expAdapterLabel = new ExpandableViewListAdapter(this, groupsLabels, new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {
                ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.toggleButtonEditNote);
                toggleButton.setVisibility(View.VISIBLE);
                toggleButton.setTag(ExpandableViewListAdapter.getGroup(groupPosition));
                toggleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expAdapterLabel.setEditMode(!expAdapterLabel.isEditMode());
                        footerLayout.setVisibility(expAdapterLabel.isEditMode() ? View.VISIBLE : View.GONE);
                        expAdapterLabel.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, final int childPosition, final View convertView) {
                convertView.findViewById(R.id.imageView).setTag(ExpandableViewListAdapter.getChild(groupPosition, childPosition));
                convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final ListViewLabelItem itemTag = (ListViewLabelItem) view.getTag();

                        Animation button_right_in = AnimationUtils.loadAnimation(PintactProfileActivity.this, R.anim.anim_button_right_in);
                        convertView.findViewById(R.id.textViewDeleteLabel).startAnimation(button_right_in);

                        convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);

                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                if (convertView.findViewById(R.id.imageView).getVisibility() == View.VISIBLE)
                                    return;

                                Animation button_right_out = AnimationUtils.loadAnimation(PintactProfileActivity.this, R.anim.anim_button_right_out);
                                convertView.findViewById(R.id.textViewDeleteLabel).startAnimation(button_right_out);
                                itemTag.setLabelDeleteVisible(true);
                                convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.VISIBLE);

                                button_right_out.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        itemTag.setLabelDeleteVisible(false);
                                        convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                            }
                        });

                        convertView.findViewById(R.id.textViewDeleteLabel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                itemTag.setLabelDeleteVisible(false);
                                convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.INVISIBLE);
                                sendUpdatedLabels(itemTag.getPosition());
                            }
                        });

                    }
                });
            }
        });

        lvLabel.setAdapter(expAdapterLabel);
        expAdapterLabel.setEditMode(editMode);

        footerLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        footerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(PintactProfileActivity.this, LabelMainActivity.class);
                startActivity(it);
            }
        });

        ((ExpandableListViewAdvanced) lvLabel).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvPhone).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvMail).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvAddress).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvSocial).setAllowAutosizing(true);

        ((ExpandableListViewAdvanced) lvLabel).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvPhone).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvMail).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvAddress).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvSocial).setIgnoreGroupClicks(true);

        lvLabel.expandGroup(0);
        lvPhone.expandGroup(0);
        lvMail.expandGroup(0);
        lvAddress.expandGroup(0);
        lvSocial.expandGroup(0);
    }

    public void addProfiles(int i) {
        UserProfile currentProfile = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile();
        String title = currentProfile.getName();

        LinearLayout container = (LinearLayout) findViewById(R.id.share_layout);
        final View addView = getLayoutInflater().inflate(R.layout.profile_thumb_half, null);
        container.addView(addView);

        // change the name of the text
        TextView tv = (TextView) addView.findViewById(R.id.pt_name);
        tv.setText(title);

        Log.d(TAG, "addProfiles:" + i + " title:" + title + " image" + currentProfile.getPathToImage());

        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) addView.findViewById(R.id.pt_profile_image);
        ivPhoto.setDefaultImageResId(R.drawable.silhouette);

        if (currentProfile.getPathToImage() != null) {
            ivPhoto.setImageUrl(currentProfile.getPathToImage(), AppController.getInstance().getImageLoader());
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        final RelativeLayout lo = (RelativeLayout) addView.findViewById(R.id.pt_all);
        lo.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                TextView tv = (TextView) v.findViewById(R.id.pt_name);
                String key = tv.getText().toString();
                Integer value = hm.get(key);
                lo.setBackgroundDrawable(v.getResources().getDrawable(
                        value == null ?
                                R.drawable.border_profile_thumb_sel_half :
                                R.drawable.border_profile_thumb_nosel_half
                ));
                if (value == null) {
                    hm.put(key, 1);
                } else
                    hm.remove(key);

                // we need to update tv_share
                String listProfiles = "";
                Set<Map.Entry<String, Integer>> entries = hm.entrySet();
                for (Map.Entry<String, Integer> entry : entries) {
                    listProfiles += entry.getKey() + ",";
                }


                TextView tvInfo = (TextView) findViewById(R.id.tv_share);
                if (listProfiles.length() > 1)
                    listProfiles = listProfiles.substring(0, listProfiles.length() - 1);

                tvInfo.setText(listProfiles);

            }
        });

    }


    public void updateSharedProfileIcons() {

        try {
            // show shared profiles
            for (int i = 0; mIsContactView && i < SingletonLoginData.getInstance().getUserProfiles().size(); i++) {
                addProfiles(i);
            }

            ContactDTO contactDTO = SingletonLoginData.getInstance().getCurrentContactDto();
            ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
            if (contactDTO.getSourceProfileIds() != null && contactDTO.getSourceProfileIds().size() > 0) {
                List<Long> sourceProfileIds = contactDTO.getSourceProfileIds();
                for (Long profileId : sourceProfileIds) {
                    profiles.add(ProfileTableDbInterface.getInstance().getProfile(profileId));
                }
            }
            String listProfiles = "";
            List<String> profileList = new ArrayList<String>();
            for (int i = 0; i < profiles.size(); i++) {
                listProfiles += profiles.get(i).getUserProfile().getName() + ",";
                profileList.add(profiles.get(i).getUserProfile().getName());
            }
            if (listProfiles.length() > 1)
                listProfiles = listProfiles.substring(0, listProfiles.length() - 1);

            LinearLayout container = (LinearLayout) findViewById(R.id.share_layout);
            int totalProfiles = SingletonLoginData.getInstance().getUserProfiles().size();
            for (int i = 0; i < totalProfiles; i++) {
                String title = SingletonLoginData.getInstance().getUserProfiles().get(i).getUserProfile().getName();
                if (profileList.contains(title)) {
                    hm.put(title, 1);
                    View v = container.getChildAt(i);
                    RelativeLayout lo = (RelativeLayout) v.findViewById(R.id.pt_all);
                    // for API 15 - Jeff's crab
                    lo.setBackgroundDrawable(v.getResources().getDrawable(R.drawable.border_profile_thumb_sel_half));
                }
            }
            TextView tvInfo = (TextView) findViewById(R.id.tv_share);
            tvInfo.setText(listProfiles);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Long[] getSharedProfileIds() {
        Long profId[] = new Long[hm.size()];
        List<ProfileDTO> profiles = SingletonLoginData.getInstance().getUserProfiles();
        Set<Map.Entry<String, Integer>> entries = hm.entrySet();
        int j = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            for (int i = 0; i < profiles.size(); i++) {
                UserProfile prof = profiles.get(i).getUserProfile();
                if (entry.getKey().equals(prof.getName())) {
                    profId[j++] = prof.getId();
                }
            }
        }

        return profId;
    }

    public void sendUpdatedSharedProfile() {
        Long ids[] = getSharedProfileIds();
        isUpdateSharedProfile = true;
        if (ids.length == 0) {
            alertDialog(getString(R.string.confirm_unshare), getString(R.string.confirm_unshare_desc) + " " + userDTO.getFirstName() + "." + getString(R.string.are_you_sure));
        } else
            shareProfile();

    }


    public void showContactView() {

        // add delete view
        TextView delete = (TextView) findViewById(R.id.tv_button_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleteContact = true;
                String msg = "You are about to delete this contact. This operation cannot be undone. Do you still wish to continue?";
                alertDialog("ARE YOU SURE", msg);
            }
        });

        // add insert view
        TextView insert = (TextView) findViewById(R.id.tv_button_add);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertContact();
            }
        });

        // add share view
        TextView share = (TextView) findViewById(R.id.tv_share_button);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUpdatedSharedProfile();
            }
        });

        TextView pvshare = (TextView) findViewById(R.id.tv_preview_share_button);
        pvshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long ids[] = getSharedProfileIds();
                if (ids.length == 0) {
                    myDialog(getString(R.string.gm_no_profile_title), getString(R.string.gm_no_profile_detail));
                    return;
                }

                UiControllerUtil.openPreviewShareActivity(ids);
                Intent myIntent = PintactProfileActivity.getInstanceForShareView(PintactProfileActivity.this);
                startActivity(myIntent);
                PintactProfileActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

            }
        });


        String strNote = SingletonLoginData.getInstance().getCurrentContactDto().getContactNote();
        final EditText editTextPersonalNotes = (EditText) findViewById(R.id.editTextPersonalNotes);
        editTextPersonalNotes.setText(strNote);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButtonEditNote);
        setEditable(R.id.editTextPersonalNotes, false);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setEditable(R.id.editTextPersonalNotes, b);

                if (b == false) {
                    sendPersonalNotes(editTextPersonalNotes.getText().toString());
                } else {
                    editTextPersonalNotes.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editTextPersonalNotes, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    public void insertContact() {
        int result = AppService.insertContact(mProfile, false);
        if (result == 1) {
            myDialog("Contact Added", "The contact has been added into your address book.");
        } else {
            myDialog("Contact Updated", "The contact in your address book has been updated.");
        }
    }

    public void sendUpdatedLabels(int index) {
        isDeleteContactLabel = true;
        deleteContactLabel = null;
        deleteContactLabelIndex = index;

        String path = "/api/contacts/" + mProfile.getUserId() + "/labels/update.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);

        Gson gson = new GsonBuilder().create();

        ArrayList<String> mLabels = new ArrayList<String>();
        ArrayList<ViewListItem> arrayList = expAdapterLabel.getGroup(0).getChilds();
        int cnt = 0;
        for (ViewListItem e : arrayList) {
            final String currentLabel = ((ListViewLabelItem) e).getTitle();
            if (cnt != index) {
                mLabels.add(currentLabel);
            } else {
                deleteContactLabel = currentLabel;
            }
            cnt++;
        }

        String params = gson.toJson(mLabels);

        params = "{\"label\":" + params + "}";
        new HttpConnection().access(this, path, params, "POST");
    }

    public void updateLabelList() {
        if (expAdapterLabel != null) {
            List<String> labels = SingletonLoginData.getInstance().getContactLabels();
            List<ViewListItem> itemList = new ArrayList<ViewListItem>();

            for (String label : labels) {
                itemList.add(new ListViewLabelItem(label));
            }

            fillListData(expAdapterLabel, itemList);
        }
    }

    public void sendPersonalNotes(String text) {
        // send request to get shared profiles
        SingletonLoginData.getInstance().getCurrentContactDto().setContactNote(text);

        isUpdatedPNotes = true;
        String path = "/api/contacts/" + mProfile.getUserId() + "/note.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);

        String params = "{\"note\":\"" + text + "\"}";
        new HttpConnection().access(this, path, params, "POST");

    }


    public void deleteContact() {
        isDeleteContact = true;
        String path = "/api/contacts/" + mProfile.getUserId() + "/delete.json?" + SingletonLoginData.getInstance().getPostParam();
        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, "", "POST");

    }

    public void onIntroduce() {
        Intent it = new Intent(this, ContactIntroduceListActivity.class);
        startActivity(it);
    }

    public void addAttributeNew(ProfileDTO profile) {
        addAttributeTypeNew(profile, AttributeType.PHONE_NUMBER, expAdapterPhone);
        addAttributeTypeNew(profile, AttributeType.EMAIL, expAdapterMail);
        addAttributeTypeNew(profile, AttributeType.SERVICE_ID, expAdapterSocial);

        UserProfileAttribute attr = new UserProfileAttribute();
        attr.setType(AttributeType.PRIVATE_NOTE);
        attr.setLabel(((TextView) findViewById(R.id.notesEntryTitle)).getText().toString());
        attr.setValue(((EditText) findViewById(R.id.notesTextEntry)).getText().toString());
        profile.getUserProfileAttributes().add(attr);
    }

    public void addAttributeTypeNew(ProfileDTO profile, AttributeType type, ExpandableViewListAdapter adapter) {
        for (Object o : adapter.getGroups().get(0).getChilds()) {
            ListViewItemShowDefault li = (ListViewItemShowDefault) o;
            UserProfileAttribute attr = new UserProfileAttribute();
            attr.setType(type);
            attr.setLabel(li.getTitle());
            attr.setValue(li.getText());
            profile.getUserProfileAttributes().add(attr);
        }
    }

    public void addAddressNew(ProfileDTO profile) {
        for (Object o : expAdapterAddress.getGroups().get(0).getChilds()) {
            ListViewItemEditAddress li = (ListViewItemEditAddress) o;
            UserProfileAddress attr = new UserProfileAddress();
            attr.setLabel(li.getTitle());
            attr.setAddressLine1(li.getStreet());
            attr.setCity(li.getCity());
            attr.setState(li.getState());
            attr.setPostalCode(li.getZip());
            profile.getUserProfileAddresses().add(attr);
        }
    }

    public class ImageDTO {

        public Long sourceId;
        public String thumbnailPath;

    }

    public void onPostNetwork() {

        SingletonNetworkStatus.getInstance().setDoNotShowStatus(false);
        if (SingletonNetworkStatus.getInstance().getCode() != 200) {
            myDialog(SingletonNetworkStatus.getInstance().getMsg(),
                    SingletonNetworkStatus.getInstance().getErrMsg());
            return;
        }

        if (isUpdateSharedProfile) {
            AppService.handleUpdateContactResponse();
            isUpdateSharedProfile = false;
            return;
        }

        if (isUpdatedPNotes) {
            AppService.handleUpdateContactResponse();
            isUpdatedPNotes = false;
            return;
        }

        if (isQuerySharedProfile) {
            isQuerySharedProfile = false;
            updateSharedProfileIcons();
            return;
        }

        if (isDeleteContactLabel) {
            isDeleteContactLabel = false;

            AppService.handleUpdateContactResponse();

            SingletonLoginData.getInstance().getContactLabels().remove(deleteContactLabelIndex);

            updateLabelList();

            return;
        }

        if (isDeleteContact) {
            AppService.handleDeleteContactResponse(SingletonLoginData.getInstance().getContactUser().getId());
            SingletonLoginData.getInstance().getCurrentContactDto().setAcontact(false);
            SingletonLoginData.getInstance().getCurrentContactDto().isLocalContact = false;
            SingletonLoginData.getInstance().setIsStatusChanged(true);
            goBack();
            return;
        }

        if (isDeleteProfile) {
            List<ProfileDTO> profs = SingletonLoginData.getInstance().getUserProfiles();
            profs.remove(mArgInt);
            goBack();
            return;
        }

        if (isUploadingImage) {
            isUploadingImage = false;

            Gson gson = new GsonBuilder().create();
            ImageDTO imInfo = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ImageDTO.class);
            mProfile.getUserProfile().setPathToImage(imInfo.thumbnailPath);

            return;
        }

        Gson gson = new GsonBuilder().create();
        AppService.handleGetSingleProfileResponse();
        // 200 is ok
        if (mArgInt >= -1) {
            Intent intent = getIntent();

            List<ProfileDTO> profs = SingletonLoginData.getInstance().getUserProfiles();
            if (mArgInt > -1) {
                profs.remove(mArgInt);
                profs.add(mArgInt, mProfile);
            } else {
                mProfile = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ProfileDTO.class);
                profs.add(mProfile);
                intent.putExtra(PintactProfileActivity.ARG_PROFILE_VIEW, profs.size() - 1);
            }

            goBack();
            startActivity(intent);
        } else {
            // for login new
            Intent it = new Intent(this, ProfileCreatedSuccessfulActivity.class);
            startActivity(it);
        }

    }


    public void onDummy(View view) {
    }

    private void shareProfile() {

        Long[] ids = getSharedProfileIds();
        ContactShareRequest req = new ContactShareRequest();
        req.setDestinationUserId(mProfile.getUserId());
        req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
        req.setUserProfileIdsShared(ids);


        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(req);
        SingletonNetworkStatus.getInstance().setActivity(this);
        String path = "/api/contacts/updateSharedProfiles.json?" + SingletonLoginData.getInstance().getPostParam();
        new HttpConnection().access(this, path, params, "POST", R.string.DIALOG_MESSAGE_CONTACT_SHARE_UPDATING);

    }

    public void showUnshareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.confirm_unshare));
        builder.setMessage(getString(R.string.confirm_unshare_desc) + " " + userDTO.getFirstName() + "." + getString(R.string.are_you_sure));
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();

                        if (isDeleteContact) {
                            deleteContact();
                        }
                    }
                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        isDeleteContact = false;
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
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

                        if (isDeleteContact) {
                            deleteContact();
                        } else if (isUpdateSharedProfile) {
                            shareProfile();
                        }
                    }
                });
        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        isDeleteContact = false;
                        isUpdateSharedProfile = false;
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        updateLabelList();
    }

    public void goBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        System.out.println("OnBackPressed - Activity.");
        goBack();
    }


    public void setEditable(int id, boolean editable) {
        EditText ed = (EditText) findViewById(id);
        ed.setEnabled(editable);
        ed.setFocusable(editable);
        if (editable) {
            ed.setFocusableInTouchMode(editable);
        }
    }


    public void openIntent(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

}
