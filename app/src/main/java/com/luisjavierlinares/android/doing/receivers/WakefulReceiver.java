package com.luisjavierlinares.android.doing.receivers;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.luisjavierlinares.android.doing.services.UpdateAndNotifyJob;
import com.luisjavierlinares.android.doing.services.UpdateAndNotifyService;

/**
 * Created by Luis on 25/09/2017.
 */

public class WakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, UpdateAndNotifyWakefulService.class);
        startWakefulService(context, service);
    }

    class UpdateAndNotifyWakefulService extends IntentService {

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */
        public UpdateAndNotifyWakefulService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Context context = getApplicationContext();
            UpdateAndNotifyService.start(context);
            completeWakefulIntent(intent);
        }
    }
}
