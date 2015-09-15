package com.pinplanet.pintact.login;

/**
 * Created by Andy on 5/31/2015.
 */
public class NetworkContact {
    String contactName, pathToImage, pintactId;
    Boolean connected, groupMember, invited;

    public NetworkContact(String pintactId, String contactName, String pathToImage) {
        this.connected = false;
        this.pintactId = pintactId;
        this.contactName = contactName;
        this.pathToImage = pathToImage;
        this.groupMember = false;
        this.invited = false;
    }

    public NetworkContact(String pintactId, String contactName, String pathToImage, Boolean groupMember) {
        this.connected = false;
        this.pintactId = pintactId;
        this.contactName = contactName;
        this.pathToImage = pathToImage;
        this.groupMember = groupMember;
        this.invited = false;
    }

    public String getPintactId() {
        return pintactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public Boolean getGroupMember() {
        return groupMember;
    }

    public Boolean getInvited() {
        return invited;
    }

    public void setInvited(Boolean invited) {
        this.invited = invited;
    }
}
