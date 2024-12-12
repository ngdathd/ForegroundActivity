package com.ngdat.foregroundactivity;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AccessibilityServiceExtend extends AccessibilityService {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
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

            String currentPackageName = componentName.getPackageName();
            String currentActivityName = componentName.flattenToShortString();
            String currentAppName;
            try {
                PackageManager pm = getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(currentPackageName, 0);
                currentAppName = pm.getApplicationLabel(appInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                // Try fallback with Launch Intent
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(currentPackageName);
                if (launchIntent != null) {
                    ResolveInfo resolveInfo = getPackageManager().resolveActivity(launchIntent, 0);
                    if (resolveInfo != null) {
                        currentAppName = resolveInfo.loadLabel(getPackageManager()).toString();
                    } else {
                        currentAppName = "Unknown";
                    }
                } else {
                    currentAppName = "Unknown";
                }
            }

            if (Utilities.WhiteList.contains(currentPackageName)) {
                return;
            }

            updateNotification(currentAppName, currentActivityName);

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
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification = createNotification("Waiting...", "No activity detected yet");
        startForeground(Utilities.NOTIFICATION_1_ID, notification);
    }

    private void saveActivityToPreferences(ItemActivityInfo itemActivityInfo) {
        // Load the current list from SharedPreferences
        List<ItemActivityInfo> activityList = Utilities.loadActivityListFromAccessibilityService(sharedPreferences);

        // Remove any existing item with the same activity name
        for (int i = 0; i < activityList.size(); i++) {
            if (activityList.get(i).getActivityName().equals(itemActivityInfo.getActivityName())) {
                activityList.remove(i);
                break; // Exit the loop after removing the item
            }
        }

        // Add the new item to the top of the list
        itemActivityInfo.setEndTime(System.currentTimeMillis());
        activityList.add(0, itemActivityInfo);

        // Keep only the 20 most recent items
        if (activityList.size() > 20) {
            activityList = activityList.subList(0, 20);
        }

        // Save the updated list back to SharedPreferences
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "accessibility_service_channel";
            CharSequence name = "AccessibilityService Channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            String description = "Channel for AccessibilityService";
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String appName, String activityName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "accessibility_service_channel")
                .setContentTitle(appName)
                .setContentText(activityName)
                .setSmallIcon(R.drawable.ic_baseline_accessibility)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);
        return builder.build();
    }

    private void updateNotification(String appName, String activityName) {
        Notification notification = createNotification(appName, activityName);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(Utilities.NOTIFICATION_1_ID, notification);
    }
}