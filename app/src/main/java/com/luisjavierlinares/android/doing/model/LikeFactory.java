package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import java.util.Date;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.Like.LikeType;

/**
 * Created by Luis on 08/04/2017.
 */

public class LikeFactory {

    private static LikeFactory sLikeFactory;
    private Context mContext;

    public static LikeFactory get(Context context){
        if (sLikeFactory == null){
            sLikeFactory = new LikeFactory(context);
        }
        return sLikeFactory;
    }

    private LikeFactory(Context context) {
        mContext = context;
    }

    public Like getLike() {
        return new LikeImpl();
    };

    public Like getLike(User sender, Doing doing) {
        return new LikeImpl(sender, doing);
    }

    public Like getLike(User sender, Doing doing, Date date) {
        return new LikeImpl(sender, doing, date);
    }

    public Like getLike(User sender, Doing doing, LikeType type) {
        return new LikeImpl(sender, doing, type);
    }

    public Like getLike(User sender, Doing doing, LikeType type, Date date) {
        return new LikeImpl(sender, doing, type, date);
    }

    public Like getLike(UUID id, User sender, Doing doing, LikeType type) {
        return new LikeImpl(id, sender, doing, type);
    }

    public Like getLike(UUID id, User sender, Doing doing, LikeType type, Date date) {
        return new LikeImpl(id, sender, doing, type, date);
    }
}
