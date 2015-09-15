package com.pinplanet.pintact.data;

public class DeviceRegistrationRequest {
  
  public enum DeviceType {
    
    APPLE,
    
    ANDROID;

  }
  
  private Long userId;
  
  String deviceId;
  
  String staticId;
  
  DeviceType deviceType;
  
  boolean userPreference;
  
  String clientInfo;
  
  boolean production = true;

  public Long getUserId() {
    return userId;
  }
  
  public void setUserId(Long userId) {
    this.userId = userId;
  }
  
  public String getDeviceId() {
    return deviceId;
  }
  
  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
  
  public String getStaticId() {
    return staticId;
  }
  
  public void setStaticId(String staticId) {
    this.staticId = staticId;
  }
  
  public DeviceType getDeviceType() {
    return deviceType;
  }
  
  public void setDeviceType(DeviceType deviceType) {
    this.deviceType = deviceType;
  }
  
  public boolean getUserPreference() {
    return userPreference;
  }
  
  public void setUserPreference(boolean userPreference) {
    this.userPreference = userPreference;
  }
  
  public String getClientInfo() {
    return clientInfo;
  }

  public void setClientInfo(String clientInfo) {
    this.clientInfo = clientInfo;
  }
  
  public boolean isProduction() {
    return production;
  }
  
  public void setProduction(boolean production) {
    this.production = production;
  }

}
