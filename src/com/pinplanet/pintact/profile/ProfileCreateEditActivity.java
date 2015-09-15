package com.pinplanet.pintact.profile;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.label.ActivitySelectLabel;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.HttpConnectionForImage;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.late.gui.ExpandableListViewAdvanced;
import de.late.widget.ExpandableViewListAdapter;
import de.late.widget.ViewListItemGroup;
import de.late.widget.ViewListItem;


public class ProfileCreateEditActivity extends MyActivity {

    private static final String TAG = ProfileCreateEditActivity.class.getName();

    public ProfileDTO mProfile;
    boolean isUploadingImage = false;
    boolean isDeleteProfile = false;
    String mImagePath;

    public static final String PROFILE_PARAM_MODE = "PROFILE_PARAM_MODE";
    public static final String PROFILE_PARAM_NUMBER = "PROFILE_PARAM_NUMBER";
    public static final int MODE_FIRST_NEW_PROFILE = 1, MODE_NEW_PROFILE = 2, MODE_EDIT_PROFILE = 3;
    //MODE_FIRST_NEW_PROFILE is a special case after the registration
    public int paramMode = 0, paramNumber = 0;

    public static Intent getInstance(Context context, int mode, int profile) {
        Bundle b = new Bundle();
        b.putInt(PROFILE_PARAM_MODE, mode);
        b.putInt(PROFILE_PARAM_NUMBER, profile);
        Intent i = new Intent(context, ProfileCreateEditActivity.class);
        i.putExtras(b);
        return i;
    }

    public static Intent getInstance(Context context, int mode) {
        Bundle b = new Bundle();
        b.putInt(PROFILE_PARAM_MODE, mode);
        Intent i = new Intent(context, ProfileCreateEditActivity.class);
        i.putExtras(b);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_create_edit);

        if (getIntent().getExtras() != null) {
            paramMode = getIntent().getExtras().getInt(PROFILE_PARAM_MODE, 0);
            paramNumber = getIntent().getExtras().getInt(PROFILE_PARAM_NUMBER, -1);
        }

        if (paramMode == MODE_EDIT_PROFILE)
        {
            mProfile = SingletonLoginData.getInstance().getUserProfiles().get(paramNumber);
            showTitle(mProfile.getUserProfile().getName());
            ((EditText) findViewById(R.id.pcn_profile_name)).setText(mProfile.getUserProfile().getName());
        }
        else//new profile
        {
            showTitle(R.string.ab_new_profile);
            mProfile = new ProfileDTO();
            mProfile.setUserProfile(new UserProfile());
            mProfile.setUserProfileAttributes(new ArrayList<UserProfileAttribute>());
            mProfile.setUserProfileAddresses(new ArrayList<UserProfileAddress>());
        }

