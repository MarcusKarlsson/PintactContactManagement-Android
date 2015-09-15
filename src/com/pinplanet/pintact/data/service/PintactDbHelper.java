package com.pinplanet.pintact.data.service;

import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.UserProfileChildType;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PintactDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "pintact_data_db";
	private static final int DATABASE_VERSION = 15;
	private static final String TAG = PintactDbHelper.class.getSimpleName();

  private static PintactDbHelper pintactDbHelper;

	private PintactDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

  public static void init(Context context){
    pintactDbHelper = new PintactDbHelper(context);
  }

  public static PintactDbHelper getInstance(){
    return pintactDbHelper;
  }
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {

    createPintactTables(sqLiteDatabase);

    sqLiteDatabase.execSQL("CREATE TABLE "+ContactTableContract.CONTACT_ADDRESS_TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + ContactTableContract.USER_ID +" int, "
        + ContactTableContract.ADDRESS_BOOK_ID + " text, "
        + "UNIQUE(" + ContactTableContract.USER_ID+ ") On CONFLICT REPLACE);");

    sqLiteDatabase.execSQL("CREATE TABLE "+InvitedEmailAddressContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + InvitedEmailAddressContract.EMAIL_ID +" text, "
        + "UNIQUE(" + InvitedEmailAddressContract.EMAIL_ID + ") On CONFLICT REPLACE);");

    sqLiteDatabase.execSQL("CREATE TABLE "+LabelTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + LabelTableContract.LABEL_TEXT +" text, "
        + "UNIQUE(" + LabelTableContract.LABEL_TEXT + ") On CONFLICT REPLACE);");
    
    sqLiteDatabase.execSQL("CREATE TABLE " + UserProfileChildLabelContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE + " text, "
        + UserProfileChildLabelContract.ATTRIBUTE_TYPE + " text, "
        + UserProfileChildLabelContract.LABEL + " text, "
        + "UNIQUE(" + UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE + ", "
        + UserProfileChildLabelContract.ATTRIBUTE_TYPE + ", "
        + UserProfileChildLabelContract.LABEL + ") On CONFLICT REPLACE);");

        sqLiteDatabase.execSQL("CREATE TABLE " + ContactTableContract.IS_PINTACT_TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
                + ContactTableContract.LOCAL_CONTACT_ID + " text, "
                + ContactTableContract.USER_ID + " int, "
                + "UNIQUE(" + ContactTableContract.LOCAL_CONTACT_ID + ") On CONFLICT REPLACE);");
    
    initUserProfileChildLabelTable(sqLiteDatabase);
        sqLiteDatabase.execSQL("CREATE TABLE "+TopicsTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
                + TopicsTableContract.TOPIC_ID +" int, "
                + TopicsTableContract.GROUP_ID + " int, "
                + TopicsTableContract.LAST_SEEN_MESSAGE_TIME + " long, "
                + "UNIQUE(" + TopicsTableContract.TOPIC_ID+ ") On CONFLICT REPLACE);");
	}

  private void createPintactTables(SQLiteDatabase sqLiteDatabase){
    sqLiteDatabase.execSQL("CREATE TABLE "+ContactTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + ContactTableContract.USER_ID +" int, "
        + ContactTableContract.CONTACT_DTO + " text, "
            + ContactTableContract.FIRST_NAME + " text, "
            + ContactTableContract.LAST_NAME + " text, "
            + ContactTableContract.TITLE + " text, "
            + ContactTableContract.LABELS + " text, "
            + ContactTableContract.PATH_IMAGE + " text, "
        + ContactTableContract.CREATED_AT + " date, "
        + ContactTableContract.UPDATED_AT + " date, "
        + "UNIQUE(" + ContactTableContract.USER_ID+ ") On CONFLICT REPLACE);");

    sqLiteDatabase.execSQL("CREATE TABLE "+ProfileTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
        + ProfileTableContract.PROFILE_ID +" int, "
        + ProfileTableContract.PROFILE_DTO + " text, "
        + ProfileTableContract.CREATED_AT + " date, "
        + ProfileTableContract.UPDATED_AT + " date, "
        + "UNIQUE(" + ProfileTableContract.PROFILE_ID+ ") On CONFLICT REPLACE);");
  }

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
	  Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

	  if (oldVersion < 8) {
	    sqLiteDatabase.execSQL("CREATE TABLE "+LabelTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
	        + LabelTableContract.LABEL_TEXT +" text, "
	        + "UNIQUE(" + LabelTableContract.LABEL_TEXT + ") On CONFLICT REPLACE);");
	  }
	  
	  if (oldVersion < 9) {
	    sqLiteDatabase.execSQL("CREATE TABLE " + UserProfileChildLabelContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
	        + UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE + " text, "
	        + UserProfileChildLabelContract.ATTRIBUTE_TYPE + " text, "
	        + UserProfileChildLabelContract.LABEL + " text, "
	        + "UNIQUE(" + UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE + ", "
	        + UserProfileChildLabelContract.ATTRIBUTE_TYPE + ", "
	        + UserProfileChildLabelContract.LABEL + ") On CONFLICT REPLACE);");
	    initUserProfileChildLabelTable(sqLiteDatabase);
	  }
        if(oldVersion <14)
        {
            sqLiteDatabase.execSQL("drop table "+ContactTableContract.TABLE_NAME);
            sqLiteDatabase.execSQL("CREATE TABLE "+ContactTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
                    + ContactTableContract.USER_ID +" int, "
                    + ContactTableContract.CONTACT_DTO + " text, "
                    + ContactTableContract.FIRST_NAME + " text, "
                    + ContactTableContract.LAST_NAME + " text, "
                    + ContactTableContract.TITLE + " text, "
                    + ContactTableContract.LABELS + " text, "
                    + ContactTableContract.PATH_IMAGE + " text, "
                    + ContactTableContract.CREATED_AT + " date, "
                    + ContactTableContract.UPDATED_AT + " date, "
                    + "UNIQUE(" + ContactTableContract.USER_ID+ ") On CONFLICT REPLACE);");

            sqLiteDatabase.execSQL("CREATE TABLE " + ContactTableContract.IS_PINTACT_TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
                    + ContactTableContract.LOCAL_CONTACT_ID + " text, "
                    + ContactTableContract.USER_ID + " int, "
                    + "UNIQUE(" + ContactTableContract.LOCAL_CONTACT_ID + ") On CONFLICT REPLACE);");
        }
        if(oldVersion <15)
        {
            sqLiteDatabase.execSQL("CREATE TABLE "+TopicsTableContract.TABLE_NAME + " (_id INTEGER PRIMARY KEY autoincrement, "
                    + TopicsTableContract.TOPIC_ID +" int, "
                    + TopicsTableContract.GROUP_ID + " int, "
                    + TopicsTableContract.LAST_SEEN_MESSAGE_TIME + " long, "
                    + "UNIQUE(" + TopicsTableContract.TOPIC_ID+ ") On CONFLICT REPLACE);");
        }
	}

  public void reinitDB(){
    SQLiteDatabase sqLiteDatabase =  this.getWritableDatabase();
    sqLiteDatabase.delete(ProfileTableContract.TABLE_NAME, null, null);
    sqLiteDatabase.delete(ContactTableContract.TABLE_NAME, null, null);
    sqLiteDatabase.delete(LabelTableContract.TABLE_NAME, null, null);
    sqLiteDatabase.delete(InvitedEmailAddressContract.TABLE_NAME, null, null);
    sqLiteDatabase.delete(ContactTableContract.CONTACT_ADDRESS_TABLE_NAME, null, null);
    sqLiteDatabase.delete(UserProfileChildLabelContract.TABLE_NAME, null, null);
      sqLiteDatabase.delete(TopicsTableContract.TABLE_NAME, null, null);
  }
  
  /**
   * Manually populate defaults in user profile child label table until cloud integration is complete
   */
  public void initUserProfileChildLabelTable(SQLiteDatabase sqLiteDatabase) {
    try {
      this.addLabel(sqLiteDatabase, UserProfileChildType.ADDRESS, null, "Home");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ADDRESS, null, "Office");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.EMAIL, "Personal");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.EMAIL, "Private");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.EMAIL, "Work");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PHONE_NUMBER, "Home");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PHONE_NUMBER, "Home Fax");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PHONE_NUMBER, "Mobile");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PHONE_NUMBER, "Work");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PHONE_NUMBER, "Work Fax");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "AIM");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "Facebook");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "LinkedIn");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "Pintact");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "Quora");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "Skype");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.SERVICE_ID, "Twitter");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PRIVATE_NOTE, "Office Notes");
      this.addLabel(sqLiteDatabase, UserProfileChildType.ATTRIBUTE, AttributeType.PRIVATE_NOTE, "Profile Notes");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void addLabel(SQLiteDatabase sqLiteDatabase, UserProfileChildType userProfileChildType,
      AttributeType attributeType, String label) throws Exception {
    ContentValues contentValues = new ContentValues();
    contentValues.put(UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE, userProfileChildType.name());
    contentValues.put(UserProfileChildLabelContract.ATTRIBUTE_TYPE, attributeType == null ? null : attributeType.name());
    contentValues.put(UserProfileChildLabelContract.LABEL, label);
    sqLiteDatabase.insert(UserProfileChildLabelContract.TABLE_NAME, null, contentValues);
  }

}
