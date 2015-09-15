package com.pinplanet.pintact.data.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.pinplanet.pintact.data.ProfileDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileTableDbInterface {

  private final PintactDbHelper dbHelper;

  private static ProfileTableDbInterface profileTableDbInterface;

  public ProfileTableDbInterface(PintactDbHelper pintactDbHelper) {
    this.dbHelper = pintactDbHelper;
  }

  public static ProfileTableDbInterface getInstance(){
    if(profileTableDbInterface == null)
    {
      profileTableDbInterface = new ProfileTableDbInterface(PintactDbHelper.getInstance());
    }

    return profileTableDbInterface;
  }

  public void addProfile(List<ProfileDTO> profileDTOs) throws Exception {
    for (ProfileDTO profileDTO : profileDTOs) {
      addProfile(profileDTO);
    }
  }
  
  public void removeProfile(ProfileDTO profileDTO) throws Exception {
    dbHelper.getWritableDatabase().delete(ProfileTableContract.TABLE_NAME,
        ProfileTableContract.PROFILE_ID + " = ?", new String[]{profileDTO.getUserProfile().getId().toString()});
  }

  public void removeProfiles() throws Exception {
    dbHelper.getWritableDatabase().delete(ProfileTableContract.TABLE_NAME, null, null);
  }

  public void addProfile(ProfileDTO profileDTO) throws Exception {
    if(profileDTO != null) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(ProfileTableContract.PROFILE_DTO, new Gson().toJson(profileDTO));
      contentValues.put(ProfileTableContract.CREATED_AT, new Date().getTime());
      contentValues.put(ProfileTableContract.UPDATED_AT, new Date().getTime());
      contentValues.put(ProfileTableContract.PROFILE_ID, profileDTO.getUserProfile().getId());
      if (getProfile(profileDTO.getUserProfile().getId()) == null) {
        dbHelper.getWritableDatabase().insert(ProfileTableContract.TABLE_NAME, null, contentValues);
      } else {
        dbHelper.getWritableDatabase().update(ProfileTableContract.TABLE_NAME, contentValues,
            ProfileTableContract.PROFILE_ID + " = ?", new String[]{profileDTO.getUserProfile().getId().toString()});
      }
    }
  }

  public List<ProfileDTO> getProfiles() throws Exception {
    List<ProfileDTO> feeds = new ArrayList<ProfileDTO>();
    String selectQuery = "SELECT  * FROM " + ProfileTableContract.TABLE_NAME;

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        feeds.add(cursorToFeed(cursor));
      } while (cursor.moveToNext());
    }
    return feeds;
  }

  public ProfileDTO getProfile(Long profileId) throws Exception {
    String selectQuery = "SELECT  * FROM " + ProfileTableContract.TABLE_NAME + " where "+ ProfileTableContract.PROFILE_ID+" = " + profileId;

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      return cursorToFeed(cursor);
    }
    return null;
  }

  public ProfileDTO cursorToFeed(Cursor cursor) throws Exception {
    String contactJson = cursor.getString(cursor.getColumnIndex(ProfileTableContract.PROFILE_DTO));
    ProfileDTO profileDTO = new Gson().fromJson(contactJson, ProfileDTO.class);
    return profileDTO;
  }
}
