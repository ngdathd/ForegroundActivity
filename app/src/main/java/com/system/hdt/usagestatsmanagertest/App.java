package com.system.hdt.usagestatsmanagertest;

import android.app.Application;


public class App extends Application {

    private static App app;

    public static App getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
