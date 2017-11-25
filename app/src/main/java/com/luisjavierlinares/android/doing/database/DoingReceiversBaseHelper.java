package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.luisjavierlinares.android.doing.database.DoingReceiversSchema.DoingReceiversTable;

/**
 * Created by Luis on 24/06/2017.
 */

public class DoingReceiversBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "doingReceiversBase.db";

    private static DoingReceiversBaseHelper sDoingReceiversBaseHelper;

    public static synchronized DoingReceiversBaseHelper get(Context context) {
        if(sDoingReceiversBaseHelper == null) {
            sDoingReceiversBaseHelper = new DoingReceiversBaseHelper(context);
        }
        return sDoingReceiversBaseHelper;
    }

    private DoingReceiversBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                DoingReceiversTable.NAME + "(" + " _id integer primary key autoincrement, " +
                DoingReceiversTable.Cols.DOING_ID + ", " +
                DoingReceiversTable.Cols.RECEIVER_CODE + ", " +
                DoingReceiversTable.Cols.RECEIVER_FRIENDCODE + ", " +
                DoingReceiversTable.Cols.RECEIVER_NAME + ", " +
                "unique(" + DoingReceiversTable.Cols.DOING_ID + ", " + DoingReceiversTable.Cols.RECEIVER_CODE + ") ON CONFLICT replace" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