        if (paramMode == MODE_FIRST_NEW_PROFILE) {
            hideLeft();
        } else {
            showLeftImage(R.drawable.actionbar_left_arrow);
            addLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack();
                }
            });
        }

        showRightText(getResources().getString(R.string.ab_done));
        addRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveNew();
            }
        });

        initViews(true);

        fillUserData();

        if(paramMode==MODE_EDIT_PROFILE)
        {
            fillAddressData();
            fillNoteData();
            fillListData(expAdapterMail, ListViewItemType.ITEM_TYPE.EMAIL, AttributeType.EMAIL);
            fillListData(expAdapterPhone, ListViewItemType.ITEM_TYPE.PHONE, AttributeType.PHONE_NUMBER);
            fillListData(expAdapterSocial, ListViewItemType.ITEM_TYPE.SOCIAL, AttributeType.SERVICE_ID);
        }
    }

    //private ExpArrayAdapter expAdapterPhone, expAdapterMail, expAdapterSocial, expAdapterAddress;
    private ExpandableViewListAdapter expAdapterPhone, expAdapterMail, expAdapterSocial, expAdapterAddress;
    private ExpandableListView lvPhone, lvMail, lvAddress, lvSocial;


    private void itemDeleteClicked(ExpandableViewListAdapter adapter, ViewListItem item)
    {
        Log.i(TAG, "itemDeleteClicked" + item.getPosition());
        adapter.getGroups().get(0).getChilds().remove(item.getPosition());
        adapter.notifyDataSetChanged();
    }

    public void initViews(boolean editMode) {
        //################# View Setup ###################

        setEditable(R.id.notesTextEntry, editMode);
        
        if (editMode) {
          // constraints on note field
          final TextView noteView = (TextView)findViewById(R.id.notesTextEntry);
          noteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 500) {
                  noteView.setError("Value cannot exceed 500 characters");
                } else {
                  noteView.setError(null);
                }
            }
          });
          
          // constraints on profile name
          final TextView profileNameView = (TextView)findViewById(R.id.pcn_profile_name);
          profileNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
              int contentsLength = editable.toString().length();
              if (contentsLength == 0) {
                profileNameView.setError("Value is required");
              } else if (contentsLength > 50) {
                profileNameView.setError("Value cannot exceed 50 characters");
              } else {
                profileNameView.setError(null);
              }
            }
          });
          
          // constraints on first name
          final TextView firstNameView = (TextView)findViewById(R.id.pcn_first_name);
          firstNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
              int contentsLength = editable.toString().length();
              if (contentsLength == 0) {
                firstNameView.setError("Value is required");
              } else if (contentsLength > 50) {
                firstNameView.setError("Value cannot exceed 50 characters");
              } else {
                firstNameView.setError(null);
              }
            }
          });
          
          // constraints on last name
          final TextView lastNameView = (TextView)findViewById(R.id.pcn_last_name);
          lastNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 50) {
                  lastNameView.setError("Value cannot exceed 50 characters");
                } else {
                  lastNameView.setError(null);
                }
            }
          });
          
          // constraints on middle name
          final TextView middleNameView = (TextView)findViewById(R.id.pcn_middle_name);
          middleNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 50) {
                  middleNameView.setError("Value cannot exceed 50 characters");
                } else {
                  middleNameView.setError(null);
                }
            }
          });
          
          // constraints on title
          final TextView titleView = (TextView)findViewById(R.id.pcn_title);
          titleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 50) {
                  titleView.setError("Value cannot exceed 50 characters");
                } else {
                  titleView.setError(null);
                }
            }
          });
          
          // constraints on company
          final TextView companyView = (TextView)findViewById(R.id.pcn_company);
          companyView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 200) {
                  companyView.setError("Value cannot exceed 200 characters");
                } else {
                  companyView.setError(null);
                }
            }
          });
        }

        //delete button
        if (paramMode == MODE_EDIT_PROFILE)
        {
            TextView tv = (TextView) findViewById(R.id.buttonDeleteProfile);
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog(getString(R.string.DIALOG_TITLE_SURE), getString(R.string.DIALOG_MESSAGE_DELETE_PROFILE));
                }
            });
        }

        //################# ListView/Adapter Setup ###################

        View footer = null;
        TextView textViewFooter = null;

        //phone
        lvPhone = (ExpandableListView) findViewById(R.id.listViewPhone);

        final SparseArray<ViewListItemGroup> groupsPhone=new SparseArray<ViewListItemGroup>();
        groupsPhone.put(0,new ListViewGroupItem(R.drawable.profile_phone));

        expAdapterPhone=new ExpandableViewListAdapter(this,groupsPhone,new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {}

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter,int groupPosition, final int childPosition, View convertView) {
                convertView.findViewById(R.id.imageView).setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewListItem itemTag=(ViewListItem)view.getTag();
                        itemDeleteClicked(ExpandableViewListAdapter, itemTag);
                    }
                });

                TextView tv=(TextView)convertView.findViewById(R.id.textViewEntryTitle);
                tv.setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListViewItemEditDefault itemTag=(ListViewItemEditDefault)view.getTag();
                        Intent i=ActivitySelectLabel.getInstance(ProfileCreateEditActivity.this,ActivitySelectLabel.LABEL_TYPE_PHONE,itemTag.getTitle(),itemTag.getPosition());
                        startActivityForResult(i,ActivitySelectLabel.LABEL_TYPE_PHONE);
                    }
                });
            }
        });

        if (editMode) {
            footer = getLayoutInflater().inflate(R.layout.list_view_profile_footer, null);
            textViewFooter = (TextView) footer.findViewById(R.id.textViewFooter);
            textViewFooter.setText(R.string.pcn_add_phone);

            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListViewItemEditDefault li = new ListViewItemEditDefault(null, null, ListViewItemType.ITEM_TYPE.PHONE, null);
                    expAdapterPhone.getGroups().get(0).getChilds().add(li);
                    expAdapterPhone.notifyDataSetChanged();
                }
            });

            lvPhone.addFooterView(footer);
        }

        lvPhone.setAdapter(expAdapterPhone);
        expAdapterPhone.setEditMode(editMode);

        //mail
        lvMail = (ExpandableListView) findViewById(R.id.listViewEmail);

        final SparseArray<ViewListItemGroup> groupsMail=new SparseArray<ViewListItemGroup>();
        groupsMail.put(0,new ListViewGroupItem(R.drawable.profile_mail));

        expAdapterMail=new ExpandableViewListAdapter(this,groupsMail,new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {}

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter,int groupPosition, final int childPosition, View convertView) {
                convertView.findViewById(R.id.imageView).setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewListItem itemTag=(ViewListItem)view.getTag();
                        itemDeleteClicked(ExpandableViewListAdapter, itemTag);
                    }
                });

                TextView tv=(TextView)convertView.findViewById(R.id.textViewEntryTitle);
                tv.setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListViewItemEditDefault itemTag=(ListViewItemEditDefault)view.getTag();
                        Intent i=ActivitySelectLabel.getInstance(ProfileCreateEditActivity.this,ActivitySelectLabel.LABEL_TYPE_MAIL,itemTag.getTitle(),itemTag.getPosition());
                        startActivityForResult(i,ActivitySelectLabel.LABEL_TYPE_MAIL);
                    }
                });
            }
        });

        if (editMode) {
            footer = getLayoutInflater().inflate(R.layout.list_view_profile_footer, null);
            textViewFooter = (TextView) footer.findViewById(R.id.textViewFooter);
            textViewFooter.setText(R.string.pcn_add_email);

            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListViewItemEditDefault li = new ListViewItemEditDefault(null, null, ListViewItemType.ITEM_TYPE.EMAIL, null);
                    expAdapterMail.getGroups().get(0).getChilds().add(li);
                    expAdapterMail.notifyDataSetChanged();
                }
            });

            lvMail.addFooterView(footer);
        }

        lvMail.setAdapter(expAdapterMail);
        expAdapterMail.setEditMode(editMode);

        //address
        lvAddress = (ExpandableListView) findViewById(R.id.listViewAddress);

        final SparseArray<ViewListItemGroup> groupsAddress=new SparseArray<ViewListItemGroup>();
        groupsAddress.put(0,new ListViewGroupItem(R.drawable.profile_address));

        expAdapterAddress=new ExpandableViewListAdapter(this,groupsAddress,new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {}

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter,int groupPosition, final int childPosition, View convertView) {
                convertView.findViewById(R.id.imageView).setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewListItem itemTag=(ViewListItem)view.getTag();
                        itemDeleteClicked(ExpandableViewListAdapter, itemTag);
                    }
                });


                TextView tv=(TextView)convertView.findViewById(R.id.textViewEntryTitle);
                tv.setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListViewItemEditAddress itemTag=(ListViewItemEditAddress)view.getTag();
                        Intent i=ActivitySelectLabel.getInstance(ProfileCreateEditActivity.this,ActivitySelectLabel.LABEL_TYPE_ADDRESS,itemTag.getTitle(),itemTag.getPosition());
                        startActivityForResult(i,ActivitySelectLabel.LABEL_TYPE_ADDRESS);
                    }
                });
            }
        });

        if (editMode) {
            footer = getLayoutInflater().inflate(R.layout.list_view_profile_footer, null);
            textViewFooter = (TextView) footer.findViewById(R.id.textViewFooter);
            textViewFooter.setText(R.string.pcn_add_addr);

            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListViewItemEditAddress li = new ListViewItemEditAddress(null, "", "", "", "", null);
                    expAdapterAddress.getGroups().get(0).getChilds().add(li);
                    expAdapterAddress.notifyDataSetChanged();
                }
            });

            lvAddress.addFooterView(footer);
        }

        lvAddress.setAdapter(expAdapterAddress);
        expAdapterAddress.setEditMode(editMode);

        //social
        lvSocial = (ExpandableListView) findViewById(R.id.listViewSozial);

        final SparseArray<ViewListItemGroup> groupsSocial=new SparseArray<ViewListItemGroup>();
        groupsSocial.put(0,new ListViewGroupItem(R.drawable.profile_url));

        expAdapterSocial=new ExpandableViewListAdapter(this,groupsSocial,new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView) {}

            @Override
            public void initChild(final ExpandableViewListAdapter ExpandableViewListAdapter,int groupPosition, final int childPosition, View convertView) {
                convertView.findViewById(R.id.imageView).setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewListItem itemTag=(ViewListItem)view.getTag();
                        itemDeleteClicked(ExpandableViewListAdapter, itemTag);
                    }
                });

                TextView tv=(TextView)convertView.findViewById(R.id.textViewEntryTitle);
                tv.setTag(ExpandableViewListAdapter.getChild(groupPosition,childPosition));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ListViewItemEditDefault itemTag=(ListViewItemEditDefault)view.getTag();
                        Intent i=ActivitySelectLabel.getInstance(ProfileCreateEditActivity.this,ActivitySelectLabel.LABEL_TYPE_SOCIAL,itemTag.getTitle(),itemTag.getPosition());
                        startActivityForResult(i,ActivitySelectLabel.LABEL_TYPE_SOCIAL);
                    }
                });
            }
        });


        if (editMode) {
            footer = getLayoutInflater().inflate(R.layout.list_view_profile_footer, null);
            textViewFooter = (TextView) footer.findViewById(R.id.textViewFooter);
            textViewFooter.setText(R.string.pcn_add_url);

            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListViewItemEditDefault li = new ListViewItemEditDefault(null, null, ListViewItemType.ITEM_TYPE.SOCIAL, null);
                    expAdapterSocial.getGroups().get(0).getChilds().add(li);
                    expAdapterSocial.notifyDataSetChanged();
                }
            });

            lvSocial.addFooterView(footer);
        }

        lvSocial.setAdapter(expAdapterSocial);
        expAdapterSocial.setEditMode(editMode);

        ((ExpandableListViewAdvanced) lvPhone).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvMail).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvAddress).setAllowAutosizing(true);
        ((ExpandableListViewAdvanced) lvSocial).setAllowAutosizing(true);

        ((ExpandableListViewAdvanced) lvPhone).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvMail).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvAddress).setIgnoreGroupClicks(true);
        ((ExpandableListViewAdvanced) lvSocial).setIgnoreGroupClicks(true);

        lvPhone.expandGroup(0);
        lvMail.expandGroup(0);
        lvAddress.expandGroup(0);
        lvSocial.expandGroup(0);
    }


    private void fillUserData()
    {
        ImageView ivPhoto = (ImageView) findViewById(R.id.imageViewAdd);
        Bitmap bm = SingletonLoginData.getInstance().getBitmap(paramNumber);
        if (bm != null) {
            ivPhoto.setImageBitmap(bm);
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            ivPhoto.setImageResource(R.drawable.add_an_image_no_text);
        }


        if (paramMode == MODE_NEW_PROFILE || paramMode == MODE_FIRST_NEW_PROFILE) {
            setEditable(R.id.pcn_first_name, SingletonLoginData.getInstance().getUserData().firstName);
            setEditable(R.id.pcn_last_name, SingletonLoginData.getInstance().getUserData().lastName);
            setEditable(R.id.pcn_middle_name, SingletonLoginData.getInstance().getUserData().middleName);

            ListViewItemEditDefault li = new ListViewItemEditDefault(null, SingletonLoginData.getInstance().getUserData().emailId, ListViewItemType.ITEM_TYPE.EMAIL, null);
            expAdapterMail.getGroups().get(0).getChilds().add(li);
            expAdapterMail.notifyDataSetChanged();
        }
        else // MODE_EDIT_PROFILE
        {
            setEditable(R.id.pcn_first_name, mProfile.getUserProfile().getFirstName());
            setEditable(R.id.pcn_last_name, mProfile.getUserProfile().getLastName());
            setEditable(R.id.pcn_middle_name, mProfile.getUserProfile().getMiddleName());
            setEditable(R.id.pcn_title, mProfile.getUserProfile().getTitle());
            setEditable(R.id.pcn_company, mProfile.getUserProfile().getCompanyName());
        }
    }

    private void fillListData(ExpandableViewListAdapter adapter, ListViewItemType.ITEM_TYPE listItemType, AttributeType attributeTypeToFind) {
        List<UserProfileAttribute> attributes = mProfile.getUserProfileAttributes();
        ArrayList<ViewListItem> list = adapter.getGroups().get(0).getChilds();
        list.clear();
        if(attributes != null) {
            for (UserProfileAttribute attribute : attributes) {
                if (attribute.getType() == attributeTypeToFind) {
                    list.add(new ListViewItemEditDefault(attribute.getLabel(), attribute.getValue(), listItemType, attribute));
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void fillAddressData() {

        List<UserProfileAddress> list = mProfile.getUserProfileAddresses();
        ArrayList<ViewListItem> listItems = expAdapterAddress.getGroups().get(0).getChilds();
        listItems.clear();
        if(list != null) {
            for (UserProfileAddress address : list) {
                ListViewItemEditAddress addr = new ListViewItemEditAddress(address.getLabel(), address.getAddressLine1(),
                        address.getCity(), address.getState(), address.getPostalCode(), address);
                listItems.add(addr);
            }
        }
        expAdapterAddress.notifyDataSetChanged();
    }

    private void fillNoteData()
    {
        List<UserProfileAttribute> attributes = mProfile.getUserProfileAttributes();
        if(attributes != null) {
            for (UserProfileAttribute attribute : attributes) {
                if (attribute.getType() == AttributeType.PRIVATE_NOTE) {
                    ((TextView) findViewById(R.id.notesEntryTitle)).setText(attribute.getLabel());
                    ((EditText) findViewById(R.id.notesTextEntry)).setText(attribute.getValue());
                    break;
                }
            }
        }
    }

    /**
     * (Dennis)
     * we can read directly from the adapters now
     * */
    public void onSaveNew()
    {
        boolean error = false;
        
        // collect required data
        String profileName = ((EditText)findViewById(R.id.pcn_profile_name)).getText().toString();
        if (profileName == null || profileName.length() == 0) {
          error = true;
          ((EditText)findViewById(R.id.pcn_profile_name)).setError("Value is required");
        }
        String firstName = ((EditText)findViewById(R.id.pcn_first_name)).getText().toString();
        if (firstName == null || firstName.length() == 0) {
          error = true;
          ((EditText)findViewById(R.id.pcn_first_name)).setError("Value is required");
        }
      
        error = error || this.checkForErrorOnChildView(findViewById(R.id.profile_edit_view));
        if (error) {
          myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
          return;
        }
      
        // collect rest of data
        String middleName = ((EditText)findViewById(R.id.pcn_middle_name)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.pcn_last_name)).getText().toString();
        String title = ((EditText)findViewById(R.id.pcn_title)).getText().toString();
        String company = ((EditText)findViewById(R.id.pcn_company)).getText().toString();

        ProfileDTO profile = new ProfileDTO();
        profile.setUserProfile(new UserProfile());
        profile.setUserProfileAttributes(new ArrayList<UserProfileAttribute>());
        profile.setUserProfileAddresses(new ArrayList<UserProfileAddress>());

        profile.setUserId(mProfile.getUserId());
        profile.setProfileId(mProfile.getUserProfile().getId());
        profile.getUserProfile().setId(mProfile.getUserProfile().getId());
        profile.getUserProfile().setName(profileName);
        profile.getUserProfile().setFirstName(firstName);
        profile.getUserProfile().setMiddleName(middleName);
        profile.getUserProfile().setLastName(lastName);
        profile.getUserProfile().setTitle(title);
        profile.getUserProfile().setCompanyName(company);
        profile.getUserProfile().setPathToImage(mProfile.getUserProfile().getPathToImage());

        addAttributeNew(profile);
        addAddressNew(profile);

        Gson gson = new GsonBuilder().create();
        String params = gson.toJson(profile);
        String path;

        if (paramMode == MODE_EDIT_PROFILE  )
        {
            path = "/api/profiles/" + profile.getUserProfile().getId()
                    + "/update.json?" + SingletonLoginData.getInstance().getPostParam();
        }
        else
        {
            path = "/api/profiles.json?" + SingletonLoginData.getInstance().getPostParam();
        }

        SingletonNetworkStatus.getInstance().setActivity(this);
        new HttpConnection().access(this, path, params, "POST",R.string.DIALOG_MESSAGE_PROFILE_SAVE);

        mProfile = profile;
    }
    
    private boolean checkForErrorOnChildView(View v) {
      if (!(v instanceof ViewGroup)) {
        if (v instanceof TextView && ((TextView)v).getError() != null) {
          return true;
        }
        return false;
      }
      
      ViewGroup vg = (ViewGroup) v;
      for (int i = 0; i < vg.getChildCount(); i++) {
          boolean result = checkForErrorOnChildView(vg.getChildAt(i));
          if (result == true) {
            return true;
          }
      }
      return false;
    }

    public void addAttributeNew(ProfileDTO profile) {
        addAttributeTypeNew(profile, AttributeType.PHONE_NUMBER, expAdapterPhone);
        addAttributeTypeNew(profile, AttributeType.EMAIL, expAdapterMail);
        addAttributeTypeNew(profile, AttributeType.SERVICE_ID, expAdapterSocial);
        
        String noteValue = ((EditText)findViewById(R.id.notesTextEntry)).getText().toString();
        if (noteValue != null && noteValue.length() > 0) {
          UserProfileAttribute userProfileAttribute = null;
          for (UserProfileAttribute attribute : mProfile.getUserProfileAttributes()) {
            if (attribute.getType() == AttributeType.PRIVATE_NOTE) {
              userProfileAttribute = attribute;
            }
          }
          if (userProfileAttribute == null) {
            userProfileAttribute = new UserProfileAttribute();
            userProfileAttribute.setType(AttributeType.PRIVATE_NOTE);
            userProfileAttribute.setLabel(((TextView)findViewById(R.id.notesEntryTitle)).getText().toString());
            if (profile.getProfileId() != null) {
              userProfileAttribute.setUserId(profile.getUserId());
              userProfileAttribute.setUserProfileId(profile.getProfileId());
            }
          }
          userProfileAttribute.setValue(((EditText)findViewById(R.id.notesTextEntry)).getText().toString());
          profile.getUserProfileAttributes().add(userProfileAttribute);
        }
    }

    public void addAttributeTypeNew(ProfileDTO profile, AttributeType type,ExpandableViewListAdapter adapter)
    {
        for(Object o:adapter.getGroups().get(0).getChilds())
        {
            ListViewItemEditDefault li=(ListViewItemEditDefault)o;
            if (li.getText() != null && li.getText().length() > 0) {
              UserProfileAttribute userProfileAttribute = li.getUserProfileAttribute();
              if (userProfileAttribute == null) {
                userProfileAttribute = new UserProfileAttribute();
                userProfileAttribute.setType(type);
                if (profile.getProfileId() != null) {
                  userProfileAttribute.setUserId(profile.getUserId());
                  userProfileAttribute.setUserProfileId(profile.getProfileId());
                }
              }
              userProfileAttribute.setLabel(li.getTitle());
              userProfileAttribute.setValue(li.getText());
              profile.getUserProfileAttributes().add(userProfileAttribute);
            }
        }
    }

    public void addAddressNew(ProfileDTO profile) {
        for(Object o:expAdapterAddress.getGroups().get(0).getChilds())
        {
            ListViewItemEditAddress li=(ListViewItemEditAddress)o;
            if ((li.getStreet() != null && li.getStreet().length() > 0)
                || (li.getCity() != null && li.getCity().length() > 0)
                || (li.getState() != null && li.getState().length() > 0)
                || (li.getZip() != null && li.getZip().length() > 0)) {
              UserProfileAddress userProfileAddress = li.getUserProfileAddress();
              if (userProfileAddress == null) {
                userProfileAddress = new UserProfileAddress();
                if (profile.getProfileId() != null) {
                  userProfileAddress.setUserId(profile.getUserId());
                  userProfileAddress.setUserProfileId(profile.getProfileId());
                }
              }
              userProfileAddress.setLabel(li.getTitle());
              userProfileAddress.setAddressLine1(li.getStreet());
              userProfileAddress.setCity(li.getCity());
              userProfileAddress.setState(li.getState());
              userProfileAddress.setPostalCode(li.getZip());
              profile.getUserProfileAddresses().add(userProfileAddress);
            }
        }
    }

	public class ImageDTO {
		public Long sourceId;
		public String thumbnailPath;
	}

	public void onPostNetwork() {

		SingletonNetworkStatus.getInstance().setDoNotShowStatus(false);
		if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
			myDialog(SingletonNetworkStatus.getInstance().getMsg(),
					SingletonNetworkStatus.getInstance().getErrMsg());
			SingletonNetworkStatus.getInstance().setCode(0);
			return;
		}

		if ( isDeleteProfile ) {
			List<ProfileDTO> profs = SingletonLoginData.getInstance().getUserProfiles();
			AppService.removeProfile(profs.get(paramNumber));
			SingletonLoginData.getInstance().removeBitmap(paramNumber);
			goBack();
			return;
		}

		if ( isUploadingImage ) {
			isUploadingImage = false;

			Gson gson = new GsonBuilder().create();
			ImageDTO imInfo = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ImageDTO.class);
			mProfile.getUserProfile().setPathToImage(imInfo.thumbnailPath);

			return;
		}

        //this creates the new profile
		Gson gson = new GsonBuilder().create();
        AppService.handleGetSingleProfileResponse();
		// 200 is ok
		if ( paramMode!=MODE_FIRST_NEW_PROFILE  )
		{
			List<ProfileDTO> profs = SingletonLoginData.getInstance().getUserProfiles();
			if ( paramMode == MODE_EDIT_PROFILE )
            {
				profs.remove(paramNumber);
				profs.add(paramNumber, mProfile);
			}
            else //paramMode == MODE_NEW_PROFILE
            {
				mProfile = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ProfileDTO.class);
				profs.add(mProfile);
			}

			goBack();
		}
        else //paramMode==MODE_FIRST_NEW_PROFILE, we start the success screen
        {
			Intent it = new Intent(this, ProfileCreatedSuccessfulActivity.class);
			startActivity(it);
		}

	}



	public void setViewValue(int id, String value, boolean b)
	{
		EditText v = (EditText) findViewById(id);

		v.setText(value);
		v.setFocusable(b);
		v.setClickable(b);

		if ( b )
			v.setFocusableInTouchMode(b);
	}


    public void setEditable(int id, boolean editable)
    {
        EditText ed = (EditText) findViewById(id);
        ed.setEnabled(editable);
        ed.setFocusable(editable);
        if(editable)
        { ed.setFocusableInTouchMode(editable);}
    }

	public void setEditable(int id, String value)
	{
		setViewValue(id, value, true);
	}


	public void alertDialog(String title, String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(this, R.style.AlertDialogCustom));

		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("YES",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				deleteProfile();
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

	public void deleteProfile() {
		isDeleteProfile = true;
		String path = "/api/profiles/" + mProfile.getUserProfile().getId() + "/delete.json?" + SingletonLoginData.getInstance().getPostParam();
		SingletonNetworkStatus.getInstance().setActivity(this);
		new HttpConnection().access(this, path, "", "POST");

	}


	private static final String TEMP_PHOTO_FILE = "temporary_holder.png";


	public String saveBitmap2File(Bitmap bmp) {
		FileOutputStream out;
		String filename;

	    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

	        filename = Environment.getExternalStorageDirectory() + "/" + TEMP_PHOTO_FILE;
	    } else
	    	return null;

		try {
		       out = new FileOutputStream(filename);
		       bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		       out.close();
		       return filename;
		} catch (Exception e) {
		    e.printStackTrace();
		    return filename;
		}
	}

	public void onUploadProfImage(View view) {
		System.out.println("Select an image from gallery.");

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK); //, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		photoPickerIntent.setType("image/*");
		//photoPickerIntent.putExtra("crop", "true");
		//photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
		//photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(photoPickerIntent, 1);

	}

	//create helping method cropCapturedImage(Uri picUri)
	public void cropCapturedImage(Uri picUri)
	{
		//call the standard crop action intent
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		//indicate image type and Uri of image
		cropIntent.setDataAndType(picUri, "image/*");
		//set crop properties
		cropIntent.putExtra("crop", "true");
		//indicate aspect of desired crop
		cropIntent.putExtra("aspectX", 1);
		cropIntent.putExtra("aspectY", 1);
		//indicate output X and Y
		cropIntent.putExtra("outputX", 256);
		cropIntent.putExtra("outputY", 256);
		//retrieve data on return
		cropIntent.putExtra("return-data", true);
		//start the activity - we handle returning in onActivityResult
		startActivityForResult(cropIntent, 2);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode > 100 && resultCode == RESULT_OK)//label selection
        {
            Log.d(TAG,"onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);

            final int indexOfItem=data.getIntExtra(ActivitySelectLabel.PARAM_LABEL_SOURCE_INDEX,0);
            final String label=data.getStringExtra(ActivitySelectLabel.PARAM_SELECTED_LABEL);

            Log.d(TAG,"onActivityResult label:"+label);

            switch(requestCode)
            {
                case ActivitySelectLabel.LABEL_TYPE_ADDRESS:
                    ((ListViewItemEditAddress)expAdapterAddress.getChild(0,indexOfItem)).setTitle(label);
                    expAdapterAddress.notifyDataSetChanged();
                break;

                case ActivitySelectLabel.LABEL_TYPE_SOCIAL:
                ((ListViewItemEditDefault)expAdapterSocial.getChild(0,indexOfItem)).setTitle(label);
                expAdapterSocial.notifyDataSetChanged();
                break;

                case ActivitySelectLabel.LABEL_TYPE_MAIL:
                    ((ListViewItemEditDefault)expAdapterMail.getChild(0,indexOfItem)).setTitle(label);
                    expAdapterMail.notifyDataSetChanged();
                break;

                case ActivitySelectLabel.LABEL_TYPE_PHONE:
                    ((ListViewItemEditDefault)expAdapterPhone.getChild(0,indexOfItem)).setTitle(label);
                    expAdapterPhone.notifyDataSetChanged();
                break;

            }

            return;
        }

		if (requestCode == 1 && resultCode == RESULT_OK && data != null)
		{
			Uri uri = data.getData();
			if (uri == null)
				return;

			try {
				/*the user's device may not support cropping*/
				String path = getPath(uri);
				if ( path != null && path.length() > 1 )
					System.out.println("Getting image from : " + path);
				cropCapturedImage(uri);
			}
			catch(ActivityNotFoundException aNFE){
				//display an error message if user device doesn't support
				String errorMessage = "Sorry - your device doesn't support the crop action!";
				Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
				toast.show();
			}
		}

		if ( requestCode == 2 && resultCode == RESULT_OK && data != null ) {

			//Create an instance of bundle and get the returned data
			Bundle extras = data.getExtras();
			//get the cropped bitmap from extras
			Bitmap bm = extras.getParcelable("data");

			//Bitmap bm = getBitmapFromUri(uri);
			ImageView ivPhoto = (ImageView) findViewById(R.id.imageViewAdd);
			if ( bm != null ) {
				SingletonLoginData.getInstance().setBitmap(paramNumber, bm);
				ivPhoto.setImageBitmap(bm);
				ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
			}

			// WRITE Bitmap TO FILE
			mImagePath = saveBitmap2File(bm);

			// CALL THIS METHOD TO GET THE URI FROM THE BITMAP
			//Uri tempUri = getImageUri(getApplicationContext(), bm);

			// CALL THIS METHOD TO GET THE ACTUAL PATH
			//mImagePath = getTempUri();
			//mImagePath = getRealPathFromURI(tempUri);

			System.out.println("file path : " + mImagePath);

			if ( mImagePath == null || mImagePath.length() < 1)
				return;

			// send file to server?
			isUploadingImage = true;
			String path = "/api/image.json?" +
					SingletonLoginData.getInstance().getPostParam();

			SingletonNetworkStatus.getInstance().setActivity(this);
			new HttpConnectionForImage().access(this, path, "", "POST", mImagePath);

			//File finalFile = new File(filePath);

		}
	}

	@SuppressWarnings("deprecation")
	public String getPath(Uri uri) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		String imagePath = cursor.getString(column_index);
		System.out.println("getPath: " + imagePath);

		return imagePath;
	}

    public void goBack() {
		finish();
		overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
    }
    
    @Override
    public void onBackPressed() {
    	System.out.println("OnBackPressed - Activity.");
    	goBack();
    }




}
