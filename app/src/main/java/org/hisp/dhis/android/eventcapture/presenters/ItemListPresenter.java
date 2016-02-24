package org.hisp.dhis.android.eventcapture.presenters;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.eventcapture.mapper.ItemListRowMapper;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class ItemListPresenter extends AbsPresenter {

    private IItemListView mItemListView;
    private final ItemListRowMapper itemListRowMapper;

    public ItemListPresenter() {
        this.itemListRowMapper = new ItemListRowMapper();
    }

    public void setItemListView(@NonNull IItemListView mItemListView) {
        this.mItemListView = mItemListView;
    }

    @Override
    public void onCreate() {
//        this.loadEventList();
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void loadEventList(OrganisationUnit organisationUnit, Program program) {
        this.getEventsList(organisationUnit, program);
    }

    public void getEventsList(OrganisationUnit organisationUnit, Program program) {
        Subscriber<List<Event>> subscriber = new EventSubscriber();
        showItemListRows(D2.events().list(organisationUnit, program));

    }

    public void showItemListRows(List<Event> eventList) {
        List<ItemListRow> itemListRows = itemListRowMapper.transform(eventList);
        mItemListView.renderItemRowList(itemListRows);
    }

    public void showItemListRows(Observable<List<Event>> eventList) {
        List<ItemListRow> itemListRows = itemListRowMapper.transform(eventList);
        mItemListView.renderItemRowList(itemListRows);
    }

    private void showViewLoading() {
        this.mItemListView.showViewLoading();
    }

    private void hideViewLoading() {
        this.mItemListView.hideViewLoading();
    }

    private void showViewRetry() {
        this.mItemListView.showViewRetry();
    }

    private void hideViewRetry() {
        this.mItemListView.hideViewRetry();
    }

    private void showErrorMessage(String error) {
        this.mItemListView.showErrorMessage(error);
    }

    private final class EventSubscriber extends Subscriber<List<Event>> {

        @Override
        public void onCompleted() {
//            ItemListPresenter.this.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            showErrorMessage(e.getMessage());
            showViewRetry();
            hideViewLoading();
        }

        @Override
        public void onNext(List<Event> events) {
            ItemListPresenter.this.showItemListRows(events);
        }
    }
}
