package com.techweezy.smartsync.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.techweezy.smartsync.utils.MessageReceiver;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TextBroadcastService extends JobService {
    MessageReceiver messageReceiver;
    static String TAG = "TextBroadcastService";

    @Override
    public boolean onStartJob(JobParameters params) {
        //Code to be executed by the service

        this.messageReceiver = new MessageReceiver();

        Log.d(TAG, "onStartJob: serviceRunning: " );
        ServiceUtil.scheduleJob(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: Service destroyed:  ");
        return false;
    }
}
