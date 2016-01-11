package org.hisp.dhis.android.eventcapture.fragments;


import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

public interface ISelectorPresenter {
    void onCreate();

    Fragment createPickerFragment();

    void onResume();

    Picker getProgramPicker();

    Picker getOrganisationUnitPicker();
}
