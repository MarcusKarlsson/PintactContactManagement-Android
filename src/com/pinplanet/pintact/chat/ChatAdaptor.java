package com.pinplanet.pintact.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.SingletonLoginData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avinash on 4/10/14.
 */
public class ChatAdaptor extends BaseAdapter {
    private static final String TAG = "Debugging";
    private Context context;
    private List<Chat> ChatItems;
    Boolean zoomOut = false;

    public ChatAdaptor(Context context, List<Chat> ChatItems) {
        this.context = context;
        this.ChatItems = ChatItems;
    }

    @Override
    public int getCount() {
        return ChatItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < getCount()) {
            return ChatItems.get(position);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getCount()) {
            UserDTO user = getUserContact(ChatItems.get(position).getFrom());
            if (user != null) {
                if (user.getId().equals(SingletonLoginData.getInstance().getUserData().getId())) {
                    return 0;
                }
            }
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    public void clearEntries() {
        // Clear all the data points
        ChatItems.clear();
        notifyDataSetChanged();
    }

    public void addEntriesToBottom(List<Chat> entries) {
        if (ChatItems == null) {
            ChatItems = new ArrayList<Chat>();
        }

        if (entries != null) {
            // Add entries to the bottom of the list
            ChatItems.addAll(entries);
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
        String from = parseFrom(ChatItems.get(position).getFrom());
        ViewHolder viewHolder = null;
        //If new view, create view holder for it, if not, get the viewHolder
        //This prevents bugs in the listview when it is scrolled through
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (getItemViewType(position) == 0) {
                convertView = mInflater.inflate(R.layout.chat_list_item_self, null);
            } else {
                convertView = mInflater.inflate(R.layout.chat_list_item_others, null);
            }
            viewHolder = new ViewHolder();
            viewHolder.user = getUserContact(from);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.from);
            viewHolder.sentImageView = (ImageView) convertView.findViewById(R.id.sentImageView);
            viewHolder.initalTextView = (TextView) convertView.findViewById(R.id.chatInitialTV);
            final ViewHolder finalViewHolder = viewHolder;

            viewHolder.chatText = (TextView) convertView.findViewById(R.id.text);
            viewHolder.timeText = (TextView) convertView.findViewById(R.id.chatTimeStamp);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder.user != null) {
            if (viewHolder != null)
                viewHolder.txtTitle.setText(viewHolder.user.getName());
        } else {
            if (viewHolder != null)
                viewHolder.txtTitle.setText(from);
        }
        final ChatBodyWrapper chatBodyWrapper = new ChatBodyWrapper(ChatItems.get(position).getMessage());
        viewHolder.sentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(context, ChatImageFullScreenActivity.class);
                fullScreenIntent.putExtra("FileData", chatBodyWrapper.getFileData());
                fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fullScreenIntent);
            }
        });
        try {
            JSONObject json = new JSONObject(ChatItems.get(position).getMessage());
            if (chatBodyWrapper.getType() == 1) {
                if (viewHolder != null) {
                    viewHolder.chatText.setText(chatBodyWrapper.getContent());
                    viewHolder.chatText.setVisibility(View.VISIBLE);
                    viewHolder.sentImageView.setVisibility(View.GONE);
                }
            } else if (chatBodyWrapper.getType() == 2) {
                byte[] decodedString = Base64.decode(chatBodyWrapper.getFileData(), Base64.DEFAULT);
                //Log.d(TAG, "FileData: " + chatBodyWrapper.getFileData());
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    int height = decodedByte.getHeight();
                    int width = decodedByte.getWidth();
                    double ratio;
                    //Scale images
                    int maxHeight = 400;
                    int maxWidth = 400;
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
                    if (viewHolder != null) {
                        viewHolder.sentImageView.setImageBitmap(Bitmap.createScaledBitmap(decodedByte, width, height, false));
                        viewHolder.sentImageView.setVisibility(View.VISIBLE);
                        viewHolder.chatText.setVisibility(View.GONE);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
            if (viewHolder != null)
                viewHolder.chatText.setText(ChatItems.get(position).getMessage());
        }
        if (viewHolder != null) {
            viewHolder.timeText.setText(new SimpleDateFormat("h:mm aa").format(ChatItems.get(position).getTime()));
            viewHolder.imageProfileView = (CustomNetworkImageView) convertView.findViewById(R.id.chatImageViewProfilePicture);
        }

        Uri.Builder uriBuilder = new Uri.Builder();
        if (viewHolder.user != null) {
            //If user has set a profile picture
            if (viewHolder.user.getPathToImage() != null) {
                if (viewHolder != null) {
                    viewHolder.imageProfileView.setImageUrl(viewHolder.user.getPathToImage(), AppController.getInstance().getImageLoader());
                    viewHolder.imageProfileView.setVisibility(View.VISIBLE);
                    viewHolder.initalTextView.setVisibility(View.GONE);
                }

            }
            //If not, set it to default avatar
            else {
                String[] nameArray = viewHolder.user.getName().split(" ");
                String initials = new StringBuilder().append(nameArray[0].charAt(0)).
                        append(nameArray[1].charAt(0)).toString();
                viewHolder.initalTextView.setText(initials);
                viewHolder.initalTextView.setVisibility(View.VISIBLE);
                viewHolder.imageProfileView.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        public UserDTO user;
        public TextView txtTitle;
        public ImageView sentImageView;
        public TextView chatText;
        public TextView timeText;
        public CustomNetworkImageView imageProfileView;
        public TextView initalTextView;
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

    public void addChatMessage(Chat chat) {
        ChatItems.add(chat);
        notifyDataSetChanged();
    }
}

