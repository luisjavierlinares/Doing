package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Luis on 09/04/2017.
 */

public class CommentaryFactory {

    private static CommentaryFactory sCommentaryFactory;
    private Context mContext;

    public static CommentaryFactory get(Context context){
        if (sCommentaryFactory == null){
            sCommentaryFactory = new CommentaryFactory(context);
        }
        return sCommentaryFactory;
    }

    private CommentaryFactory(Context context) {
        mContext = context;
    }

    public Commentary getCommentary() {
        return new CommentaryImpl();
    };

    public Commentary getCommentary(User sender, Doing doing, String text) {
        return new CommentaryImpl(sender, doing, text);
    }

    public Commentary getCommentary(User sender, Doing doing, String text, Date date) {
        return new CommentaryImpl(sender, doing, text, date);
    }

    public Commentary getCommentary(UUID id, User sender, Doing doing, String text) {
        return new CommentaryImpl(id, sender, doing, text);
    }

    public Commentary getCommentary(UUID id, User sender, Doing doing, String text, Date date) {
        return new CommentaryImpl(id, sender, doing, text, date);
    }
}
