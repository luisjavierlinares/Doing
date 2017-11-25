package com.luisjavierlinares.android.doing.model;

import android.content.Context;
import android.database.Cursor;

import com.luisjavierlinares.android.doing.database.DoingDbSchema;

import java.util.Date;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.Doing.DoingAction;

/**
 * Created by Luis on 04/04/2017.
 */

public class DoingFactory {

    private static DoingFactory sDoingFactory;
    private Context mContext;

    public static DoingFactory get(Context context) {
        if (sDoingFactory == null) {
            sDoingFactory = new DoingFactory(context);
        }
        return sDoingFactory;
    }

    private DoingFactory(Context context) {
        mContext = context;
    }

    public Doing getDoing() {
        Doing doing = new DoingImpl();
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(User user, DoingAction action, String text) {
        Doing doing = new DoingImpl(user, action, text);
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(User user, DoingAction action, String text, Date date) {
        Doing doing = new DoingImpl(user, action, text, date);
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(UUID id, User user, DoingAction action, String text, Date date) {
        Doing doing = new DoingImpl(id, user, action, text, date);
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(UUID id, User user, DoingAction action, String text, Date date, int numNormalLikes, int numCommentaries) {
        Doing doing = new DoingImpl(id, user, action, text, date, numNormalLikes, numCommentaries);
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(UUID id, User user, DoingAction action, String text, Date date, int numNormalLikes, int numCommentaries, boolean isLikedByMe) {
        Doing doing = new DoingImpl(id, user, action, text, date, numNormalLikes, numCommentaries);
        doing.setLikedByMe(isLikedByMe);
        setLikeAndCommentariesProxys(doing);

        return doing;
    }

    public Doing getDoing(Cursor cursor) {
        try {
            int columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.DOING_ID);
            UUID id = UUID.fromString(cursor.getString(columnIndex));

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.USER_ID);
            UserDAO usersCatalog = UserDAO.get(mContext);
            User user = usersCatalog.getUser(UUID.fromString(cursor.getString(columnIndex)));

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.ACTION);
            DoingAction action = DoingAction.valueOf(cursor.getString(columnIndex));

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.TEXT);
            String text = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.DATE);
            Date date = new Date(cursor.getLong(columnIndex));

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.LIKES_COUNT);
            Integer numNormalLikes = cursor.getInt(columnIndex);

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.COMMENTARIES_COUNT);
            Integer numCommentaries = cursor.getInt(columnIndex);

            columnIndex = cursor.getColumnIndex(DoingDbSchema.DoingTable.Cols.LIKED_BY_ME);
            Integer isLikedByMeInt = cursor.getInt(columnIndex);
            Boolean isLikedByMe = (isLikedByMeInt == 1);

            Doing doing = new DoingImpl(id, user, action, text, date, numNormalLikes, numCommentaries);
            doing.setLikedByMe(isLikedByMe);
            setLikeAndCommentariesProxys(doing);

            return doing;
        } catch (android.database.CursorIndexOutOfBoundsException exception) {
            return null;
        }

    }

    private void setLikeAndCommentariesProxys(Doing doing) {
        Likes likes = new LikesProxy(doing.getId(), LikeDAO.get(mContext));
        doing.setLikes(likes);
        Commentaries commentaries = new CommentariesProxy(doing.getId(),CommentaryDAO.get(mContext));
        doing.setCommentaries(commentaries);
    }

}
