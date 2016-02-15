package org.hisp.dhis.android.eventcapture.datasync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A service for the stub authenticator required by the sync adapter framework.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
