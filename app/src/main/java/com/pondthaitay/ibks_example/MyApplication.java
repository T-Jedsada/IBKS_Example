package com.pondthaitay.ibks_example;

import android.app.Application;

import com.estimote.coresdk.common.config.EstimoteSDK;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EstimoteSDK.initialize(this, "jedsada-wisdomlanna-com-s--2sb", "jedsada-wisdomlanna-com-s--2sb");
        EstimoteSDK.enableDebugLogging(true);
    }
}
