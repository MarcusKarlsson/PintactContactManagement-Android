package com.pinplanet.pintact.data;

public class UserDTO extends BasicUserDTO{

  public String emailId;
  public String mobileNumber;

  public String getEmailId() {
    return emailId;
  }
  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }
  public String getMobileNumber() {
    return mobileNumber;
  }
  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }


    public boolean isManualContact(){
        if(getUserType()!= null && getUserType().equals("MANUAL_CONTACT"))
        {
            return true;
        }else
            return false;
    }

}
