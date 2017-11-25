package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.Doing;

/**
 * Created by Luis on 11/05/2017.
 */

public class DoingReceivedEvent {

    private Doing mDoing;

    public DoingReceivedEvent(Doing doing){
        mDoing = doing;
    }

    public Doing getDoing() {
        return mDoing;
    }

}
