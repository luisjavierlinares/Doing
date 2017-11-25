package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.luisjavierlinares.android.doing.database.MessagingUpdatesDbSchema.*;

/**
 * Created by Luis on 15/05/2017.
 */

public class MessagingUpdatesBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "messagingUpdatesBase.db";

    private static MessagingUpdatesBaseHelper sMessagingBaseHelper;

    public static synchronized MessagingUpdatesBaseHelper get(Context context) {
        if(sMessagingBaseHelper == null) {
            sMessagingBaseHelper = new MessagingUpdatesBaseHelper(context);
        }
        return sMessagingBaseHelper;
    }

    private MessagingUpdatesBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                MessagingUpdatesTable.NAME + "(" + " _id integer primary key autoincrement, " +
                MessagingUpdatesTable.Cols.ID + ", " +
                MessagingUpdatesTable.Cols.TYPE + ", " +
                MessagingUpdatesTable.Cols.LAST_UPDATE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
