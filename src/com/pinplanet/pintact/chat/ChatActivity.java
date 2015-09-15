package com.pinplanet.pintact.chat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.service.TopicsTableDbInterface;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Avinash on 15/3/15.
 */
public class ChatActivity extends MyActivity implements ChatListener {
    private static final String TAG = "ChatActivity Debugging";
    private static final int SELECT_PICTURE = 1;

    private ListView feedsList;
    private View sendMessage;
    private ChatAdaptor chatAdaptor = null;
    private List<Chat> chats;
    private EditText chatText;
    private ChatTopicDTO chatDto;
    private Date chatPingTime;
    private static int maxRetryCount = 3;
    private int retryAttempt = 0;
    private String retryText = "";
    private int retryType = 1;
    private ImageView testImageView;

    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatPingTime = new Date();

        setContentView(R.layout.chat_list);

        chatDto = (ChatTopicDTO) getIntent().getSerializableExtra("topicDto");
        setChatDto(chatDto);

        chats = AppController.getInstance().getChatData().getTopicsChats(chatDto.getId().toString());

        feedsList = (ListView) findViewById(R.id.ChatsList);

        chatText = (EditText) findViewById(R.id.chat);

        setAdaptor(chats);


        hideOneRight();
        showLeftImage(R.drawable.actionbar_left_arrow);

        View.OnClickListener backLn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        addLeftClickListener(backLn);
        showTitle(chatDto.getName());

        AppController.getInstance().getChatData().setCurrentChatActivity(this);
        initGroupChat();

    }

    private void setAdaptor(List<Chat> chats) {
        for (Chat chat : chats) {
            AppController.getInstance().getChatData().updateLastMessageSeenTime(chatDto.getId().toString(), chat.getTime());
            Log.d(TAG, "ChatData: " + AppController.getInstance().getChatData().toString());
        }
        chatAdaptor = new ChatAdaptor(getApplicationContext(), chats);
        Log.d(TAG, "ChatSize: " + chatAdaptor.getCount());
        feedsList.setAdapter(chatAdaptor);
    }

    public void addMessage(Chat chat) {
        if (chatAdaptor != null) {
            chatAdaptor.notifyDataSetChanged();
            feedsList.invalidate();
        }
        AppController.getInstance().getChatData().updateLastMessageSeenTime(chatDto.getId().toString(), chat.getTime());
        chatDto.setLastMessageTime(AppController.getInstance().getChatData().getLastMessageSeentime(chatDto.getId().toString()));
    }

    public String getChatMessage() {
        return chatText.getText().toString();
    }

    public String getChatText() {
        return chatText.getText().toString();
    }

    public void resetChatText() {
        chatText.setText("");
    }

    public void sendMessage(View view) {
        sendMessage(getChatText(), 1);
        resetChatText();
    }

    public void sendMessage(String message, int messageType) {
        if (!message.equals("")) {
            try {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                ChatBodyWrapper chatBodyWrapper = new ChatBodyWrapper(SingletonLoginData.getInstance().getUserData().getId().toString(), chatDto.getId().toString(), message, messageType, dateFormat.format(date).toString(), 0);
                AppController.getInstance().getChatManager().sendMessage(chatBodyWrapper.toJson().toString());
                retryAttempt = 0;
                retryText = "";
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                retryAttempt++;
                if (retryAttempt <= maxRetryCount) {
                    Log.d("PINTACT", "Retrying connection, attempt number " + retryAttempt);
                    retryText = message;
                    retryType = messageType;
                    sendMessgaeInBackground();
                } else {
                    Log.d("PINTACT", "Retry attempts over..");
                }
            }
        }
    }

    public void sendMessgaeInBackground() {
        new ConnectAndSendMessage().execute(chatDto.getId().toString());
    }

    public void attachImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Runtime.getRuntime().maxMemory();
                Uri selectedImageUri = data.getData();

                String picturePath = getPath(selectedImageUri);
                Bitmap bm = null;
                if (picturePath == null) {
                    try {
                        bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.toString());
                    }
                } else {
                    //cursor.close();
                    bm = BitmapFactory.decodeFile(picturePath);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (bm != null) {
                    int height = bm.getHeight();
                    int width = bm.getWidth();
                    double ratio;

                    //Scale images
                    int maxHeight = 1200;
                    int maxWidth = 1000;
                    if (height > maxHeight) {
                        ratio = (double) maxHeight / (double) height;
                        height = maxHeight;
                        width = (int) (width * ratio);
                    }
                    if (width > maxWidth) {
                        ratio = (double) maxWidth / (double) width;
                        width = maxWidth;
                        height = (int) (height * ratio);
                    }
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, width, height, false);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);

                    bm.recycle();
                }
                bm = null;
                byte[] b = baos.toByteArray();
                String base64Encode = Base64.encodeToString(b, Base64.DEFAULT);

                String room = AppController.getInstance().getChatManager().getMchat().getRoom();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();

                sendMessage(base64Encode, 2);

            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }


    public void initGroupChat() {
        //Executing in background thread as it will be a network call
        new ConnectToEjabber().execute(chatDto.getId().toString());
    }

    private class ConnectToEjabber extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... urls) {
            String topic = urls[0];
            AppController.getInstance().getChatManager().join(topic);
            return null;
        }
    }

    private class ConnectAndSendMessage extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... urls) {
            String topic = urls[0];
            AppController.getInstance().getChatManager().join(topic);
            sendMessage(retryText, retryType);
            return null;
        }

    }

    @Override
    public void processMessage(final Chat chat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chat.getTime().compareTo(chatPingTime) > 0) {
                    //play sound
                    //MySoundPool.playSound(MySoundPool.MESSAGE_SOUND_ID);
                }
                chat.setFrom(ChatAdaptor.parseFrom(chat.getFrom()));
                addMessage(chat);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        TopicsTableDbInterface.getInstance().updateTime(chatDto.getId(), chatDto.getGroupId(), AppController.getInstance().getChatData().getLastMessageSeentime(chatDto.getId().toString()));
        AppController.getInstance().getChatData().setCurrentChatActivity(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppController.getInstance().getChatData().setCurrentChatActivity(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppController.getInstance().getChatData().setCurrentChatActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppController.getInstance().getChatData().setCurrentChatActivity(this);
    }

    public ChatTopicDTO getChatDto() {
        return chatDto;
    }

    public void setChatDto(ChatTopicDTO chatDto) {
        this.chatDto = chatDto;
    }
}
