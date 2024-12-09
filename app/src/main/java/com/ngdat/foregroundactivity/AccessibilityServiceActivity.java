package com.ngdat.foregroundactivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AccessibilityServiceActivity extends AppCompatActivity {
    private MaterialButton buttonRequestAccessibilityService;
    private TextView tvAccessibilityServiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_service);

        // Enable back arrow in Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AccessibilityServiceActivity");
        }

        // Handle back navigation using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back navigation logic
                finish(); // Close the activity
            }
        });

        buttonRequestAccessibilityService = findViewById(R.id.btn_request_accessibility_service);
        tvAccessibilityServiceStatus = findViewById(R.id.tv_accessibility_service_status);

        updatePermissionStatus();

        buttonRequestAccessibilityService.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences = getSharedPreferences(Utilities.PREFS_NAME_AccessibilityService, MODE_PRIVATE);
        List<ItemActivityInfo> activityList = Utilities.loadActivityListFromAccessibilityService(sharedPreferences);

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
        if (isAccessibilityServiceEnabled(this)) {
            buttonRequestAccessibilityService.setText("Granted");
            tvAccessibilityServiceStatus.setText("Data will be refreshed in real time.");

            Intent intent = new Intent(this, AccessibilityServiceMonitorService.class);
            startService(intent);
        } else {
            buttonRequestAccessibilityService.setText("Allow");
            tvAccessibilityServiceStatus.setText("You need to grant permission for the app to work");
        }
    }

    private boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;

        List<AccessibilityServiceInfo> enabledServices = manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains(context.getPackageName()) &&
                    service.getId().contains(AccessibilityServiceExtend.class.getSimpleName())) {
                return true;
            }
        }
        return false;
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
