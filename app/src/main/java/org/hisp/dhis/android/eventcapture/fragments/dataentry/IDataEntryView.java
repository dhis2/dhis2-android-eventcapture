package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.List;

public interface IDataEntryView {
    void initializeViewPager(List<ProgramStageSection> programStageSections);
    void setEvent(Event event);
}
