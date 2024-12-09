package com.ngdat.foregroundactivity;

public class ItemActivityInfo {
    private String activityName;
    private String appName;
    private String packageName;
    private long startTime;
    private long endTime;

    public ItemActivityInfo(String activityName, String appName, String packageName, long startTime, long endTime) {
        this.activityName = activityName;
        this.appName = appName;
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}