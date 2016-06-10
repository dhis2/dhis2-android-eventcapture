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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class LocationProvider {
    private Activity activity;

    // temporary code
    private Subject<Location, Location> locationSubject = ReplaySubject.createWithSize(1);

    public LocationProvider(Activity activity) {
        this.activity = activity;
    }

    public Observable<Location> locations() {
        return locationSubject;
    }

    /**
     * This method expects the client to have checked weather or not the app has
     * the following permission: Manifest.permission.ACCES_FINE_LOCATION.
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressWarnings("MissingPermission")
    public void requestLocation() {
        // the gps coordinates test:

        //acquire a ref to the system LocationManager
        LocationManager locationManager = (LocationManager) activity.getSystemService(
                Context.LOCATION_SERVICE);

        //Define a listener that responds to location changes:
        //will be a listener:
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("HomeActivity", "onLocationChanged: " + location);
                System.out.println("Location updated: " + location);

                locationSubject.onNext(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("Location Status changed");
            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println("Location provider enabled");
                //reinit subject ??
            }

            @Override
            public void onProviderDisabled(String provider) {
                System.out.println("Location provider disabled");

                locationSubject.onCompleted();
            }
        };

        //TODO: evaluate location ?
        //Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        ///if(lastKnown.getTime() + 5000 > DateTime.now().getMillis()) {
        // use the cahced location ?
        //} else {
        //see if this or just use old location:
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        //}
    }
}