package com.pinplanet.pintact.data;


import com.pinplanet.pintact.contact.ActionButtonType;

public class SearchDTO implements ListableEntity {

  private String profileId;

  private String firstName;

  private String lastName;

  private String title;

  private String companyName;

  private String userId;

  private String city;

  private String pathToImage;

  private boolean isGrouping;

  private String groupName;

  private String groupPin;

  public String getPathToImage() {
    return pathToImage;
  }

  public void setPathToImage(String pathToImage) {
    this.pathToImage = pathToImage;
  }

  public boolean isGrouping() {
    return isGrouping;
  }

  public void setGrouping(boolean isGrouping) {
    this.isGrouping = isGrouping;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupPin() {
    return groupPin;
  }

  public void setGroupPin(String groupPin) {
    this.groupPin = groupPin;
  }

  private boolean inContactList;

  public boolean isInContactList() {
    return inContactList;
  }

  public void setInContactList(boolean isInContactList) {
    this.inContactList = isInContactList;
  }

  public String getProfileId() {
    return profileId;
  }

  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
  
  public String getName() {
    return firstName + " " + lastName;
  }
  
  public boolean isLocalContact() {
    return false;
  }
  
  public String getSubtitle() {
    if (this.getCompanyName() != null && this.getCompanyName().length() > 0) {
      return this.getCompanyName();
    }
    if (this.getTitle() != null && this.getTitle().length() > 0) {
      return this.getTitle();
    }
    return this.getCity();
  }
  
  public boolean isSelectable() {
    return false;
  }
  
  public boolean isShowAction() {
    return !this.inContactList || getGroupPin()!= null ;
  }

    public ActionButtonType getActionLabel()
    {
        return ActionButtonType.ADD;
    }


  
}
