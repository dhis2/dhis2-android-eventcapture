package org.hisp.dhis.android.eventcapture.fragments.selector;


public interface ISelectorPresenter {
    void onCreate();

    void onResume();

    void initializeSynchronization(Boolean force);

}
