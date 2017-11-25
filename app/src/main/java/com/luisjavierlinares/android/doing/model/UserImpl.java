package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 25/03/2017.
 */

public class UserImpl implements Serializable, User{

    private UUID mId;
    private String mName;
    private String mUserCode;
    private String mFriendName;
    private String mFriendCode;
    private UserState mState;
    private Date mCreationDate;
    private Date mLastUpdate;

    protected UserImpl() {
        mCreationDate = new Date();
        mLastUpdate = new Date(0);
    }

    protected UserImpl(UUID id, String name, UserState state) {
        mId = id;
        mName = name;
        mState = state;
        mCreationDate = new Date();
        mLastUpdate = new Date(0);
    }

    @Override
    public UUID getId() {
        return mId;
    }

    @Override
    public void setId(UUID id) {
        mId = id;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getFriendName() {
        return mFriendName;
    }

    @Override
    public void setFriendName(String friendName) {
        mFriendName = friendName;
    }

    @Override
    public void setFriendCode(String friendCode) {
        mFriendCode = friendCode;
    }

    @Override
    public String getFriendCode() {
        return mFriendCode;
    }

    @Override
    public boolean setUserCode(String userCode) {

        if (userCode == null) {
            return false;
        }

        if (userCode.isEmpty()) {
            return false;
        }

        mUserCode = userCode;
        return true;
    }

    @Override
    public String getUserCode() {
        return mUserCode;
    }

    @Override
    public UserState getState() {
        return mState;
    }

    @Override
    public void setState(UserState state) {
        mState = state;
    }

    @Override
    public Date getCreationDate() {
        return mCreationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        mCreationDate = creationDate;
    }

    @Override
    public Date getLastUpdate() {
        return mLastUpdate;
    }

    @Override
    public void setLastUpdate(Date lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    @Override
    public boolean isMine(Doing doing) {
        return mId.equals(doing.getUser().getId());
    }

    @Override
    public boolean likes(Doing doing) {
        List<Like> likes = doing.getLikes();

        for(int i = 0; i < likes.size(); i++) {
            Like like = likes.get(i);
            if (mId.equals(like.getSender().getId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isMe() {
        return mState == UserState.ME;
    }

    @Override
    public boolean isUnknown() {
        return mState == UserState.UNKNOWN;
    }

    @Override
    public boolean isAFriendOfAFriend() {
        return mState == UserState.FRIEND_OF_A_FRIEND;
    }

    @Override
    public boolean isActive() {
        return mState == UserState.ACTIVE;
    }

    @Override
    public boolean isInactive() {
        return mState == UserState.INACTIVE;
    }

    @Override
    public boolean isInvitedByMe() {
        return mState == UserState.INVITED_BY_ME;
    }

    @Override
    public boolean isInvitingMe() {
        return mState == UserState.INVITING_ME;
    }

    @Override
    public boolean isIgnoredByMe() {
        return mState == UserState.IGNORED_BY_ME;
    }

    @Override
    public boolean isIgnoringMe() {
        return mState == UserState.IGNORING_ME;
    }

    @Override
    public void activate() {
        mState = UserState.ACTIVE;
    }

    @Override
    public void deactivate() {
        mState = UserState.INACTIVE;
    }

    @Override
    public void setAsInvited() {
        mState = UserState.INVITED_BY_ME;
    }

    @Override
    public void setAsInvitingMe() {
        mState = UserState.INVITING_ME;
    }

    @Override
    public void setAsIgnored() {
        mState = UserState.IGNORED_BY_ME;
    }

    @Override
    public void setAsIgnoringMe() {
        mState = UserState.IGNORING_ME;
    }

    @Override
    public void setAsUnknown() {
        mState = UserState.UNKNOWN;
    }

    @Override
    public void setAsFriendOfAFriend() {
        mState = UserState.FRIEND_OF_A_FRIEND;
    }
}
