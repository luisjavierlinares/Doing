package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.User;

/**
 * Created by Luis on 03/05/2017.
 */

public class UserUpdatedEvent {

    private User mUser;

    public UserUpdatedEvent(User user){
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
