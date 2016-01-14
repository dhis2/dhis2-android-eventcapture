package org.hisp.dhis.android.eventcapture.fragments.selector;


import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

public interface ISelectorPresenter {
    void onCreate();

    Fragment createPickerFragment();

    void registerPickerCallbacks();

    void onResume();

    Picker getProgramPicker();

    Picker getOrganisationUnitPicker();
}
