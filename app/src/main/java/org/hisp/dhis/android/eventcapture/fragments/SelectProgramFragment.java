/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.eventcapture.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.dialogs.ItemStatusDialogFragment;
import org.hisp.dhis.android.eventcapture.fragments.settings.SettingsFragment;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.events.OnRowClick;
import org.hisp.dhis.android.sdk.events.OnTrackerItemClick;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.sdk.persistence.models.BaseSerializableModel;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.ui.adapters.AbsAdapter;
import org.hisp.dhis.android.sdk.ui.adapters.EventAdapter;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.EventItemRow;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.DataEntryFragment;
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment;
import org.hisp.dhis.android.sdk.ui.fragments.selectprogram.SelectProgramFragmentForm;
import org.hisp.dhis.android.sdk.ui.fragments.settings.SettingsFragment;
import org.hisp.dhis.android.sdk.ui.views.FloatingActionButton;
import org.hisp.dhis.android.sdk.utils.UiUtils;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramFragment extends org.hisp.dhis.android.sdk.ui.fragments.selectprogram.SelectProgramFragment {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();

    private FloatingActionButton mRegisterEventButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        new MenuInflater(this.getActivity()).inflate(org.hisp.dhis.android.eventcapture.R.menu.menu_selected_trackedentityinstance, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info=
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        final EventItemRow itemRow = (EventItemRow) mListView.getItemAtPosition(info.position);

        if(item.getTitle().toString().equals(getResources().getString(R.string.go_to_dataentry_fragment)))
        {
            mNavigationHandler.switchFragment(EventDataEntryFragment.newInstance(mState.getOrgUnitId(),mState.getProgramId()
            , MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getUid(),
                    itemRow.getmEvent().getLocalId()), TAG, true);
        }
        else if(item.getTitle().toString().equals(getResources().getString(org.hisp.dhis.android.sdk.R.string.delete)))
        {

            if( !(itemRow.getStatus().equals(OnRowClick.ITEM_STATUS.SENT))) // if not sent to server, present dialog to user
            {

                UiUtils.showConfirmDialog(getActivity(), getActivity().getString(R.string.confirm),
                        getActivity().getString(R.string.warning_delete_unsent_tei),
                        getActivity().getString(R.string.delete), getActivity().getString(R.string.cancel),
                        (R.drawable.ic_event_error),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                itemRow.getmEvent().delete();
                                dialog.dismiss();
                            }
                        });
            }
            else
            {
                //if sent to server, be able to soft delete without annoying the user
                itemRow.getmEvent().delete();
            }
        }
        return true;
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
    protected ProgramType[] getProgramTypes() {
        return new ProgramType[] {
                ProgramType.WITHOUT_REGISTRATION
        };
    }

    protected AbsAdapter getAdapter(Bundle savedInstanceState) {
        return new EventAdapter(getLayoutInflater(savedInstanceState));
    }

    public Loader<SelectProgramFragmentForm> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            modelsToTrack.add(Event.class);
            modelsToTrack.add(FailedItem.class);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack,
                    new SelectProgramFragmentQuery(mState.getOrgUnitId(), mState.getProgramId()));
        }
        return null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onItemClick(OnTrackerItemClick eventClick) {
        if (eventClick.isOnDescriptionClick()) {
            DataEntryFragment fragment = EventDataEntryFragment.newInstance(
                    mState.getOrgUnitId(), mState.getProgramId(),
                    MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getUid(),
                    eventClick.getItem().getLocalId()
            );
            mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG, true);
        } else {
            showStatusDialog(eventClick.getItem());
        }
    }

    @Subscribe
    public void onReceivedUiEvent(UiEvent uiEvent) {
        super.onReceivedUiEvent(uiEvent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_new_event: {
                DataEntryFragment fragment2 = EventDataEntryFragment.newInstance(
                        mState.getOrgUnitId(), mState.getProgramId(),
                        MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getUid()
                );
                mNavigationHandler.switchFragment(
                        fragment2, DataEntryFragment.TAG, true
                );
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == org.hisp.dhis.android.sdk.R.id.action_settings) {
            mNavigationHandler.switchFragment(
                    new SettingsFragment(), SettingsFragment.TAG, true);
        }
        if (id == org.hisp.dhis.android.sdk.R.id.action_about) {
            mNavigationHandler.switchFragment(
                    new AboutUsFragment(), AboutUsFragment.TAG, true);
        }

        return super.onOptionsItemSelected(item);
    }

    public void showStatusDialog(BaseSerializableModel model) {

        ItemStatusDialogFragment fragment = ItemStatusDialogFragment.newInstance(model);
        fragment.show(getChildFragmentManager());
    }

    protected void handleViews(int level) {
        mAdapter.swapData(null);
        switch (level) {
            case 0:
                mRegisterEventButton.hide();
                break;
            case 1:
                mRegisterEventButton.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mNavigationHandler.switchFragment(
                    new SettingsFragment(), SettingsFragment.TAG, true);
        }

        return true;
    }
}