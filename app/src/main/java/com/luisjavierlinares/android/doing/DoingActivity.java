package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.luisjavierlinares.android.doing.managers.AdsManager;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.User;

public class DoingActivity extends SingleFragmentActivity  implements DoingDetailCallbacks, UserHistoryCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_ADDED_FRIEND = 2;
    private static final String DIALOG_CREATE_FRIEND = "com.luisjavierlinares.android.Doing.dialog_add_friend";

    private InterstitialAd mInterstitialAd;

    @Override
    protected Fragment createFragment() {
        return new DoingFragment().newInstance();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, DoingActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        loadInterstitialAd();
    }

    @Override
    public void onDoingSelected(Doing doing, DOING_LIST_TYPE List_type) {
        Intent intent = DoingDetailActivity.newIntent(this, doing.getId(), List_type);
        startActivityMaybeWithAd(intent);

    }

    @Override
    public void onDoingSelected(Doing doing) {
        Intent intent = DoingDetailActivity.newIntent(this, doing.getId());
        startActivityMaybeWithAd(intent);
    }

    @Override
    public void onUserHistory(User user) {
        Intent intent = UserHistoryActivity.newIntent(this, user.getId());
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void startActivityMaybeWithAd(Intent intent) {
        setAdListener(intent);
        showInterstitialAd(intent);
    }

    public void loadInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(AdsManager.INTERSTITIAL_AD_ID);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("BDD91807971547591A3B72B538EB15D1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void setAdListener(final Intent intent) {
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
                AdsManager.get(getApplicationContext()).resetViewsAndTimeSinceLastAd();
            }

            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                startActivity(intent);
            }

        });
    }

    public void showInterstitialAd(Intent intent) {
        if ((AdsManager.get(this).isTimeForInterstitialAd()) && (mInterstitialAd.isLoaded())) {
            mInterstitialAd.show();
        } else {
            startActivity(intent);
        }
    }

 }