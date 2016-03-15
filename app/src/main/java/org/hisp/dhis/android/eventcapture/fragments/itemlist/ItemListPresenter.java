package org.hisp.dhis.android.eventcapture.fragments.itemlist;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.eventcapture.mapper.ItemListRowMapper;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.IItemListRow;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class ItemListPresenter extends AbsPresenter {

    private IItemListView itemListView;
    private final ItemListRowMapper itemListRowMapper;

    public ItemListPresenter(@NonNull IItemListView itemListView) {
        this.itemListView = itemListView;
        this.itemListRowMapper = new ItemListRowMapper();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void loadEventList(Observable<OrganisationUnit> organisationUnitObservable, Observable<Program> programObservable) {
        if(organisationUnitObservable == null || programObservable == null) {
            return;
        }
        this.getEventsList(
                organisationUnitObservable.toBlocking().first(),
                programObservable.toBlocking().first());
    }

    public void getEventsList(OrganisationUnit organisationUnit, Program program) {
        Subscriber<List<Event>> subscriber = new EventSubscriber();
        showItemListRows(D2.events().list(organisationUnit, program));

    }

    public void showItemListRows(List<Event> eventList) {
        List<IItemListRow> itemListRows = itemListRowMapper.transform(eventList);
        itemListView.renderItemRowList(itemListRows);
    }

    public void showItemListRows(Observable<List<Event>> eventList) {
        List<IItemListRow> itemListRows = itemListRowMapper.transform(eventList);
        itemListView.renderItemRowList(itemListRows);
    }

    private void showViewLoading() {
        this.itemListView.showViewLoading();
    }

    private void hideViewLoading() {
        this.itemListView.hideViewLoading();
    }

    private void showViewRetry() {
        this.itemListView.showViewRetry();
    }

    private void hideViewRetry() {
        this.itemListView.hideViewRetry();
    }

    private void showErrorMessage(String error) {
        this.itemListView.showErrorMessage(error);
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
