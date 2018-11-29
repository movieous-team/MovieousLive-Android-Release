package com.movieous.streaming.demo;

import android.app.Application;

import com.movieous.base.Log;
import com.movieous.streaming.UStreamingEnv;

public class UApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UStreamingEnv.setLogLevel(Log.I);
        UStreamingEnv.init(getApplicationContext());
    }
}
