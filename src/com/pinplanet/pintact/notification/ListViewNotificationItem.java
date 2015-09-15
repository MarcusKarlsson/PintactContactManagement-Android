package com.pinplanet.pintact.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.UiControllerUtil;

import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 02.10.2014.
 */
public class ListViewNotificationItem extends ViewListItem {

    public NotificationDTO getNotification() {
        return notification;
    }

    private NotificationDTO notification;
    private boolean showPin=false,showIsSeen=true;

  public ListViewNotificationItem(NotificationDTO notification){
    this.notification = notification;
  }

    public ListViewNotificationItem(boolean showPin,NotificationDTO notification){
        this(notification);
        this.showPin=showPin;
    }

    public void setShowIsSeen(boolean showIsSeen) {
        this.showIsSeen = showIsSeen;
    }

    class ViewHandle {
    public TextView entryName, subTitle, textViewInitial;
    public CustomNetworkImageView imageView;
  }

  @Override
  public View createView(Context context) {
    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = vi.inflate(getLayoutID(), null);

    final ViewHandle vh = new ViewHandle();
    vh.entryName = (TextView) v.findViewById(R.id.textViewName);
    vh.subTitle = (TextView) v.findViewById(R.id.textViewSubTitle);
    vh.imageView = (CustomNetworkImageView) v.findViewById(R.id.imageViewPicture);
    vh.textViewInitial = (TextView) v.findViewById(R.id.textViewInitial);

    v.setTag(vh);

    return v;
  }

  @Override
  public void fillView(View v) {
    ViewHandle vh = (ViewHandle) v.getTag();

    UserDTO user= notification.getData().sender;

    vh.entryName.setText(user.getName());

    String firstName = user.getFirstName();
    String lastName = user.getLastName();

    vh.textViewInitial.setText(UiControllerUtil.getInitial(firstName , lastName));

     if(showIsSeen && !notification.isSeen())
     {
         v.setBackgroundResource(R.color.PINTACT_LIGHT_BLUE_COLOR);
     }
     else
     {
         v.setBackgroundResource(R.color.PINTACT_WHITE_COLOR);
     }

    if(showPin)
        vh.subTitle.setText(user.getPin());
        else
        vh.subTitle.setText(notification.summaryText);

    vh.imageView.setImageUrl(user.getPathToImage(), AppController.getInstance().getImageLoader());
  }

  @Override
  public Class<?> getViewHandle() {
    return ViewHandle.class;
  }

  @Override
  public int getLayoutID() {
    return R.layout.list_view_notification_item;
  }

}
