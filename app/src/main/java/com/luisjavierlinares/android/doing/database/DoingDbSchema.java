package com.luisjavierlinares.android.doing.database;

/**
 * Created by Luis on 31/03/2017.
 */

public class DoingDbSchema {
    public static final class DoingTable {
        public static final String NAME = "doings";

        public static final class Cols {
            public static final String DOING_ID = "doing_id";
            public static final String USER_ID = "user_id";
            public static final String ACTION = "action";
            public static final String TEXT = "text";
            public static final String DATE = "date";
            public static final String LIKES_COUNT = "likes_count";
            public static final String COMMENTARIES_COUNT = "commentaries_count";
            public static final String LIKED_BY_ME = "liked_by_me";
            public static final String HAS_NEW_LIKES = "has_new_likes";
            public static final String HAS_NEW_COMMENTARIES = "has_new_commentaries";
        }
    }
}
