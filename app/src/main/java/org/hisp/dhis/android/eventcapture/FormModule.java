package org.hisp.dhis.android.eventcapture;

import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenter;
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenterImpl;
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
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor stageSectionInteractor, Logger logger) {
        return new FormSectionPresenterImpl(programStageInteractor, stageSectionInteractor, logger);
    }

    @Provides
    public DataEntryPresenter providesDataEntryPresenter(
            @Nullable ProgramStageSectionInteractor stageSectionInteractor,
            @Nullable ProgramStageDataElementInteractor programStageDataElementInteractor,
            Logger logger) {
        return new DataEntryPresenterImpl(stageSectionInteractor,
                programStageDataElementInteractor, logger);
    }
}
