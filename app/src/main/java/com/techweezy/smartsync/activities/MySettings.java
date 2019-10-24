package com.techweezy.smartsync.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.techweezy.smartsync.R;
import com.techweezy.smartsync.utils.MessageReceiver;

import java.util.Objects;

public class MySettings extends AppCompatActivity {
    private static final String TAG = "MySettings";
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        mToolbar = findViewById(R.id.settingsTooldbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        }
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public  static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference urlPreference = findPreference("settings_server_url");


            SwitchPreferenceCompat synOnOffPreference = findPreference("settings_sync");


            assert synOnOffPreference != null;
            synOnOffPreference.setOnPreferenceChangeListener
                    (new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                        if (newValue.equals(true)){

                            PackageManager packageManager2 = getContext().getPackageManager();
                            ComponentName componentName =
                                    new ComponentName(getContext(), MessageReceiver.class);
                            packageManager2.setComponentEnabledSetting
                                    (componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                            PackageManager.DONT_KILL_APP);
                            Toast.makeText(getContext(),
                                    "Sync is Turned On", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            PackageManager packageManager = getContext().getPackageManager();
                            ComponentName componentName1 =
                                    new ComponentName(getContext(), MessageReceiver.class);
                            packageManager.setComponentEnabledSetting(componentName1, PackageManager
                                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                            Toast.makeText(getContext(),
                                    "Sync is Turned Off", Toast.LENGTH_SHORT).show();
                        }
//                    syncEnabledTrueOption();

                    return true;
                }
            });

        }
    }


    @Override
    public void onResume() {
        super.onResume();
//        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
//        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// todo: goto back activity from here
            Intent intent =
                    new Intent(MySettings.this, MainActivity.class);
            intent.addFlags
                    (Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}