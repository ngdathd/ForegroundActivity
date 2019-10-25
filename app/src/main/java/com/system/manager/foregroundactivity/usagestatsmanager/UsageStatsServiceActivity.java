package com.system.manager.foregroundactivity.usagestatsmanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.system.manager.foregroundactivity.R;

public class UsageStatsServiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats_service);
        Intent intent = new Intent(this, UsageStatsService.class);
        startService(intent);
//        finish();
    }
}
