package org.hisp.dhis.android.eventcapture.fragments.selector;

import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

public interface ISelectorView {
    void onFinishLoading();

    void onLoadingError();

    void onStartLoading();

}
