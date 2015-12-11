package org.hisp.dhis.android.eventcapture;

import android.app.Application;

import org.hisp.dhis.android.eventcapture.utils.ReleaseTree;

import timber.log.Timber;

public class EventCaptureApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new ReleaseTree());
    }
}
