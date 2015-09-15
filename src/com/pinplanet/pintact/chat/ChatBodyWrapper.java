package com.pinplanet.pintact.chat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wildcat on 4/19/2015.
 */
public class ChatBodyWrapper {
    private static final String TAG = "Debugging";
    private static final String SMACK_ROOM = "2763713869812598289@ec2-107-20-207-41.compute-1.amazonaws.com/Smack";

    private String fromUserId;  // Sender JID
    private String toUserId;    // Receiver JID
    private String content;     // message content
    private int type;        //  message type  1 : text, 2 : image 3 : voice  4: location 5 : gif etc
    private String messageId;   // local DB message Table Id
    private String timeSend;    //  Time that sender send message
    private String fileData;    // when send file , if need
    private int fileSize;    // when send file , it need
    private int location_x;  // sender location information
    private int location_y;  // sender's location information
    private int timeLen;     // voice timelen

    //Constructor for text based message and picture message
    public ChatBodyWrapper(String fromUserId, String toUserId, String contentData, int type, String timeSend, int fileSize) {
        this.fromUserId = fromUserId;
        //this.toUserId = SMACK_ROOM;
        this.toUserId = toUserId;
        this.timeSend = timeSend;
        this.type = type;
        if (type == 1) {
            this.content = contentData;
            this.fileSize = 0;
        } else if (type == 2) {
            this.fileData = contentData;
            this.fileSize = fileSize;
        }
        this.location_x = 0;
        this.location_y = 0;
        this.timeLen = 0;
    }

    //Constructor for parsing JSON string
    public ChatBodyWrapper(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            this.type = jsonObject.getInt("type");
            if (type == 1) {
                this.content = jsonObject.getString("content");
            } else if (type == 2) {
                this.fileData = jsonObject.getString("fileData");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    //Convert to JSON Object
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("location_y", this.location_y);
            jsonObject.put("fileSize", this.fileSize);
            if (this.type == 1) {
                jsonObject.put("content", this.content);
            } else if (this.type == 2) {
                jsonObject.put("fileData", this.fileData);
            }
            jsonObject.put("location_x", this.location_x);
            jsonObject.put("timeLen", this.timeLen);
            jsonObject.put("toUserId", this.toUserId);
            jsonObject.put("fromUserId", this.fromUserId);
            jsonObject.put("type", this.type);
            jsonObject.put("timeSend", this.timeSend);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }

        return jsonObject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }
}
