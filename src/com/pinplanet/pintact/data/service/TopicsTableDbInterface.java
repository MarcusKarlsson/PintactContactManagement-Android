package com.pinplanet.pintact.data.service;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pinplanet.pintact.data.ChatTopicDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopicsTableDbInterface {

    private final PintactDbHelper dbHelper;
    private static final String TAG = ContactTableDbInterface.class.getSimpleName();

    private static TopicsTableDbInterface topicsTableDbInterface;

    public TopicsTableDbInterface(PintactDbHelper pintactDbHelper) {
        this.dbHelper = pintactDbHelper;
    }

    public static TopicsTableDbInterface getInstance() {
        if (topicsTableDbInterface == null) {
            topicsTableDbInterface = new TopicsTableDbInterface(PintactDbHelper.getInstance());
        }

        return topicsTableDbInterface;
    }

    public void updateTime(Long topicId, Long groupId, Date lastMessageTime){
        if(lastMessageTime != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put(TopicsTableContract.GROUP_ID, groupId);
            contentValues.put(TopicsTableContract.LAST_SEEN_MESSAGE_TIME, lastMessageTime.getTime());
            dbHelper.getWritableDatabase().update(TopicsTableContract.TABLE_NAME, contentValues,
                    TopicsTableContract.TOPIC_ID + " = '"+ topicId+"'", null);
        }
    }

    public void addTopic(Long topicId, Long groupId, Date lastMessageTime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TopicsTableContract.GROUP_ID, groupId);
        contentValues.put(TopicsTableContract.TOPIC_ID, topicId);
        contentValues.put(TopicsTableContract.LAST_SEEN_MESSAGE_TIME, (lastMessageTime != null )? lastMessageTime.getTime():null);
        dbHelper.getWritableDatabase().insert(TopicsTableContract.TABLE_NAME, null, contentValues);
    }

    public List<ChatTopicDTO> getAllTopics(){
        List<ChatTopicDTO> chats = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TopicsTableContract.TABLE_NAME;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ChatTopicDTO chatTopicDTO = new ChatTopicDTO();
                chatTopicDTO.setId(cursor.getLong(cursor.getColumnIndex(TopicsTableContract.TOPIC_ID)));
                chatTopicDTO.setGroupId(cursor.getLong(cursor.getColumnIndex(TopicsTableContract.GROUP_ID)));
                Date time = new Date();
                time.setTime(cursor.getLong(cursor.getColumnIndex(TopicsTableContract.LAST_SEEN_MESSAGE_TIME)));
                chatTopicDTO.setLastMessageTime(time);
                chats.add(chatTopicDTO);
            } while (cursor.moveToNext());
        }
        return chats;
    }

}
