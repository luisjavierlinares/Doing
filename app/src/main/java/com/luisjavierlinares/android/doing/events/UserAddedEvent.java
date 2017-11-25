package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.User;

/**
 * Created by Luis on 02/05/2017.
 */

public class UserAddedEvent {

    private User mUser;

    public UserAddedEvent(User user){
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
