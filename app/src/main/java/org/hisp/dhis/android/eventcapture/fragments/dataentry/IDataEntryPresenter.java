package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.android.eventcapture.utils.IPresenter;
import org.hisp.dhis.client.sdk.models.event.Event;

public interface IDataEntryPresenter extends IPresenter {
    void listProgramStageSections(String programUId);

    void createNewEvent(String organisationUnitId, String programId);
    Event getEvent(String eventUId);
}
