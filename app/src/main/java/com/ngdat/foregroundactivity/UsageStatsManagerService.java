package com.ngdat.foregroundactivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class UsageStatsManagerService extends Service {
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Notification notification = createNotification("Waiting...", "No activity detected yet");
        startForeground(Utilities.NOTIFICATION_2_ID, notification);

        handler = new Handler();
        // Update notification every 3 seconds
        Runnable updateNotificationRunnable = new Runnable() {
            @Override
            public void run() {
                ItemActivityInfo itemActivityInfo = Utilities.loadActivityListFromUsageStatsManager(UsageStatsManagerService.this).get(0);
                if (itemActivityInfo != null) {
                    updateNotification(itemActivityInfo.getAppName(), itemActivityInfo.getActivityName());
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateNotificationRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "usage_stats_manager_channel";
            CharSequence name = "UsageStatsManager Service Channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            String description = "Channel for UsageStatsManager Service";
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String appName, String activityName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "usage_stats_manager_channel")
                .setContentTitle(appName)
                .setContentText(activityName)
                .setSmallIcon(R.drawable.ic_baseline_data_usage)
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
        notificationManager.notify(Utilities.NOTIFICATION_2_ID, notification);
    }
}