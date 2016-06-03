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

package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.SettingsPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.AbsSettingsFragment;

import javax.inject.Inject;

public class SettingsFragment extends AbsSettingsFragment implements SettingsView {

    private String androidSyncWarning;

    @Inject
    SettingsPresenter settingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidSyncWarning = getResources().getString(R.string.sys_sync_disabled_warning);

        ((EventCaptureApp) getActivity().getApplication()).getUserComponent().inject(this);
        settingsPresenter.setSettingsView(this);
    }

    @Override
    public boolean onBackgroundSynchronizationClick() {
        return false;
    }

    @Override
    public boolean onBackgroundSynchronizationChanged(boolean isEnabled) {
        settingsPresenter.setBackgroundSynchronisation(isEnabled, androidSyncWarning);
        return true;
    }

    @Override
    public boolean onSynchronizationPeriodClick() {
        return false;
    }

    @Override
    public boolean onSyncNotificationsChanged(boolean isEnabled) {
        settingsPresenter.setSyncNotifications(isEnabled);
        return true;
    }

    @Override
    public boolean onSynchronizationPeriodChanged(String newPeriodMinutes) {
        settingsPresenter.setUpdateFrequency(Integer.parseInt(newPeriodMinutes));
        return true;
    }

    @Override
    public boolean onCrashReportsClick() {
        return false;
    }

    @Override
    public boolean onCrashReportsChanged(boolean isEnabled) {
        settingsPresenter.setCrashReports(isEnabled);
        return true;
    }

    @Override
    public void showMessage(CharSequence msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }
}
