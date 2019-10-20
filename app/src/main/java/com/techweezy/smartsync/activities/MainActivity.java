package com.techweezy.smartsync.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.techweezy.smartsync.R;
import com.techweezy.smartsync.db.MessageContract;
import com.techweezy.smartsync.db.MessageDbHelper;
import com.techweezy.smartsync.model.Message;
import com.techweezy.smartsync.utils.MessageListener;
import com.techweezy.smartsync.utils.MessageReceiver;

import org.json.JSONException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.Preferences;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.conn.ConnectionPoolTimeoutException;

public class MainActivity extends
        AppCompatActivity  implements MessageListener {
    final int PERMISSIONS_CODE = 1;
    private static int successMessageCounter = 0;
    private static int failedMessageCounter = 0;
    String sender, timestamp, msgID, text_message;
    String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    SimpleDateFormat formatter =
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private static int counter = 0;
    TextView failedSync, successSync;

    private static final String TAG ="MainActivity";
    private static  String BASE_API_URL= "";
    public static final String PREFS_NAME = "server_url";


    Toolbar mToolbar;
    Handler mHandler;
    MessageDbHelper dbHelper;
    private static Bundle bundle = new Bundle();

    private BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_messages:
                    startActivity(new
                            Intent(getApplicationContext(), ViewMessages.class));
                    return true;
                case R.id.navigation_settings:
                    startActivity(new
                            Intent(getApplicationContext(), SettingActivity.class));

//                    LayoutInflater dialogLayoutInflater =
//                            LayoutInflater.from(getApplicationContext());
//                    View mView = dialogLayoutInflater
//                            .inflate(R.layout.settings_dialog_box,null);
//                    showSettingsDialog(mView);

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
//        contentLayout = findViewById(R.id.main_frameLayout);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(mToolbar);
         failedSync =  findViewById(R.id.failedToSyncTV);
         successSync = findViewById(R.id.sentSuccessfulTV);

        successSync.setText(String.valueOf(successMessageCounter));
        failedSync.setText(String.valueOf(failedMessageCounter));

        MessageReceiver.bindListener(this);

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_API_URL = settings.getString("server_url_str", "");
        Log.d(TAG, "onCreate: base url"+BASE_API_URL);

        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS};

        if (!hasPermissions(this, permissions)) {

            ActivityCompat.requestPermissions
                    (this, permissions, PERMISSIONS_CODE);
        }

        mHandler = new Handler();
        dbHelper = new MessageDbHelper(getApplicationContext());

        if (!isNetworkConnectionAvailable()){
            Toast.makeText(this,
                    "Your Device is not Connected",
                    Toast.LENGTH_SHORT).show();
        }

    }
    public void saveSyncState(boolean syncState) {
        SharedPreferences sharedPreferences = getSharedPreferences("toggleButton",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SYNC_TOGGLE_BUTTON_STATE", syncState);
        editor.commit();
    }

    public static boolean hasPermissions(Context context, String... permissions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context != null && permissions != null) {
            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onClickCardAction(View view) {

        switch (view.getId()){
            case R.id.sending_failed_cardView:
//                startActivity(new Intent(getApplicationContext(),FailedMessages.class));
                break;
            case R.id.sent_messages_cardView:
                startActivity(new
                        Intent(getApplicationContext(), ViewMessages.class));

                break;
        }
    }

    public void showSettingsDialog(final View view) {

        final AlertDialog.Builder settingsDialogBuilder =
                new AlertDialog
                        .Builder(new
                        ContextThemeWrapper(this, R.style.SettingsDialog));
        settingsDialogBuilder.setView(view);


        ToggleButton disableEnBtn= view.findViewById(R.id.disable_enable_syncBtn);
        SharedPreferences toglePrefsettings = this.
                getSharedPreferences("toggleButton",MODE_PRIVATE);
        boolean toggleBtnState = toglePrefsettings
                .getBoolean("SYNC_TOGGLE_BUTTON_STATE",true);

        if (toggleBtnState){
//            Toast.makeText(this, "I was On", Toast.LENGTH_SHORT).show();
            disableEnBtn.setTextOn("Sync ON");
        }
        else {
//            Toast.makeText(this, "I was Off", Toast.LENGTH_SHORT).show();
            disableEnBtn.setTextOff("Sync OFF");
        }

        disableEnBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    syncEnabledTrueOption();
                    saveSyncState(true);
                }
                else {
                    syncDisabledFalseOption();
                    saveSyncState(false);
                }
            }
        });

         final EditText server_urlET =
                view.findViewById(R.id.settings_server_urlET);

        settingsDialogBuilder
                .setTitle("App Settings")
                .setCancelable(true)
                .setNegativeButton("Cancel ",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                })
                .setPositiveButton("SAVE",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String server_urlStr = server_urlET.getText().toString();
                        if (server_urlStr.isEmpty()){
                            Toast.makeText(MainActivity.this,
                                    "Url Cannot be Empty ",
                                    Toast.LENGTH_LONG).show();

                        }
                        if (!URLUtil.isValidUrl(server_urlStr)){
                            Toast.makeText(MainActivity.this,
                                    "Please provide a valid Url",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            SharedPreferences dialogPrefSettings =
                                    getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = dialogPrefSettings.edit();
                            editor.putString("server_url_str", server_urlStr);
                            editor.commit();
                            Toast.makeText(MainActivity.this,
                                    "URL Saved Successfully ",
                                    Toast.LENGTH_SHORT).show();

                            recreate();
                            Log.d(TAG, "onClick: server URL"+server_urlStr);

                        }
                    }
                });

        AlertDialog settingsAlertDialog = settingsDialogBuilder.create();
        settingsAlertDialog.show();

    }
    private void syncEnabledTrueOption(){
        PackageManager packageManager2 = MainActivity.this.getPackageManager();
        ComponentName componentName =
                new ComponentName(MainActivity.this, MessageReceiver.class);
        packageManager2.setComponentEnabledSetting
                (componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
        Toast.makeText(MainActivity.this,
                "Sync is Turned On", Toast.LENGTH_SHORT).show();
    }

    private void syncDisabledFalseOption(){
        PackageManager packageManager = MainActivity.this.getPackageManager();
        ComponentName componentName1 =
                new ComponentName(MainActivity.this, MessageReceiver.class);
        packageManager.setComponentEnabledSetting(componentName1, PackageManager
                .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(MainActivity.this,
                "Sync is Turned Off", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getReceivedMessage(final SmsMessage currentMessage) {
        if (currentMessage == null){
            Toast.makeText(this, "no message to sync",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (BASE_API_URL.isEmpty()){
            Toast.makeText(this, "Please Provide a valid Server URL",
                    Toast.LENGTH_LONG).show();
        }

        int msgNo = counter++;
        msgID = "SMS_ID_0" + msgNo;
        sender = currentMessage.getDisplayOriginatingAddress();
        text_message = currentMessage.getDisplayMessageBody();

        long timestampMilliseconds = System.currentTimeMillis();
        timestamp = formatter.format(timestampMilliseconds);

//        msgID = UUID.randomUUID().toString();

        String received_message = "You Received message from: " +sender
                + " message: " + text_message
                + " at: " + timestamp;
        Log.d(TAG, "getReceivedMessage: received msg "+received_message);

        Toast.makeText(this, received_message, Toast.LENGTH_SHORT).show();

        try {
            uploadMessageData();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "getReceivedMessage: exception "+e.getMessage());
            Log.d(TAG, "getReceivedMessage: exception: errorCause  "+e.getCause());
        }

    }

    private void uploadMessageData() throws JSONException{

        final RequestParams requestParams = new RequestParams();
        requestParams.put("sender",sender);
        requestParams.put("message",text_message);
        requestParams.put("timestamp",timestamp);
        requestParams.put("sms_id", msgID);

        requestParams.setHttpEntityIsRepeatable(true);
        final AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setMaxRetriesAndTimeout(5,300000);
        httpClient.setConnectTimeout(30000);

        try {
            httpClient.post(getApplicationContext(), BASE_API_URL, requestParams,
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[]
                                headers, byte[] responseBody) {
                            if (statusCode == 200){
                                if (responseBody !=null){
                                    Log.d(TAG, "onSuccess: StatusCode "+statusCode);

                                    successMessageCounter ++;
                                    Toast.makeText(getApplicationContext(),
                                            "message synced successfully ",
                                            Toast.LENGTH_SHORT).show();

                                    Message message =
                                            new Message(sender,text_message,
                                                    timestamp,msgID,"success");
                                    saveDataLocally(message);


                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        recreate();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onFailure(int statusCode,
                                              Header[] headers,
                                              byte[] responseBody, final Throwable error) {

                            Log.d(TAG, "onFailure: Failed to sync "+error);
                            Log.d(TAG, "onFailure: Failed to sync "+error);
                            Toast.makeText(MainActivity.this, ""
                                            +error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Message message =
                                    new Message(sender,text_message,
                                            timestamp,msgID, "failed");
                            saveDataLocally(message);

//                            failedMessageCounter++;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    failedMessageCounter++;
                                    recreate();

                                }
                            });

                        }

                        @Override
                        public void onRetry(int retryNo) {
                            // called when request is retried
                            Log.d(TAG, "onRetry: retryNo "+retryNo);

                        }
                    });
        }
        catch (Exception e){
            Log.d(TAG, "uploadMessageData: ");
            Toast.makeText(this, ""+e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

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
        Log.d(TAG, "onSuccess: SQLIteTABLE "+newRowId);
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo =
                connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder  builder=
                new AlertDialog
                        .Builder(MainActivity.this);
        builder.setTitle(R.string.app_name)
                .setMessage("Are You Sure You Want to Exit the Application ?")
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}
