package org.hisp.dhis.android.eventcapture.presenters;

public interface DataEntryPresenter extends Presenter {
    void createDataEntryFormStage(String programStageId);

    void createDataEntryFormSection(String programStageSectionId);
}
