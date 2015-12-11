package org.hisp.dhis.android.eventcapture;

import android.app.Application;

import timber.log.Timber;

public final class EventCaptureApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
