package com.pinplanet.pintact.notification;

import java.util.HashSet;
import java.util.Locale;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.NotifyMaps;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.SingletonLoginData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotifyInviteLVAdapter extends BaseAdapter
{
    
    private Context mContext;
    boolean  mIsUpdate;
    PageDTO<NotificationDTO> mData;
    HashSet<Integer> mSet;
    int mOffset = 0;
    
    public NotifyInviteLVAdapter(Context context, boolean isUpdate) 
    {
	    super();
	    mContext=context;
	    mIsUpdate = isUpdate;
	    mData = SingletonLoginData.getInstance().getNotifications();
	    mSet = new HashSet<Integer>();
    }

  public void refreshNotifications()
  {
    mData = SingletonLoginData.getInstance().getNotifications();
    this.notifyDataSetChanged();
  }
       
    public int getCount() 
    {
    	return mData.getTotalCount();
    }

    // getView method is called for each item of ListView
    public View getView(int position,  View view, ViewGroup parent) 
    {
    	// inflate the layout for each item of listView
	    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      NotifyMaps notiData = SingletonLoginData.getInstance().getNotifications().getData().get(position).getData();
      NotificationDTO notificationDTO = SingletonLoginData.getInstance().getNotifications().getData().get(position);

	    boolean isTopic = mData.getData().get(position).getNotificationId() == -1L ? true : false;
	    if ( ! isTopic) {
	    	view = inflater.inflate(R.layout.notification_list_invite, null);
	    } else {
	    	view = inflater.inflate(R.layout.notification_list_topic, null);
		    TextView topicTV=(TextView)view.findViewById(R.id.notify_topic);
		    topicTV.setText(mData.getData().get(position).topic);
        if(mData.getData().get(position).topic.equals("UPDATES")) {
          TextView markAllAsRead = (TextView) view.findViewById(R.id.mark_all_as_read);
          markAllAsRead.setVisibility(View.VISIBLE);
          markAllAsRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //AppService.markAllNotificationMarked(mContext, SingletonLoginData.getInstance().getLastNotificationId());
            }
          });
        }
	    	return view;
	    }
	    
	    String fName = SingletonLoginData.getInstance().getNotifications().getData().get(position).getData().sender.firstName;
	    String lName = SingletonLoginData.getInstance().getNotifications().getData().get(position).getData().sender.lastName;
	    
	    // get the reference of textViews
	    TextView initTV=(TextView)view.findViewById(R.id.nli_initial);
	    TextView nameTV=(TextView)view.findViewById(R.id.nli_name);
      TextView summaryTextView=(TextView)view.findViewById(R.id.nli_summaryText);
	    
	    // Set the Sender number and smsBody to respective TextViews
		String senderName =  fName + " " + lName;
	    char ab[] = "ab".toCharArray();
	    ab[0] = senderName.charAt(0);
	    if ( lName == null || lName.length() == 0 ) {
	    	ab[1] = ab[0];
	    } else {
	    	ab[1] = lName.charAt(0);
	    }
	    String init = new String(ab);
	    init = init.toUpperCase(Locale.US);
	    
	    nameTV.setText(senderName);
      summaryTextView.setText(formatString(notificationDTO.summaryText));
	    initTV.setText(init);

        CustomNetworkImageView imageView = (CustomNetworkImageView) view.findViewById(R.id.imageViewPicture);
      imageView.setImageUrl(notiData.sender.getPathToImage(), AppController.getInstance().getImageLoader());
	    return view;
    }

  private String formatString(String st)
  {
    if(st != null && st.length() > 30)
    {
      return st.substring(0, 27)+"...";
    }
    return st;
  }

    public Object getItem(int position) {
        return SingletonLoginData.getInstance().getNotifications().getData().get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}
