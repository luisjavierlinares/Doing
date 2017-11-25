package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.User;

import java.util.List;

/**
 * Created by Luis on 31/10/2017.
 */

public class FriendSearchEvent {

    private List<User> mFriends;
    private int mLimit;

    public FriendSearchEvent(List<User> friends, int limit) {
        mFriends = friends;
        mLimit = limit;
    }

    public List<User> getFriends() {
        return mFriends;
    }

    public void setFriends(List<User> friends) {
        mFriends = friends;
    }

    public int getLimit() {
        return mLimit;
    }

    public void setLimit(int limit) {
        mLimit = limit;
    }

    public boolean thereAreMore() {
        if (mFriends.isEmpty()) {
            return false;
        }

        if (mFriends.size() < mLimit) {
            return false;
        } else {
            return true;
        }
    }
}
