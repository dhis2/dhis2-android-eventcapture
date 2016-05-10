package org.hisp.dhis.android.eventcapture.presenters;

public interface FormSectionPresenter extends Presenter {
    void createDataEntryForm(String organisationUnitId, String programId);
}
