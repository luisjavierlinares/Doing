package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.luisjavierlinares.android.doing.database.DoingDbSchema.DoingTable;

/**
 * Created by Luis on 31/03/2017.
 */

public class DoingBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "doingBase.db";

    private static DoingBaseHelper sDoingBaseHelper;

    public static synchronized DoingBaseHelper get(Context context) {
        if(sDoingBaseHelper == null) {
            sDoingBaseHelper = new DoingBaseHelper(context);
        }
        return sDoingBaseHelper;
    }

    private DoingBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                DoingTable.NAME + "(" + " _id integer primary key autoincrement, " +
                DoingTable.Cols.DOING_ID + ", " +
                DoingTable.Cols.USER_ID + ", " +
                DoingTable.Cols.ACTION + ", " +
                DoingTable.Cols.TEXT + ", " +
                DoingTable.Cols.DATE + ", " +
                DoingTable.Cols.LIKES_COUNT + ", " +
                DoingTable.Cols.COMMENTARIES_COUNT + ", " +
                DoingTable.Cols.LIKED_BY_ME + ", " +
                DoingTable.Cols.HAS_NEW_LIKES + ", " +
                DoingTable.Cols.HAS_NEW_COMMENTARIES + ", " +
                "unique(" + DoingTable.Cols.DOING_ID + ") ON CONFLICT replace" +
                ")"
        );
        db.execSQL("create index doing_user_id_idx ON " + DoingTable.NAME + "(" + DoingTable.Cols.USER_ID + ")");
        db.execSQL("create index doing_date_idx ON " + DoingTable.NAME + "(" + DoingTable.Cols.DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
