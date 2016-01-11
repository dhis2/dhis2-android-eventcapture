package org.hisp.dhis.android.eventcapture.fragments;


import android.support.v4.app.Fragment;

public interface ISelectorPresenter {
    void onCreate();

    Fragment createPickerFragment();

    void onResume();
}
