package org.hisp.dhis.android.eventcapture.views;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.IItemListRow;

public interface IEventListRow extends IItemListRow {
    Event getEvent();

    void setEvent(Event event);
}
