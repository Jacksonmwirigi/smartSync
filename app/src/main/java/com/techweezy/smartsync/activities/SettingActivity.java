package com.techweezy.smartsync.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.techweezy.smartsync.R;
import com.techweezy.smartsync.utils.MessageReceiver;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private static  String BASE_API_URL= "";
    public static final String PREFS_NAME = "server_url";
    private static final String TAG ="SettingsActivity";
    private Button saveBTN;

    Switch aSwitch;
    ToggleButton disableEnBtn;
    EditText server_urlET;
    String server_urlStr;
    SharedPreferences toglePrefsettings;
    String toggleBtnState;
    private static Bundle bundleState = new Bundle();
    Toolbar myToolbar;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dialog_box);

        disableEnBtn =  findViewById(R.id.disable_enable_syncBtn);
        server_urlET = findViewById(R.id.settings_server_urlET);
        saveBTN = findViewById(R.id.svaesettBTN);


        myToolbar = findViewById(R.id.toolbar3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_API_URL = settings.getString("server_url_str", "");
        if (BASE_API_URL !=null ){
            server_urlET.setText(BASE_API_URL);
        }

        disableEnBtn.setChecked(bundleState.getBoolean("SYNC_TOGGLE_BUTTON_STATE"));


//        toglePrefsettings = this.
//                getSharedPreferences("toggleButton",MODE_PRIVATE);
//        toggleBtnState = toglePrefsettings
//                .getBoolean("SYNC_TOGGLE_BUTTON_STATE","");
//        disableEnBtn.setText(toggleBtnState);

//        if (toggleBtnState.equals("ON")){
//            Toast.makeText(this, "I was On"+toggleBtnState, Toast.LENGTH_SHORT).show();
//            disableEnBtn.setText(R.string.sync_on);
//        }
//        else if (toggleBtnState.equals("OFF")){
//            //            Toast.makeText(this, "I was Off", Toast.LENGTH_SHORT).show();
//            disableEnBtn.setText(R.string.sync_off);
//
//        }
//        else {
//            Toast.makeText(this, "no state", Toast.LENGTH_SHORT).show();
//        }

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

        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                server_urlStr = server_urlET.getText().toString();

                if (server_urlStr.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Url Cannot be Empty ",
                            Toast.LENGTH_LONG).show();
                    return;

                }
                if (BASE_API_URL.equals(server_urlStr)){
                    Toast.makeText(SettingActivity.this,
                            "Sorry!! No Change is Detected",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!URLUtil.isValidUrl(server_urlStr)){
                    Toast.makeText(getApplicationContext(),
                            "Please provide a valid Url",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                    SharedPreferences dialogPrefSettings =
                            getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = dialogPrefSettings.edit();
                    editor.putString("server_url_str", server_urlStr);
                    editor.commit();
                    Toast.makeText(getApplicationContext(),
                            "URL Saved Successfully ",
                            Toast.LENGTH_SHORT).show();

                    recreate();
                    Log.d(TAG, "onClick: server URL"+server_urlStr);

            }
        });

    }


    public void saveSyncState(boolean syncState) {
        SharedPreferences sharedPreferences = getSharedPreferences("toggleButton",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SYNC_TOGGLE_BUTTON_STATE", syncState);
        editor.commit();
    }

    public void showSettingsDialog( ) {
        server_urlStr = server_urlET.getText().toString();
                                if (server_urlStr.isEmpty()){
                                    Toast.makeText(SettingActivity.this,
                                            "Url Cannot be Empty ",
                                            Toast.LENGTH_LONG).show();

                                }
                                if (!URLUtil.isValidUrl(server_urlStr)){
                                    Toast.makeText(SettingActivity.this,
                                            "Please provide a valid Url",
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    SharedPreferences dialogPrefSettings =
                                            getSharedPreferences(PREFS_NAME, 0);
                                    SharedPreferences.Editor editor = dialogPrefSettings.edit();
                                    editor.putString("server_url_str", server_urlStr);
                                    editor.commit();
                                    Toast.makeText(SettingActivity.this,
                                            "URL Saved Successfully ",
                                            Toast.LENGTH_SHORT).show();

                                    recreate();
                                    Log.d(TAG, "onClick: server URL"+server_urlStr);

                                }



    }
    private void syncEnabledTrueOption(){
        PackageManager packageManager2 = SettingActivity.this.getPackageManager();
        ComponentName componentName =
                new ComponentName(SettingActivity.this, MessageReceiver.class);
        packageManager2.setComponentEnabledSetting
                (componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
        Toast.makeText(SettingActivity.this,
                "Sync is Turned On", Toast.LENGTH_SHORT).show();
    }

    private void syncDisabledFalseOption(){
        PackageManager packageManager = SettingActivity.this.getPackageManager();
        ComponentName componentName1 =
                new ComponentName(SettingActivity.this, MessageReceiver.class);
        packageManager.setComponentEnabledSetting(componentName1, PackageManager
                .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(SettingActivity.this,
                "Sync is Turned Off", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        bundleState.putBoolean("ToggleButtonState", disableEnBtn.isChecked());
//        saveSyncState(disableEnBtn.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        disableEnBtn.setChecked(bundleState.getBoolean("ToggleButtonState",false));
//        saveSyncState(disableEnBtn.isChecked());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableEnBtn.setChecked(bundleState.getBoolean("ToggleButtonState",false));
        saveSyncState(disableEnBtn.isChecked());

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// todo: goto back activity from here
            Intent intent =
                    new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
