package com.luisjavierlinares.android.doing.events;

/**
 * Created by Luis on 02/10/2017.
 */

public class AccountRecoveredEvent {

    private boolean mSuccess;

    public AccountRecoveredEvent(boolean success) {
        mSuccess = success;
    }

    public boolean success() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }
}
