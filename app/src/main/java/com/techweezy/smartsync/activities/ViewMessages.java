package com.techweezy.smartsync.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.techweezy.smartsync.R;
import com.techweezy.smartsync.adapter.MessageAdapter;
import com.techweezy.smartsync.db.MessageContract;
import com.techweezy.smartsync.db.MessageDbHelper;
import com.techweezy.smartsync.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewMessages extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MessageAdapter messageAdapter;
    private final static String TAG ="ViewMessage";
    Toolbar mToolbar;
    ProgressBar progressBar;
    TextView emptyViewTxt;
    MessageDbHelper messageDbHelper;
    Handler backgroundHandler;

    static  String BASE_API_URL= "";
    public static final String PREFS_NAME = "server_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_messages);
        mRecyclerView = findViewById(R.id.messages_recyclerView);

        progressBar = findViewById(R.id.view_sm_progressBar);
        emptyViewTxt = findViewById(R.id.view_error_msg);


        mToolbar = findViewById(R.id.toolbar1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_API_URL = settings.getString("server_url_str",
                "http://157.245.12.136/sms/receive");

        messageDbHelper = new MessageDbHelper(getApplicationContext());


        Log.d(TAG, "onCreate: base url"+BASE_API_URL);

        HandlerThread bgHandlerThread=new HandlerThread("MyCustomThread");
        bgHandlerThread.start();
        backgroundHandler=new Handler(bgHandlerThread.getLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadDataFromSQLite();
            }
        };
        backgroundHandler.post(runnable);

    }


    private void loadDataFromSQLite (){

        // sorting orders
        String sortOrder =
                MessageContract.TxtMessageEntry.COLUMN_TIMESTAMP + " DESC";

        final List<Message> messageList = new ArrayList<>();

        SQLiteDatabase messagesDb = messageDbHelper.getReadableDatabase();
        String[] projection = {
                MessageContract.TxtMessageEntry.COLUMN_SENDER_PHONE,
                MessageContract.TxtMessageEntry.COLUMN_TEXT_MESSAGE,
                MessageContract.TxtMessageEntry.COLUMN_TIMESTAMP,
                MessageContract.TxtMessageEntry.COLUMN_SYNC_STATUS,
        };
        String limit = "10";
        Cursor cursor = messagesDb.query(
                MessageContract.TxtMessageEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
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

                Message message = new Message(sender,messageTxt,timestamp,null, null);
                Log.d(TAG, "loadDataFromSQLite: Sync Status: "
                        +sync_status);

                messageList.add(message);

            } while (cursor.moveToNext());
        }

        cursor.close();
        messagesDb.close();
        loadMessageRecyclerViews(messageList);

    }

    private void loadMessageRecyclerViews(final List<Message> list){
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (list.isEmpty()){
                    emptyViewTxt.setVisibility(View.VISIBLE);
                }

                RecyclerView.LayoutManager layoutManager =
                        new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(layoutManager);
            }
        });
        messageAdapter = new MessageAdapter(getApplicationContext(),list);
        mRecyclerView.setAdapter(messageAdapter);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// todo: goto back activity from here
            Intent intent =
                    new Intent(ViewMessages.this, MainActivity.class);
            intent.addFlags
                    (Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
