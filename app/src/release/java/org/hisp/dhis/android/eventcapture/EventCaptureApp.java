package org.hisp.dhis.android.eventcapture;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.hisp.dhis.android.eventcapture.utils.ReleaseTree;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public final class EventCaptureApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        Timber.plant(new ReleaseTree());
    }
}
