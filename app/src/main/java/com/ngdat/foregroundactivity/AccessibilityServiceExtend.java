package com.ngdat.foregroundactivity;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AccessibilityServiceExtend extends AccessibilityService {
    private static AccessibilityServiceExtend instance;

    private SharedPreferences sharedPreferences;
    private String currentPackageName = "";
    private String currentActivityName = "";
    private String currentAppName = "";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        sharedPreferences = getSharedPreferences(Utilities.PREFS_NAME_AccessibilityService, MODE_PRIVATE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Get the package name and activity name
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            currentPackageName = componentName.getPackageName();
            currentActivityName = componentName.getClassName();

            // Get app name
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(currentPackageName, 0);
                currentAppName = getPackageManager().getApplicationLabel(appInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                currentAppName = "Unknown";
            }

            // Get the current timestamp
            long startTime = System.currentTimeMillis();

            // Create a new activity item
            ItemActivityInfo activityInfo = new ItemActivityInfo(currentActivityName, currentAppName, currentPackageName, startTime, -1);

            // Save to SharedPreferences
            saveActivityToPreferences(activityInfo);
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    private void saveActivityToPreferences(ItemActivityInfo itemActivityInfo) {
        List<ItemActivityInfo> activityList = Utilities.loadActivityListFromAccessibilityService(sharedPreferences);

        // If the list already contains the same activity, update its end time
        for (ItemActivityInfo item : activityList) {
            if (item.getActivityName().equals(itemActivityInfo.getActivityName())) {
                item.setEndTime(System.currentTimeMillis());
                saveActivityListToPreferences(activityList);
                return;
            }
        }

        // Otherwise, add a new activity to the list
        activityList.add(itemActivityInfo);
        saveActivityListToPreferences(activityList);
    }

    private void saveActivityListToPreferences(List<ItemActivityInfo> activityList) {
        JSONArray jsonArray = new JSONArray();
        for (ItemActivityInfo item : activityList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("activityName", item.getActivityName());
                jsonObject.put("appName", item.getAppName());
                jsonObject.put("packageName", item.getPackageName());
                jsonObject.put("startTime", item.getStartTime());
                jsonObject.put("endTime", item.getEndTime());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().putString(Utilities.PREFS_KEY_AccessibilityService, jsonArray.toString()).apply();
    }

    public static AccessibilityServiceExtend getInstance() {
        return instance;
    }

    public String getForegroundPackageName() {
        return currentPackageName;
    }

    public String getForegroundActivityName() {
        return currentActivityName;
    }

    public String getForegroundAppName() {
        return currentAppName;
    }
}