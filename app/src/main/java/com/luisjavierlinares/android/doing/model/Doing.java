package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 04/04/2017.
 */

public interface Doing extends Serializable{

    public static enum DoingAction {READING, WATCHING, PLAYING, LISTENING, ENJOYING};

    public UUID getId();

    public void setId(UUID id);

    public User getUser();

    public void setUser(User user);

    public DoingAction getAction();

    public void setAction(DoingAction action);

    public String getText();

    public void setText(String text);

    public Date getDate();

    public void setDate(Date date);

    public Integer getLikesCount();

    public void setLikesCount(Integer likesCount);

    public Integer getCommentariesCount();

    public void setCommentariesCount(Integer commentariesCount);

    public void setLikes(Likes Likes);

    public void setCommentaries(Commentaries commentaries);

    public Boolean addLike(Like like);

    public List<Like> getLikes();

    public void addCommentary(Commentary commentary);

    public List<Commentary> getCommentaries();

    public Boolean isLikedByMe();

    public void setLikedByMe(Boolean isLikedByMe);

    public Boolean hasNewLikes();

    public void setHasNewLikes(Boolean haveNewLikes);

    public Boolean hasNewCommentaries();

    public void setHasNewCommentaries(Boolean haveNewCommentaries);
}
