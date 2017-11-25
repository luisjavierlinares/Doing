package com.luisjavierlinares.android.doing.messaging;

/**
 * Created by Luis on 25/05/2017.
 */

public class UserMessage {

    private String userCode;
    private String friendName;
    private String friendCode;
    private Long creationDate;
    private Long lastUpdate;

    public UserMessage() {};

    public UserMessage(String userCode, String friendName, String friendCode, Long creationDate, Long lastUpdate) {
        this.userCode = userCode;
        this.friendName = friendName;
        this.friendCode = friendCode;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
