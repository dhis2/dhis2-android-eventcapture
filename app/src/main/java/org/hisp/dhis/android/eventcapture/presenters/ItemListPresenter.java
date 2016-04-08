/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.presenters;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.android.eventcapture.views.EventListRowImpl;
import org.hisp.dhis.android.eventcapture.views.ItemListView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.EItemListRowStatus;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class ItemListPresenter {
    private ItemListView itemListView;
    private final ItemListRowMapper itemListRowMapper;
    private CompositeSubscription subscriptions;

    public ItemListPresenter(@NonNull ItemListView itemListView) {
        this.itemListView = itemListView;
        this.itemListRowMapper = new ItemListRowMapper();
        this.subscriptions = new CompositeSubscription();
    }
//
//    public void onDestroy() {
//        subscriptions.unsubscribe();
//    }

    public void loadEventList(Observable<OrganisationUnit> organisationUnitObservable, Observable<Program> programObservable) {
        if (organisationUnitObservable == null || programObservable == null) {
            return;
        }
        /*this.getEventsList(
                organisationUnitObservable.toBlocking().first(),
                programObservable.toBlocking().first());*/
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
                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                            if (programStageDataElement.isDisplayInReports()) {
                                stageDataElementsIsDisplayInReport.add(programStageDataElement);
                            }
                        }
                        return stageDataElementsIsDisplayInReport;
                    }
                })
                .zipWith(D2.events().list(organisationUnit, program), new Func2<List<ProgramStageDataElement>, List<Event>, List<ItemListRow>>() {
                    @Override
                    public List<ItemListRow> call(List<ProgramStageDataElement> programStageDataElements, List<Event> events) {
                        Map<String, String> map = new HashMap<>();

                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                            map.put(programStageDataElement.getUId(), "");
                        }

                        List<ItemListRow> eventRows = new ArrayList<>();

                        for (Event event : events) {
                            List<TrackedEntityDataValue> dataValues = event.getDataValues();

                            for (TrackedEntityDataValue val : dataValues) {
                                if (map.containsKey(val.getDataElement())) {
                                    map.put(val.getDataElement(), val.getValue());
                                }
                            }

                            eventRows.add(itemListRowMapper.transform(event, map));
                        }

                        return eventRows;
                    }
                }).subscribe(new Action1<List<ItemListRow>>() {
                    @Override
                    public void call(List<ItemListRow> eventListRows) {
                        if (itemListView != null) {
                            if (eventListRows.size() == 0) {
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
        List<ItemListRow> itemListRows = itemListRowMapper.transform(eventList);
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

    private static class ItemListRowMapper {
        public final String TAG = this.getClass().getSimpleName();

        public ItemListRowMapper() {

        }

        public ItemListRow transform(Event event) {
            isNull(event, "Event object must not be null");


            List<TrackedEntityDataValue> trackedEntityDataValues = event.getDataValues();
            EventListRowImpl itemListRow = null;
//        new ItemListRow(event, event.getTrackedEntityDataValues(), event.getStatus());


            return itemListRow;
        }

        public List<ItemListRow> transform(List<Event> events) {

            return null;
        }

        public ItemListRow transform(Event event, Map<String, String> map) {
            List<Pair<String, Integer>> valuePos = new ArrayList<>();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                int i = valuePos.size();
                if (valuePos.size() == 3) {
                    break;
                }
                Map.Entry<String, String> entry = iterator.next();
                valuePos.add(new Pair<>(entry.getValue(), i));
                iterator.remove();
            }


            return EventListRowImpl.create(event, valuePos, event.getStatus().name());

        }

        public ItemListRow transformToEventListRow(Program program, Event event) {


            EventListRowImpl eventListRow = EventListRowImpl.create(event,
                    new ArrayList<Pair<String, Integer>>(), event.getStatus().name());
            return eventListRow;
        }

        public List<ItemListRow> getDummyData() {
            Event event1 = new Event();
            event1.setUId("001");
            List<Pair<String, Integer>> itemListRow1Values = new ArrayList<>();
            itemListRow1Values.add(new Pair<>("Erling", 1));
            itemListRow1Values.add(new Pair<>("Fjelstad", 2));
            itemListRow1Values.add(new Pair<>("Mann", 3));
            ItemListRow itemListRow1 = EventListRowImpl.create(event1, itemListRow1Values, EItemListRowStatus.OFFLINE.toString());

            Event event2 = new Event();
            event2.setUId("002");
            List<Pair<String, Integer>> itemListRow2Values = new ArrayList<>();
            itemListRow2Values.add(new Pair<>("Simen", 1));
            itemListRow2Values.add(new Pair<>("R", 1));
            itemListRow2Values.add(new Pair<>("Russnes", 2));
            itemListRow2Values.add(new Pair<>("Mann", 3));
            ItemListRow itemListRow2 = EventListRowImpl.create(event2, itemListRow2Values, EItemListRowStatus.SENT.toString());

            Event event3 = new Event();
            event3.setUId("003");
            List<Pair<String, Integer>> itemListRow3Values = new ArrayList<>();
            itemListRow3Values.add(new Pair<>("Araz", 1));
            itemListRow3Values.add(new Pair<>("AB", 1));
            itemListRow3Values.add(new Pair<>("Abishov", 2));
            itemListRow3Values.add(new Pair<>("Man", 3));
            ItemListRow itemListRow3 = EventListRowImpl.create(event3, itemListRow3Values, EItemListRowStatus.ERROR.toString());
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick");
                }
            };
            View.OnClickListener onStatusClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onStatusClick");
                }
            };
            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "onLongClick");
                    return true;
                }
            };
            itemListRow1.setOnRowClickListener(onClickListener);
            itemListRow1.setOnStatusClickListener(onStatusClickListener);
            itemListRow1.setOnLongClickListener(onLongClickListener);
            itemListRow2.setOnRowClickListener(onClickListener);
            itemListRow2.setOnStatusClickListener(onStatusClickListener);
            itemListRow2.setOnLongClickListener(onLongClickListener);
            itemListRow3.setOnRowClickListener(onClickListener);
            itemListRow3.setOnStatusClickListener(onStatusClickListener);
            itemListRow3.setOnLongClickListener(onLongClickListener);
            List<ItemListRow> itemListRows = new ArrayList<>();
            itemListRows.add(itemListRow1);
            itemListRows.add(itemListRow2);
            itemListRows.add(itemListRow3);

            return itemListRows;
        }
    }
}
