package org.hisp.dhis.android.eventcapture;

import android.app.Application;

import org.hisp.dhis.client.sdk.android.common.D2;

import timber.log.Timber;

public final class EventCaptureApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        // Feed context to D2
        D2.init(this);

        // TODO
        // Add stetho initialization code here
    }
}
