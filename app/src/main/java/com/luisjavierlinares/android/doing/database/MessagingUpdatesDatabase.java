package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.messaging.MessagingUpdate;
import com.luisjavierlinares.android.doing.messaging.MessagingUpdate.UpdateType;

import static com.luisjavierlinares.android.doing.database.MessagingUpdatesDbSchema.*;

/**
 * Created by Luis on 15/05/2017.
 */

public class MessagingUpdatesDatabase {

    private static MessagingUpdatesDatabase sMessagingDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized MessagingUpdatesDatabase get(Context context){
        if(sMessagingDatabase == null) {
            sMessagingDatabase = new MessagingUpdatesDatabase(context);
        }
        return sMessagingDatabase;
    }

    private MessagingUpdatesDatabase(Context context) {
        mDatabase = MessagingUpdatesBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private MessagingUpdatesCursorWrapper queryLikes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                MessagingUpdatesTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new MessagingUpdatesCursorWrapper(cursor);
    }

    public void add(MessagingUpdate messagingUpdate) {
        ContentValues values = getContentValues(messagingUpdate);
        mDatabase.insert(MessagingUpdatesTable.NAME, null, values);
    }

    public MessagingUpdate get(String id) {

        if (id == null) {
            return null;
        }

        MessagingUpdatesCursorWrapper cursor = queryLikes(
                MessagingUpdatesTable.Cols.ID + " = ?",
                new String[] {id}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getMessagingUpdate();
        } finally {
            cursor.close();
        }
    }

    public void remove(MessagingUpdate messagingUpdate) {
        String uuidString = messagingUpdate.getId();

        mDatabase.delete(MessagingUpdatesTable.NAME, MessagingUpdatesTable.Cols.ID + " = ?", new String[] {uuidString});
    }

    public void update(MessagingUpdate messagingUpdate) {
        String uuidString = messagingUpdate.getId();
        ContentValues values = getContentValues(messagingUpdate);

        mDatabase.update(MessagingUpdatesTable.NAME, values, MessagingUpdatesTable.Cols.ID + " = ?", new String[] { uuidString });
    }

    private static ContentValues getContentValues(MessagingUpdate messagingUpdate) {
        ContentValues values = new ContentValues();

        values.put(MessagingUpdatesTable.Cols.ID, messagingUpdate.getId());
        values.put(MessagingUpdatesTable.Cols.TYPE, messagingUpdate.getType().name());
        values.put(MessagingUpdatesTable.Cols.LAST_UPDATE, messagingUpdate.getLastUpdate());

        return values;
    }

    public class MessagingUpdatesCursorWrapper extends CursorWrapper {

        public MessagingUpdatesCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public MessagingUpdate getMessagingUpdate() {
            String idString = getString(getColumnIndex(MessagingUpdatesTable.Cols.ID));
            String typeString = getString(getColumnIndex(MessagingUpdatesTable.Cols.TYPE));
            Long lastUpdate = getLong(getColumnIndex(MessagingUpdatesTable.Cols.LAST_UPDATE));

            UpdateType updateType = UpdateType.valueOf(typeString);

            MessagingUpdate messagingUpdate = new MessagingUpdate(idString, updateType, lastUpdate);

            return messagingUpdate;
        }
    }

}
