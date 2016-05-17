package org.hisp.dhis.android.eventcapture.presenters;

import org.joda.time.DateTime;

public interface FormSectionPresenter extends Presenter {
    void createDataEntryForm(String eventUid);

    void saveEventDate(String eventUid, DateTime eventDate);
}
