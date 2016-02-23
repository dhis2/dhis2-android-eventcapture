package org.hisp.dhis.android.eventcapture.fragments.itemlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.fragments.dataentry.DataEntryActivity;
import org.hisp.dhis.android.eventcapture.fragments.selector.SelectorFragment;
import org.hisp.dhis.android.eventcapture.presenters.ItemListPresenter;
import org.hisp.dhis.android.eventcapture.utils.ActivityUtils;
import org.hisp.dhis.android.eventcapture.utils.RxBus;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRowAdapter;

import java.util.List;

import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class ItemListFragment extends org.hisp.dhis.client.sdk.ui.fragments.ItemListFragment implements IItemListView {
    public static final String TAG = ItemListFragment.class.getSimpleName();
    private ItemListRowAdapter mItemListRowAdapter;
    private ItemListPresenter mItemListPresenter;
    private CompositeSubscription compositeSubscription;
    private RxBus rxBus;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rxBus = ((EventCaptureApp) getActivity().getApplication()).getRxBusSingleton();
    }

    @Override
    public void onStart() {
        super.onStart();

        compositeSubscription = new CompositeSubscription();

        ConnectableObservable<Object> tapEvent = rxBus.toObserverable().publish();

        compositeSubscription.add(tapEvent.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if(event instanceof SelectorFragment.OnOrganisationUnitPickerValueUpdated) {
                    Log.d("TAG", "onOrganisationUnitPickerValueUpdated");
                }
                if(event instanceof SelectorFragment.OnProgramPickerValueUpdated) {
                    Log.d("TAG", "onProgramPickerValueUpdated");
                }
            }
        }));
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
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
