package com.universl.hp.hithatawadinawadan.Util;

import android.app.Application;

import com.universl.hp.hithatawadinawadan.BuildConfig;

import net.gotev.uploadservice.UploadService;

public class Initializer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }

}
