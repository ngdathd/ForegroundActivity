package com.ngdat.foregroundactivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;
    private boolean isReturningFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                showPermissionToast();
            } else {
                showPermissionDialog();
            }
        });
        checkAndRequestNotificationPermission();

        MaterialButton buttonUsageStatsManager = findViewById(R.id.button_usage_stats_manager);
        buttonUsageStatsManager.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UsageStatsManagerActivity.class);
            startActivity(intent);
        });

        MaterialButton buttonAccessibilityService = findViewById(R.id.button_accessibility_service);
        buttonAccessibilityService.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccessibilityServiceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturningFromSettings) {
            isReturningFromSettings = false; // Reset flag
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog();
            } else {
                showPermissionToast();
            }
        }
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                showPermissionToast();
            }
        } else {
            showPermissionToast();
        }
    }

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required");
        builder.setMessage("You need to grant the notification permission for the app to function. Do you want to go to settings?");
        builder.setPositiveButton("Yes", (dialog, which) -> openAppSettings());
        builder.setNegativeButton("No", (dialog, which) -> showExitDialog());
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("The app will exit because the required permission was not granted. Do you want to exit?");
        builder.setPositiveButton("Yes", (dialog, which) -> finish());
        builder.setNegativeButton("No", (dialog, which) -> showPermissionDialog());
        builder.setCancelable(false);
        builder.show();
    }

    private void openAppSettings() {
        isReturningFromSettings = true;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void showPermissionToast() {
        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
    }
}
