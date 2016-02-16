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

package org.hisp.dhis.android.eventcapture.fragments.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.hisp.dhis.client.sdk.ui.fragments.AbsSettingsFragment;

public class SettingsFragment extends AbsSettingsFragment {
    SettingsPresenter mSettingsPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsPresenter = new SettingsPresenter(this);
        mSettingsPresenter.setSettingsFragment(this);
    }

    @Override
    public boolean onBackgroundSynchronizationClick() {
        return false;
    }

    @Override
    public boolean onBackgroundSynchronizationChanged(boolean isEnabled) {
        mSettingsPresenter.setBackgroundSynchronisation(getContext(), isEnabled);
        if (!ContentResolver.getMasterSyncAutomatically() && isEnabled) {
            //warn the user that synchronization is globally off.
        }


        return true;
    }

    @Override
    public boolean onSynchronizationPeriodClick() {
        return false;
    }

    @Override
    public boolean onSynchronizationPeriodChanged(String newPeriod) {
        Log.d("SettingsFragment", "newPeriod = " + newPeriod);
        mSettingsPresenter.setUpdateFrequency(getContext(), Integer.parseInt(newPeriod));
        return true;
    }

    @Override
    public boolean onCrashReportsClick() {
        return false;
    }

    @Override
    public boolean onCrashReportsChanged(boolean isEnabled) {
        mSettingsPresenter.setCrashReports(getContext(), isEnabled);
        return true;
    }

    public void showMessage(CharSequence msg) {
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }
}
