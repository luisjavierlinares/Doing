package com.luisjavierlinares.android.doing.services;

import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by Luis on 10/09/2017.
 */

public class UpdateAndNotifyJob extends JobService {

    private static final String RECURRING_JOB_TAG = UpdateAndNotifyJob.class.getSimpleName() + "_RECURRING";
    private static final String JOB_TAG = UpdateAndNotifyJob.class.getSimpleName();

    private static final int PERIOD_LOW = 600;
    private static final int PERIOD_HIGH = 900;

    public static void setServiceJob(Context context) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        dispatcher.mustSchedule(
                dispatcher.newJobBuilder()
                        .setService(UpdateAndNotifyJob.class)
                        .setTag(JOB_TAG)
                        .build()
        );
    }


    public static void setRecurringServiceJob(Context context, boolean isOn) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        if (isOn) {

            dispatcher.mustSchedule(
                    dispatcher.newJobBuilder()
                            .setService(UpdateAndNotifyJob.class)
                            .setTag(RECURRING_JOB_TAG)
                            .setRecurring(true)
                            .setLifetime(Lifetime.FOREVER)
                            .setTrigger(Trigger.executionWindow(PERIOD_LOW, PERIOD_HIGH))
                            .setReplaceCurrent(false)
                            .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                            .setConstraints(Constraint.ON_ANY_NETWORK)
                            .build()
            );
        } else {
            dispatcher.cancel(RECURRING_JOB_TAG);
        }
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        runJobOnBackground(job);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Context context = getApplicationContext();

        DoingUpdater doingUpdater = new DoingUpdater(context);
        doingUpdater.cancel();

        jobFinished(job, true);
        return true;
    }

    public void runJobOnBackground(final JobParameters job) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doJob();
                jobFinished(job, false);
            }
        }).start();
    }

    public void doJob() {
        Log.i("UpdateAndNotifyJob", "Job running");
        Context context = getApplicationContext();

        DoingUpdater doingUpdater = new DoingUpdater(context);
        doingUpdater.update();
    }

    public static void clearAllNotifications(Context context) {
        DoingUpdater doingUpdater = new DoingUpdater(context);
        doingUpdater.clearAllNotifications(context);
    }
}
