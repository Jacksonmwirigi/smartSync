package com.techweezy.smartsync.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.techweezy.smartsync.db.MessageContract;
import com.techweezy.smartsync.db.MessageDbHelper;
import com.techweezy.smartsync.model.Message;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MessageSyncWorker extends Worker {

    private String sender, timestamp, msgID, text_message;
    private static  String BASE_API_URL= "";
    private static final String TAG = "MessageSyncWorker";
    String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    SimpleDateFormat formatter =
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    private static int counter = 0;
    static int successMessageCounter = 0;
    static int failedMessageCounter = 0;
    MessageDbHelper dbHelper;

    Handler mHandler;
    public MessageSyncWorker(@NonNull Context context,
                             @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        dbHelper = new MessageDbHelper(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {

        Data inputData1 = getInputData();

        sender = inputData1.getString("sender");
        text_message = inputData1.getString("message");
        timestamp = inputData1.getString("timestamp");
        msgID = inputData1.getString("sms_id");
        Log.d(TAG, "doWork: Mesage "+text_message);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        BASE_API_URL = sharedPreferences.getString("settings_server_url", "");
        Log.d(TAG, "doWork: BASE URL "+BASE_API_URL);

        try {
            uploadMessageData();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "doWork: uploadMessage method Exception "+e.getMessage());
        }

//        Data inputData = getInputData();
//        Log.d(TAG, "doWork: " +" inputdata "+inputData +
//                " DataValueMAP "+inputData.getKeyValueMap());
//
//        Bundle extras = new Bundle();
//        for (String key : inputData.getKeyValueMap().keySet()) {
////      extras.putString(key,String.valueOf(inputData.getString(key))); //This also works well
//            extras.putString(key,inputData.getString(key));
//            Log.d(TAG, "doWork: ExtrasKeySet " +extras.keySet());
//            Log.d(TAG, "doWork: Extra Extra: "+inputData.getString(key));
//
//        }
//        Log.d(TAG, "doWork:  Extras: Tena "+extras);
////        passReceivedMsg(extras);
//        processBundleData(extras);

        return Result.success();
    }

    private void processBundleData (Bundle bundle){
        if (bundle !=null){
            Object[] data = (Object[]) bundle.get("pdus");
            SmsMessage message;
                for (Object pdu : data) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        String format = bundle.getString("format");
                        message = SmsMessage.createFromPdu((byte[]) pdu, format);
                        Log.d(TAG, "processBundleData: messge "+message);
                    }
                    else {
                        message = SmsMessage.createFromPdu((byte[]) pdu);
                        Log.d(TAG, "processBundleData: msg "+message);
                    }

                }


//            Object[] newpdusObj = new Object[] {  bundle.get("pdus") };
//            Log.d(TAG, "processBundleData: Bundle "+newpdusObj);
//            if (newpdusObj !=null){
//                for (int i = 0; i < newpdusObj.length; i++){
//
//                    SmsMessage currentMessage =
//                            SmsMessage.createFromPdu((byte[]) newpdusObj[i]);
//                    Log.d(TAG, "processBundleData: CurrentSMS "+currentMessage);
//
//                }
//            }

        }

    }

    private void uploadMessageData() throws JSONException{
        HandlerThread bgHandlerThread=new HandlerThread("MyCoolBackgroundThread");
        bgHandlerThread.start();
        mHandler=new Handler(bgHandlerThread.getLooper());

        Runnable backgroundRunnable = new Runnable() {
            @Override
            public void run() {

                final RequestParams requestParams = new RequestParams();
                requestParams.put("sender",sender);
                requestParams.put("message",text_message);
                requestParams.put("timestamp",timestamp);
                requestParams.put("sms_id", msgID);

                requestParams.setHttpEntityIsRepeatable(true);
                final AsyncHttpClient httpClient = new AsyncHttpClient();
                httpClient.setMaxRetriesAndTimeout(5,300000);
                httpClient.setConnectTimeout(30000);
                SharedPreferences sharedPreferences = getApplicationContext()
                        .getSharedPreferences("called_times",
                                Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();

                try {

                    httpClient.post(getApplicationContext(), BASE_API_URL, requestParams,
                            new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[]
                                        headers, byte[] responseBody) {
                                    if (statusCode == 200){
                                        if (responseBody !=null){
                                            Log.d(TAG, "onSuccess: StatusCode "+statusCode);
                                            successMessageCounter++;

                                    Message message =
                                            new Message(sender,text_message,
                                                    timestamp,msgID, "success");
                                            Toast.makeText(getApplicationContext(), "message synced " +
                                                    "successfully", Toast.LENGTH_SHORT).show();
                                            saveDataLocally(message);

                                            editor.putInt("successcounter", successMessageCounter);
                                            editor.commit();
                                            Log.d(TAG, "onSuccess: SuccessCounter "+successMessageCounter);
                                        }

                                    }

                                }

                                @Override
                                public void onFailure(int statusCode,
                                                      Header[] headers,
                                                      byte[] responseBody, final Throwable error) {

                                    Log.d(TAG, "onFailure: Failed to sync "+error);
                                    Log.d(TAG, "onFailure: Failed to sync "+error);
                                    failedMessageCounter++;

                                    Toast.makeText(getApplicationContext(), " "+error,
                                            Toast.LENGTH_LONG).show();

                            Message message =
                                    new Message(sender,text_message,
                                            timestamp,msgID, "failed");
                                    saveDataLocally(message);

                                    editor.putInt("failedcounter", failedMessageCounter);
                                    editor.commit();
                                    Log.d(TAG, "onFailure: FailCounte "+failedMessageCounter);

                                }

                                @Override
                                public void onRetry(int retryNo) {
                                    // called when request is retried
                                    Log.d(TAG, "onRetry: retryNo "+retryNo);

                                }
                            });
                }
                catch (Exception e){
                    Log.d(TAG, "uploadMessageData: "+e.getMessage());
                    Toast.makeText(getApplicationContext(), ""+e.getMessage(),
                            Toast.LENGTH_LONG).show();

                }

            }
        };
        mHandler.post(backgroundRunnable);

    }
    private void saveDataLocally (Message message){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageContract.TxtMessageEntry
                .COLUMN_SENDER_PHONE, message.getSender());

        values.put(MessageContract.TxtMessageEntry
                .COLUMN_TEXT_MESSAGE, message.getMessage());
        values.put(MessageContract.TxtMessageEntry
                .COLUMN_SMS_ID, message.getSms_id());
        values.put(MessageContract.TxtMessageEntry
                .COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(MessageContract.TxtMessageEntry
                .COLUMN_SYNC_STATUS, message.getSync_status());

        long newRowId = db.insert(MessageContract.
                        TxtMessageEntry.TABLE_NAME,
                null, values);

        Log.d(TAG, "saveDataLocally: Row number "+newRowId);
    }

}
