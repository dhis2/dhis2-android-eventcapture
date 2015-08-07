package org.hisp.dhis.android.eventcapture.fragments;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;

import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.sdk.controllers.Dhis2;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.events.OnTrackerItemClick;
import org.hisp.dhis.android.sdk.fragments.dataentry.DataEntryFragment;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.utils.ui.adapters.AbsAdapter;
import org.hisp.dhis.android.sdk.utils.ui.adapters.EventAdapter;
import org.hisp.dhis.android.sdk.utils.ui.adapters.rows.events.EventRow;
import org.hisp.dhis.android.sdk.utils.ui.dialogs.ProgramDialogFragment;
import org.hisp.dhis.android.sdk.utils.ui.views.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramFragment extends org.hisp.dhis.android.sdk.fragments.selectprogram.SelectProgramFragment {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();

    private FloatingActionButton mRegisterEventButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
    protected View.OnClickListener getProgramButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgramDialogFragment fragment = ProgramDialogFragment
                        .newInstance(SelectProgramFragment.this, mState.getOrgUnitId(),
                                Program.ProgramType.SINGLE_EVENT_WITHOUT_REGISTRATION,
                                Program.ProgramType.WITHOUT_REGISTRATION);
                fragment.show(getChildFragmentManager());
            }
        };
    }

    protected AbsAdapter getAdapter(Bundle savedInstanceState) {
        return new EventAdapter(getLayoutInflater(savedInstanceState));
    }

    public Loader<List<EventRow>> onCreateLoader(int id, Bundle args) {
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
            DataEntryFragment fragment = DataEntryFragment.newInstance(
                    mState.getOrgUnitId(), mState.getProgramId(),
                    MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getId(),
                    eventClick.getItem().getLocalId()
            );
            mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG, true);
        } else {
            Dhis2.showStatusDialog(getChildFragmentManager(), eventClick.getItem());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_new_event: {
                DataEntryFragment fragment2 = DataEntryFragment.newInstance(
                        mState.getOrgUnitId(), mState.getProgramId(),
                        MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getId()
                );
                mNavigationHandler.switchFragment(
                        fragment2, DataEntryFragment.TAG, true
                );
                break;
            }
        }
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
}