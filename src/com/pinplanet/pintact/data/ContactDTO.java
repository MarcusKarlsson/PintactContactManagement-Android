package com.pinplanet.pintact.data;


import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.pinplanet.pintact.contact.ActionButtonType;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.SingletonLoginData;

public class ContactDTO implements ListableEntity, Serializable {
  private Long userId;
  private UserDTO contactUser;
  private List<ProfileDTO> sharedProfiles;
  private List<String> labels;
  private String contactNote;
  private Long groupingId;
  private boolean acontact;
  public  boolean isLocalContact = false;
  public String localContactId;
  public List<Long> sourceProfileIds;
  private boolean alreadyInvited;
  private String subTitle;
  private String firstName;
  private String lastName;
  private String pathToImage;

  public boolean isAcontact() {
    return acontact;
  }

  public void setAcontact(boolean acontact) {
    this.acontact = acontact;
  }

  public boolean isAlreadyInvited() {
    return alreadyInvited;
  }

  public void setAlreadyInvited(boolean alreadyInvited) {
    this.alreadyInvited = alreadyInvited;
  }

  public List<Long> getSourceProfileIds() {
    return sourceProfileIds;
  }

  public void setSourceProfileIds(List<Long> sourceProfileIds) {
    this.sourceProfileIds = sourceProfileIds;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public List<ProfileDTO> getSharedProfiles() {
    return sharedProfiles;
  }

  public void setSharedProfiles(List<ProfileDTO> sharedProfiles) {
    this.sharedProfiles = sharedProfiles;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public UserDTO getContactUser() {
    return contactUser;
  }

  public void setContactUser(UserDTO contactUser) {
    this.contactUser = contactUser;
  }
  
  public String getContactNote() {
    return contactNote;
  }
  
  public void setContactNote(String contactNote) {
    this.contactNote = contactNote;
  }
  
  public Long getGroupingId() {
    return groupingId;
  }
  
  public void setGroupingId(Long groupingId) {
    this.groupingId = groupingId;
  }
  
  public String getFirstName() {
    if(firstName != null)
        return firstName;
      else
        return (this.getContactUser()!= null)?this.getContactUser().getFirstName(): null;
  }
  
  public String getLastName() {
      if(lastName != null)
          return lastName;
      else
        return (this.getContactUser()!= null)?this.getContactUser().getLastName(): null;
  }
  
  public String getName() {
      String name = getFirstName();
      if(getLastName() != null)
      {
          name =  name + " "+getLastName();
      }
      return name;
  }
  
  public boolean isLocalContact() {
    return this.isLocalContact;
  }

  public void setSubTitle(String subTitle)
  {
      this.subTitle = subTitle;
  }
  
  public String getSubtitle() {
      if(subTitle != null)
      {
          return subTitle;
      }
    if (isLocalContact) {
      if (this.getSharedProfiles().get(0).getUserProfileAttributes() == null
          || this.getSharedProfiles().get(0).getUserProfileAttributes().size() == 0)
        return "";
      else
        return this.getSharedProfiles().get(0).getUserProfileAttributes().get(0).getValue();
    } else {
      if (this.getSharedProfiles() == null || this.getSharedProfiles().size() == 0) {
        return "";
      } else {
        ProfileDTO mergedProfile = AppService.getMergedProfile(this);
        String result = mergedProfile.getUserProfile().getCompanyName();
        if (result == null || result.length() == 0) {
          result = mergedProfile.getUserProfile().getTitle();
        }
        if (result == null || result.length() == 0) {
          for (UserProfileAttribute userProfileAttribute : mergedProfile.getUserProfileAttributes()) {
            if (userProfileAttribute.getType() == AttributeType.PHONE_NUMBER) {
              result = userProfileAttribute.getValue();
              break;
            }
            if (userProfileAttribute.getType()== AttributeType.EMAIL
                && (result == null || result.length() == 0)) {
              result = userProfileAttribute.getValue();
            }
          }
        }
        return result;
      }
    }
  }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }

    public String getPathToImage() {
     if(pathToImage != null)
         return pathToImage;

    else if (this.getSharedProfiles() != null && this.getSharedProfiles().size() > 0) {
      return this.getSharedProfiles().get(0).getUserProfile().getPathToImage();
    }
    return null;
  }
  
  public boolean isSelectable() {
    return true;
  }
  
  public boolean isShowAction() {
    if (!this.isLocalContact) {
      return false;
    }
      Map<String, Long> userMap = SingletonLoginData.getInstance().getLocalContactSearchMap();
      HashSet<Long> contactUserList = SingletonLoginData.getInstance().getContactuserList();
      if(userMap != null && userMap.containsKey(localContactId) && contactUserList!= null && contactUserList.contains(userMap.get(localContactId)))
      {
          return false;
      }

    for (UserProfileAttribute userProfileAttribute :this.getSharedProfiles().get(0).getUserProfileAttributes()) {
      if (userProfileAttribute.getType()== AttributeType.PHONE_NUMBER
          || userProfileAttribute.getType()== AttributeType.EMAIL) {
        return true;
      }
    }
    return false;
  }

    public ActionButtonType getActionLabel()
    {
        if (this.isLocalContact) {
            Map<String, Long> userMap = SingletonLoginData.getInstance().getLocalContactSearchMap();
            if(userMap != null && userMap.containsKey(localContactId))
            {
                return ActionButtonType.CONNECT;
            }
        }
        return ActionButtonType.INVITE;
    }


    public boolean isManualContact(){
        if(contactUser != null && contactUser.getUserType()!= null && contactUser.getUserType().equals("MANUAL_CONTACT"))
        {
            return true;
        }else
            return false;
    }
  
}
