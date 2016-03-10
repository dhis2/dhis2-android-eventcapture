package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.android.eventcapture.utils.IPresenter;

public interface IEventDataEntryPresenter extends IPresenter {
//    void listDataEntryFields(String programStageSectionUid);
    void listDataEntryFieldsWithEventValues(String eventUId, String programStageSectionUid);

}
