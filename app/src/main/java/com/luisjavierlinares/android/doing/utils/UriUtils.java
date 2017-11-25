package com.luisjavierlinares.android.doing.utils;

import android.net.Uri;

/**
 * Created by Luis on 18/09/2017.
 */

public class UriUtils {

    private static final String GOOGLE_PLAY_APP_PREF = "market://details?id=";
    private static final String GOOGLE_PLAY_APP_HTTPS_PREF = "https://play.google.com/store/apps/details?id=";

    public static String getGooglePlayAppPref() {
        return GOOGLE_PLAY_APP_PREF;
    }

    public static String getGooglePlayAppString(String appFullName) {
        return getGooglePlayAppPref() + appFullName;
    }

    public static Uri getGooglePlayAppUri(String appFullName) {
        return Uri.parse(getGooglePlayAppString(appFullName));
    }

    public static String getGooglePlayAppHttpsPref() {
        return GOOGLE_PLAY_APP_HTTPS_PREF;
    }

    public static String getGooglePlayAppHttpsString(String appFullName) {
        return getGooglePlayAppHttpsPref() + appFullName;
    }

    public static Uri getGooglePlayAppHttpsUri(String appFullName) {
        return Uri.parse(getGooglePlayAppHttpsString(appFullName));
    }
}
