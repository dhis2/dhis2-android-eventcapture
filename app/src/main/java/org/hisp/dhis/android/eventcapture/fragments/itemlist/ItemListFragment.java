package org.hisp.dhis.android.eventcapture.fragments.itemlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.hisp.dhis.android.eventcapture.fragments.dataentry.DataEntryActivity;
import org.hisp.dhis.android.eventcapture.presenters.ItemListPresenter;
import org.hisp.dhis.android.eventcapture.utils.ActivityUtils;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRowAdapter;

import java.util.List;

public class ItemListFragment extends org.hisp.dhis.client.sdk.ui.fragments.ItemListFragment implements IItemListView {
    public static final String TAG = ItemListFragment.class.getSimpleName();
    ItemListRowAdapter mItemListRowAdapter;
    ItemListPresenter mItemListPresenter;

    public ItemListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemListPresenter = new ItemListPresenter();
        mItemListPresenter.setItemListView(this);

    }

    @Override
    public void renderItemRowList(List<ItemListRow> itemListRowCollection) {
        if(itemListRowCollection!= null) {
            mItemListRowAdapter = new ItemListRowAdapter(itemListRowCollection);
            mRecyclerView.setAdapter(mItemListRowAdapter);

            for(ItemListRow itemListRow : itemListRowCollection) {
                itemListRow.setOnRowClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getContext().startActivity(new Intent(getContext(), DataEntryActivity.class));
                    }
                });
            }
        }
    }

    @Override
    public void viewEditEvent(Object object) {
        if(object instanceof Event) {
            Event event = (Event) object;
            //show dataEntryFragment
        }
    }

    @Override
    public void hideViewRetry() {

    }

    @Override
    public void showViewRetry() {

    }

    @Override
    public void hideViewLoading() {

    }

    @Override
    public void showViewLoading() {

    }

    @Override
    public void showErrorMessage(String error) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mItemListPresenter.onCreate();
    }
}
