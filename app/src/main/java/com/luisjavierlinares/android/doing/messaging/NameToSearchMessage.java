package com.luisjavierlinares.android.doing.messaging;

/**
 * Created by Luis on 30/10/2017.
 */

public class NameToSearchMessage {

    private String userName;
    private String userCode;
    private String friendCode;

    public NameToSearchMessage() {};

    public NameToSearchMessage(String userName, String userCode, String friendCode) {
        this.userName = userName;
        this.userCode = userCode;
        this.friendCode = friendCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }
}
