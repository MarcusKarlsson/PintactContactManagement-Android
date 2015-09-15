package com.pinplanet.pintact.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Avinash on 13/3/15.
 */
public class ChatTopicDTO implements Serializable{

    private Long id;
    private Long groupId;
    private String name;
    private Date lastMessageTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}