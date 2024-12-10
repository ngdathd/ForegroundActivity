package com.ngdat.foregroundactivity;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UsageStatsManagerActivity extends AppCompatActivity {
    private MaterialButton buttonRequestUsageStatsManager;
    private TextView tvUsageStatsManagerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats_manager);

        // Enable back arrow in Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("UsageStatsManagerActivity");
        }

        // Handle back navigation using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back navigation logic
                finish(); // Close the activity
            }
        });

        buttonRequestUsageStatsManager = findViewById(R.id.btn_request_usage_stats_manager);
        tvUsageStatsManagerStatus = findViewById(R.id.tv_usage_stats_manager_status);

        updatePermissionStatus();

        buttonRequestUsageStatsManager.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ItemActivityInfo> activityList = Utilities.loadActivityListFromUsageStatsManager(this);

        ItemActivityInfoAdapter adapter = new ItemActivityInfoAdapter(activityList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check and update permission status when returning to this activity
        updatePermissionStatus();
    }

    private void updatePermissionStatus() {
        if (isUsageAccessGranted()) {
            buttonRequestUsageStatsManager.setText("Granted");
            tvUsageStatsManagerStatus.setText("Notification will be updated every 3 seconds");

            // Start Foreground Service
            Intent intent = new Intent(this, UsageStatsManagerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            buttonRequestUsageStatsManager.setText("Allow");
            tvUsageStatsManagerStatus.setText("You need to grant permission for the app to work");
        }
    }

    private boolean isUsageAccessGranted() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // Handle Back Arrow Click in Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
