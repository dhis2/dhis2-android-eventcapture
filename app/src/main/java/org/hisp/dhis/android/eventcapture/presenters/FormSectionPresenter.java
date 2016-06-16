package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.LocationProvider;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.Presenter;
import org.joda.time.DateTime;

public interface FormSectionPresenter extends Presenter {
    void createDataEntryForm(String eventUid);

    void saveEventDate(String eventUid, DateTime eventDate);

    void saveEventStatus(String eventUid, Event.EventStatus eventStatus);

    void subscribeToLocations(LocationProvider locationProvider);
}
