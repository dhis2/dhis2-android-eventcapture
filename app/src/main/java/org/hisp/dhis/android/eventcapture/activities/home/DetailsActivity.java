package org.hisp.dhis.android.eventcapture.activities.home;


import android.os.Bundle;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.client.sdk.ui.fragments.WrapperFragment;


public class DetailsActivity extends HomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        attachFragment(WrapperFragment.newInstance(ItemListFragment.class,
                getString(R.string.drawer_item_events)));
    }
}
