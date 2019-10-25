## ForegroundActivity
Get foreground activty using AccessibilityService or UsageStatsManager

## AccessibilityService

Warning: Google Play violation

Google has threatened to remove apps from the Play Store if they use accessibility services for non-accessibility purposes. However, this is reportedly being reconsidered.

### Use an [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html)
* You can detect the currently active window by using an [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html).
* In the [onAccessibilityEvent](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#onAccessibilityEvent(android.view.accessibility.AccessibilityEvent)) callback, check for the [TYPE_WINDOW_STATE_CHANGED](https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent.html#TYPE_WINDOW_STATE_CHANGED) [event type](https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent.html#getEventType()) to determine when the current window changes.
* Check if the window is an activity by calling [PackageManager.getActivityInfo()](https://developer.android.com/reference/android/content/pm/PackageManager.html#getActivityInfo(android.content.ComponentName,%20int)).
### Benefits
* Tested and working in Android 2.2 (API 8) through Android 7.1 (API 25).
* Doesn't require polling.
* Doesn't require the [GET_TASKS](https://developer.android.com/reference/android/Manifest.permission.html#GET_TASKS) permission.
### Disadvantages
* Each user must enable the service in Android's accessibility settings.
* The service is always running.
* When a user tries to enable the [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html), they can't press the OK button if an app has placed an overlay on the screen. Some apps that do this are Velis Auto Brightness and Lux. This can be confusing because the user might not know why they can't press the button or how to work around it.
* The [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html) won't know the current activity until the first **change** of activity.

## UsageStatsManager
* This technique uses the UsageStatsManager API introduced in Android 5.0 to gain access to a device’s usage history and statistics.
* The malware queries the usage statistics of all the applications for the past two seconds and then computes the most recent activity.
* The malware requests the user to grant a system-level permission, “android.permission.PACKAGE_USAGE_STATS", to use this API.
* This permission can only be granted through the Settings application. In order to overcome this, the malware uses social engineering by programmatically starting the usage access permission activity while masquerading as Google Chrome by mimicking the app’s icon and name.
