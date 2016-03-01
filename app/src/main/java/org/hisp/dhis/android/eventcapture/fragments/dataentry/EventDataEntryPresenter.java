package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;

import rx.Subscription;

public class EventDataEntryPresenter extends AbsPresenter
        implements IEventDataEntryPresenter {
    private IEventDataEntryView eventDataEntryView;
    private Subscription programDataEntryRowSubscription;

    public EventDataEntryPresenter(IEventDataEntryView eventDataEntryView) {
        this.eventDataEntryView = eventDataEntryView;
    }

    @Override
    public void listDataEntryFields() {
//        programDataEntryRowSubscription = D2.programStageDataElements()
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }
}
