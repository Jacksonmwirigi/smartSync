package com.techweezy.smartsync.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Telephony;

import android.util.Log;

import androidx.annotation.RequiresApi;

import com.techweezy.smartsync.utils.MessageReceiver;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TextBroadcastService extends JobService {
    MessageReceiver messageReceiver;
    static String TAG = "TextBroadcastService";

    @Override
    public boolean onStartJob(JobParameters params) {
        //Code to be executed by the service
        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        getApplicationContext().registerReceiver(messageReceiver, filter);

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
