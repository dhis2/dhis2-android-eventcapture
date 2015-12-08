/*
 * Copyright (c) 2015, University of Oslo
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

package org.hisp.dhis.android.eventcapture.fragments.selectprogram;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.hisp.dhis.android.eventcapture.activities.ISynchronizable;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.adapters.AbsAdapter;
import org.hisp.dhis.android.eventcapture.adapters.EventAdapter;
import org.hisp.dhis.android.eventcapture.dialogs.AutoCompleteDialogFragment;
import org.hisp.dhis.android.eventcapture.activities.INavigationHandler;
import org.hisp.dhis.android.sdk.common.D2;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;

import java.util.List;

import rx.Observable;

public class SelectProgramFragment extends Fragment
        implements View.OnClickListener, AutoCompleteDialogFragment.OnOptionSelectedListener,
        SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    protected final String STATE = "state:SelectProgramFragment";

    private FloatingActionButton mRegisterEventButton;

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ListView mListView;
    protected ProgressBar mProgressBar;
    protected AbsAdapter mAdapter;

    protected TextView mOrganisationUnitSelector;
    protected TextView mProgramSelector;

    protected SelectProgramFragmentState mState;
    protected SelectProgramFragmentPreferences mPrefs;

    protected INavigationHandler mNavigationHandler;
    private ISynchronizable mSynchronizable;

    private static final int SELECT_ORGANISATION_UNIT_DIALOG_ID = 2;
    private static final int SELECT_PROGRAM_DIALOG_ID = 1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof INavigationHandler) {
            mNavigationHandler = (INavigationHandler) activity;
        } else {
            throw new IllegalArgumentException("Activity must " +
                    "implement INavigationHandler interface");
        }

        if (activity instanceof ISynchronizable) {
            mSynchronizable = (ISynchronizable) activity;
        } else {
            throw new IllegalArgumentException("Activity must " +
                    "implement ISyncronizable interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // we need to nullify reference
        // to parent activity in order not to leak it
        mNavigationHandler = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_program, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPrefs = new SelectProgramFragmentPreferences(
                getActivity().getApplicationContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.blue, R.color.orange);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mListView = (ListView) view.findViewById(R.id.event_listview);
        mAdapter = new EventAdapter(getLayoutInflater(savedInstanceState));
        View header = getListViewHeader(savedInstanceState);
        mProgressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mOrganisationUnitSelector = (EditText) header.findViewById(R.id.select_organisation_unit);
        mProgramSelector = (EditText) header.findViewById(R.id.select_program);

        mOrganisationUnitSelector.setOnClickListener(this);
        mProgramSelector.setOnClickListener(this);

        mOrganisationUnitSelector.setEnabled(true);
        mProgramSelector.setEnabled(false);
        mListView.addHeaderView(header, TAG, false);
        mListView.setAdapter(mAdapter);
        registerForContextMenu(mListView);

        if (savedInstanceState != null &&
                savedInstanceState.getParcelable(STATE) != null) {
            mState = savedInstanceState.getParcelable(STATE);
        }

        if (mState == null) {
            // restoring last selection of program
            Pair<String, String> orgUnit = mPrefs.getOrgUnit();
            Pair<String, String> program = mPrefs.getProgram();
            mState = new SelectProgramFragmentState();
            if (orgUnit != null) {
                mState.setOrgUnit(orgUnit.first, orgUnit.second);
                if (program != null) {
                    mState.setProgram(program.first, program.second);
                }
            }
        }

        onRestoreState(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        new MenuInflater(this.getActivity()).inflate(org.hisp.dhis.android.eventcapture.R.menu.menu_selected_trackedentityinstance, menu);

    }

    protected View getListViewHeader(Bundle savedInstanceState) {
        View header = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_select_program_header, mListView, false
        );
        mRegisterEventButton = (FloatingActionButton) header.findViewById(R.id.register_new_event);
        mRegisterEventButton.setOnClickListener(this);
        mRegisterEventButton.hide();
        return header;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //mNavigationHandler.switchFragment(
            //        new SettingsFragment(), SettingsFragment.TAG, true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(STATE, mState);
        super.onSaveInstanceState(out);
    }

    @Override
    public void onOptionSelected(int dialogId, int position, String id, String name) {
        switch (dialogId) {
            case SELECT_ORGANISATION_UNIT_DIALOG_ID: {
                onUnitSelected(id, name);
                break;
            }
            case SELECT_PROGRAM_DIALOG_ID: {
                onProgramSelected(id, name);
                break;
            }
        }
    }

    public void onRefreshFinished() {
        setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (isAdded()) {
            Context context = getActivity().getBaseContext();
            Toast.makeText(context, getString(R.string.synchronizing), Toast.LENGTH_SHORT).show();
            mSynchronizable.synchronize();
        }
    }

    protected void setRefreshing(final boolean refreshing) {
        /* workaround for bug in android support v4 library */
        if (mSwipeRefreshLayout.isRefreshing() != refreshing) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(refreshing);
                }
            });
        }
    }

    public void onRestoreState(boolean hasUnits) {
        mOrganisationUnitSelector.setEnabled(hasUnits);
        if (!hasUnits) {
            return;
        }

        SelectProgramFragmentState backedUpState = new SelectProgramFragmentState(mState);
        if (!backedUpState.isOrgUnitEmpty()) {
            onUnitSelected(
                    backedUpState.getOrgUnitId(),
                    backedUpState.getOrgUnitLabel()
            );

            if (!backedUpState.isProgramEmpty()) {
                onProgramSelected(
                        backedUpState.getProgramId(),
                        backedUpState.getProgramName()
                );
            }
        }
    }

    public void onUnitSelected(String orgUnitId, String orgUnitLabel) {
        mOrganisationUnitSelector.setText(orgUnitLabel);
        mProgramSelector.setEnabled(true);

        mState.setOrgUnit(orgUnitId, orgUnitLabel);
        mState.resetProgram();

        mPrefs.putOrgUnit(new Pair<>(orgUnitId, orgUnitLabel));
        mPrefs.putProgram(null);
    }

    public void onProgramSelected(String programId, String programName) {
        mProgramSelector.setText(programName);

        mState.setProgram(programId, programName);
        mPrefs.putProgram(new Pair<>(programId, programName));

        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.select_organisation_unit) {
            AutoCompleteDialogFragment selectOrganisationUnitDialogFragment = new AutoCompleteDialogFragment();
            AutoCompleteDialogFragment.ValueQuerier querier = new AutoCompleteDialogFragment.ValueQuerier() {
                @Override
                public List<String> getValues() {
                    Observable<List<OrganisationUnit>> listObservable = D2.currentUser().listAssignedOrganisationUnits();
                    List<String> values = null;
                    return values;
                }
            };
            selectOrganisationUnitDialogFragment.setValueQuerier(querier);
            selectOrganisationUnitDialogFragment.setDialogId(SELECT_ORGANISATION_UNIT_DIALOG_ID);
        }
    }
}