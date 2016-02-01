package org.hisp.dhis.android.eventcapture.fragments.selector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;

public class ContainerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater linf, ViewGroup viewGroup, Bundle bundle) {
        return linf.inflate(R.layout.fragment_container, viewGroup, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        View contentFrameOne = view.findViewById(R.id.selector_container);
        View contentFrameTwo = view.findViewById(R.id.itemlist_container);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.selector_container, new SelectorFragment())
                .commit();

        if(contentFrameTwo != null) {
            Log.d("ContainerFrag", "landscape");
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.itemlist_container, new ItemListFragment())
                    .commit();
        }
    }
}
