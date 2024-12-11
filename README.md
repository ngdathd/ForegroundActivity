
# Get the List of Activities in the Foreground State

## Introduction to App ForegroundActivity

| ![](https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea)                 | - The application requires post notification permission.<br/>- The application requires post notification permission.                   |
|-------------------------|-------------------------------------|

https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea

![](https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea)

<img src="https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea.mp4" width="300" height="300" />
<iframe src="https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea" width="480" height="270"  class="giphy-embed" allowFullScreen></iframe>

<table>
  <tr>
    <td style="width: 30%; text-align: center;">
      <video controls style="max-width: 100%; border: 1px solid #ddd; border-radius: 4px; padding: 5px;">
        <source src="https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea" type="video/mp4">
        Your browser does not support the video tag.
      </video>
   </td>
    <td style="width: 70%; vertical-align: top; padding-left: 20px;">
      <p style="font-size: 15px;">- The application requires post notification permission.</p>
      <p style="font-size: 15px;">- In <code>MainActivity</code>, I created 2 <code>CardView</code> elements to display basic information about the two methods. When you press the <strong>"More"</strong> button, you will navigate to the details of each method.</p>
      <p style="font-size: 15px;">- Each method's screen consists of two parts:</p>
      <ul style="font-size: 15px;">
        <li><strong>Header Section:</strong> Introduction, a button to request permissions, and a notification displaying the content of the current activity</li>
        <li><strong>List Section:</strong> Displays a list of activities</li>
      </ul>
      <p style="font-size: 15px;">- After granting the required permissions, the application will create a notification showing the activity currently in the foreground. You can switch to another application to test this feature.</p>
    </td>
  </tr>
</table>

## Method 1: Using `UsageStatsManager`

### Description
`UsageStatsManager` can be used to retrieve the usage history of applications, including information about the time and frequency of app usage on the device through the `queryUsageStats` method. The most recently used application is highly likely to be in the **foreground** state. Periodically, call the `queryUsageStats` method to update the data.

### Advantages
- Easy to implement.
- Does not require access to personal information.

### Disadvantages
- Data is updated with a delay (not in real time).

### Google Policy Notes
- Using `UsageStatsManager` requires declaring the `PACKAGE_USAGE_STATS` permission. This permission does not require manual user consent but must be enabled in **Settings > Apps > Special app access > Usage access**.
- The application must be transparent with users about how this data is used and comply with Google Play’s privacy policies.

### Implementation Steps
<details>
<summary>Click to expand</summary>

1. **Declare permissions in `AndroidManifest.xml`:**
   ```xml
   <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
   ```

2. **Check permission in the code:**
   ```java
   private boolean isUsageAccessGranted() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
   ```

3. **Query information using `UsageStatsManager`:**
   ```java
   UsageStatsManager usageStatsManager =
           (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
   
   if (usageStatsManager == null) {
       return new ArrayList<>(); // Return empty list if UsageStatsManager is unavailable
   }
   
   // Query usage stats for the last 24 hours
   List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
           UsageStatsManager.INTERVAL_DAILY,
           currentTime - (24 * 60 * 60 * 1000), // Start time (24 hours ago)
           currentTime // End time (now)
   );

   if (usageStatsList == null || usageStatsList.isEmpty()) {
       return new ArrayList<>(); // Return empty list if no data is available
   }
   ```
</details>

---

## Method 2: Using `AccessibilityService`

### Description
`AccessibilityService` has the ability to monitor apps in the **foreground**, allowing you to retrieve app information via the `TYPE_WINDOW_STATE_CHANGED` event.

### Advantages
- Data is updated in real-time.

### Disadvantages
- Complex to implement.
- Granted permissions can be revoked when the application is closed.
- When granting `AccessibilityService` access, other information like text content, gesture behavior, etc., may also be accessed.

### Google Policy Notes
- Using `AccessibilityService` requires the app to clearly explain the reason for requesting this permission and not misuse it to collect unrelated data.
- According to Google Play policy, this permission is only accepted if it serves a purpose that aids the user, such as supporting disabled users.
- Misuse of this permission could result in the app being removed from Google Play.

### Implementation Steps
<details>
<summary>Click to expand</summary>

1. **Create a class that extends `AccessibilityService`:**
   ```java
   public class AccessibilityServiceExtend extends AccessibilityService {
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
           }
       }

       @Override
       public void onInterrupt() {
       }
   }
   ```

2. **Declare the service in `AndroidManifest.xml`:**
   ```xml
   <!--android:foregroundServiceType="mediaPlayback" is used for the post notification feature-->
   <service
        android:name=".AccessibilityServiceExtend"
        android:exported="true"
        android:foregroundServiceType="mediaPlayback"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
        <intent-filter>
            <action android:name="android.accessibilityservice.AccessibilityService" />
        </intent-filter>

        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibility_service_config" />
   </service>
   ```

3. **Configure the file `res/xml/accessibility_config.xml`:**
   ```xml
   <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
        android:accessibilityEventTypes="typeWindowStateChanged"
        android:accessibilityFeedbackType="feedbackGeneric"
        android:canRetrieveWindowContent="false"
        android:description="@string/app_name"
        android:notificationTimeout="100" />
   ```

4. **Activate the service in the device’s Accessibility settings:**
   - The user needs to enable the service in **Settings > Accessibility > ForegroundActivity**.

</details>

---

## Comparison of the 2 Methods

| Feature                 | UsageStatsManager                   | AccessibilityService                  |
|-------------------------|-------------------------------------|---------------------------------------|
| **Setup**               | Easy                            | Complex                              |
| **Update Frequency**  | Delayed                          | Real-time                   |
| **Permission Scope**      | Does not require sensitive access       | Requires high and sensitive permissions|
| **Accuracy**        | Depends on query timing   | High accuracy via system events    |
| **Policy Compliance** | Requires transparent declaration         | Strict requirements to avoid misuse      |

---

**Note:** The choice of method depends on project requirements and the priority among performance, security, and compliance with Google’s policies.
