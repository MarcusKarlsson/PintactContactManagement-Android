package com.pinplanet.pintact.data;

public class ContactShareRequest {

    private Long sourceUserId;

    private String destinationPin;

    private Long destinationUserId;

    private Long[] destinationUserIdArray;

    private String note;

    private Long[] userProfileIdsShared;

    private String groupName;

    private String expiryTimeInUTC;

    private UserDTO destinationUserInfo;

    //private boolean moderated, moderator;

    //private Long[] moderators;

    //private int pendingMembersCount;

    private String purpose, groupVisibility;

    private Long id;

    public UserDTO getDestinationUserInfo() {
        return destinationUserInfo;
    }

    public void setDestinationUserInfo(UserDTO destinationUserInfo) {
        this.destinationUserInfo = destinationUserInfo;
    }

    public String getExpiryTimeInUTC() {
        return expiryTimeInUTC;
    }

    public void setExpiryTimeInUTC(String expiryTimeInUTC) {
        this.expiryTimeInUTC = expiryTimeInUTC;
    }

    public String getDestinationPin() {
        return destinationPin;
    }

    public void setDestinationPin(String destinationPin) {
        this.destinationPin = destinationPin;
    }

    public Long getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(Long sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public Long getDestinationUserId() {
        return destinationUserId;
    }

    public void setDestinationUserId(Long destinationUserId) {
        this.destinationUserId = destinationUserId;
    }

    public Long[] getUserProfileIdsShared() {
        return userProfileIdsShared;
    }

    public void setUserProfileIdsShared(Long[] userProfileIdsShared) {
        this.userProfileIdsShared = userProfileIdsShared;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long[] getDestinationUserIdArray() {
        return destinationUserIdArray;
    }

    public void setDestinationUserIdArray(Long[] destinationUserIdArray) {
        this.destinationUserIdArray = destinationUserIdArray;
    }

//    public boolean isModerated() {
//        return moderated;
//    }

//    public void setModerated(boolean moderated) {
//        this.moderated = moderated;
//    }

//    public boolean isModerator() {
//        return moderator;
//    }
//
//    public void setModerator(boolean moderator) {
//        this.moderator = moderator;
//    }
//
//    public Long[] getModerators() {
//        return moderators;
//    }
//
//    public void setModerators(Long[] moderators) {
//        this.moderators = moderators;
//    }
//
//    public int getPendingMembersCount() {
//        return pendingMembersCount;
//    }
//
//    public void setPendingMembersCount(int pendingMembersCount) {
//        this.pendingMembersCount = pendingMembersCount;
//    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getGroupVisibility() {
        return groupVisibility;
    }

    public void setGroupVisibility(String groupVisibility) {
        this.groupVisibility = groupVisibility;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
