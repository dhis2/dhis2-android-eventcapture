package org.hisp.dhis.android.eventcapture.fragments;

import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

public interface ISelectorView {
    void attachPickerFragment();

    void attachItemListFragment();

    PickerFragment getPickerFragment();

    void attachFragment(int resId, Fragment fragment, String tag);

}
