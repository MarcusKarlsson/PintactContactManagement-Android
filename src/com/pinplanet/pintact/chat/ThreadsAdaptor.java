package com.pinplanet.pintact.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Avinash on 14/3/15.
 */
public class ThreadsAdaptor extends BaseAdapter {
    private final static String TAG = "Debugging";

    private Context context;
    private List<ChatTopicDTO> topicDTOs;
    private View.OnClickListener listener;

    public ThreadsAdaptor(Context context, List<ChatTopicDTO> topicDTOs, View.OnClickListener listener) {
        this.context = context;
        this.topicDTOs = topicDTOs;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return topicDTOs.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < getCount()) {
            return topicDTOs.get(position);
        }
        return null;
    }

    public void clearEntries() {
        // Clear all the data points
        topicDTOs.clear();
        notifyDataSetChanged();
    }

    public void addEntriesToBottom(List<ChatTopicDTO> entries) {
        if (topicDTOs == null) {
            topicDTOs = new ArrayList<ChatTopicDTO>();
        }

        if (entries != null) {
            // Add entries to the bottom of the list
            topicDTOs.addAll(entries);
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static String parseFrom(String from) {
        int index = from.lastIndexOf("/");
        if (index != -1) {
            return from.substring(index + 1);
        }
        index = from.indexOf("@");
        if (index != -1) {
            return from.substring(0, index);
        }
        return from;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.topic_list_item, null);
        }
        ChatTopicDTO topicDTO = topicDTOs.get(position);

        TextView topicTitle = (TextView) convertView.findViewById(R.id.topic);
        topicTitle.setText(topicDTO.getName());

        TextView lastMessageTV = (TextView) convertView.findViewById(R.id.lastMessageTV);
        TextView lastMessageTimeTV = (TextView) convertView.findViewById(R.id.lastMessageTimeTV);
        setLastText(lastMessageTV, lastMessageTimeTV, topicDTO.getId().toString());


        TextView newMessageCount = (TextView) convertView.findViewById(R.id.newMessageCount);
        int newMessages = AppController.getInstance().getChatData().getUnseenMessageCount(topicDTO.getId().toString());
        if (newMessages > 0) {
            newMessageCount.setText(Integer.toString(newMessages));
            newMessageCount.setVisibility(View.VISIBLE);
        } else {
            newMessageCount.setVisibility(View.INVISIBLE);
        }

        convertView.setTag(topicDTO);
        convertView.setOnClickListener(listener);
        return convertView;
    }

    private UserDTO getUserContact(String id) {
        List<ContactDTO> groupContacts = SingletonLoginData.getInstance().getGroupContacts();
        for (ContactDTO contactDTO : groupContacts) {
            if (contactDTO.getContactUser().getId().toString().equals(id)) {
                return contactDTO.getContactUser();
            }
        }
        return null;
    }

    public void addChatMessage(ChatTopicDTO chat) {
        topicDTOs.add(chat);
        notifyDataSetChanged();
    }

    private void setLastText(TextView lastMessageTV, TextView lastMessageTimeTV, String topicId) {
        //Chat lastChat = AppController.getInstance().getChatData().isLastSeenTimeUpdated(topicId);
        Chat lastChat = new Chat();
        if (AppController.getInstance().getChatData().getLastChat(topicId) != null)
            lastChat = AppController.getInstance().getChatData().getLastChat(topicId);

        //lastChat.getTime()// last message time
        if (lastChat.getMessage() != null) {
            ChatBodyWrapper chatBodyWrapper = new ChatBodyWrapper(lastChat.getMessage());
            UserDTO user = getUserContact(lastChat.getFrom());
            //(chatBodyWrapper.getType() == 1 )? "Text":"Image" //we dont support video as of now
            if (chatBodyWrapper.getType() == 1) {
                //lastMessageTV.setText(chatBodyWrapper.getContent());
                if (user != null)
                    lastMessageTV.setText(user.getName() + ": " + chatBodyWrapper.getContent());
                ; // will return the message text
            }
        }

        if (lastChat.getTime() != null) {
            Date date = lastChat.getTime();
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTime(date);

            TimeZone tz = TimeZone.getDefault();
            DateFormat formatter = new SimpleDateFormat("hh:mm M/dd/yyyy");
            formatter.setTimeZone(tz);
            lastMessageTimeTV.setText(formatter.format(lastDate.getTime()));
        }
    }


}
