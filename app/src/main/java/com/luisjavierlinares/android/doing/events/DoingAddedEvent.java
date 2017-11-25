package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.Doing;

/**
 * Created by Luis on 02/04/2017.
 */

public class DoingAddedEvent {

    private Doing mDoing;

    public DoingAddedEvent(Doing doing){
        mDoing = doing;
    }

    public Doing getDoing() {
        return mDoing;
    }
}
