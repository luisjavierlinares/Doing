package com.luisjavierlinares.android.doing.model;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Luis on 04/04/2017.
 */

public interface User extends Serializable{

    public static int FRIEND_CODE_LENGTH = 6;

    public static enum UserState {ME, INVITED_BY_ME, INVITING_ME, ACTIVE, INACTIVE, IGNORED_BY_ME, IGNORING_ME, UNKNOWN, FRIEND_OF_A_FRIEND};

    public UUID getId();

    public void setId(UUID id);

    public String getName();

    public void setName(String name);

    public String getFriendName();

    public void setFriendName(String friendName);

    public void setFriendCode(String friendCode);

    public String getFriendCode();

    public boolean setUserCode(String userCode);

    public String getUserCode();

    public UserState getState();

    public void setState(UserState state);

    public Date getCreationDate();

    public void setCreationDate(Date date);

    public Date getLastUpdate();

    public void setLastUpdate(Date date);

    public boolean isMine(Doing doing);

    public boolean likes(Doing doing);

    public boolean isMe();

    public boolean isUnknown();

    public boolean isAFriendOfAFriend();

    public boolean isActive();

    public boolean isInactive();

    public boolean isInvitedByMe();

    public boolean isInvitingMe();

    public boolean isIgnoredByMe();

    public boolean isIgnoringMe();

    public void activate();

    public void deactivate();

    public void setAsInvited();

    public void setAsInvitingMe();

    public void setAsIgnored();

    public void setAsIgnoringMe();

    public void setAsUnknown();

    public void setAsFriendOfAFriend();

}
