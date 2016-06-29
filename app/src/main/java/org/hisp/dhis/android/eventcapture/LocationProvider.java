package org.hisp.dhis.android.eventcapture;

import android.location.Location;

import rx.Observable;

public interface LocationProvider {

    Observable<Location> locations();

    void requestLocation();

    void stopUpdates();

    boolean isBetterLocation(Location location, Location currentBestLocation);
}