package com.luisjavierlinares.android.doing.events;

/**
 * Created by Luis on 29/05/2017.
 */

public class FriendCheckedEvent {

    private Boolean mSuccess;
    private Boolean mExists;
    private String mFriendCode;
    private String mUserCode;
    private String mFiendName;

    public FriendCheckedEvent(Boolean success, Boolean exists, String friendCode, String userCode, String friendName) {
        mSuccess = success;
        mExists = exists;
        mFriendCode = friendCode;
        mUserCode = userCode;
        mFiendName = friendName;
    }

    public Boolean success() {
        return mSuccess;
    }

    public void setSuccess(Boolean success) {
        mSuccess = success;
    }

    public Boolean exists() {
        return mExists;
    }

    public void setExists(Boolean exists) {
        mExists = exists;
    }

    public String getFriendCode() {
        return mFriendCode;
    }

    public void setFriendCode(String friendCode) {
        mFriendCode = friendCode;
    }

    public String getUserCode() {
        return mUserCode;
    }

    public void setUserCode(String userCode) {
        mUserCode = userCode;
    }

    public String getFriendName() {
        return mFiendName;
    }

    public void setFriendName(String name) {
        mFiendName = name;
    }
}
