package org.hisp.dhis.android.eventcapture.fragments.itemlist;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.eventcapture.mapper.ItemListRowMapper;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.IItemListRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class ItemListPresenter extends AbsPresenter {

    private IItemListView itemListView;
    private final ItemListRowMapper itemListRowMapper;
    private CompositeSubscription subscriptions;

    public ItemListPresenter(@NonNull IItemListView itemListView) {
        this.itemListView = itemListView;
        this.itemListRowMapper = new ItemListRowMapper();
    }

    @Override
    public void onCreate() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    public void loadEventList(Observable<OrganisationUnit> organisationUnitObservable, Observable<Program> programObservable) {
        if(organisationUnitObservable == null || programObservable == null) {
            return;
        }
        this.getEventsList(
                organisationUnitObservable.toBlocking().first(),
                programObservable.toBlocking().first());
    }

    public void getEventsList(final OrganisationUnit organisationUnit, final Program program) {
        subscriptions.add(D2.programStages().list(program)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<ProgramStage>, ProgramStage>() {
                    @Override
                    public ProgramStage call(List<ProgramStage> programStages) {
                        return programStages.get(0);
                    }
                }).map(new Func1<ProgramStage, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(ProgramStage programStage) {
                        List<ProgramStageDataElement> programStageDataElements;
                        List<ProgramStageDataElement> stageDataElementsIsDisplayInReport = new ArrayList<>();
                        programStageDataElements = D2.programStageDataElements().list(programStage).toBlocking().first();
                        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {
                            if(programStageDataElement.isDisplayInReports()) {
                                stageDataElementsIsDisplayInReport.add(programStageDataElement);
                            }
                        }
                        return stageDataElementsIsDisplayInReport;
                    }
                })
                .zipWith(D2.events().list(organisationUnit, program), new Func2<List<ProgramStageDataElement>, List<Event>, List<IItemListRow>>() {
                    @Override
                    public List<IItemListRow> call(List<ProgramStageDataElement> programStageDataElements, List<Event> events) {
                        Map<String, String> map = new HashMap<>();

                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                            map.put(programStageDataElement.getUId(), "");
                        }

                        List<IItemListRow> eventRows = new ArrayList<>();

                        for (Event event : events) {
                            List<TrackedEntityDataValue> dataValues = event.getTrackedEntityDataValues();

                            for (TrackedEntityDataValue val : dataValues) {
                                if (map.containsKey(val.getDataElement())) {
                                    map.put(val.getDataElement(), val.getValue());
                                }
                            }

                            eventRows.add(itemListRowMapper.transform(event, map));
                        }

                        return eventRows;
                    }
                }).subscribe(new Action1<List<IItemListRow>>() {
                    @Override
                    public void call(List<IItemListRow> eventListRows) {
                        if (itemListView != null) {
                            if(eventListRows.size() == 0) {
                                itemListView.renderItemRowList(itemListRowMapper.getDummyData());
                            }
                            itemListView.renderItemRowList(eventListRows);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.getCause().toString());
                    }
                }));

    }

    public void showItemListRows(List<Event> eventList) {
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

}
