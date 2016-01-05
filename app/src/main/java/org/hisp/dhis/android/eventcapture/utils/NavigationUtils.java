/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.hisp.dhis.android.eventcapture.activities.LogInActivity;
import org.hisp.dhis.android.eventcapture.activities.MainActivity;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public final class NavigationUtils {

    private NavigationUtils() {
        // private constructor
    }

    public static void changeDefaultActivity(Context context, boolean isLogIn) {
        isNull(context, "Context must not be null");

        final ComponentName logInActivity = new ComponentName(context, LogInActivity.class);
        final ComponentName mainActivity = new ComponentName(context, MainActivity.class);

        final int logInActivityState = isLogIn ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        final int mainActivityState = !isLogIn ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

        context.getPackageManager().setComponentEnabledSetting(logInActivity, logInActivityState,
                PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(mainActivity, mainActivityState,
                PackageManager.DONT_KILL_APP);
    }
}
