package org.hisp.dhis.android.eventcapture;

import org.hisp.dhis.android.eventcapture.model.RxRulesEngine;
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenter;
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenterImpl;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenterImpl;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.optionset.OptionSetInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramRuleActionInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramRuleInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramRuleVariableInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.android.user.CurrentUserInteractor;
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
    public RxRulesEngine providesRuleEngine(
            @Nullable ProgramRuleInteractor programRuleInteractor,
            @Nullable ProgramRuleActionInteractor programRuleActionInteractor,
            @Nullable ProgramRuleVariableInteractor programRuleVariableInteractor,
            @Nullable EventInteractor eventInteractor, Logger logger) {
        return new RxRulesEngine(programRuleInteractor, programRuleActionInteractor,
                programRuleVariableInteractor, eventInteractor, logger);
    }

    @Provides
    @PerActivity
    public FormSectionPresenter providesFormSectionPresenter(
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor stageSectionInteractor,
            @Nullable EventInteractor eventInteractor, RxRulesEngine rxRulesEngine, Logger logger) {
        return new FormSectionPresenterImpl(programStageInteractor,
                stageSectionInteractor, eventInteractor, rxRulesEngine, logger);
    }

    @Provides
    public DataEntryPresenter providesDataEntryPresenter(
            @Nullable CurrentUserInteractor currentUserInteractor,
            @Nullable ProgramStageInteractor programStageInteractor,
            @Nullable ProgramStageSectionInteractor stageSectionInteractor,
            @Nullable ProgramStageDataElementInteractor programStageDataElementInteractor,
            @Nullable OptionSetInteractor optionSetInteractor,
            @Nullable EventInteractor eventInteractor,
            @Nullable TrackedEntityDataValueInteractor dataValueInteractor,
            RxRulesEngine rxRulesEngine, Logger logger) {
        return new DataEntryPresenterImpl(currentUserInteractor, programStageInteractor,
                stageSectionInteractor, programStageDataElementInteractor, optionSetInteractor,
                eventInteractor, dataValueInteractor, rxRulesEngine, logger);
    }
}
