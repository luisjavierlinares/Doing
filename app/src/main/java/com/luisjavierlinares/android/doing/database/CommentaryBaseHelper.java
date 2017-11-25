package com.luisjavierlinares.android.doing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.luisjavierlinares.android.doing.database.CommentaryDbSchema.CommentaryTable;

/**
 * Created by Luis on 09/04/2017.
 */

public class CommentaryBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "commentaryBase.db";

    private static CommentaryBaseHelper sCommentaryBaseHelper;

    public static synchronized CommentaryBaseHelper get(Context context) {
        if(sCommentaryBaseHelper == null) {
            sCommentaryBaseHelper = new CommentaryBaseHelper(context);
        }
        return sCommentaryBaseHelper;
    }

    private CommentaryBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +
                CommentaryTable.NAME + "(" + " _id integer primary key autoincrement, " +
                CommentaryTable.Cols.COMMENTARY_ID + ", " +
                CommentaryTable.Cols.SENDER_ID + ", " +
                CommentaryTable.Cols.SENDER_CODE + ", " +
                CommentaryTable.Cols.SENDER_FRIENDNAME + ", " +
                CommentaryTable.Cols.SENDER_FRIENDCODE + ", " +
                CommentaryTable.Cols.DOING_ID + ", " +
                CommentaryTable.Cols.TEXT + ", " +
                CommentaryTable.Cols.DATE + ", " +
                "unique(" + CommentaryTable.Cols.COMMENTARY_ID + ") ON CONFLICT replace" +
                ")"
        );
        db.execSQL("create index commentary_doing_id_idx ON " + CommentaryTable.NAME + "(" + CommentaryTable.Cols.DOING_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
