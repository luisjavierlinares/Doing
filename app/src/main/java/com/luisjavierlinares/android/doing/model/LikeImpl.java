package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Luis on 25/03/2017.
 */

public class LikeImpl implements Serializable, Like{

    private UUID mId;
    private User mSender;
    private Doing mDoing;
    private LikeType mType;
    private Date mDate;

    protected LikeImpl() {};

    protected LikeImpl(User sender, Doing doing) {
        this(UUID.randomUUID(), sender, doing, LikeType.NORMAL, new Date());
    }

    protected LikeImpl(User sender, Doing doing, Date date) {
        this(UUID.randomUUID(), sender, doing, LikeType.NORMAL, date);
    }

    protected LikeImpl(User sender, Doing doing, LikeType type) {
        this(UUID.randomUUID(), sender, doing, type, new Date());
    }

    protected LikeImpl(User sender, Doing doing, LikeType type, Date date) {
        this(UUID.randomUUID(), sender, doing, type, date);
    }

    protected LikeImpl(UUID id, User sender, Doing doing, LikeType type) {
        this(id, sender, doing, type, new Date());
    }

    protected LikeImpl(UUID id, User sender, Doing doing, LikeType type, Date date) {
        mId = id;
        mSender = sender;
        mDoing = doing;
        mType = type;
        mDate = date;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public User getSender() {
        return mSender;
    }

    public void setSender(User sender) {
        mSender = sender;
    }

    public Doing getDoing() {
        return mDoing;
    }

    public void setDoing(Doing doing) {
        mDoing = doing;
    }

    public LikeType getType() {
        return mType;
    }

    public void setType(LikeType type) {
        mType = type;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
