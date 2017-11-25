package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.User.UserState;

/**
 * Created by Luis on 08/04/2017.
 */

public class UserFactory {

    private static UserFactory sUserFactory;
    private Context mContext;

    public static UserFactory get(Context context){
        if (sUserFactory == null){
            sUserFactory = new UserFactory(context);
        }
        return sUserFactory;
    }

    private UserFactory(Context context) {
        mContext = context;
    }

    public User getUser() {
        return new UserImpl();
    }

    public User getUser(String name) {
        UUID id = UUID.randomUUID();
        UserState state = UserState.INVITED_BY_ME;
        return new UserImpl(id, name, state);
    }

    public User getUser(String name, UserState state) {
        UUID id = UUID.randomUUID();
        return new UserImpl(id, name, state);
    }

    public User getUser(UUID id, String name, UserState state) {
        return new UserImpl(id, name, state);
    }
}
