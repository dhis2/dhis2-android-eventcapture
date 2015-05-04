package org.hisp.dhis2.android.eventcapture;

import android.support.v4.app.Fragment;

/**
 * Created by araz on 31.03.2015.
 */
public interface INavigationHandler {
    void switchFragment(Fragment fragment, String tag, boolean addToBackStack);
    void onBackPressed();
}
