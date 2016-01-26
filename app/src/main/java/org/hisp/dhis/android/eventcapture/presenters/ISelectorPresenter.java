package org.hisp.dhis.android.eventcapture.presenters;


import android.support.v4.app.Fragment;
import android.view.View;

import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

public interface ISelectorPresenter {
    void onCreate();

    Fragment createPickerFragment();

    void registerPickerCallbacks();

    Fragment createItemListFragment();

    void onResume();

    void initializeSynchronization();

}
