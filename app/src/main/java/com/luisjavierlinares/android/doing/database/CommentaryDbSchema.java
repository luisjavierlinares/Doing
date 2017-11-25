package com.luisjavierlinares.android.doing.database;

/**
 * Created by Luis on 09/04/2017.
 */

public class CommentaryDbSchema {
    public static final class CommentaryTable {
        public static final String NAME = "commentaries";

        public static final class Cols {
            public static final String COMMENTARY_ID = "commentary_id";
            public static final String SENDER_ID = "sender_id";
            public static final String SENDER_CODE = "sender_code";
            public static final String SENDER_FRIENDNAME ="sender_friendname";
            public static final String SENDER_FRIENDCODE ="sender_friendcode";
            public static final String DOING_ID = "doing_id";
            public static final String TEXT = "text";
            public static final String DATE = "date";
        }
    }
}
