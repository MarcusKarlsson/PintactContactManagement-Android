package com.pinplanet.pintact.chat;

import android.util.Log;

import com.pinplanet.pintact.AppController;

/**
 * Created by Avinash on 16/3/15.
 */
public class ChatListenerImpl implements ChatListener {

    @Override
    public void processMessage(Chat chat) {
        AppController.getInstance().getChatData().addToMap(chat);
        ChatActivity chatActivity = AppController.getInstance().getChatData().getCurrentChatActivity();
        if(chatActivity != null && chatActivity.getChatDto().getId().toString().equals(chat.getTo())){
            chatActivity.processMessage(chat);
        }
    }
}
