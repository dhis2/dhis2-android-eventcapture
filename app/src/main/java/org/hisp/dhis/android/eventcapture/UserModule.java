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

package org.hisp.dhis.android.eventcapture;

import org.hisp.dhis.android.eventcapture.presenters.HomePresenter;
import org.hisp.dhis.android.eventcapture.presenters.HomePresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.LauncherPresenter;
import org.hisp.dhis.android.eventcapture.presenters.LauncherPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.LoginPresenter;
import org.hisp.dhis.android.eventcapture.presenters.LoginPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.ProfilePresenter;
import org.hisp.dhis.android.eventcapture.presenters.ProfilePresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.SettingsPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SettingsPresenterImpl;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.android.user.CurrentUserInteractor;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;


@Module
public class UserModule {

    public UserModule() {
        // in cases when we already configured D2
    }

    public UserModule(String serverUrl) {
        // it can throw exception in case if configuration has failed
        Configuration configuration = new Configuration(serverUrl);
        D2.configure(configuration).toBlocking().first();
    }

    @Provides
    @Nullable
    @PerUser
    public CurrentUserInteractor providesUserAccountInteractor() {
        if (D2.isConfigured()) {
            return D2.me();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public UserOrganisationUnitInteractor providesUserOrganisationUnitInteractor() {
        if (D2.isConfigured()) {
            return D2.me().organisationUnits();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public UserProgramInteractor providesUserProgramInteractor() {
        if (D2.isConfigured()) {
            return D2.me().programs();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public ProgramStageInteractor providesProgramStageInteractor() {
        if (D2.isConfigured()) {
            return D2.programStages();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public ProgramStageSectionInteractor providesProgramStageSectionInteractor() {
        if (D2.isConfigured()) {
            return D2.programStageSections();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public ProgramStageDataElementInteractor providesProgramStageDataElementInteractor() {
        if (D2.isConfigured()) {
            return D2.programStageDataElements();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public OrganisationUnitInteractor providesOrganisationUnitInteractor() {
        if (D2.isConfigured()) {
            return D2.organisationUnits();
        }

        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public ProgramInteractor providesProgramInteractor() {
        if (D2.isConfigured()) {
            return D2.programs();
        }

        return null;
    }

    @Provides
    @PerUser
    public LauncherPresenter providesLauncherPresenter(
            @Nullable CurrentUserInteractor accountInteractor) {
        return new LauncherPresenterImpl(accountInteractor);
    }

    @Provides
    @PerUser
    public LoginPresenter providesLoginPresenter(
            @Nullable CurrentUserInteractor accountInteractor, Logger logger) {
        return new LoginPresenterImpl(accountInteractor, logger);
    }

    @Provides
    @PerUser
    public HomePresenter providesHomerPresenter(
            @Nullable CurrentUserInteractor accountInteractor, Logger logger) {
        return new HomePresenterImpl(accountInteractor, logger);
    }

    @Provides
    @PerUser
    public SelectorPresenter providesSelectorPresenter(
            @Nullable UserOrganisationUnitInteractor interactor,
            @Nullable UserProgramInteractor programInteractor,
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor programStageSectionInteractor,
            @Nullable ProgramStageDataElementInteractor programStageDataElementInteractor,
            Logger logger) {
        return new SelectorPresenterImpl(
                interactor, programInteractor, programStageInteractor,
                programStageSectionInteractor, programStageDataElementInteractor, logger);
    }

    @Provides
    @PerUser
    public SettingsPresenter provideSettingsPresenter() {
        return new SettingsPresenterImpl();
    }

    @Provides
    @PerUser
    public ProfilePresenter providesProfilePresenter(
            @Nullable CurrentUserInteractor userAccountInteractor, Logger logger) {
        return new ProfilePresenterImpl(userAccountInteractor, logger);
    }
}
