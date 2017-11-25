package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.luisjavierlinares.android.doing.database.LikeDbSchema.LikeTable;

/**
 * Created by Luis on 31/03/2017.
 */

public class LikeBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "likeBase.db";

    private static LikeBaseHelper sLikeBaseHelper;

    public static synchronized LikeBaseHelper get(Context context) {
        if(sLikeBaseHelper == null) {
            sLikeBaseHelper = new LikeBaseHelper(context);
        }
        return sLikeBaseHelper;
    }

    private LikeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                LikeTable.NAME + "(" + " _id integer primary key autoincrement, " +
                LikeTable.Cols.LIKE_ID + ", " +
                LikeTable.Cols.SENDER_ID + ", " +
                LikeTable.Cols.SENDER_FRIENDNAME + ", " +
                LikeTable.Cols.SENDER_CODE + ", " +
                LikeTable.Cols.SENDER_FRIENDCODE + ", " +
                LikeTable.Cols.DOING_ID + ", " +
                LikeTable.Cols.TYPE + ", " +
                LikeTable.Cols.DATE + ", " +
                "unique(" + LikeTable.Cols.LIKE_ID + ") ON CONFLICT replace" +
                ")"
        );
        db.execSQL("create index like_doing_id_idx ON " + LikeTable.NAME + "(" + LikeTable.Cols.DOING_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

