package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.database.DoingReceiversSchema.DoingReceiversTable;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 24/06/2017.
 */

public class DoingReceiversDatabase {

    private static DoingReceiversDatabase sDoingReceiversDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized DoingReceiversDatabase get(Context context){
        if(sDoingReceiversDatabase == null) {
            sDoingReceiversDatabase = new DoingReceiversDatabase(context);
        }
        return sDoingReceiversDatabase;
    }

    private DoingReceiversDatabase(Context context) {
        mDatabase = DoingReceiversBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private DoingReceiversCursorWrapper queryReceivers(String whereClause, String[] whereArgs) {
        return queryReceivers(whereClause, whereArgs, null);
    }

    private DoingReceiversCursorWrapper queryReceivers(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                DoingReceiversTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );

        return new DoingReceiversCursorWrapper(cursor);
    }

    public void add(Doing doing, User user) {
        ContentValues values = getContentValues(doing, user);
        mDatabase.insert(DoingReceiversTable.NAME, null, values);
    }

    public List<User> getReceiversFromDoing(UUID doingId){
        List<User> receivers = new ArrayList<>();

        DoingReceiversCursorWrapper cursor = queryReceivers(
                DoingReceiversTable.Cols.DOING_ID + " = ?",
                new String[] {doingId.toString()});

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                receivers.add(cursor.getReceiver());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return receivers;
    }

    public List<User> getReceivers(int sizeLimit){
        List<User> receivers = new ArrayList<>();
        String orderBy = " _id DESC LIMIT " + sizeLimit;

        DoingReceiversCursorWrapper cursor = queryReceivers(null, null, orderBy);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                receivers.add(cursor.getReceiver());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return receivers;
    }

    private static ContentValues getContentValues(Doing doing, User user) {
        ContentValues values = new ContentValues();
        values.put(DoingReceiversTable.Cols.DOING_ID, doing.getId().toString());
        values.put(DoingReceiversTable.Cols.RECEIVER_CODE, user.getUserCode().toString());
        values.put(DoingReceiversTable.Cols.RECEIVER_FRIENDCODE, user.getFriendCode());
        values.put(DoingReceiversTable.Cols.RECEIVER_NAME, user.getFriendName());

        return values;
    }

    public class DoingReceiversCursorWrapper extends CursorWrapper {

        public DoingReceiversCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public User getReceiver() {
            String receiverCode = getString(getColumnIndex(DoingReceiversTable.Cols.RECEIVER_CODE));
            String receiverFriendCode = getString(getColumnIndex(DoingReceiversTable.Cols.RECEIVER_FRIENDCODE));
            String receiverName = getString(getColumnIndex(DoingReceiversTable.Cols.RECEIVER_NAME));

            User receiver = UserDAO.get(mContext).getUserWithFriendCode(receiverFriendCode);

            if (receiver.isUnknown()) {
                receiver.setAsFriendOfAFriend();
                receiver.setUserCode(receiverCode);
                receiver.setFriendCode(receiverFriendCode);
                receiver.setFriendName(receiverName);
                receiver.setName(receiverName);
            }

            return receiver;
        }
    }

}
