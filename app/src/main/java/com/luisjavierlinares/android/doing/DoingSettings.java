package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.luisjavierlinares.android.doing.model.UserDAO;
import com.luisjavierlinares.android.doing.model.UserFactory;

import java.util.UUID;

import static android.content.SharedPreferences.Editor;
import static com.luisjavierlinares.android.doing.model.UserImpl.UserState;

/**
 * Created by Luis on 29/03/2017.
 */

public class DoingSettings {

    private static final String PREF_IS_FIRST_TIME = "isFirstTime";
    private static final String PREF_UNIQUE_ID = "PREF_APP_UNIQUE_ID";
    private static final String PREF_FRIEND_CODE = "PREF_APP_FRIEND_CODE";
    private static final String PREF_ONLINE_ID = "PREF_APP_ONLINE_ID";
    private static final String PREF_ONLINE_SECRET = "PREF_APP_ONLINE_SECRET";
    private static final String PREF_ADS_DISABLED = "PREF_ADS_DISABLED";
    private static final String PREF_LAST_DOING_NOTIFICATION_COUNT = "lastDoingNotificationCount";
    private static final String PREF_LAST_COMMENTARY_NOTIFICATION_COUNT = "lastCommentaryNotificationCount";
    private static final String PREF_LAST_LIKE_NOTIFICATION_COUNT = "lastLikeNotificationCount";
    private static final String PREF_LAST_INVITATION_NOTIFICATION_COUNT = "lastInvitationsNotificationCount";
    private static final String PREF_IS_UPDATE_AND_NOTIFY_ON = "isUpdateAndNotifyOn";
    private static final String PREF_NOTIFICATIONS_ON = "PREF_NOTIFICATIONS_ON";
    private static final String PREF_AVATAR_UPLOAD_PENDING = "PREF_AVATAR_UPLOAD_PENDING";
    private static final String PREF_UPDATE_CHANNEL = "PREF_UPDATE_CHANNEL";
    private static final String PREF_LAST_LOCAL_DATA_BACKUP = "PREF_LAST_LOCAL_DATA_BACKUP";
    private static final String PREF_VIEWS_SINCE_LAST_AD = "PREF_VIEWS_SINCE_LAST_AD";
    private static final String PREF_TIME_SINCE_LAST_AD = "PREF_TIME_SINCE_LAST_AD";

    public static final int FRIEND_CODE_SIZE = 6;

    private static String uniqueID = null;


