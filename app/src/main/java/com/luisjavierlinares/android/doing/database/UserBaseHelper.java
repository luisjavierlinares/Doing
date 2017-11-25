package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.luisjavierlinares.android.doing.database.UserDbSchema.UserTable;

/**
 * Created by Luis on 31/03/2017.
 */

public class UserBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userBase.db";

    private static UserBaseHelper sUserBaseHelper;

    public static synchronized UserBaseHelper get(Context context) {
        if(sUserBaseHelper == null) {
            sUserBaseHelper = new UserBaseHelper(context);
        }
        return sUserBaseHelper;
    }

    private UserBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                UserTable.NAME + "(" + " _id integer primary key autoincrement, " +
                UserTable.Cols.USER_ID + ", " +
                UserTable.Cols.NAME + ", " +
                UserTable.Cols.USER_CODE + ", " +
                UserTable.Cols.FRIEND_NAME + ", " +
                UserTable.Cols.FRIEND_CODE + ", " +
                UserTable.Cols.STATUS + ", " +
                UserTable.Cols.CREATION_DATE + ", " +
                UserTable.Cols.LAST_UPDATE + ", " +
                "unique(" + UserTable.Cols.USER_CODE + ") ON CONFLICT replace" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
