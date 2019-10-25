package com.system.manager.foregroundactivity.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.system.manager.foregroundactivity.R;
import com.system.manager.foregroundactivity.Utils;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

public class WindowChangeDetectingService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean isServiceActive = Utils.getSharedPref(this, R.string.sharedprefs_key1, false);
        if (isServiceActive && event.getEventType() == TYPE_WINDOW_STATE_CHANGED) {
            ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
            ActivityInfo activityInfo = getActivityInfo(componentName);
            boolean isActivity = activityInfo != null;
            if (isActivity) {
                String activityName = componentName.flattenToShortString();
                Toast.makeText(this, activityName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Ignored
    }

    private ActivityInfo getActivityInfo(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
