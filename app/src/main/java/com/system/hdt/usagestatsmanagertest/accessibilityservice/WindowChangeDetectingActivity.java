package com.system.hdt.usagestatsmanagertest.accessibilityservice;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.system.hdt.usagestatsmanagertest.R;
import com.system.hdt.usagestatsmanagertest.Utils;

public class WindowChangeDetectingActivity extends AppCompatActivity {
    public static final String TAG = WindowChangeDetectingActivity.class.getSimpleName();

    private EditText mEditPackage;
    private EditText mEditClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window_change_detecting);
        mEditPackage = findViewById(R.id.editPackageName);
        mEditClass = findViewById(R.id.editClassName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem switchItem = menu.findItem(R.id.toggleservice);
        Switch mSwitch = (Switch) switchItem.getActionView();
        // First time will initialize with default value
        boolean isSavedAsChecked = Utils.getSharedPref(this, R.string.sharedprefs_key1, false);
        mSwitch.setChecked(isSavedAsChecked);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.setSharedPref(WindowChangeDetectingActivity.this, R.string.sharedprefs_key1, isChecked);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void onClick(View v) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    public void onNewIntent(View v) {
        // Open activity with packageName & className
        String packageName = mEditPackage.getText().toString();
        String className = packageName + "." + mEditClass.getText().toString();
        String deviceName = Build.MANUFACTURER;
        if (deviceName.equals("HUAWEI")) {
            startForeignActivity(packageName, className);
        } else {
            Log.i(TAG, "onNewIntent: " + deviceName);
            Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
        }
    }

    private void startForeignActivity(String packageName, String className) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(packageName, className));
        startActivity(intent);
    }
}
