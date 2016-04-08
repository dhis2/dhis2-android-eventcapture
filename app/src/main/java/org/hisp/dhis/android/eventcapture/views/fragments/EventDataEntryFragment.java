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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.EventDataEntryPresenter;
import org.hisp.dhis.android.eventcapture.presenters.IEventDataEntryPresenter;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

public class EventDataEntryFragment extends Fragment implements IEventDataEntryView {
    private RowViewAdapter rowViewAdapter;
    private static String EXTRA_SECTION_UID = "extra:SectionUid";
    private static String EXTRA_EVENT_UID = "extra:EventUid";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventDataEntryFragment newInstance(String eventUId, String sectionUid) {
        EventDataEntryFragment eventDataEntryFragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_EVENT_UID, eventUId);
        args.putString(EXTRA_SECTION_UID, sectionUid);
        eventDataEntryFragment.setArguments(args);

        return eventDataEntryFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eventdataentry, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rowViewAdapter = new RowViewAdapter(getFragmentManager());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_eventdataentry);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rowViewAdapter);
        recyclerView.addItemDecoration(new DividerDecoration(getContext()));

        String eventUId = getArguments().getString(EXTRA_EVENT_UID);
        String sectionUid = getArguments().getString(EXTRA_SECTION_UID);

        IEventDataEntryPresenter eventDataEntryPresenter = new EventDataEntryPresenter(this);
        eventDataEntryPresenter.listDataEntryFields(sectionUid);
    }

    @Override
    public void setDataEntryFields(List<DataEntity> dataEntities) {
        rowViewAdapter.swap(dataEntities);
    }
}
