package com.techweezy.smartsync.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.techweezy.smartsync.R;

public class MainActivity extends
        AppCompatActivity {
    final int PERMISSIONS_CODE = 1;
    TextView failedSync, successSync;
    Toolbar main_activity_mToolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_messages:
                    startActivity(new
                            Intent(getApplicationContext(), SuccessSMSync.class));
                    return true;
                case R.id.navigation_settings:
                    startActivity(new
                            Intent(getApplicationContext(), MySettings.class));

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
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        main_activity_mToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(main_activity_mToolbar);
         failedSync =  findViewById(R.id.failedToSyncTV);
         successSync = findViewById(R.id.sentSuccessfulTV);

        SharedPreferences sharedPreferences = this.getSharedPreferences("called_times", MODE_PRIVATE);
        int main_success_counter = sharedPreferences.getInt("successcounter", 0);
        int failedToSyn = sharedPreferences.getInt("failedcounter",0);
        successSync.setText(String.valueOf(main_success_counter));
        failedSync.setText(String.valueOf(failedToSyn));


        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS};

        if (!hasPermissions(this, permissions)) {

            ActivityCompat.requestPermissions
                    (this, permissions, PERMISSIONS_CODE);
        }

        if (!isNetworkConnectionAvailable()){
            Toast.makeText(this,
                    "Your Device is not Connected",
                    Toast.LENGTH_SHORT).show();
        }

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
                startActivity(new Intent(getApplicationContext(), ViewFailedSMSsync.class));
                break;
            case R.id.sent_messages_cardView:
                startActivity(new
                        Intent(getApplicationContext(), SuccessSMSync.class));

                break;
        }
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
        SharedPreferences sharedPreferences = this.getSharedPreferences("called_times", MODE_PRIVATE);
        int successcounter = sharedPreferences.getInt("successcounter", 0);
        int failedToSyn = sharedPreferences.getInt("failedcounter",0);
        successSync.setText(String.valueOf(successcounter));
        failedSync.setText(String.valueOf(failedToSyn));

    }
}
