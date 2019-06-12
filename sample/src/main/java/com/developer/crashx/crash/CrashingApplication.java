package com.developer.crashx.crash;

import android.app.Application;
import android.util.Log;

import com.developer.crashx.CrashActivity;
import com.developer.crashx.config.CrashConfig;

public class CrashingApplication extends Application {

    private static final String TAG = "CrashX";

    @Override
    public void onCreate() {
        super.onCreate();

        CrashConfig.Builder.create()
                .apply();
    }

    private static class CustomEventListener implements CrashActivity.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            Log.i(TAG, "onLaunchErrorActivity()");
        }

        @Override
        public void onRestartAppFromErrorActivity() {
            Log.i(TAG, "onRestartAppFromErrorActivity()");
        }

        @Override
        public void onCloseAppFromErrorActivity() {
            Log.i(TAG, "onCloseAppFromErrorActivity()");
        }
    }
}
