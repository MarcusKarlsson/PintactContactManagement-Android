package com.pinplanet.pintact.data;

import com.pinplanet.pintact.contact.ActionButtonType;

public interface ListableEntity {
  
  public String getFirstName();
  
  public String getLastName();
  
  public String getName();
  
  public boolean isLocalContact();
  
  public String getSubtitle();
  
  public String getPathToImage();
  
  public boolean isSelectable();
  
  public boolean isShowAction();

  public ActionButtonType getActionLabel();

}
