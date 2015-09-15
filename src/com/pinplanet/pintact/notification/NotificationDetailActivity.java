package com.pinplanet.pintact.notification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.EventType;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.group.GroupProfileShareActivity;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;
import com.pinplanet.pintact.utility.UiControllerUtil;

import java.util.Date;

public class NotificationDetailActivity  extends MyActivity {

  NotificationDTO notificationDTO;
  int index;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.notification_detail_main);
    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);
    //hideOneRight();

    notificationDTO = (NotificationDTO)getIntent().getSerializableExtra("notification");
    index = getIntent().getIntExtra("index",  0);
    TextView updateFromTitle = (TextView)findViewById(R.id.noti_detail_update_from);
    TextView updateDetailTitle = (TextView)findViewById(R.id.noti_detail_update_details);

    if(notificationDTO != null) {
      String fName = notificationDTO.getData().sender.firstName;
      String lName = notificationDTO.getData().sender.lastName;
      String senderName =  UiControllerUtil.getName(fName, lName);

      String init = UiControllerUtil.getInitial(fName, lName);

      TextView tv = (TextView) findViewById(R.id.actionBar);
      
      LinearLayout acceptlayout = (LinearLayout)findViewById(R.id.nli_accept);

      EventType eventType = notificationDTO.getEventType();
      String title = getResources().getString(R.string.noti_title_update);

      TextView upText = (TextView)findViewById(R.id.noti_detail_update_details_text);
      upText.setText(notificationDTO.detailedText);

      switch (eventType){
        case CONTACT_INTRODUCE:
          title = getResources().getString(R.string.noti_title_pintroduction);
          updateFromTitle.setText(getResources().getString(R.string.noti_detail_introduced_by));
          updateDetailTitle.setText(getResources().getString(R.string.noti_detail_message_from)+" "+senderName.toUpperCase());
          upText.setText(notificationDTO.getData().personalNote);

          findViewById(R.id.introduced_to_title).setVisibility(View.VISIBLE);
          findViewById(R.id.introduced_to_lo).setVisibility(View.VISIBLE);

          TextView introducedName = (TextView)findViewById(R.id.introduced_to_name);
          introducedName.setVisibility(View.VISIBLE);
          introducedName.setText(UiControllerUtil.getName(notificationDTO.getData().introducingUser.firstName , notificationDTO.getData().introducingUser.lastName));

          acceptlayout.setVisibility(View.VISIBLE);
            CustomNetworkImageView imageView = (CustomNetworkImageView) this.findViewById(R.id.introducedToimageViewPicture);
          imageView.setImageUrl(notificationDTO.getData().introducingUser.getPathToImage(), AppController.getInstance().getImageLoader());

          break;
        case CONTACT_INVITE:
          title = getResources().getString(R.string.noti_title_pinivite);
          updateFromTitle.setText(getResources().getString(R.string.noti_detail_invitation_from));
          updateDetailTitle.setText(getResources().getString(R.string.noti_detail_message_from)+" "+senderName.toUpperCase());
          upText.setText(notificationDTO.getData().personalNote);
          
          acceptlayout.setVisibility(View.VISIBLE);

          break;
        default:
          if(!notificationDTO.isSeen()) {
            AppService.markSeenNotification(notificationDTO);
          }
      }
      tv.setText(title);
      //hideOneRight();

        CustomNetworkImageView senderImageView = (CustomNetworkImageView) this.findViewById(R.id.imageViewPicture);
      senderImageView.setImageUrl(notificationDTO.getData().sender.getPathToImage(), AppController.getInstance().getImageLoader());

      TextView initTV=(TextView)this.findViewById(R.id.nli_initial);
      TextView nameTV=(TextView)this.findViewById(R.id.nli_name);
      TextView titleTV=(TextView)this.findViewById(R.id.nli_title_company);



      nameTV.setText(senderName);
      titleTV.setText(UiControllerUtil.getTitleCompanyName(notificationDTO.getData().sender));
      initTV.setText(init);

      if(eventType == EventType.GROUP_CREATE || eventType == EventType.GROUP_JOINED){
        TextView gText1= (TextView)findViewById(R.id.noti_detail_associate_group_pin);
        TextView gText2 = (TextView)findViewById(R.id.set_item_sep2);
        TextView gText3 = (TextView)findViewById(R.id.noti_detail_associate_group_pin_text);
        gText1.setVisibility(View.VISIBLE);
        gText2.setVisibility(View.VISIBLE);
        gText3.setVisibility(View.VISIBLE);

        gText3.setText(notificationDTO.getData().groupName+" - "+notificationDTO.getData().groupPin);

      }

      TextView whenText = (TextView)findViewById(R.id.noti_detail_when_text);
      Date d = notificationDTO.getParsedDate();
      if(d != null)
        whenText.setText(DateUtils.getRelativeTimeSpanString(d.getTime()));


    }
  }

  public void onAcceptInvite(View v) {
    ContactShareRequest req = new ContactShareRequest();
    Long destId = notificationDTO.getData().sender.id;
    req.setDestinationUserId(destId);
    req.setSourceUserId(SingletonLoginData.getInstance().getUserData().id);
    SingletonLoginData.getInstance().setContactShareRequest(req);

    Intent myIntent = new Intent(this, GroupProfileShareActivity.class);
    myIntent.putExtra(GroupProfileShareActivity.ARG_PROFILE_SHARE, index);
    startActivity(myIntent);
  }

  public void onRejectInvite(View v) {

    alertDialog("Confirm Rejection", "Are you sure you want to reject this invitation?");
  }

  public void alertDialog(String title, String info) {
    AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(this, R.style.AlertDialogCustom));
    //AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setMessage(info);
    builder.setInverseBackgroundForced(true);
    builder.setPositiveButton("YES",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog,
                              int which) {
            dialog.dismiss();
            rejectInvite();
          }
        });
    builder.setNegativeButton("NO",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog,
                              int which) {
            dialog.dismiss();
          }
        });

    AlertDialog alert = builder.create();
    alert.show();
  }

  public void rejectInvite() {
    Long destId = notificationDTO.getData().sender.id;
    String path = "/api/contacts/" + destId + "/reject.json?" + SingletonLoginData.getInstance().getPostParam();

    SingletonNetworkStatus.getInstance().clean();
    SingletonNetworkStatus.getInstance().setActivity(this);
    new HttpConnection().access(this, path, "", "POST");

  }

  public void onPostNetwork () {

    if (SingletonNetworkStatus.getInstance().getCode() != 200) {
      myDialog(SingletonNetworkStatus.getInstance().getMsg(),
          SingletonNetworkStatus.getInstance().getErrMsg());
      finish();
      return;
    }

    AppService.markSeenNotification(notificationDTO);
  }
}