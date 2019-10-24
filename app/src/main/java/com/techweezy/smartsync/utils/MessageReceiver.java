package com.techweezy.smartsync.utils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.techweezy.smartsync.db.MessageDbHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessageReceiver extends BroadcastReceiver {
    Context context;
    String sender, timestamp, msgID, text_message;
    private static final String TAG = "MessageReceiver";
    String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    SimpleDateFormat formatter =
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private static int counter = 0;

    public MessageReceiver() {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context= context;
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
           final Bundle data = intent.getExtras();

            if (data !=null ) {
                try {
                    final Object[] pdusObj = (Object[]) data.get("pdus");
                    if (pdusObj != null) {
                        for (int i = 0; i < pdusObj.length; i++) {
                            SmsMessage currentMessage =
                                    SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                            Log.d(TAG, "run: currentMessage: " + currentMessage);

                            Log.d(TAG, "passReceivedMsg: handleMessage: message " + currentMessage);

                            int msgNo = counter++;
                            msgID = "SMS_ID_0" + msgNo;
                            sender = currentMessage.getDisplayOriginatingAddress();
                            text_message = currentMessage.getDisplayMessageBody();

                            long timestampMilliseconds = System.currentTimeMillis();
                            timestamp = formatter.format(timestampMilliseconds);

                            Data.Builder dataBuilder = new Data.Builder();
                            dataBuilder.putString("sender",sender);
                            dataBuilder.putString("message",text_message);
                            dataBuilder.putString("timestamp",timestamp);
                            dataBuilder.putString("sms_id",msgID);
//                            Log.d(TAG, "onReceive:  key:" +key+ " and keyValue "+data.get(key));

                            WorkManager mWorkManager = WorkManager.getInstance();
                            OneTimeWorkRequest mRequest = new OneTimeWorkRequest
                                    .Builder(MessageSyncWorker.class)
                                    .setInputData(dataBuilder.build())
                                    .build();
                            mWorkManager.enqueue(mRequest);
                        }
                    }
                }
                catch (Exception e){
                    Log.d(TAG, "onReceive: Exception occured "+e.getMessage());
                }
            }


       }

    }
}

