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

package org.hisp.dhis.android.eventcapture.presenters;

import android.content.ContentResolver;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.model.AppAccountManager;
import org.hisp.dhis.android.eventcapture.views.fragments.SettingsView;
import org.hisp.dhis.client.sdk.ui.SettingPreferences;

/**
 * This is the presenter, using MVP.
 * This class controls what is shown in the view. (AbsSettingsFragment).
 * <p/>
 * Created by Vladislav Georgiev Alfredov on 1/15/16.
 */
public class SettingsPresenterImpl implements SettingsPresenter {
    public static final String TAG = SettingsPresenterImpl.class.getSimpleName();

    SettingsView settingsView;

    @Override
    public void logout() {
        // D2.signOut();
        ///D2.me().signOut();

        // ActivityUtils.changeDefaultActivity(context, true);
        //context.startActivity(new Intent(settingsView.getActivity(), LoginActivity.class));

        //TODO: When loging out functionality works test the following:
        //log in with 1 user, log out and log in with another.
        //Now sync triggers twice, once for each account. But the app is only logged in with one.
        // Maybe we should remove the account before/during logging out ?
        //removeAccountExplicitly(Account account)
    }

    @Override
    public void synchronize() {
        AppAccountManager.getInstance().syncNow();
    }

    @Override
    public void setUpdateFrequency(int frequency) {
        SettingPreferences.setBackgroundSyncFrequency(frequency);
        AppAccountManager.getInstance().setPeriodicSync((long) (frequency * 60));
    }

    @Override
    public int getUpdateFrequency() {
        return SettingPreferences.getBackgroundSyncFrequency();
    }

    @Override
    public void setBackgroundSynchronisation(Boolean enabled, String warning) {
        SettingPreferences.setBackgroundSyncState(enabled);

        if (enabled) {
            if (!ContentResolver.getMasterSyncAutomatically()) {
                //display a notification to the user to enable synchronization globally.
                settingsView.showMessage(warning);
            }
            synchronize();
            AppAccountManager.getInstance().setPeriodicSync((long) getUpdateFrequency());
        } else {
            AppAccountManager.getInstance().removePeriodicSync();
        }
    }

    @Override
    public Boolean getBackgroundSynchronisation() {
        return SettingPreferences.getBackgroundSyncState();
    }

    @Override
    public Boolean getCrashReports() {
        return SettingPreferences.getCrashReportsState();
    }

    @Override
    public void setCrashReports(Boolean enabled) {
        SettingPreferences.setCrashReportsState(enabled);
    }

    public void setSettingsView(SettingsView settingsView) {
        this.settingsView = settingsView;
    }
}
