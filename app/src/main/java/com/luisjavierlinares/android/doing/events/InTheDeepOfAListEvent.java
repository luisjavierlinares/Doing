package com.luisjavierlinares.android.doing.events;

/**
 * Created by Luis on 10/04/2017.
 */

public class InTheDeepOfAListEvent {
    boolean mValue = false;

    public InTheDeepOfAListEvent(boolean value) {
        mValue = value;
    }

    public boolean isTrue() {
        return mValue==true;
    }

    public void setValue(boolean value) {
        mValue = value;
    }
}
