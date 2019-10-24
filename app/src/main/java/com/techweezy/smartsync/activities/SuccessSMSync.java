package com.techweezy.smartsync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techweezy.smartsync.R;
import com.techweezy.smartsync.adapter.MessageAdapter;
import com.techweezy.smartsync.db.MessageContract;
import com.techweezy.smartsync.db.MessageDbHelper;
import com.techweezy.smartsync.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SuccessSMSync extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MessageAdapter success_messageAdapter;
    private final static String TAG ="ViewMessage";
    Toolbar mToolbar;
    ProgressBar success_progressBar;
    TextView viee_success_emptyViewTxt;
    MessageDbHelper messageDbHelper;
    Handler success_M_backgroundHandler;
    List success_messageList = new ArrayList<>();

    static  String BASE_API_URL= "";
    public static final String PREFS_NAME = "server_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_succes_sync);
        mRecyclerView = findViewById(R.id.messages_recyclerView);

        success_progressBar = findViewById(R.id.view_sm_progressBar);
        viee_success_emptyViewTxt = findViewById(R.id.view_error_msg);


        mToolbar = findViewById(R.id.toolbar1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_API_URL = settings.getString("server_url_str", "");

        messageDbHelper = new MessageDbHelper(getApplicationContext());

        Log.d(TAG, "onCreate: base url"+BASE_API_URL);

        HandlerThread bgHandlerThread=new HandlerThread("MyCustomThread");
        bgHandlerThread.start();
        success_M_backgroundHandler=new Handler(bgHandlerThread.getLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadDataFromSQLite();
            }
        };
        success_M_backgroundHandler.post(runnable);

    }

    private void loadDataFromSQLite (){
        // sorting orders
        String sortOrder =
                MessageContract.TxtMessageEntry.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase messagesDb = messageDbHelper.getReadableDatabase();
        String[] projection = {
                MessageContract.TxtMessageEntry.COLUMN_SENDER_PHONE,
                MessageContract.TxtMessageEntry.COLUMN_TEXT_MESSAGE,
                MessageContract.TxtMessageEntry.COLUMN_TIMESTAMP,
                MessageContract.TxtMessageEntry.COLUMN_SYNC_STATUS
        };

        // Filter results WHERE "sync_status" = 'success'
        String selection = MessageContract.TxtMessageEntry.COLUMN_SYNC_STATUS + " = ?";

        String[] selectionArgs = { "success" };
        String limit = "10";
        Cursor cursor = messagesDb.query(
                MessageContract.TxtMessageEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder ,              // The sort order
                limit
        );
        if (cursor.moveToFirst()) {
            do {
                String sender =
                        cursor.getString(cursor.getColumnIndexOrThrow("sender"));
                String messageTxt =
                        cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String timestamp =
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                String sync_status =
                        cursor.getString(cursor.getColumnIndexOrThrow("sync_status"));
                Message message = new Message(sender,messageTxt,timestamp,null, sync_status);
                success_messageList.add(message);
                Log.d(TAG, "loadDataFromSQLite: Sync Status: "
                        +sync_status);


            } while (cursor.moveToNext());


        }

        cursor.close();
        messagesDb.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success_messageList.isEmpty()){
                    viee_success_emptyViewTxt.setVisibility(View.VISIBLE);

                }

                RecyclerView.LayoutManager layoutManager =
                        new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(layoutManager);
                success_messageAdapter = new MessageAdapter(getApplicationContext(),success_messageList);
                mRecyclerView.setAdapter(success_messageAdapter);

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// todo: goto back activity from here
            Intent intent =
                    new Intent(SuccessSMSync.this, MainActivity.class);
            intent.addFlags
                    (Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
