package com.luisjavierlinares.android.doing.services;

import android.content.Context;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessaging;
import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.utils.RandomUtils;

/**
 * Created by Luis on 27/09/2017.
 */

public class UpdateAndNotifyService {

    public static final String UPDATE_EVENT = "updateEvent";

    public static void start(Context context) {
        // We use a high priority FCM message to start the updating process
        // (jobs with net requirements do not work well with Doze and App Standby Mode)
            int updateChannel = DoingSettings.getUpdateChannel(context);
            if ((updateChannel < 1) || (updateChannel > 15)) {
                updateChannel =  RandomUtils.getRandomNumberBetween(1, 15);
                DoingSettings.setUpdateChannel(context, updateChannel);
            }

            String topic = UPDATE_EVENT.concat(String.valueOf(updateChannel));
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    public static void clearAllNotifications(Context context) {
        DoingUpdater doingUpdater = new DoingUpdater(context);
        doingUpdater.clearAllNotifications(context);
    }
}
