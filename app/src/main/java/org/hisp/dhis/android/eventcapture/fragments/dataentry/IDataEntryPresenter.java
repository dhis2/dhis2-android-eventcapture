package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.android.eventcapture.utils.IPresenter;

public interface IDataEntryPresenter extends IPresenter {
    void listDataEntryFields(String programId, int sectionNumber);
}
