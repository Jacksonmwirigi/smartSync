package com.techweezy.smartsync.utils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {

    private static MessageListener messageListener;
    Handler mHandler;

    private static final String TAG = "MessageReceiver";

    public MessageReceiver() {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(final Context context, Intent intent) {

       if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
           final Bundle data = intent.getExtras();
           HandlerThread bgHandlerThread=new HandlerThread("MyCoolBackgroundThread");
           bgHandlerThread.start();
           mHandler=new Handler(bgHandlerThread.getLooper());

           Runnable backgroundRunnable = new Runnable() {
               @Override
               public void run() {
                   passReceivedMsg(data);
               }
           };
           mHandler.post(backgroundRunnable);

       }

    }

    private void passReceivedMsg(final Bundle bundleData) {
        if (bundleData !=null ){
            try {
                final Object[] pdusObj = (Object[]) bundleData.get("pdus");
                if (pdusObj != null) {
                    for (int i = 0; i < pdusObj.length; i++) {
                        SmsMessage currentMessage =
                                SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        Log.d(TAG, "run: currentMessage: "+currentMessage);

                        messageListener.getReceivedMessage(currentMessage);
                        Log.d(TAG, "handleMessage: message "+currentMessage);

                    }
                }
            }
            catch (Exception e){
                Log.d(TAG, "onReceive: Error occurred "+e);

            }

        }

    }

    public static void bindListener (MessageListener listener){
            messageListener = listener;
    }

}
