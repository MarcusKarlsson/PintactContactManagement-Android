package com.pinplanet.pintact.data.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.UserProfileChildType;

public class UserProfileChildLabelTableDbInterface {

  private final PintactDbHelper dbHelper;
  private static final String TAG = UserProfileChildLabelTableDbInterface.class.getSimpleName();

  private static UserProfileChildLabelTableDbInterface userProfileChildLabelTableDbInterface;

  public UserProfileChildLabelTableDbInterface(PintactDbHelper pintactDbHelper) {
    this.dbHelper = pintactDbHelper;
  }

  public static UserProfileChildLabelTableDbInterface getInstance(){
    if(userProfileChildLabelTableDbInterface == null)
    {
      userProfileChildLabelTableDbInterface = new UserProfileChildLabelTableDbInterface(PintactDbHelper.getInstance());
    }

    return userProfileChildLabelTableDbInterface;
  }

  public void addUserProfileChildLabel(UserProfileChildType userProfileChildType, AttributeType attributeType,
      List<String> labels) throws Exception {
    for (String label : labels) {
      addLabel(userProfileChildType, attributeType, label);
    }
  }

  public void addLabel(UserProfileChildType userProfileChildType, AttributeType attributeType,
      String label) throws Exception {
    ContentValues contentValues = new ContentValues();
    contentValues.put(UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE, userProfileChildType.name());
    contentValues.put(UserProfileChildLabelContract.ATTRIBUTE_TYPE, attributeType == null ? null : attributeType.name());
    contentValues.put(UserProfileChildLabelContract.LABEL, label);
    dbHelper.getWritableDatabase().insert(UserProfileChildLabelContract.TABLE_NAME, null, contentValues);
  }
  
  public void removeLabel(UserProfileChildType userProfileChildType, AttributeType attributeType,
      String label) throws Exception {
    dbHelper.getWritableDatabase().delete(UserProfileChildLabelContract.TABLE_NAME,
        UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE + " = ? and "
        + UserProfileChildLabelContract.ATTRIBUTE_TYPE + " = ? and "
        + UserProfileChildLabelContract.LABEL + " = ?",
        new String[]{userProfileChildType.name(), attributeType == null ? null : attributeType.name(), label});
  }

  public Map<UserProfileChildType, Map<AttributeType, List<String>>> getLabels() throws Exception {
    Map<UserProfileChildType, Map<AttributeType, List<String>>> feeds =
        new HashMap<UserProfileChildType, Map<AttributeType, List<String>>>();
    String selectQuery = "SELECT  * FROM " + UserProfileChildLabelContract.TABLE_NAME;

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        String[] feed = cursorToFeed(cursor);
        UserProfileChildType userProfileChildType = feed[0] == null ? null : UserProfileChildType.valueOf(feed[0]);
        AttributeType attributeType = feed[1] == null ? null : AttributeType.valueOf(feed[1]);
        if (!feeds.containsKey(userProfileChildType)) {
          feeds.put(userProfileChildType, new HashMap<AttributeType, List<String>>());
        }
        if (!feeds.get(userProfileChildType).containsKey(attributeType)) {
          feeds.get(userProfileChildType).put(attributeType, new ArrayList<String>());
        }
        feeds.get(userProfileChildType).get(attributeType).add(feed[2]);
      } while (cursor.moveToNext());
    }
    return feeds;
  }

  public String[] cursorToFeed(Cursor cursor) throws Exception {
    return new String[]{cursor.getString(cursor.getColumnIndex(UserProfileChildLabelContract.USER_PROFILE_CHILD_TYPE)),
        cursor.getString(cursor.getColumnIndex(UserProfileChildLabelContract.ATTRIBUTE_TYPE)),
        cursor.getString(cursor.getColumnIndex(UserProfileChildLabelContract.LABEL))};
  }
}
