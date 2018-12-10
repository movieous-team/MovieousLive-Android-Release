package com.movieous.streaming.demo;

import android.app.Application;

import com.movieous.base.Log;
import com.movieous.streaming.UStreamingEnv;
import com.vender.fusdk.FuSDKManager;

public class UApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initUStreaming();
        initFaceunity();
    }

    public void initUStreaming() {
        UStreamingEnv.setLogLevel(Log.I);
        UStreamingEnv.init(getApplicationContext());
    }

    public void initFaceunity() {
        FuSDKManager.init(getApplicationContext());
    }

}
