package com.pinplanet.pintact.data;

import java.util.List;

public class GroupDTO {

    private String id;
    private String groupName;
    private String groupPin;
    private String expiredTime;
    private boolean isExpired;
    private String memberCount;
    private String createdBy;
    private Boolean moderated;
    private boolean moderator;
    private List<String> moderators;
    private List<String> sharedProfileIds;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupPin() {
        return groupPin;
    }

    public void setGroupPin(String groupPin) {
        this.groupPin = groupPin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    public Boolean getModerated() {
        return moderated;
    }

    public void setModerated(Boolean moderated) {
        this.moderated = moderated;
    }

    public boolean isModerator() {
        return moderator;
    }

    public void setModerator(boolean moderator) {
        this.moderator = moderator;
    }

    public List<String> getModerators() {
        return moderators;
    }

    public void setModerators(List<String> moderators) {
        this.moderators = moderators;
    }

    public List<String> getSharedProfileIds() {
        return sharedProfileIds;
    }

    public void setSharedProfileIds(List<String> sharedProfileIds) {
        this.sharedProfileIds = sharedProfileIds;
    }
}
