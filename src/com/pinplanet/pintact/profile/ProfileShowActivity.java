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
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
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


public class ProfileShowActivity extends MyActivity {

    public ProfileDTO mProfile;
    boolean isUploadingImage = false;
    boolean isDeleteProfile = false;
    String mImagePath;

    public static final String PROFILE_PARAM_MODE = "PROFILE_PARAM_MODE";
    public static final String PROFILE_PARAM_NUMBER = "PROFILE_PARAM_NUMBER";
    public static final int MODE_FIRST_NEW_PROFILE = 1, MODE_NEW_PROFILE = 2, MODE_EDIT_PROFILE = 3;
    //MODE_FIRST_NEW_PROFILE is a special case after the registration
    public int paramMode = 0, paramNumber = 0;


    public static Intent getInstance(Context context, int profile) {
        Bundle b = new Bundle();
        b.putInt(PROFILE_PARAM_NUMBER, profile);
        Intent i = new Intent(context, ProfileShowActivity.class);
        i.putExtras(b);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_show);

        if (getIntent().getExtras() != null) {
            paramNumber = getIntent().getExtras().getInt(PROFILE_PARAM_NUMBER, -1);
        }


        mProfile = SingletonLoginData.getInstance().getUserProfiles().get(paramNumber);
        showTitle(mProfile.getUserProfile().getName());

        showLeftImage(R.drawable.actionbar_left_arrow);
        addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        showRightText(getResources().getString(R.string.ab_edit));
        addRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditActivity();
            }
        });

        initViews(false);

        fillUserData();
        fillAddressData();
        fillNoteData();
        fillListData(expAdapterMail, ListViewItemType.ITEM_TYPE.EMAIL, AttributeType.EMAIL);
        fillListData(expAdapterPhone, ListViewItemType.ITEM_TYPE.PHONE, AttributeType.PHONE_NUMBER);
        fillListData(expAdapterSocial, ListViewItemType.ITEM_TYPE.SOCIAL, AttributeType.SERVICE_ID);
    }

    private ExpandableViewListAdapter expAdapterPhone, expAdapterMail, expAdapterSocial, expAdapterAddress;
    private ExpandableListView lvPhone, lvMail, lvAddress, lvSocial;


    public void initViews(boolean editMode) {
        //################# View Setup ###################

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

        //phone
        lvPhone = (ExpandableListView) findViewById(R.id.listViewPhone);

        final SparseArray<ViewListItemGroup> groupsPhone=new SparseArray<ViewListItemGroup>();
        groupsPhone.put(0,new ListViewGroupItem(R.drawable.profile_phone));

        expAdapterPhone=new ExpandableViewListAdapter(this,groupsPhone);

        lvPhone.setAdapter(expAdapterPhone);
        expAdapterPhone.setEditMode(editMode);

        //mail
        lvMail = (ExpandableListView) findViewById(R.id.listViewEmail);

        final SparseArray<ViewListItemGroup> groupsMail=new SparseArray<ViewListItemGroup>();
        groupsMail.put(0,new ListViewGroupItem(R.drawable.profile_mail));

        expAdapterMail=new ExpandableViewListAdapter(this,groupsMail);

        lvMail.setAdapter(expAdapterMail);
        expAdapterMail.setEditMode(editMode);

        //address
        lvAddress = (ExpandableListView) findViewById(R.id.listViewAddress);

        final SparseArray<ViewListItemGroup> groupsAddress=new SparseArray<ViewListItemGroup>();
        groupsAddress.put(0, new ListViewGroupItem(R.drawable.profile_address));

        expAdapterAddress=new ExpandableViewListAdapter(this,groupsAddress);

        lvAddress.setAdapter(expAdapterAddress);
        expAdapterAddress.setEditMode(editMode);

        //social
        lvSocial = (ExpandableListView) findViewById(R.id.listViewSozial);

        final SparseArray<ViewListItemGroup> groupsSocial=new SparseArray<ViewListItemGroup>();
        groupsSocial.put(0,new ListViewGroupItem(R.drawable.profile_url));

        expAdapterSocial=new ExpandableViewListAdapter(this,groupsSocial);

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
        CustomNetworkImageView ivPhoto = (CustomNetworkImageView) findViewById(R.id.imageViewAdd);

        if ( mProfile.getUserProfile().getPathToImage() != null &&
                mProfile.getUserProfile().getPathToImage().length() > 0)
        {
            ivPhoto.setImageUrl(mProfile.getUserProfile().getPathToImage(), AppController.getInstance().getImageLoader());
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //loadImage(position, item.getUserProfile().getPathToImage(), image);
        }else {
            ivPhoto.setLocalBitmapFromResource(this,R.drawable.silhouette);
        }


        setText(R.id.pcn_first_name, mProfile.getUserProfile().getFirstName());
        setText(R.id.pcn_last_name, mProfile.getUserProfile().getLastName());
        setText(R.id.pcn_middle_name, mProfile.getUserProfile().getMiddleName());
        setText(R.id.pcn_title, mProfile.getUserProfile().getTitle());
        setText(R.id.pcn_company, mProfile.getUserProfile().getCompanyName());
    }

    private void fillListData(ExpandableViewListAdapter adapter, ListViewItemType.ITEM_TYPE listItemType, AttributeType attributeTypeToFind) {
        List<UserProfileAttribute> attributes = mProfile.getUserProfileAttributes();
        ArrayList<ViewListItem> list = adapter.getGroups().get(0).getChilds();
        list.clear();
        if(attributes != null) {
            for (UserProfileAttribute attribute : attributes) {
                if (attribute.getType() == attributeTypeToFind && attribute.getValue() != null
                        && attribute.getValue().length() > 0) {
                    list.add(new ListViewItemShowDefault(attribute.getLabel(), attribute.getValue(), listItemType, attribute));
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
                ListViewItemShowAddress addr = new ListViewItemShowAddress(address.getLabel(), address.getAddressLine1(), address.getCity(),
                        address.getState(), address.getPostalCode(), address);
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
                    ((TextView) findViewById(R.id.notesTextEntry)).setText(attribute.getValue());
                    break;
                }
            }
        }
    }


    public void openEditActivity()
    {
        Intent myIntent = ProfileCreateEditActivity.getInstance(this,ProfileCreateEditActivity.MODE_EDIT_PROFILE, paramNumber);
        startActivity(myIntent);
        finish();
        overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
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
			return;
		}

		if ( isDeleteProfile ) {
			List<ProfileDTO> profs = SingletonLoginData.getInstance().getUserProfiles();
			profs.remove(paramNumber);
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

	public void setText(int id, String value)
	{
        ((TextView)findViewById(id)).setText(value);
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
