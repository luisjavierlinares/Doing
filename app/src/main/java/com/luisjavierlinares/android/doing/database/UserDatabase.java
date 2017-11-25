package com.luisjavierlinares.android.doing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.database.UserDbSchema.UserTable;
import static com.luisjavierlinares.android.doing.model.User.UserState;

/**
 * Created by Luis on 02/04/2017.
 */

public class UserDatabase {

    private static UserDatabase sUserDatabase;
    private SQLiteDatabase mDatabase;

    private Context mContext;

    public static synchronized UserDatabase get(Context context){
        if(sUserDatabase == null) {
            sUserDatabase = new UserDatabase(context);
        }
        return sUserDatabase;
    }

    private UserDatabase(Context context) {
        mDatabase = UserBaseHelper.get(context).getWritableDatabase();
        mContext = context;
    }

    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                UserTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new UserCursorWrapper(cursor);
    }

    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                UserTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );

        return new UserCursorWrapper(cursor);
    }

    public void add(User user) {
        ContentValues values = getContentValues(user);
        mDatabase.insert(UserTable.NAME, null, values);
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        UserCursorWrapper cursor = queryUsers(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                users.add(cursor.getUser());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return users;
    }

    public List<User> getUsersOrderByName() {
        List<User> users = new ArrayList<>();
        String orderBy = UserTable.Cols.NAME + " ASC";

        UserCursorWrapper cursor = queryUsers(null, null, orderBy);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                users.add(cursor.getUser());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return users;
    }

    public User getUser(UUID id) {
        if (id == null) {
            return null;
        }

        UserCursorWrapper cursor = queryUsers(
                UserTable.Cols.USER_ID + " = ?",
                new String[] {id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getUser();
        } finally {
            cursor.close();
        }
    }

    public User getUserWithCode(String userCode) {
        if (userCode == null) {
            return null;
        }

        UserCursorWrapper cursor = queryUsers(
                UserTable.Cols.USER_CODE + " = ?",
                new String[] {userCode}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getUser();
        } finally {
            cursor.close();
        }
    }

    public User getUserWithFriendCode(String friendCode) {
        if (friendCode == null) {
            return null;
        }

        UserCursorWrapper cursor = queryUsers(
                UserTable.Cols.FRIEND_CODE + " = ?",
                new String[] {friendCode}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getUser();
        } finally {
            cursor.close();
        }
    }

    public void remove(User user) {
        String uuidString = user.getId().toString();

        mDatabase.delete(UserTable.NAME, UserTable.Cols.USER_ID + " = ?", new String[] {uuidString});
    }

    public void update(User user) {
        String uuidString = user.getId().toString();

        if (uuidString == null) {
            return;
        }

        ContentValues values = getContentValues(user);

        mDatabase.update(UserTable.NAME, values, UserTable.Cols.USER_ID + " = ?", new String[] { uuidString });
    }

    private static ContentValues getContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(UserTable.Cols.USER_ID, user.getId().toString());
        values.put(UserTable.Cols.NAME, user.getName());
        values.put(UserTable.Cols.USER_CODE, user.getUserCode());
        values.put(UserTable.Cols.FRIEND_NAME, user.getFriendName());
        values.put(UserTable.Cols.FRIEND_CODE, user.getFriendCode());
        values.put(UserTable.Cols.STATUS, user.getState().name());
        values.put(UserTable.Cols.CREATION_DATE, user.getCreationDate().getTime());
        values.put(UserTable.Cols.LAST_UPDATE, user.getLastUpdate().getTime());

        return values;
    }

    public class UserCursorWrapper extends CursorWrapper {

        public UserCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public User getUser() {
            String userIdString = getString(getColumnIndex(UserTable.Cols.USER_ID));
            String name = getString(getColumnIndex(UserTable.Cols.NAME));
            String userCode = getString(getColumnIndex(UserTable.Cols.USER_CODE));
            String friendName = getString(getColumnIndex(UserTable.Cols.FRIEND_NAME));
            String friendCode = getString(getColumnIndex(UserTable.Cols.FRIEND_CODE));
            String stateString = getString(getColumnIndex(UserTable.Cols.STATUS));
            Long creationDateLong = getLong(getColumnIndex(UserTable.Cols.CREATION_DATE));
            Long lastUpdateLong = getLong(getColumnIndex(UserTable.Cols.LAST_UPDATE));

            UUID userId = UUID.fromString(userIdString);
            UserState state = UserState.valueOf(stateString);
            Date creationDate = new Date(creationDateLong);
            Date lastUpdate = new Date(lastUpdateLong);

            User user = UserFactory.get(mContext).getUser(userId, name, state);
            user.setUserCode(userCode);
            user.setFriendName(friendName);
            user.setFriendCode(friendCode);
            user.setCreationDate(creationDate);
            user.setLastUpdate(lastUpdate);

            return user;
        }
    }
}
