package com.pinplanet.pintact.data;

import java.util.List;

public class ContactUpdateDTO {

  private Long lastSeenNotificationId;
  private Long activeContactCount;
  private List<ContactDTO> contacts;
  private int totalUnseenNotifications;

  public int getTotalUnseenNotifications() {
    return totalUnseenNotifications;
  }

  public void setTotalUnseenNotifications(int totalUnseenNotifications) {
    this.totalUnseenNotifications = totalUnseenNotifications;
  }

  public Long getLastSeenNotificationId() {
    return lastSeenNotificationId;
  }

  public void setLastSeenNotificationId(Long lastSeenNotificationId) {
    this.lastSeenNotificationId = lastSeenNotificationId;
  }

  public Long getActiveContactCount() {
    return activeContactCount;
  }

  public void setActiveContactCount(Long activeContactCount) {
    this.activeContactCount = activeContactCount;
  }

  public List<ContactDTO> getContacts() {
    return contacts;
  }

  public void setContacts(List<ContactDTO> contacts) {
    this.contacts = contacts;
  }
}

