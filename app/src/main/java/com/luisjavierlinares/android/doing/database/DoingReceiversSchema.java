package com.luisjavierlinares.android.doing.database;

/**
 * Created by Luis on 24/06/2017.
 */

public class DoingReceiversSchema {
    public static final class DoingReceiversTable {
        public static final String NAME = "doing_receivers";

        public static final class Cols {
            public static final String DOING_ID = "doing_id";
            public static final String RECEIVER_CODE = "receiver_code";
            public static final String RECEIVER_FRIENDCODE = "receiver_friendcode";
            public static final String RECEIVER_NAME ="receiver_name";
        }
    }
}
