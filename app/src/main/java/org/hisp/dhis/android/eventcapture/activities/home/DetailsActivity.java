package org.hisp.dhis.android.eventcapture.activities.home;


import android.os.Bundle;

import org.hisp.dhis.android.eventcapture.fragments.WrapperFragment;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;

public class DetailsActivity extends HomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachFragment(WrapperFragment.newInstanceWithItemlistFragment(this));
    }
}
