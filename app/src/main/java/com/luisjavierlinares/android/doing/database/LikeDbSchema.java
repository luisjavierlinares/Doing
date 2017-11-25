package com.luisjavierlinares.android.doing.database;

/**
 * Created by Luis on 31/03/2017.
 */

public class LikeDbSchema {
    public static final class LikeTable {
        public static final String NAME = "likes";

        public static final class Cols {
            public static final String LIKE_ID = "like_id";
            public static final String SENDER_ID = "sender_id";
            public static final String SENDER_CODE = "sender_code";
            public static final String SENDER_FRIENDNAME ="sender_friendname";
            public static final String SENDER_FRIENDCODE ="sender_friendcode";
            public static final String DOING_ID = "doing_id";
            public static final String TYPE = "type";
            public static final String DATE = "date";
        }
    }
}
