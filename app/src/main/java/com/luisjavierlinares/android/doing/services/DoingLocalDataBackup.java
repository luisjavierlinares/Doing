package com.luisjavierlinares.android.doing.services;

import android.content.Context;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.messaging.MessagingSystem;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserDAO;

import java.util.Calendar;

/**
 * Created by Luis on 04/10/2017.
 */

public class DoingLocalDataBackup {

    private static int PERIODIC_BACKUP_TIME = 4 * 60 * 60 * 1000;

    private Context mContext;

    public DoingLocalDataBackup(Context context) {
        mContext = context;
    }

    public void start() {
        if (!isTimeToBackup()) {
            return;
        }

        User myUser = UserDAO.get(mContext).getMyUser();
        MessagingSystem messagingSystem = MessagingSystem.get(mContext);
        messagingSystem.backupLocalData(myUser);

        updateLastBackupTime();
    }

    private boolean isTimeToBackup() {
        long lastUpdate = DoingSettings.getLastLocalDataBackup(mContext);
        long actualTime = Calendar.getInstance().getTimeInMillis();
        long millisDiff = actualTime - lastUpdate;

        if (millisDiff < PERIODIC_BACKUP_TIME) {
            return false;
        } else {
            return true;
        }
    }

    private void updateLastBackupTime() {
        long actualTime = Calendar.getInstance().getTimeInMillis();
        DoingSettings.setLastLocalDataBackup(mContext, actualTime);
    }
}
