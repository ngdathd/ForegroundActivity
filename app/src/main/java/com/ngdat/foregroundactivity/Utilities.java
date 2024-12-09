package com.ngdat.foregroundactivity;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static final String PREFS_NAME_AccessibilityService = "AccessibilityService_ActivityHistory";
    public static final String PREFS_KEY_AccessibilityService = "AccessibilityService_ActivityList";

    public static List<ItemActivityInfo> loadActivityListFromAccessibilityService(SharedPreferences sharedPreferences) {
        String json = sharedPreferences.getString(PREFS_KEY_AccessibilityService, "[]");
        List<ItemActivityInfo> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String activityName = jsonObject.getString("activityName");
                String appName = jsonObject.getString("appName");
                String packageName = jsonObject.getString("packageName");
                long startTime = jsonObject.getLong("startTime");
                long endTime = jsonObject.optLong("endTime", -1);
                list.add(new ItemActivityInfo(activityName, appName, packageName, startTime, endTime));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
