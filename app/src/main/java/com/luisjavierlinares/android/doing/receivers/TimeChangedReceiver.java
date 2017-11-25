package com.luisjavierlinares.android.doing.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.luisjavierlinares.android.doing.messaging.MessagingSystem;

/**
 * Created by Luis on 27/05/2017.
 */

public class TimeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MessagingSystem.get(context).reset();
    }
}
