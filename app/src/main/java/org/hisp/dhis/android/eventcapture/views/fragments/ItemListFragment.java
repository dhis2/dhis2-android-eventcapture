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

package org.hisp.dhis.android.eventcapture.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.ItemListPresenter;
import org.hisp.dhis.android.eventcapture.views.activities.DataEntryActivity;
import org.hisp.dhis.android.eventcapture.RxBus;
import org.hisp.dhis.android.eventcapture.views.EventListRow;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRowAdapter;

import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class ItemListFragment extends Fragment implements IItemListView, View.OnClickListener {
    public static final String TAG = ItemListFragment.class.getSimpleName();
    public static final String ORG_UNIT_UID = "extra:orgUnitUId";
    public static final String PROGRAM_UID = "extra:ProgramUId";
    public static final String EVENT_UID = "extra:EventUId";
    public static final String FLOATING_BUTTON_STATE = "state:FloatingButtonState";

    private ItemListRowAdapter itemListRowAdapter;
    private ItemListPresenter itemListPresenter;
    private Subscription busSubscription;
    private RxBus rxBus;
    private Observable<OrganisationUnit> organisationUnitObservable;
    private Observable<Program> programObservable;

    private CircularProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView emptyItemsTextView;
    private Button editColumnsButton;
    private FloatingActionButton floatingActionButton;
    private boolean hideFloatingActionButton;

    public ItemListFragment() {
        //empty constructor
   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemListPresenter = new ItemListPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(org.hisp.dhis.client.sdk.ui.R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rxBus = ((EventCaptureApp) getActivity().getApplication()).getRxBusSingleton();
    }

    @Override
    public void onStart() {
        super.onStart();

        busSubscription = rxBus.toObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof SelectorFragment.OnOrganisationUnitPickerValueUpdated) {
                    SelectorFragment.OnOrganisationUnitPickerValueUpdated onOrgUnitSelectedClick =
                            (SelectorFragment.OnOrganisationUnitPickerValueUpdated) event;
                    setOrganisationUnitObservable(onOrgUnitSelectedClick.getOrganisationUnitObservable());
                }
                if (event instanceof SelectorFragment.OnProgramPickerValueUpdated) {
                    SelectorFragment.OnProgramPickerValueUpdated onProgramSelectedClick =
                            (SelectorFragment.OnProgramPickerValueUpdated) event;
                    setProgramObservable(onProgramSelectedClick.getProgramObservable());
                }

                itemListPresenter.loadEventList
                        (organisationUnitObservable,
                                programObservable);

                //TODO: remove this when the Rx ItemListPresenter NullPointer exception is resolved.
                activate();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (busSubscription != null && !busSubscription.isUnsubscribed()) {
            busSubscription.unsubscribe();
            busSubscription = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void renderItemRowList(List<ItemListRow> itemListRowCollection) {
        if (itemListRowCollection != null) {
            itemListRowAdapter = new ItemListRowAdapter(itemListRowCollection);
            recyclerView.setAdapter(itemListRowAdapter);

            for (ItemListRow itemListRow : itemListRowCollection) {
                if(itemListRow instanceof EventListRow) {
                    final EventListRow eventListRow = (EventListRow) itemListRow;

                    eventListRow.setOnRowClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), DataEntryActivity.class);
                            intent.putExtra(PROGRAM_UID,
                                    programObservable.toBlocking().first().getUId());
                            intent.putExtra(ORG_UNIT_UID,
                                    organisationUnitObservable.toBlocking().first().getUId());
                            intent.putExtra(EVENT_UID, eventListRow.getEvent().getUId());

                            getContext().startActivity(intent);
                        }
                    });
                    activate();
                }
            }
        }
    }

    @Override
    public void viewEditEvent(Object object) {
        if (object instanceof Event) {
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
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_itemlistfragment);
        emptyItemsTextView = (TextView) view.findViewById(R.id.no_items_textview_itemlistfragment);
//        editColumnsButton = (Button) view.findViewById(R.id.edit_columns_itemlistfragment);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (savedInstanceState == null) {
            hideFloatingActionButton = true;
        } else {
            hideFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE, hideFloatingActionButton);
        }

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingactionbutton_itemlistfragment);
        floatingActionButton.setOnClickListener(this);

//        editColumnsButton.setOnClickListener(this);

        if (hideFloatingActionButton) {
            floatingActionButton.hide();
//            editColumnsButton.setVisibility(View.INVISIBLE);
        } else {
            floatingActionButton.show();
//            editColumnsButton.setVisibility(View.VISIBLE);

        }
        progressBar = (CircularProgressBar) view.findViewById(R.id.progress_bar_circular);
    }

    public void setOrganisationUnitObservable(Observable<OrganisationUnit> organisationUnitObservable) {
        this.organisationUnitObservable = organisationUnitObservable;
    }

    public void setProgramObservable(Observable<Program> programObservable) {
        this.programObservable = programObservable;
    }

    public void activate() {
        floatingActionButton.show();
//        editColumnsButton.setVisibility(View.VISIBLE);
        hideFloatingActionButton = false;
    }

    public void deactivate() {
        floatingActionButton.hide();
        hideFloatingActionButton = true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == floatingActionButton.getId()) {
        Intent intent = new Intent(getContext(), DataEntryActivity.class);
        intent.putExtra(PROGRAM_UID,
                programObservable.toBlocking().first().getUId());
        intent.putExtra(ORG_UNIT_UID,
                organisationUnitObservable.toBlocking().first().getUId());
        intent.putExtra(EVENT_UID,"");

        getContext().startActivity(intent);
        }
//        else if(v.getId() == editColumnsButton.getId()) {
//            Timber.d("Edit columns button");
//        }

    }
}
