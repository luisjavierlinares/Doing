package com.luisjavierlinares.android.doing.receivers;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.luisjavierlinares.android.doing.services.DoingUpdater;

/**
 * Created by Luis on 12/03/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        int requestCode = intent.getIntExtra(DoingUpdater.REQUEST_CODE, 0);
        Notification notification = (Notification) intent.getParcelableExtra(DoingUpdater.NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);
    }
}
