package com.pinplanet.pintact.data;

public class UserProfileChildLabelDTO {

  UserProfileChildType userProfileChildType;
  
  AttributeType attributeType;
  
  String label;
  
  public UserProfileChildType getUserProfileChildType() {
    return userProfileChildType;
  }
  public void setUserProfileChildType(UserProfileChildType userProfileChildType) {
    this.userProfileChildType = userProfileChildType;
  }
  public AttributeType getAttributeType() {
    return attributeType;
  }
  public void setAttributeType(AttributeType attributeType) {
    this.attributeType = attributeType;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  
}
