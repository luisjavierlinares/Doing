package com.luisjavierlinares.android.doing.managers;

import android.content.Context;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.events.DisableAdsPermanentlyEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

/**
 * Created by Luis on 07/09/2017.
 */

public class AdsManager {

    private static final String ADMOB_APP_ID = "ca-app-pub-2740169353239994~6627753963";

    public static final String BANNER_AD_ID = "ca-app-pub-2740169353239994/9529249473";
    public static final String INTERSTITIAL_AD_ID = "ca-app-pub-2740169353239994/6634200192";

    private static final int MIN_VIEWS_SINCE_LAST_AD = 15;
    private static final int MIN_TIME_SINCE_LAST_AD = 10 * 60 * 1000;

    private static AdsManager sAdsManager;

    private Context mContext;
    private Boolean mAreBannerAdsEnabled;
    private Boolean mAreAdsPermanentlyDisabled;

    private EventBus mEventBus;

    public static synchronized AdsManager get(Context context){
        if (sAdsManager == null){
            sAdsManager = new AdsManager(context);
        }
        return sAdsManager;
    }

    private AdsManager(Context context) {
        mContext = context;
        mEventBus = EventBus.getDefault();
        mAreAdsPermanentlyDisabled = DoingSettings.areAdsPermanentlyDisabled(mContext);
//        if (mAreAdsPermanentlyDisabled) {
//            mAreBannerAdsEnabled = false;
//        } else {
//            mAreBannerAdsEnabled = true;
//        }
        mAreBannerAdsEnabled = false;
    }

    public final String getAdmobAppId() {
        return ADMOB_APP_ID;
    }

    public void enableAds() {
        mAreBannerAdsEnabled = true;
        mAreAdsPermanentlyDisabled = false;
    }

    public void disableBannerAds() {
        mAreBannerAdsEnabled = false;
    }

    public void disableAdsPermanently() {
        mAreBannerAdsEnabled = false;
        mAreAdsPermanentlyDisabled = true;
        DoingSettings.setAdsPermanentlyDisabled(mContext, true);
        mEventBus.post(new DisableAdsPermanentlyEvent());
    }

    public boolean areBannerAdsEnabled() {
        return mAreBannerAdsEnabled;
    }

    public boolean areAdsPermanentlyDisabled() {
        return mAreAdsPermanentlyDisabled;
    }

    public boolean isTimeForInterstitialAd() {
        if (areAdsPermanentlyDisabled()) {
            return false;
        }

        int viewsSinceLastAdd = DoingSettings.getViewsSinceLastAd(mContext);
        long lastAdTime = DoingSettings.getTimeLastAd(mContext);
        long actualTime = Calendar.getInstance().getTimeInMillis();
        long timeSinceLastAdd = actualTime - lastAdTime;

        if ((viewsSinceLastAdd < MIN_VIEWS_SINCE_LAST_AD) || (timeSinceLastAdd < MIN_TIME_SINCE_LAST_AD)) {
            DoingSettings.setViewsSinceLastAd(mContext, viewsSinceLastAdd + 1);
            return false;
        } else {
            return true;
        }
    }

    public void resetViewsAndTimeSinceLastAd() {
        DoingSettings.setViewsSinceLastAd(mContext, 0);
        long actualTime = Calendar.getInstance().getTimeInMillis();
        DoingSettings.setTimeLastAd(mContext, actualTime);
    }
}
