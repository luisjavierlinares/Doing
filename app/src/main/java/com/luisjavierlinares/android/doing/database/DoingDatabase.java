package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.DoingFactory;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.utils.LazyList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.database.DoingDbSchema.DoingTable;
import static com.luisjavierlinares.android.doing.model.Doing.DoingAction;

/**
 * Created by Luis on 01/04/2017.
 */

public class DoingDatabase {

    private static DoingDatabase sDoingDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized DoingDatabase get(Context context){
        if(sDoingDatabase == null) {
            sDoingDatabase = new DoingDatabase(context);
        }
        return sDoingDatabase;
    }

    private DoingDatabase(Context context) {
        mDatabase = DoingBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private DoingCursorWrapper queryDoings(String whereClause, String[] whereArgs) {
        return queryDoings(whereClause, whereArgs, null);
    }

    private DoingCursorWrapper queryDoings(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                DoingTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );

        return new DoingCursorWrapper(cursor);
    }

    public void add(Doing doing) {
        ContentValues values = getContentValues(doing);
        mDatabase.insert(DoingTable.NAME, null, values);
    }

    public List<Doing> getAllDoings() {
        List<Doing> doings = new ArrayList<>();

        DoingCursorWrapper cursor = queryDoings(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                doings.add(cursor.getDoing());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return doings;
    }

    public List<Doing> getAllDoingsOrderByDate() {
        List<Doing> doings = new ArrayList<>();
        String orderBy = DoingTable.Cols.DATE + " DESC";

        DoingCursorWrapper cursor = queryDoings(null, null, orderBy);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                doings.add(cursor.getDoing());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return doings;
    }

    public LazyList<Doing> getAllLazyDoingsOrderByDate() {
        String orderBy = DoingTable.Cols.DATE + " DESC";

        DoingCursorWrapper cursor = queryDoings(null, null, orderBy);
        return manageLazyCursor(cursor);
    }

    public LazyList<Doing> getAllLazyDoingsOrderByDate(int sizeLimit) {
        String orderBy = DoingTable.Cols.DATE + " DESC LIMIT " + sizeLimit;

        DoingCursorWrapper cursor = queryDoings(null, null, orderBy);
        return manageLazyCursor(cursor);
    }

    public LazyList<Doing> getAllLazyDoingsFromUser(UUID userId){
        String orderBy = DoingTable.Cols.DATE + " DESC";

        DoingCursorWrapper cursor = queryDoings(
                DoingTable.Cols.USER_ID + " = ?",
                new String[] {userId.toString()},
                orderBy);

        return manageLazyCursor(cursor);
    }

    public LazyList<Doing> getAllLazyDoingsFromUser(UUID userId, int sizeLimit){
        String orderBy = DoingTable.Cols.DATE + " DESC LIMIT " + sizeLimit;

        DoingCursorWrapper cursor = queryDoings(
                DoingTable.Cols.USER_ID + " = ?",
                new String[] {userId.toString()},
                orderBy);

        return manageLazyCursor(cursor);
    }

    public LazyList<Doing> getAllLazyDoingsFromUser(UUID userId, DoingAction doingAction, int sizeLimit){
        String orderBy = DoingTable.Cols.DATE + " DESC LIMIT " + sizeLimit;

        DoingCursorWrapper cursor = queryDoings(
                DoingTable.Cols.USER_ID + " = ?" + " AND " + DoingTable.Cols.ACTION + " = ?",
                new String[] {userId.toString(), doingAction.toString()},
                orderBy);

        return manageLazyCursor(cursor);
    }

    protected LazyList<Doing> manageLazyCursor(Cursor cursor) {
        return new LazyList<>(cursor, new LazyList.ItemFactory<Doing>() {
            @Override
            public Doing create(Cursor cursor, int index) {
                cursor.moveToPosition(index);

                Doing doing = DoingFactory.get(mContext).getDoing(cursor);

                return doing;
            }
        });
    }

    public Doing getDoing(UUID id) {
        if (id == null) {
            return null;
        }

        DoingCursorWrapper cursor = queryDoings(
                DoingTable.Cols.DOING_ID + " = ?",
                new String[] {id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getDoing();
        } finally {
            cursor.close();
        }
    }

    public Doing getLastDoing(User user){
        if (user == null) {
            return null;
        }

        UUID userId = user.getId();

        if (userId == null) {
            return null;
        }

        String orderBy = DoingTable.Cols.DATE + " DESC LIMIT 1";

        DoingCursorWrapper cursor = queryDoings(
                DoingTable.Cols.USER_ID + " = ?",
                new String[] {userId.toString()},
                orderBy
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getDoing();
        } finally {
            cursor.close();
        }
    }

    public List<Doing> getOldDoings(long sizeLimit) {
        List<Doing> oldDoings = new ArrayList<>();
        String orderBy = DoingTable.Cols.DATE + " ASC LIMIT " + sizeLimit;

        DoingCursorWrapper cursor = queryDoings(null, null, orderBy);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                oldDoings.add(cursor.getDoing());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return oldDoings;
    }

    public void remove(Doing doing) {
        String uuidString = doing.getId().toString();

        mDatabase.delete(DoingTable.NAME, DoingTable.Cols.DOING_ID + " = ?", new String[] {uuidString});
    }

    public void update(Doing doing) {
        String uuidString = doing.getId().toString();
        ContentValues values = getContentValues(doing);

        mDatabase.update(DoingTable.NAME, values, DoingTable.Cols.DOING_ID + " = ?", new String[] { uuidString });
    }

    public void updateCounts(Doing doing) {
        String uuidString = doing.getId().toString();
        ContentValues values = new ContentValues();
        values.put(DoingTable.Cols.LIKES_COUNT, doing.getLikesCount());
        values.put(DoingTable.Cols.COMMENTARIES_COUNT, doing.getCommentariesCount());

        if (doing.isLikedByMe()) {
            values.put(DoingTable.Cols.LIKED_BY_ME, 1);
        } else {
            values.put(DoingTable.Cols.LIKED_BY_ME, 0);
        }

        mDatabase.update(DoingTable.NAME, values, DoingTable.Cols.DOING_ID + " = ?", new String[] { uuidString });
    }

    public long size() {
        return DatabaseUtils.queryNumEntries(mDatabase, DoingTable.NAME);
    }

    private static ContentValues getContentValues(Doing doing) {
        ContentValues values = new ContentValues();
        values.put(DoingTable.Cols.DOING_ID, doing.getId().toString());
        values.put(DoingTable.Cols.USER_ID, doing.getUser().getId().toString());
        values.put(DoingTable.Cols.ACTION, doing.getAction().name());
        values.put(DoingTable.Cols.TEXT, doing.getText());
        values.put(DoingTable.Cols.DATE, doing.getDate().getTime());
        values.put(DoingTable.Cols.LIKES_COUNT, doing.getLikesCount());
        values.put(DoingTable.Cols.COMMENTARIES_COUNT, doing.getCommentariesCount());

        if (doing.isLikedByMe()) {
            values.put(DoingTable.Cols.LIKED_BY_ME, 1);
        } else {
            values.put(DoingTable.Cols.LIKED_BY_ME, 0);
        }

        if (doing.hasNewLikes()) {
            values.put(DoingTable.Cols.HAS_NEW_LIKES, 1);
        } else {
            values.put(DoingTable.Cols.HAS_NEW_LIKES, 0);
        }

        if (doing.hasNewCommentaries()) {
            values.put(DoingTable.Cols.HAS_NEW_COMMENTARIES, 1);
        } else {
            values.put(DoingTable.Cols.HAS_NEW_COMMENTARIES, 0);
        }

        return values;
    }

    public class DoingCursorWrapper extends CursorWrapper {

        public DoingCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Doing getDoing() {
            String doingIdString = getString(getColumnIndex(DoingTable.Cols.DOING_ID));
            String userIdString = getString(getColumnIndex(DoingTable.Cols.USER_ID));
            String actionString = getString(getColumnIndex(DoingTable.Cols.ACTION));
            String text = getString(getColumnIndex(DoingTable.Cols.TEXT));
            long dateLong = getLong(getColumnIndex(DoingTable.Cols.DATE));
            int nLikes = getInt(getColumnIndex(DoingTable.Cols.LIKES_COUNT));
            int nCommentaries = getInt(getColumnIndex(DoingTable.Cols.COMMENTARIES_COUNT));
            int isLikedByMeInt = getInt(getColumnIndex(DoingTable.Cols.LIKED_BY_ME));
            int hasNewLikesInt = getInt(getColumnIndex(DoingTable.Cols.HAS_NEW_LIKES));
            int hasNewCommentariesInt = getInt(getColumnIndex(DoingTable.Cols.HAS_NEW_COMMENTARIES));

            UUID doingId = UUID.fromString(doingIdString);
            UUID userId = UUID.fromString(userIdString);
            DoingAction action = DoingAction.valueOf(actionString);
            Date date = new Date(dateLong);
            Boolean isLikedByMe = (isLikedByMeInt == 1);
            Boolean hasNewLikes = (hasNewLikesInt == 1);
            Boolean hasNewCommentaries = (hasNewCommentariesInt == 1);

            UserDatabase userDatabase = UserDatabase.get(mContext);
            User user = userDatabase.getUser(userId);

            Doing doing = DoingFactory.get(mContext).getDoing(doingId, user, action, text, date,
                    nLikes, nCommentaries, isLikedByMe);
            doing.setHasNewLikes(hasNewLikes);
            doing.setHasNewCommentaries(hasNewCommentaries);

            return doing;
        }
    }
}
