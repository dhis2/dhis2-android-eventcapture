package org.hisp.dhis.android.eventcapture.presenters;

public interface DataEntryPresenter extends Presenter {
    void createDataEntryFormStage(String eventid, String programStageId);

    void createDataEntryFormSection(String eventId, String programStageSectionId);
}
