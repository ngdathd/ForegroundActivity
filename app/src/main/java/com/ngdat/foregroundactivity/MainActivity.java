package com.ngdat.foregroundactivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        MaterialButton buttonWisomAccessDots = findViewById(R.id.button_wisom_access_dots);
        buttonWisomAccessDots.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Clicked: WisomAccessDots", Toast.LENGTH_SHORT).show()
        );
    }
}
