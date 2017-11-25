package com.luisjavierlinares.android.doing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.luisjavierlinares.android.doing.services.UpdateAndNotifyJob;
import com.luisjavierlinares.android.doing.services.UpdateAndNotifyService;

/**
 * Created by Luis on 21/09/2017.
 */

public class AppUpdatedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateAndNotifyService.start(context);
    }
}
