package com.ngdat.foregroundactivity;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utilities {
    public static final int NOTIFICATION_1_ID = 1;
    public static final int NOTIFICATION_2_ID = 2;
    public static final String PREFS_NAME_AccessibilityService = "AccessibilityService_ActivityHistory";
    public static final String PREFS_KEY_AccessibilityService = "AccessibilityService_ActivityList";
    public static final Set<String> WhiteList = new HashSet<>(Arrays.asList(
            "com.android.systemui",
            "com.google.android.apps.nexuslauncher"
    ));

    public static List<ItemActivityInfo> loadActivityListFromAccessibilityService(SharedPreferences sharedPreferences) {
        String json = sharedPreferences.getString(PREFS_KEY_AccessibilityService, "[]");
        List<ItemActivityInfo> activityInfoList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String activityName = jsonObject.getString("activityName");
                String appName = jsonObject.getString("appName");
                String packageName = jsonObject.getString("packageName");
                long startTime = jsonObject.getLong("startTime");
                long endTime = jsonObject.optLong("endTime", -1);
                activityInfoList.add(new ItemActivityInfo(activityName, appName, packageName, startTime, endTime));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return activityInfoList;
    }

    public static List<ItemActivityInfo> loadActivityListFromUsageStatsManager(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            return new ArrayList<>();
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) {
            return new ArrayList<>(); // Return empty list if UsageStatsManager is unavailable
        }

        // Get the current time
        long currentTime = System.currentTimeMillis();

        // Query usage stats for the last 24 hours
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - (24 * 60 * 60 * 1000), // Start time (24 hours ago)
                currentTime // End time (now)
        );

        if (usageStatsList == null || usageStatsList.isEmpty()) {
            return new ArrayList<>(); // Return empty list if no data is available
        }

        // Create a new list to store filtered results
        List<UsageStats> filteredList = new ArrayList<>();

        // Filter out apps in the WhiteList
        for (UsageStats usageStats : usageStatsList) {
            if (!WhiteList.contains(usageStats.getPackageName())) {
                filteredList.add(usageStats);
            }
        }

        // Sort the filtered list by lastTimeUsed in descending order
        Collections.sort(filteredList, (o1, o2) -> Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed()));

        // Convert UsageStats to ItemActivityInfo
        List<ItemActivityInfo> activityInfoList = new ArrayList<>();
        for (UsageStats usageStats : filteredList) {
            // Get the package name and activity name
            ComponentName componentName = new ComponentName(
                    usageStats.getPackageName(),
                    usageStats.getClass().getName()
            );
            String currentPackageName = componentName.getPackageName();
            String currentActivityName = componentName.flattenToShortString();
            String currentAppName;
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(currentPackageName, 0);
                currentAppName = pm.getApplicationLabel(appInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                // Try fallback with Launch Intent
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(currentPackageName);
                if (launchIntent != null) {
                    ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(launchIntent, 0);
                    if (resolveInfo != null) {
                        currentAppName = resolveInfo.loadLabel(context.getPackageManager()).toString();
                    } else {
                        currentAppName = "Unknown";
                    }
                } else {
                    currentAppName = "Unknown";
                }
            }

            long startTime = usageStats.getFirstTimeStamp();
            long endTime = usageStats.getLastTimeStamp();
            // Create an ItemActivityInfo object
            ItemActivityInfo item = new ItemActivityInfo(currentActivityName, currentAppName, currentPackageName, startTime, endTime);
            activityInfoList.add(item);

            // Stop if we reach the limit
            if (activityInfoList.size() >= 20) {
                break;
            }
        }

        return activityInfoList;
    }
}
