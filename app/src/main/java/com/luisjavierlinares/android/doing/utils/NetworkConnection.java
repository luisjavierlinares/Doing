package com.luisjavierlinares.android.doing.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Luis on 25/05/2017.
 */

public class NetworkConnection {
    public Context context;

    public NetworkConnection(Context applicationContext) {
        this.context=applicationContext;
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo()
                .isConnectedOrConnecting();

        return isNetworkConnected;
    }
}
