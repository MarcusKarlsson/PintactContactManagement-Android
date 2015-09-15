package com.pinplanet.pintact.data;

public enum EventType {
  CONTACT_INVITE((byte)0, "You have been invited to connect."),
  CONTACT_INTRODUCE((byte)1, "You have received an introduction."),
  ADDITIONAL_PROFILE_SHARE((byte)2, "An existing contact has shared an additional profile with you."),
  PROFILE_UNSHARE((byte)3, "A profile has been unshared."),
  CONTACT_INVITE_ACCEPTED((byte) 4, "An invitation has been accepted."),
  CONTACT_INVITE_REJECTED((byte)5, "An invitation has been rejected."),
  GROUP_INVITE_ACCEPTED((byte)6, "You have a new contact via a group."),
  PROFILE_UPDATE((byte)7, "One of your contacts has updated a profile."),
  PROFILE_CREATE((byte)8, "Profile created"),
  UPDATE_PROFILE_SHARE((byte)9, "An existing contact has altered the list of profiles shared with you."),
  GROUP_JOINED((byte)10, "You have joined a new group."),
  PROFILE_DELETE((byte)11, "Profile deleted"),
  CONTACT_DELETE((byte)12, "Contact deleted"),
  GROUP_CREATE((byte)13, "Group created"),
  CONTACT_INVITED ((byte)14, "Group created"),
  CONTACT_INTRODUCED((byte)15, "You have received an introduction."),
  GROUP_JOIN_REQUEST((byte)16, "A user has requested group membership"),
  GROUP_JOINED_BY_MODERATOR((byte)17, "Your request to join the group has been approved"),
  GROUP_REJECTED_BY_MODERATOR((byte)18, "Your request to join the group has been denied"),
  GROUP_JOIN_REQUESTED((byte)19, "Your request to join the group has been sent to group administrator"),
  ADDED_MANUAL_CONTACT((byte)20, "Added manual contact"),
  NEW_USER_ADDED((byte)21, "New user added");

  private byte id;
  
  private String simpleDescription;
  
  private EventType(byte id, String simpleDescription){
    this.id = id;
  }

  public byte getId(){
    return this.id;
  }
  
  public String getSimpleDescription() {
    return this.simpleDescription;
  }
}
