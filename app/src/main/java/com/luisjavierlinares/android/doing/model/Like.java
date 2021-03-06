package com.luisjavierlinares.android.doing.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Luis on 08/04/2017.
 */

public interface Like extends Serializable{

    public static enum LikeType {NORMAL, GOLDEN};

    public UUID getId();

    public void setId(UUID id);

    public User getSender();

    public void setSender(User sender);

    public Doing getDoing();

    public void setDoing(Doing doing);

    public LikeType getType();

    public void setType(LikeType type);

    public Date getDate();

    public void setDate(Date date);

}
