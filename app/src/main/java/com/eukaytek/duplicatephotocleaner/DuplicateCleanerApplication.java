package com.eukaytek.duplicatephotocleaner;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import timber.log.Timber;

public class DuplicateCleanerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        MobileAds.initialize(this, "ca-app-pub-7297936339438250~9446675023");
    }
}
