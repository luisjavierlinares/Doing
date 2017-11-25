package com.luisjavierlinares.android.doing.events;

import com.luisjavierlinares.android.doing.model.Commentary;

/**
 * Created by Luis on 27/04/2017.
 */

public class CommentaryAddedEvent {

    Commentary mCommentary;

    public CommentaryAddedEvent(Commentary commentary) {
        mCommentary = commentary;
    }

    public Commentary getCommentary() {
        return mCommentary;
    }
}
