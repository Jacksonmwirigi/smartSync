package com.techweezy.smartsync.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class ServiceUtil {
    /** This class is responsible for scheduling jobs using the JobScheduler **/
    public static void scheduleJob(Context context){
        ComponentName serviceComponent = new ComponentName(context, TextBroadcastService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder jobBuilder = new JobInfo.Builder(0,serviceComponent);
            jobBuilder.setMinimumLatency(30 *1000); // Wait at least 30s
            jobBuilder.setOverrideDeadline(60 * 1000); //Setting Maximum delay at 60s

            JobScheduler jobScheduler = (JobScheduler)context
                    .getSystemService(context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobBuilder.build());
        }

    }

}
