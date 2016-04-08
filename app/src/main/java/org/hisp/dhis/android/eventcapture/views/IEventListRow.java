package org.hisp.dhis.android.eventcapture.views;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;

public interface IEventListRow extends ItemListRow {
    Event getEvent();

    void setEvent(Event event);
}
