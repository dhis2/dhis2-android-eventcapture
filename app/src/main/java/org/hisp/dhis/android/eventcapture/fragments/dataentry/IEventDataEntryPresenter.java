package org.hisp.dhis.android.eventcapture.fragments.dataentry;


public interface IEventDataEntryPresenter {
    void listDataEntryFields(String programStageSectionUid);

    void listDataEntryFieldsWithEventValues(String eventUId, String programStageSectionUid);
}
