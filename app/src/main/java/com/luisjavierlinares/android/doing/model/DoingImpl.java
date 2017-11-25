package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 25/03/2017.
 */

public class DoingImpl implements Serializable, Doing {

    private UUID mId;
    private User mUser;
    private DoingAction mAction;
    private String mText;
    private Date mDate;
    private Boolean mIsLikedByMe;

    private Likes mLikes;
    private Boolean mHasNewLikes;
    private Commentaries mCommentaries;
    private Boolean mHasNewCommentaries;

    private Integer mLikesCount;
    private Integer mCommentariesCount;

    protected DoingImpl() {

    }

    protected DoingImpl(User user, DoingAction action, String text) {
        this(UUID.randomUUID(), user, action, text, new Date());
    }

    protected DoingImpl(User user, DoingAction action, String text, Date date) {
        this(UUID.randomUUID(), user, action, text, date);
    }

    protected DoingImpl(UUID id, User user, DoingAction action, String text, Date date) {
        this(id, user, action, text, date, 0, 0);
    }

    protected DoingImpl(UUID id, User user, DoingAction action, String text, Date date, int likesCount, int commentariesCount) {
        this(id, user, action, text, date, null, null, likesCount, commentariesCount);
    }

    protected DoingImpl(UUID id, User user, DoingAction action, String text, Date date, Likes likes,
                        Commentaries commentaries, int likesCount, int commentariesCount) {
        mId = id;
        mUser = user;
        mAction = action;
        mText = text.replaceAll("\\s+", " ");
        mDate = date;
        mLikesCount = likesCount;
        mCommentariesCount = commentariesCount;
        mLikes = likes;
        mCommentaries = commentaries;
        mIsLikedByMe = false;
        mHasNewLikes = false;
        mHasNewCommentaries = false;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public DoingAction getAction() {
        return mAction;
    }

    public void setAction(DoingAction action) {
        mAction = action;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text.replaceAll("\\s+", " ");
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Integer getLikesCount() {
        return mLikesCount;
    }

    public void setLikesCount(Integer likesCount) {
        mLikesCount = likesCount;
    }

    public Integer getCommentariesCount() {
        return mCommentariesCount;
    }

    public void setCommentariesCount(Integer commentariesCount) {
        mCommentariesCount = commentariesCount;
    }

    public void setLikes(Likes likes) {
        mLikes = likes;
    }

    public void setCommentaries(Commentaries commentaries) {
        mCommentaries = commentaries;
    }

    public Boolean addLike(Like like) {
        mLikes.update();

        // only one like per user
        for (int i = 0; i < mLikes.size(); i++) {
            Like thisLike = mLikes.get(i);
            if (thisLike.getSender().getId().equals(like.getSender().getId())) {
                return false;
            }
        }

        mLikes.add(like);
        mLikesCount = mLikes.size();
        return true;
    }

    public List<Like> getLikes() {
        mLikes.update();
        mLikesCount = mLikes.size();
        return mLikes.getAll();
    }

    public void addCommentary(Commentary commentary) {
        mCommentaries.update();
        mCommentaries.add(commentary);
        mCommentariesCount = mCommentaries.size();
    }

    public List<Commentary> getCommentaries() {
        mCommentaries.update();
        mCommentariesCount = mCommentaries.size();
        return mCommentaries.getAll();
    }

    @Override
    public Boolean isLikedByMe() {
        return mIsLikedByMe;
    }

    @Override
    public void setLikedByMe(Boolean isLikedByMe) {
        mIsLikedByMe = isLikedByMe;
    }

    @Override
    public Boolean hasNewLikes() {
        return mHasNewLikes;
    }

    @Override
    public void setHasNewLikes(Boolean haveNewLikes) {
        mHasNewLikes = haveNewLikes;
    }

    @Override
    public Boolean hasNewCommentaries() {
        return mHasNewCommentaries;
    }

    @Override
    public void setHasNewCommentaries(Boolean haveNewCommentaries) {
        mHasNewCommentaries = haveNewCommentaries;
    }
}
