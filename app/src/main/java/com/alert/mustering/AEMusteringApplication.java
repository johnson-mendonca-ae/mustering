package com.alert.mustering;

import android.app.Application;
import android.util.Log;

public class AEMusteringApplication extends Application {
    private static final String TAG = AEMusteringApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AEMusteringApplication onCreate()");
    }
}
