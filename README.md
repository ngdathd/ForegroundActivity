
# How to Use UsageStatsManager in Android

This guide explains the steps to use `UsageStatsManager` to retrieve app usage statistics in Android.

---

## Step 1: Declare the Permission in AndroidManifest.xml

Add the `PACKAGE_USAGE_STATS` permission in your `AndroidManifest.xml`. This is a special permission that must be enabled manually by the user.

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions" />
```

Note: 
- The attribute `tools:ignore="ProtectedPermissions"` is used to bypass Android Studio warnings about this special permission.

---

## Step 2: Check Permission at Runtime

Before using `UsageStatsManager`, verify whether the user has granted **Usage Access** permission for your app. This permission cannot be granted through standard runtime permission requests.

### Example Code:

```java
private boolean isUsageAccessGranted() {
    AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
    int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
        android.os.Process.myUid(), getPackageName());
    return mode == AppOpsManager.MODE_ALLOWED;
}
```

---

## Step 3: Request Permission from the User

If the permission is not granted, prompt the user to enable it in **Settings > Usage Access**.

### Example Code:

```java
if (!isUsageAccessGranted()) {
    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    startActivity(intent);
}
```

This will redirect the user to the system's **Usage Access** settings page.

---

## Step 4: Use UsageStatsManager After Permission is Granted

Once the permission is granted, you can use `UsageStatsManager` to query app usage data.

### Example Code:

```java
UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
long currentTime = System.currentTimeMillis();
long oneDayAgo = currentTime - 24 * 60 * 60 * 1000; // 24 hours ago

List<UsageStats> stats = usageStatsManager.queryUsageStats(
    UsageStatsManager.INTERVAL_DAILY, oneDayAgo, currentTime);

if (stats != null && !stats.isEmpty()) {
    for (UsageStats stat : stats) {
        Log.d("UsageStats", "Package: " + stat.getPackageName() + 
                ", Last Time Used: " + stat.getLastTimeUsed());
    }
} else {
    Log.d("UsageStats", "No data available");
}
```

---

## Step 5: Handle Cases Where Permission is Not Granted

If the user does not grant permission, handle it gracefully by showing a dialog or providing instructions.

### Example Code:

```java
if (!isUsageAccessGranted()) {
    new AlertDialog.Builder(this)
        .setTitle("Permission Required")
        .setMessage("Please grant Usage Access permission to use this feature.")
        .setPositiveButton("Grant Permission", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        })
        .setNegativeButton("Cancel", null)
        .show();
}
```

---

## Important Notes

1. **Google Play Policy Restrictions**:
   - Google requires a legitimate reason to use `UsageStatsManager`.
   - You must clearly describe this usage in your app's **Privacy Policy**.

2. **Manual Permission Granting**:
   - Users must manually grant the permission in **Settings > Usage Access**.
   - This cannot be granted programmatically.

3. **Handle No Data Case**:
   - Ensure you handle cases where no data is returned (`stats` is empty) due to lack of permissions or unavailable data.

---

With these steps, you can effectively use `UsageStatsManager` in your Android app. For any issues during implementation, feel free to reach out!
