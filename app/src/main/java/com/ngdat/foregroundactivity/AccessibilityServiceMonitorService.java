package com.ngdat.foregroundactivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class AccessibilityServiceMonitorService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();

        // Handler để cập nhật notification liên tục
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
                handler.postDelayed(this, 1000); // Cập nhật mỗi giây
            }
        };
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Loading...", "Please wait...", ""));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "demo_channel";
            CharSequence name = "Foreground Service Channel";
            String description = "Channel for foreground service";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String appName, String activityName, String packageName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "demo_channel")
                .setContentTitle(appName)
                .setContentText(activityName + " - " + packageName)
                .setSmallIcon(R.drawable.ic_baseline_notifications_active)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);
        return builder.build();
    }

    private void updateNotification() {
        AccessibilityServiceExtend service = AccessibilityServiceExtend.getInstance();
        if (service != null) {
            String currentApp = service.getForegroundAppName();
            String currentActivity = service.getForegroundActivityName();
            String currentPackage = service.getForegroundPackageName();

            Notification notification = createNotification(currentApp, currentActivity, currentPackage);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            Notification notification = createNotification("Unknown", "No activity available", "No package available");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}