    public static boolean isFirstTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_FIRST_TIME, true);
    }

    public static void setFirstTime(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_FIRST_TIME, isOn)
                .apply();
    }

    public synchronized static UUID getMyId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                UserDAO usersCatalog = UserDAO.get(context);
                usersCatalog.addUser(UserFactory.get(context).getUser(UUID.fromString(uniqueID), null, UserState.ME));
                Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return UUID.fromString(uniqueID);
    }

    public synchronized static void setMyOnlineId(Context context, String onlineId) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_ONLINE_ID, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(PREF_ONLINE_ID, onlineId);
        editor.commit();
    }

    public synchronized static String getMyOnlineId(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_ONLINE_ID, Context.MODE_PRIVATE);
        return sharedPrefs.getString(PREF_ONLINE_ID, null);
    }

    public synchronized static void setMyOnlineSecret(Context context, String onlineSecret) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_ONLINE_SECRET, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(PREF_ONLINE_SECRET, onlineSecret);
        editor.commit();
    }

    public synchronized static String getMyOnlineSecret(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_ONLINE_SECRET, Context.MODE_PRIVATE);
        return sharedPrefs.getString(PREF_ONLINE_SECRET, null);
    }

    public static Boolean areAdsPermanentlyDisabled(Context context) {
        return context.getSharedPreferences(PREF_ADS_DISABLED, Context.MODE_PRIVATE)
                .getBoolean(PREF_ADS_DISABLED, false);
    }

    public static void setAdsPermanentlyDisabled(Context context, Boolean areDisabled) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_ADS_DISABLED, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(PREF_ADS_DISABLED, areDisabled);
        editor.commit();
    }

    public static int getLastDoingNotificationCount(Context context) {
        return context.getSharedPreferences(PREF_LAST_DOING_NOTIFICATION_COUNT, Context.MODE_PRIVATE)
                .getInt(PREF_LAST_DOING_NOTIFICATION_COUNT, 0);
    }

    public static void setLastDoingNotificationCount(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_LAST_DOING_NOTIFICATION_COUNT, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_LAST_DOING_NOTIFICATION_COUNT, lastCount);
        editor.commit();
    }

    public static int getLastLikeNotificationCount(Context context) {
        return context.getSharedPreferences(PREF_LAST_LIKE_NOTIFICATION_COUNT, Context.MODE_PRIVATE)
                .getInt(PREF_LAST_LIKE_NOTIFICATION_COUNT, 0);
    }

    public static void setLastLikeNotificationCount(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_LAST_LIKE_NOTIFICATION_COUNT, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_LAST_LIKE_NOTIFICATION_COUNT, lastCount);
        editor.commit();
    }

    public static int getLastCommentaryNotificationCount(Context context) {
        return context.getSharedPreferences(PREF_LAST_COMMENTARY_NOTIFICATION_COUNT, Context.MODE_PRIVATE)
                .getInt(PREF_LAST_COMMENTARY_NOTIFICATION_COUNT, 0);
    }

    public static void setLastCommentaryNotificationCount(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_LAST_COMMENTARY_NOTIFICATION_COUNT, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_LAST_COMMENTARY_NOTIFICATION_COUNT, lastCount);
        editor.commit();
    }

    public static int getLastInvitationNotificationCount(Context context) {
        return context.getSharedPreferences(PREF_LAST_INVITATION_NOTIFICATION_COUNT, Context.MODE_PRIVATE)
                .getInt(PREF_LAST_INVITATION_NOTIFICATION_COUNT, 0);
    }

    public static void setLastInvitationNotificationCount(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_LAST_INVITATION_NOTIFICATION_COUNT, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_LAST_INVITATION_NOTIFICATION_COUNT, lastCount);
        editor.commit();
    }

    public static boolean isAvatarUploadPending(Context context) {
        return context.getSharedPreferences(PREF_AVATAR_UPLOAD_PENDING, Context.MODE_PRIVATE)
                .getBoolean(PREF_AVATAR_UPLOAD_PENDING, false);
    }

    public static void setAvatarUploadPending(Context context, boolean isPending) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_AVATAR_UPLOAD_PENDING, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(PREF_AVATAR_UPLOAD_PENDING, isPending);
        editor.commit();
    }

    public static boolean areNotificationsOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_NOTIFICATIONS_ON, true);
    }

    public static void setNotificationOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_NOTIFICATIONS_ON, isOn)
                .apply();
    }

    public static int getUpdateChannel(Context context) {
        return context.getSharedPreferences(PREF_UPDATE_CHANNEL, Context.MODE_PRIVATE)
                .getInt(PREF_UPDATE_CHANNEL, 0);
    }

    public static void setUpdateChannel(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_UPDATE_CHANNEL, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_UPDATE_CHANNEL, lastCount);
        editor.commit();
    }

    public static long getLastLocalDataBackup(Context context) {
        return context.getSharedPreferences(PREF_LAST_LOCAL_DATA_BACKUP, Context.MODE_PRIVATE)
                .getLong(PREF_LAST_LOCAL_DATA_BACKUP, 0);
    }

    public static void setLastLocalDataBackup(Context context, long lastLocalDataBackup) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_LAST_LOCAL_DATA_BACKUP, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putLong(PREF_LAST_LOCAL_DATA_BACKUP, lastLocalDataBackup);
        editor.commit();
    }

    public static int getViewsSinceLastAd(Context context) {
        return context.getSharedPreferences(PREF_VIEWS_SINCE_LAST_AD, Context.MODE_PRIVATE)
                .getInt(PREF_VIEWS_SINCE_LAST_AD, 0);
    }

    public static void setViewsSinceLastAd(Context context, int lastCount) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_VIEWS_SINCE_LAST_AD, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putInt(PREF_VIEWS_SINCE_LAST_AD, lastCount);
        editor.commit();
    }

    public static long getTimeLastAd(Context context) {
        return context.getSharedPreferences(PREF_TIME_SINCE_LAST_AD, Context.MODE_PRIVATE)
                .getLong(PREF_TIME_SINCE_LAST_AD, 0);
    }

    public static void setTimeLastAd(Context context, long timeSinceLastAd) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_TIME_SINCE_LAST_AD, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putLong(PREF_TIME_SINCE_LAST_AD, timeSinceLastAd);
        editor.commit();
    }

}
