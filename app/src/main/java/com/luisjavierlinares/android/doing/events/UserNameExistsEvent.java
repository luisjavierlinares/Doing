package com.luisjavierlinares.android.doing.events;

/**
 * Created by Luis on 30/10/2017.
 */

public class UserNameExistsEvent {

    private String mName;
    private boolean mSuccess;
    private boolean mExists;

    public UserNameExistsEvent(String name, boolean success, boolean exists) {
        mName = name;
        mSuccess = success;
        mExists = exists;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean success() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public boolean exists() {
        return mExists;
    }

    public void setExists(boolean exists) {
        mExists = exists;
    }
}
