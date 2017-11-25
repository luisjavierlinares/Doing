package com.luisjavierlinares.android.doing.services;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.luisjavierlinares.android.doing.DoingActivity;
import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.R;
import com.luisjavierlinares.android.doing.messaging.MessagingSystem;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserDAO;

import java.util.Calendar;

/**
 * Created by Luis on 26/09/2017.
 */

public class DoingUpdater {

    private static final long SYNC_PERSISTENCE_TIME = 4 * 1000;
    private static final long SYNCHRONIZATION_TIME = 4 * 1000;

    public static final String ACTION_SHOW_NOTIFICATON = "com.luisjavierlinares.android.doing.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.luisjavierlinares.android.doing.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    private static final int REQUEST_NOTIFICATION = 1;
    private static final int REQUEST_INVITATION = 2;

    private static long last_update = 0;

    private Context mContext;

    public DoingUpdater(Context context) {
        mContext = context;
    }


    public void update() {

        if (isUpdateInProcess()) {
            return;
        }

        User myUser = UserDAO.get(mContext).getMyUser();
        String myUserCode = myUser.getUserCode();
        if (myUserCode == null) {
            return;
        }

        int lastDoingCount = DoingSettings.getLastDoingNotificationCount(mContext);
        int lastLikeCount = DoingSettings.getLastLikeNotificationCount(mContext);
        int lastCommentaryCount = DoingSettings.getLastCommentaryNotificationCount(mContext);
        int lastInvitationCount = DoingSettings.getLastInvitationNotificationCount(mContext);

        MessagingSystem messagingSystem = MessagingSystem.get(mContext);

        // Start synchronisation with messagingSystem
        messagingSystem.goOnlineQuickly();
        messagingSystem.setSynced(myUser, true);

        // Wait for synchronization
        try {
            Thread.sleep(SYNC_PERSISTENCE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Attempt to sync
        messagingSystem.listenOnceForDoingsReceived(myUser);
        messagingSystem.listenOnceForLikesReceived(myUser);
        messagingSystem.listenOnceForCommentariesReceived(myUser);

        // Wait for synchronization
        try {
            Thread.sleep(SYNCHRONIZATION_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!messagingSystem.isForeground()) {
            // Stop synchronisation with messagingSystem
            messagingSystem.goOffline();
        }

        Boolean notificationsAreOn = DoingSettings.areNotificationsOn(mContext);

        int newDoingCount = DoingSettings.getLastDoingNotificationCount(mContext);
        int newLikeCount  = DoingSettings.getLastLikeNotificationCount(mContext);
        int newCommentaryCount = DoingSettings.getLastCommentaryNotificationCount(mContext);
        int newInvitationCount = DoingSettings.getLastInvitationNotificationCount(mContext);

        Boolean doingBool, likeBool, commentaryBool;

        // Notify new doings
        if ( notificationsAreOn && (newDoingCount > lastDoingCount)) {
            doingBool = true;
        } else {
            doingBool = false;
        }

        // Notify new likes
        if ( notificationsAreOn && (newLikeCount > lastLikeCount)) {
            likeBool = true;
        } else {
            likeBool = false;
        }

        // Notify new commentaries
        if ( notificationsAreOn && (newCommentaryCount > lastCommentaryCount)) {
            commentaryBool = true;
        } else {
            commentaryBool = false;
        }

        // If there are new doings, likes or commentaries and notifications are enabled send notifications
        if (doingBool || likeBool || commentaryBool) {
            sendNotification(mContext, doingBool, likeBool, commentaryBool, newDoingCount,
                    newLikeCount, newCommentaryCount);
        }

        // If there are new friend requests and notifications are enabled send notification
        if ( notificationsAreOn && (newInvitationCount > lastInvitationCount)) {
            sendInvitationNotification(mContext, newInvitationCount);
        }
    }

    public void cancel() {
        MessagingSystem messagingSystem = MessagingSystem.get(mContext);
        if (!messagingSystem.isForeground()) {
            // Stop synchronisation with messagingSystem
            messagingSystem.goOffline();
        }
    }

    private boolean isUpdateInProcess() {
        if (last_update == 0) {
            last_update = Calendar.getInstance().getTimeInMillis();
            return false;
        }

       long actualTime = Calendar.getInstance().getTimeInMillis();
       long millisDiff = actualTime - last_update;
       long processTime = (SYNC_PERSISTENCE_TIME + SYNCHRONIZATION_TIME) * 2;

       if (millisDiff < processTime) {
           return true;
       } else {
           last_update = Calendar.getInstance().getTimeInMillis();
           return false;
       }
    }

    private void sendNotification(Context context, boolean doingBool, boolean likeBool, boolean commentaryBool,
                                 int doingCount, int likeCount, int commentaryCount) {

        Notification notification = newNotification(context, doingBool,
                likeBool, commentaryBool, doingCount, commentaryCount, likeCount);

        if (notification != null) {
            if (doingBool) {
                DoingSettings.setLastDoingNotificationCount(context, doingCount);
            }

            if (likeBool) {
                DoingSettings.setLastLikeNotificationCount(context, likeCount);
            }

            if (commentaryBool) {
                DoingSettings.setLastCommentaryNotificationCount(context, commentaryCount);
            }

            showBackgroundNotification(context, notification, REQUEST_NOTIFICATION);
        }
    }

    private Notification newNotification(Context context, boolean doingBool, boolean likeBool, boolean commentaryBool,
                                        Integer doingCount, Integer commentaryCount, Integer likeCount) {

        if (context == null) {
            return null;
        }

        Resources resources = context.getResources();
        Intent notificationIntent = DoingActivity.newIntent(context);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        String notificationTitle = resources.getString(R.string.new_data_notif_title);
        String notificationText = new String();

        if (doingBool || likeBool || commentaryBool) {
            notificationText = resources.getString(R.string.notification_doing_text_ini);
        }

        if (doingBool) {
            String notificationEndText = resources.getString(R.string.notification_doing_text_end);
            if (doingCount == 1) {
                notificationEndText = resources.getString(R.string.notification_doing_text_end_one);
            }

            notificationText = notificationText.concat(" ")
                    .concat(doingCount.toString())
                    .concat(" ")
                    .concat(notificationEndText);

            if (likeBool || commentaryBool) {
                notificationText = notificationText.concat(", ");
            } else {
                notificationText = notificationText.concat(".");
            }
        }

        if (likeBool) {
            String notificationEndText = resources.getString(R.string.notification_like_text_end);
            if (likeCount == 1) {
                notificationEndText = resources.getString(R.string.notification_like_text_end_one);
            }
            notificationText = notificationText.concat(" ")
                    .concat(likeCount.toString())
                    .concat(" ")
                    .concat(notificationEndText);

            if (commentaryBool) {
                notificationText = notificationText.concat(", ");
            } else {
                notificationText = notificationText.concat(".");
            }
        }

        if (commentaryBool) {
            String notificationEndText = resources.getString(R.string.notification_commentary_text_end);
            if (commentaryCount == 1) {
                notificationEndText = resources.getString(R.string.notification_commentary_text_end_one);
            }
            notificationText = notificationText.concat(" ")
                    .concat(commentaryCount.toString())
                    .concat(" ")
                    .concat(notificationEndText)
                    .concat(".");
        }

        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(notificationTitle)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_doing)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true)
                .setNumber(1)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        return notification;
    }

    private void sendInvitationNotification(Context context, int invitationCount) {
        Notification notification = newInvitationNotification(context);

        if (notification != null) {
            DoingSettings.setLastCommentaryNotificationCount(context, invitationCount);
            showBackgroundNotification(context, notification, REQUEST_INVITATION);
        }
    }

    private Notification newInvitationNotification(Context context) {
        Resources resources = context.getResources();
        Intent notificationIntent = DoingActivity.newIntent(context);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        String notificationTitle = resources.getString(R.string.invitation_request_notif_title);
        String notificationText = resources.getString(R.string.invitation_request_notif_text);

        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(notificationTitle)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_doing)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true)
                .setNumber(1)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        return notification;
    }

    private void showBackgroundNotification(Context context, Notification notification, int requestCode) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(ACTION_SHOW_NOTIFICATON);
        intent.putExtra(REQUEST_CODE, requestCode);
        intent.putExtra(NOTIFICATION, notification);
        context.sendOrderedBroadcast(intent, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    public static void clearAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

}
