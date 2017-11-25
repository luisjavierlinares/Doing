package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.model.Commentary;
import com.luisjavierlinares.android.doing.model.CommentaryFactory;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.database.CommentaryDbSchema.CommentaryTable;

/**
 * Created by Luis on 09/04/2017.
 */

public class CommentaryDatabase {

    private static CommentaryDatabase sCommentaryDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized CommentaryDatabase get(Context context) {
        if (sCommentaryDatabase == null) {
            sCommentaryDatabase = new CommentaryDatabase(context);
        }
        return sCommentaryDatabase;
    }

    private CommentaryDatabase(Context context) {
        mDatabase = CommentaryBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private CommentaryCursorWrapper queryCommentaries(String whereClause, String[] whereArgs) {
        return queryCommentaries(whereClause, whereArgs, null);
    }

    private CommentaryCursorWrapper queryCommentaries(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                CommentaryTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );

        return new CommentaryCursorWrapper(cursor);
    }

    public void add(Commentary commentary) {
        ContentValues values = getContentValues(commentary);
        mDatabase.insert(CommentaryTable.NAME, null, values);
    }

    public List<Commentary> getAllCommentaries() {
        List<Commentary> commentaries = new ArrayList<>();

        CommentaryCursorWrapper cursor = queryCommentaries(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                commentaries.add(cursor.getCommentary());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return commentaries;
    }

    public List<Commentary> getDoingCommentaries(UUID doingId) {
        List<Commentary> commentaries = new ArrayList<>();

        if (doingId == null) {return commentaries;}

        CommentaryCursorWrapper cursor = queryCommentaries(
                CommentaryTable.Cols.DOING_ID + " = ?",
                new String[]{doingId.toString()}
        );

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                commentaries.add(cursor.getCommentary());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return commentaries;
    }

    public List<Commentary> getDoingCommentariesOrderByDate(UUID doingId) {
        List<Commentary> commentaries = new ArrayList<>();

        if (doingId == null) {return commentaries;}

        String orderBy = CommentaryTable.Cols.DATE + " ASC";

        CommentaryCursorWrapper cursor = queryCommentaries(
                CommentaryTable.Cols.DOING_ID + " = ?",
                new String[]{doingId.toString()},
                orderBy
        );

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                commentaries.add(cursor.getCommentary());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return commentaries;
    }

    public Commentary getCommentary(UUID id) {
        if (id == null) {
            return null;
        }

        CommentaryCursorWrapper cursor = queryCommentaries(
                CommentaryTable.Cols.COMMENTARY_ID + " = ?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCommentary();
        } finally {
            cursor.close();
        }
    }

    public long countCommentaries(Doing doing) {
        String uuidString = doing.getId().toString();
        return DatabaseUtils.longForQuery(mDatabase, "SELECT COUNT(*) FROM " + CommentaryTable.NAME + " WHERE " +
                CommentaryTable.Cols.DOING_ID + " = ?" , new String[]{uuidString});
    }

    public boolean hasACommentary(User user, Doing doing) {

        CommentaryCursorWrapper cursor = queryCommentaries(
                CommentaryTable.Cols.DOING_ID + " = ?" + " AND " + CommentaryTable.Cols.SENDER_ID + " = ?",
                new String[] {doing.getId().toString(), user.getId().toString()});

        if (cursor.getCount() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void remove(Commentary commentary) {
        String uuidString = commentary.getId().toString();

        mDatabase.delete(CommentaryTable.NAME, CommentaryTable.Cols.COMMENTARY_ID + " = ?", new String[]{uuidString});
    }

    public void update(Commentary commentary) {
        String uuidString = commentary.getId().toString();
        ContentValues values = getContentValues(commentary);

        mDatabase.update(CommentaryTable.NAME, values, CommentaryTable.Cols.COMMENTARY_ID + " = ?", new String[]{uuidString});
    }

    private static ContentValues getContentValues(Commentary commentary) {
        ContentValues values = new ContentValues();

        values.put(CommentaryTable.Cols.COMMENTARY_ID, commentary.getId().toString());
        values.put(CommentaryTable.Cols.SENDER_ID, commentary.getSender().getId().toString());
        values.put(CommentaryTable.Cols.SENDER_CODE, commentary.getSender().getUserCode().toString());
        values.put(CommentaryTable.Cols.SENDER_FRIENDNAME, commentary.getSender().getFriendName().toString());
        values.put(CommentaryTable.Cols.SENDER_FRIENDCODE, commentary.getSender().getFriendCode().toString());
        values.put(CommentaryTable.Cols.DOING_ID, commentary.getDoing().getId().toString());
        values.put(CommentaryTable.Cols.TEXT, commentary.getText());
        values.put(CommentaryTable.Cols.DATE, commentary.getDate().getTime());


        return values;
    }

    public class CommentaryCursorWrapper extends CursorWrapper {

        public CommentaryCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Commentary getCommentary() {
            String commentaryIdString = getString(getColumnIndex(CommentaryTable.Cols.COMMENTARY_ID));
            String senderIdString = getString(getColumnIndex(CommentaryTable.Cols.SENDER_ID));
            String senderCode = getString(getColumnIndex(CommentaryTable.Cols.SENDER_CODE));
            String senderFriendName = getString(getColumnIndex(CommentaryTable.Cols.SENDER_FRIENDNAME));
            String senderFriendCode = getString(getColumnIndex(CommentaryTable.Cols.SENDER_FRIENDCODE));
            String doingIdString = getString(getColumnIndex(CommentaryTable.Cols.DOING_ID));
            String text = getString(getColumnIndex(CommentaryTable.Cols.TEXT));
            long dateLong = getLong(getColumnIndex(CommentaryTable.Cols.DATE));

            UUID commentaryId = UUID.fromString(commentaryIdString);
            UUID senderId = UUID.fromString(senderIdString);
            UUID doingId = UUID.fromString(doingIdString);

            User sender = UserDatabase.get(mContext).getUserWithCode(senderCode);

            if (sender == null) {
                sender = UserFactory.get(mContext).getUser();
                sender.setAsFriendOfAFriend();
                sender.setId(senderId);
                sender.setUserCode(senderCode);
                sender.setFriendName(senderFriendName);
                sender.setFriendCode(senderFriendCode);
            }

            DoingDatabase doingDatabase = DoingDatabase.get(mContext);
            Doing doing = doingDatabase.getDoing(doingId);
            Date date = new Date(dateLong);

            Commentary commentary = CommentaryFactory.get(mContext).getCommentary(commentaryId, sender, doing, text, date);

            return commentary;
        }
    }
}
