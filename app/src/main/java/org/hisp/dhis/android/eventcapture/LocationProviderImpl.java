/*
 *  Copyright (c) 2016, University of Oslo
 *
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.hisp.dhis.client.sdk.utils.Logger;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class LocationProviderImpl implements LocationProvider {
    private static final String TAG = LocationProviderImpl.class.getName();
    private static final String TAG_LOCATION = LocationListener.class.getName();
    private static final int BUFFER_SIZE = 1;

    private final Context context;
    private final Logger logger;
    private final LocationListener locationListener;
    private Subject<Location, Location> locationSubject;

    public LocationProviderImpl(Context context, Logger log) {
        this.context = context;
        this.logger = log;
        this.locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                logger.d(TAG_LOCATION, "onLocationChanged: new location: " + location);
                //System.out.println("Location updated: " + location);

                locationSubject.onNext(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                logger.d(TAG_LOCATION, "onStatusChanged(): " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                logger.d(TAG_LOCATION, "onProviderEnabled()");
            }

            @Override
            public void onProviderDisabled(String provider) {
                logger.d(TAG_LOCATION, "onProviderEnabled()");
                locationSubject.onCompleted();
            }
        };

        this.locationSubject = ReplaySubject.createWithSize(BUFFER_SIZE);
    }

    /**
     * Call this method to get the observable.
     *
     * @return locationSubject.
     */
    @Override
    public Observable<Location> locations() {
        return locationSubject;
    }

    /**
     * This method expects the client to have checked weather or not the app has
     * the following permission: Manifest.permission.ACCES_FINE_LOCATION.
     */
    @SuppressWarnings("MissingPermission")
    @Override
    public void requestLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void stopUpdates() {
        ((LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE))
                .removeUpdates(locationListener);
        locationSubject.onCompleted();
        locationSubject = ReplaySubject.createWithSize(BUFFER_SIZE);
    }
}