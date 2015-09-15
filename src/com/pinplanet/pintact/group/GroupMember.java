package com.pinplanet.pintact.group;

import android.util.Log;

/**
 * Created by wildcat on 6/14/2015.
 */
public class GroupMember {
    private String name, company, pathToImage, groupId, phoneNumber, userEmail;
    private Boolean pending, connected, isUser;
    private String memberId;
    private String status;
    private String initials;

    public GroupMember(String memberId, String groupId, String name, String company, String pathToImage, Boolean pending, Boolean connected,
                       Boolean isUser, String phoneNumber, String userEmail, String initials) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.name = name;
        Log.d("Debugging", "Groupmember created: " + name);
        this.company = company;
        this.pathToImage = pathToImage;
        this.pending = pending;
        this.connected = connected;
        this.isUser = isUser;
        this.status = "";
        this.phoneNumber = phoneNumber;
        this.userEmail = userEmail;
        this.initials = initials;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public Boolean getPending() {
        return pending;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public Boolean isUser() {
        return isUser;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getInitials() {
        return initials;
    }
}
