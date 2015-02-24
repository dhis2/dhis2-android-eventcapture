package org.hisp.dhis2.android.eventcapture;

import android.app.Activity;
import android.app.Application;

import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;

/**
 * @author Simen Skogly Russnes on 20.02.15.
 */
public class EventCaptureApplication extends Dhis2Application {

    @Override
    public Class<? extends Activity> getMainActivity() {
        return new MainActivity().getClass();
    }
}
