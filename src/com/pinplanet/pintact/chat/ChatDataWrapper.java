package com.pinplanet.pintact.chat;

import android.os.AsyncTask;
import android.util.Log;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.service.TopicsTableDbInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Avinash on 16/3/15.
 */
public class ChatDataWrapper {
    private Map<String, List<Chat>> chatsMap;
    private ChatActivity currentChatActivity;
    private Map<String, Date> chatLastMessageSeenTime;
    private List<ChatTopicDTO> chatTopics;
    private boolean isChatOn = false;
    public ChatDataWrapper(){
        if(isChatOn) {
            chatsMap = new HashMap<>();
            chatLastMessageSeenTime = new HashMap<>();

            chatTopics = TopicsTableDbInterface.getInstance().getAllTopics();
            for (ChatTopicDTO chatTopic : chatTopics) {
                chatLastMessageSeenTime.put(chatTopic.getId().toString(), chatTopic.getLastMessageTime());
                Log.d("PINTACT", "Chat Topic " + chatTopic.getId() + " " + chatTopic.getName() + " time " + chatTopic.getLastMessageTime());
            }
            new ConnectToEjabber().execute("");
        }
    }

    public Map<String, List<Chat>> getChatsMap() {
        return chatsMap;
    }

    public void setChatsMap(Map<String, List<Chat>> chatsMap) {
        this.chatsMap = chatsMap;
    }

    public ChatActivity getCurrentChatActivity() {
        return currentChatActivity;
    }

    public void setCurrentChatActivity(ChatActivity currentChatActivity) {
        this.currentChatActivity = currentChatActivity;
    }

    public void addToMap(Chat chat){
        List<Chat> chatsList = chatsMap.get(chat.getTo());
        if(chatsList == null){
            synchronized (chatsMap){
                chatsList = chatsMap.get(chat.getTo());
                if(chatsList == null){
                    chatsList =  new LinkedList<>();
                    chatsMap.put(chat.getTo(), chatsList);
                }
            }
        }
        insertInSortedPos(chatsList, chat);
    }


    public boolean isLastSeenTimeUpdated(String topicId){
        return chatLastMessageSeenTime.containsKey(topicId);
    }

    public List<Chat> getTopicsChats(String topicId){
        List<Chat> chatList = chatsMap.get(topicId);
        if(chatList == null){
            chatList = new ArrayList<>();
            chatsMap.put(topicId, chatList);
        }
        return chatList;
    }

    private void insertInSortedPos(List<Chat> list, Chat dto){
        int index=0;
        synchronized(list){
            for(Chat chat: list){
                if(chat.getId() != null  && chat.getId().equals(dto.getId())){
                    return;//message already added in the list
                }
            }
            for(Chat chat: list){
                if(chat.getTime().compareTo(dto.getTime()) == 1){
                    list.add(index, dto);
                    return;
                }
                index++;
            }
            list.add(dto);
        }
    }

    public int getUnseenMessageCount(String topicId){
        List<Chat> chatList = getTopicsChats(topicId);
        Date lastMessageSeenTime = chatLastMessageSeenTime.get(topicId);

        int count = 0;

        if(lastMessageSeenTime != null){
            for(Chat chat: chatList){
                if(chat.getTime().compareTo(lastMessageSeenTime) == 1){
                    break;
                }
                count++;
            }
        }
        return chatList.size() - count;
    }

    public void updateLastMessageSeenTime(String topicId, Date time){
        Date currentTime = chatLastMessageSeenTime.get(topicId);
        if(currentTime == null){
            currentTime = time;
        }
        Date maxTime = null;
        if(currentTime != null){
            maxTime = (currentTime.compareTo(time) ==1 )?currentTime: time;
        }
        chatLastMessageSeenTime.put(topicId, maxTime);
    }

    public Date getLastMessageSeentime(String topicId){
        return  chatLastMessageSeenTime.get(topicId);
    }

    private class ConnectToEjabber extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... urls) {
            for(ChatTopicDTO chatTopic: chatTopics){
                Log.d("PINTACT", "connecting to "+ chatTopic.getId().toString());
                AppController.getInstance().getChatManager().join(chatTopic.getId().toString());
            }
            return null;
        }
    }

    public Chat getLastChat(String topicId){
        List<Chat> chatsList = chatsMap.get(topicId);
        if(chatsList != null && chatsList.size() != 0){
            return chatsList.get(chatsList.size()-1);
        }
        return null;
    }
}
