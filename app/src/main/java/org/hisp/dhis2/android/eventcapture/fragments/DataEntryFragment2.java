/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.structure.Model;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.DataValueAdapter;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;

import java.util.ArrayList;
import java.util.List;

public class DataEntryFragment2 extends Fragment
        implements LoaderManager.LoaderCallbacks<DataEntryFragment2Form> {
    public static final String TAG = DataEntryFragment2.class.getSimpleName();
    private static final int LOADER_ID = 1;

    private static final String EXTRA_ARGUMENTS = "extra:Arguments";
    private static final String EXTRA_SAVED_INSTANCE_STATE = "extra:savedInstanceState";

    private static final String ORG_UNIT_ID = "extra:orgUnitId";
    private static final String PROGRAM_ID = "extra:ProgramId";
    private static final String EVENT_ID = "extra:EventId";

    private ListView mListView;
    private ProgressBar mProgressBar;
    private DataValueAdapter mAdapter;

    private INavigationHandler mNavigationHandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof INavigationHandler) {
            mNavigationHandler = (INavigationHandler) activity;
        } else {
            throw new IllegalArgumentException("Activity must implement INavigationHandler interface");
        }
    }

    @Override
    public void onDetach() {
        // we need to nullify reference
        // to parent activity in order not to leak it
        mNavigationHandler = null;
        super.onDetach();
    }

    public static DataEntryFragment2 newInstance(String unitId, String programId) {
        DataEntryFragment2 fragment = new DataEntryFragment2();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataEntryFragment2 newInstance(String unitId, String programId,
                                                 long eventId) {
        DataEntryFragment2 fragment = new DataEntryFragment2();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putLong(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mAdapter = new DataValueAdapter(getLayoutInflater(savedInstanceState));
        mListView = (ListView) view.findViewById(R.id.datavalues_listview);
        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putBundle(EXTRA_ARGUMENTS, getArguments());
        argumentsBundle.putBundle(EXTRA_SAVED_INSTANCE_STATE, savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, argumentsBundle, this);
        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<DataEntryFragment2Form> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            // Adding Tables for tracking here is dangerous (since MetaData updates in background
            // can trigger reload of values from db which will reset all fields).
            // Hence, it would be more safe not to track any changes in any tables
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            Bundle fragmentArguments = args.getBundle(EXTRA_ARGUMENTS);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack, new DataEntryFragment2Query(
                    fragmentArguments.getString(ORG_UNIT_ID),
                    fragmentArguments.getString(PROGRAM_ID),
                    -1
            )
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<DataEntryFragment2Form> loader, DataEntryFragment2Form data) {
        if (loader.getId() == LOADER_ID) {
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mAdapter.swap(data.getRows());
        }
    }

    @Override
    public void onLoaderReset(Loader<DataEntryFragment2Form> loader) {
        if (loader.getId() == LOADER_ID) {
            mAdapter.swap(null);
        }
    }
}
