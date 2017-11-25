package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Luis on 02/04/2017.
 */

public class CommentaryImpl implements Serializable, Commentary{

    private UUID mId;
    private User mSender;
    private Doing mDoing;
    private String mText;
    private Date mDate;

    protected CommentaryImpl() {};

    protected CommentaryImpl(User sender, Doing doing, String text) {
        this(UUID.randomUUID(), sender, doing, text, new Date());
    }

    protected CommentaryImpl(User sender, Doing doing, String text, Date date) {
        this(UUID.randomUUID(), sender, doing, text, date);
    }

    protected CommentaryImpl(UUID id, User sender, Doing doing, String text) {
        this(id, sender, doing, text, new Date());
    }

    protected CommentaryImpl(UUID id, User sender, Doing doing, String text, Date date) {
        mId = id;
        mSender = sender;
        mDoing = doing;
        mText = text;
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

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
