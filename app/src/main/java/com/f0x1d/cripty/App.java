package com.f0x1d.cripty;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

public class App extends Application {

    private static App instance;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
