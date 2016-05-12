package org.hisp.dhis.android.eventcapture;

import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenter;
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenterImpl;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.optionset.OptionSetInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.OrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class FormModule {

    public FormModule() {
        // explicit empty constructor
    }

    @Provides
    @PerActivity
    public FormSectionPresenter providesFormSectionPresenter(
            @Nullable OrganisationUnitInteractor organisationUnitInteractor,
            @Nullable ProgramInteractor programInteractor,
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor stageSectionInteractor,
            @Nullable EventInteractor eventInteractor,  Logger logger) {
        return new FormSectionPresenterImpl(organisationUnitInteractor, programInteractor,
                programStageInteractor, stageSectionInteractor, eventInteractor, logger);
    }

    @Provides
    public DataEntryPresenter providesDataEntryPresenter(
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor stageSectionInteractor,
            @Nullable ProgramStageDataElementInteractor programStageDataElementInteractor,
            @Nullable OptionSetInteractor optionSetInteractor, Logger logger) {
        return new DataEntryPresenterImpl(programStageInteractor, stageSectionInteractor,
                programStageDataElementInteractor, optionSetInteractor, logger);
    }
}
