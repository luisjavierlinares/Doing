package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.Like;

/**
 * Created by Luis on 16/04/2017.
 */

public class LikeAddedEvent {

    private Like mLike;

    public LikeAddedEvent(Like like) {
        mLike = like;
    }

    public Like getLike() {
        return mLike;
    }
}
