package com.pinplanet.pintact.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class NotificationDTO implements Serializable{

  public static final String PROFILES_KEY ="profiles";
  public static final String FULL_PROFILES_KEY = "fullProfiles";
  public static final String SENDER_KEY = "sender";
  public static final String INTRODUCING_USER_KEY = "introducingUser";
  public static final String OTHER_DATA_KEY = "other";

  private Long notificationId;
  private EventType eventType;
  private NotifyMaps data;
  public String topic;
  public String summaryText;
  public String detailedText;
  private String createdAt;
  private boolean seen;

  public boolean isSeen() {
    return seen;
  }

  public void setSeen(boolean seen) {
    this.seen = seen;
  }

  public NotificationDTO(){
    data = new NotifyMaps();
  }
  
  public void setNotificationId(Long notificationId) {
    this.notificationId = notificationId;
  }
  
  public Long getNotificationId() {
    return notificationId;
  }

  public EventType getEventType() {
    return eventType;
  }

  public String getSummaryText() {
    return summaryText;
  }

  public void setSummaryText(String summaryText) {
    this.summaryText = summaryText;
  }

  public String getDetailedText() {
    return detailedText;
  }

  public void setDetailedText(String detailedText) {
    this.detailedText = detailedText;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public NotifyMaps getData() {
    return data;
  }

  public void setData(NotifyMaps data) {
    this.data = data;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public Date getParsedDate(){
    if(createdAt != null){
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
      try{
        return df.parse(createdAt);
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    return null;
  }
}
