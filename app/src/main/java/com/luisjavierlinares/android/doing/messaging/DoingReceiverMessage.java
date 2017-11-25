package com.luisjavierlinares.android.doing.messaging;

/**
 * Created by Luis on 24/06/2017.
 */

public class DoingReceiverMessage {
    private String userCode;
    private String friendName;
    private String friendCode;

    public DoingReceiverMessage() {};

    public DoingReceiverMessage(String userCode, String friendName, String friendCode) {
        this.userCode = userCode;
        this.friendName = friendName;
        this.friendCode = friendCode;
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
}
