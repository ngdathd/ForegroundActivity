package com.system.hdt.usagestatsmanagertest;

import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getForegroundProcess(Context context) {
        String topPackageName = null;
        UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);

        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : stats) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (runningTask.isEmpty()) {
                return null;
            }
            topPackageName = runningTask.get(runningTask.lastKey()).getPackageName();
        }
        if (topPackageName == null) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
        Log.d("topPackageName", "topPackageName is" + topPackageName);

        return topPackageName;
    }

    public static String[] getForegroundPackageNameClassNameByUsageStats(Context context) {
        String packageNameByUsageStats = null;
        String classByUsageStats = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            final long INTERVAL = 5000;
            final long end = System.currentTimeMillis();
            final long begin = end - INTERVAL;
            final UsageEvents usageEvents = mUsageStatsManager.queryEvents(begin, end);
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    packageNameByUsageStats = event.getPackageName();
                    classByUsageStats = event.getClassName();
                    Log.d("packageNameByUsageStats", "packageNameByUsageStats is" + packageNameByUsageStats + ", classByUsageStats is " + classByUsageStats);
                }
            }
        }
        return new String[]{packageNameByUsageStats, classByUsageStats};
    }

    /**
     * This is the only method reading prefs_name from strings.xml
     */
    private static SharedPreferences getSharedPrefs(final Context context) {
        final String prefsName = context.getString(R.string.sharedprefs_name);
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    public static boolean getSharedPref(final Context context, final int keyId, final boolean defValue) {
        return getSharedPref(context, context.getString(keyId), defValue);
    }

    public static boolean getSharedPref(final Context context, final String key, final boolean defValue) {
        boolean value = getSharedPrefs(context).getBoolean(key, defValue);
        Log.i(TAG, "Reading bool pref \"" + key + "\" = " + value);
        return value;
    }

    public static boolean setSharedPref(final Context context, final int keyId, final String value) {
        return setSharedPref(context, context.getString(keyId), value);
    }

    public static boolean setSharedPref(final Context context, final String key, final String value) {
        Log.i(TAG, "Setting string pref \"" + key + "\" = " + value);
        return getSharedPrefs(context)
                .edit()
                .putString(key, value)
                .commit();
    }

    public static boolean setSharedPref(final Context context, final int keyId, final boolean value) {
        return setSharedPref(context, context.getString(keyId), value);
    }

    public static boolean setSharedPref(final Context context, final String key, final boolean value) {
        Log.i(TAG, "Setting bool pref \"" + key + "\" = " + value);
        return getSharedPrefs(context)
                .edit()
                .putBoolean(key, value)
                .commit();
    }
}
