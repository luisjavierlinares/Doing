package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.Like;
import com.luisjavierlinares.android.doing.model.LikeFactory;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.database.LikeDbSchema.LikeTable;
import static com.luisjavierlinares.android.doing.model.Like.LikeType;

/**
 * Created by Luis on 02/04/2017.
 */

public class LikeDatabase {

    private static LikeDatabase sLikeDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized LikeDatabase get(Context context){
        if(sLikeDatabase == null) {
            sLikeDatabase = new LikeDatabase(context);
        }
        return sLikeDatabase;
    }

    private LikeDatabase(Context context) {
        mDatabase = LikeBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private LikeCursorWrapper queryLikes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LikeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new LikeCursorWrapper(cursor);
    }

    public void add(Like like) {
        ContentValues values = getContentValues(like);
        mDatabase.insert(LikeTable.NAME, null, values);
    }

    public List<Like> getAllLikes() {
        List<Like> likes = new ArrayList<>();

        LikeCursorWrapper cursor = queryLikes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                likes.add(cursor.getLike());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return likes;
    }

    public List<Like> getDoingLikes(UUID doingId) {
        List<Like> likes = new ArrayList<>();

        if (doingId == null) {return likes;}

        LikeCursorWrapper cursor = queryLikes(
                LikeTable.Cols.DOING_ID + " = ?",
                new String[] {doingId.toString()}
        );

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                likes.add(cursor.getLike());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return likes;
    }

    public Like getLike(UUID id) {

        if (id == null) {
            return null;
        }

        LikeCursorWrapper cursor = queryLikes(
                LikeTable.Cols.LIKE_ID + " = ?",
                new String[] {id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getLike();
        } finally {
            cursor.close();
        }
    }

    public long countLikes(Doing doing) {
        String uuidString = doing.getId().toString();
        return DatabaseUtils.longForQuery(mDatabase, "SELECT COUNT(*) FROM " + LikeTable.NAME + " WHERE " +
                LikeTable.Cols.DOING_ID + " = ? AND " + LikeTable.Cols.TYPE + " = ?",
                new String[]{uuidString, LikeType.NORMAL.toString() });
    }

    public void remove(Like like) {
        String uuidString = like.getId().toString();

        mDatabase.delete(LikeTable.NAME, LikeTable.Cols.LIKE_ID + " = ?", new String[] {uuidString});
    }

    public void update(Like like) {
        String uuidString = like.getId().toString();
        ContentValues values = getContentValues(like);

        mDatabase.update(LikeTable.NAME, values, LikeTable.Cols.LIKE_ID + " = ?", new String[] { uuidString });
    }

    private static ContentValues getContentValues(Like like) {
        ContentValues values = new ContentValues();

        values.put(LikeTable.Cols.LIKE_ID, like.getId().toString());
        values.put(LikeTable.Cols.SENDER_ID, like.getSender().getId().toString());
        values.put(LikeTable.Cols.SENDER_CODE, like.getSender().getUserCode().toString());
        values.put(LikeTable.Cols.SENDER_FRIENDNAME, like.getSender().getFriendName().toString());
        values.put(LikeTable.Cols.SENDER_FRIENDCODE, like.getSender().getFriendCode().toString());
        values.put(LikeTable.Cols.DOING_ID, like.getDoing().getId().toString());
        values.put(LikeTable.Cols.TYPE, like.getType().name());
        values.put(LikeTable.Cols.DATE, like.getDate().getTime());

        return values;
    }

    public class LikeCursorWrapper extends CursorWrapper {

        public LikeCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Like getLike() {
            String likeIdString = getString(getColumnIndex(LikeTable.Cols.LIKE_ID));
            String senderIdString = getString(getColumnIndex(LikeTable.Cols.SENDER_ID));
            String senderCode = getString(getColumnIndex(LikeTable.Cols.SENDER_CODE));
            String senderFriendName = getString(getColumnIndex(LikeTable.Cols.SENDER_FRIENDNAME));
            String senderFriendCode = getString(getColumnIndex(LikeTable.Cols.SENDER_FRIENDCODE));
            String doingIdString = getString(getColumnIndex(LikeTable.Cols.DOING_ID));
            String typeString = getString(getColumnIndex(LikeTable.Cols.TYPE));
            long dateLong = getLong(getColumnIndex(LikeTable.Cols.DATE));

            UUID likeId = UUID.fromString(likeIdString);
            LikeType type = LikeType.valueOf(typeString);
            UUID senderId = UUID.fromString(senderIdString);
            UUID doingId = UUID.fromString(doingIdString);

            User sender = UserDatabase.get(mContext).getUserWithCode(senderCode);

            if (sender == null) {
                sender = UserFactory.get(mContext).getUser();
                sender.setAsFriendOfAFriend();
                sender.setId(senderId);
                sender.setFriendName(senderFriendName);
                sender.setUserCode(senderCode);
                sender.setFriendCode(senderFriendCode);
            }

            DoingDatabase doingDatabase = DoingDatabase.get(mContext);
            Doing doing = doingDatabase.getDoing(doingId);
            Date date = new Date(dateLong);

            Like like = LikeFactory.get(mContext).getLike(likeId, sender, doing, type, date);

            return like;
        }
    }

}
