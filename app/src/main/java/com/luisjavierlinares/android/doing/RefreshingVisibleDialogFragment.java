package com.luisjavierlinares.android.doing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luisjavierlinares.android.doing.services.DoingUpdater;

/**
 * Created by Luis on 17/05/2017.
 */

public abstract class RefreshingVisibleDialogFragment extends DialogFragment {

    private static final long UPDATE_COUNTDOWN_MILIS = 1200;

    private CountDownTimer mRefreshTimer;
    private long mUpdateTime;
    private Boolean mIsVisibleToUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpdateTime = UPDATE_COUNTDOWN_MILIS;
        mIsVisibleToUser = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(DoingUpdater.ACTION_SHOW_NOTIFICATON);
        getActivity().registerReceiver(mOnShowNotification, filter, DoingUpdater.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        cancelUpdateTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsVisibleToUser) {
            startUpdateTimer();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRefreshTimer = new CountDownTimer(mUpdateTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                update();
                mRefreshTimer.start();
            }
        }.start();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mRefreshTimer == null) {
            return;
        }

        // if the fragment is not visible do not refresh
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                mRefreshTimer.cancel();
            } else {
                mRefreshTimer.start();
            }
        }
    }

    public void startUpdateTimer(){
        if (mRefreshTimer == null) {
            return;
        }
        mRefreshTimer.start();
    }

    public void cancelUpdateTimer() {
        if (mRefreshTimer == null) {
            return;
        }
        mRefreshTimer.cancel();
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    abstract public void update();
}
