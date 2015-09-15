package com.pinplanet.pintact.data.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class LabelTableDbInterface {

  private final PintactDbHelper dbHelper;
  private static final String TAG = LabelTableDbInterface.class.getSimpleName();

  private static LabelTableDbInterface labelTableDbInterface;

  public LabelTableDbInterface(PintactDbHelper pintactDbHelper) {
    this.dbHelper = pintactDbHelper;
  }

  public static LabelTableDbInterface getInstance(){
    if(labelTableDbInterface == null)
    {
      labelTableDbInterface = new LabelTableDbInterface(PintactDbHelper.getInstance());
    }

    return labelTableDbInterface;
  }

  public void addLabel(List<String> labels) throws Exception {
    for (String label : labels) {
      addLabel(label);
    }
  }

  public void addLabel(String label) throws Exception {
    ContentValues contentValues = new ContentValues();
    contentValues.put(LabelTableContract.LABEL_TEXT, label);
    dbHelper.getWritableDatabase().insert(LabelTableContract.TABLE_NAME, null, contentValues);
  }
  
  public void removeLabel(String label) throws Exception {
    dbHelper.getWritableDatabase().delete(LabelTableContract.TABLE_NAME,
        LabelTableContract.LABEL_TEXT + " = ?", new String[]{label});
  }

  public List<String> getLabels() throws Exception {
    List<String> feeds = new ArrayList<String>();
    String selectQuery = "SELECT  * FROM " + LabelTableContract.TABLE_NAME;

    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        feeds.add(cursorToFeed(cursor));
      } while (cursor.moveToNext());
    }
    return feeds;
  }

  public String cursorToFeed(Cursor cursor) throws Exception {
    return cursor.getString(cursor.getColumnIndex(LabelTableContract.LABEL_TEXT));
  }
}
