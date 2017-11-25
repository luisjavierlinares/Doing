package com.luisjavierlinares.android.doing.database;

/**
 * Created by Luis on 31/03/2017.
 */

public class UserDbSchema {
    public static final class UserTable {
        public static final String NAME = "users";

        public static final class Cols {
            public static final String USER_ID = "user_id";
            public static final String NAME = "name";
            public static final String USER_CODE = "user_code";
            public static final String FRIEND_NAME = "friend_name";
            public static final String FRIEND_CODE = "friend_code";
            public static final String STATUS = "status";
            public static final String CREATION_DATE = "creation_date";
            public static final String LAST_UPDATE = "last_update";
        }
    }
}
