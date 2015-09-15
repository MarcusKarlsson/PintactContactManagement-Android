package com.pinplanet.pintact.data;


public class UpdateGroupPinRequest {

  private String name;
  private String expiryTimeInUTC;

  private String groupPin;

  public String getGroupPin() {
    return groupPin;
  }

  public void setGroupPin(String groupPin) {
    this.groupPin = groupPin;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExpiryTimeInUTC() {
    return expiryTimeInUTC;
  }

  public void setExpiryTimeInUTC(String expiryTimeInUTC) {
    this.expiryTimeInUTC = expiryTimeInUTC;
  }
}
