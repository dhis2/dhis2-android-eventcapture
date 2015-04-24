package org.hisp.dhis2.android.eventcapture.fragments.selectprogram;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.EventCaptureApplication;
import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.EventAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.rows.events.EventRow;
import org.hisp.dhis2.android.eventcapture.events.OnEventClick;
import org.hisp.dhis2.android.eventcapture.fragments.dataentry.DataEntryFragment;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;
import org.hisp.dhis2.android.eventcapture.views.FloatingActionButton;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramFragment extends Fragment
        implements View.OnClickListener, OrgUnitDialogFragment.OnOrgUnitSetListener,
        ProgramDialogFragment.OnProgramSetListener,
        LoaderManager.LoaderCallbacks<List<EventRow>> {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    private static final String STATE = "state:SelectProgramFragment";
    private static final int LOADER_ID = 1;

    private ListView mListView;
    private ProgressBar mProgressBar;
    private EventAdapter mAdapter;

    private CardTextViewButton mOrgUnitButton;
    private CardTextViewButton mProgramButton;
    private FloatingActionButton mRegisterEventButton;

    private SelectProgramFragmentState mState;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_program, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.event_listview);
        mAdapter = new EventAdapter(getLayoutInflater(savedInstanceState));
        View header = getLayoutInflater(savedInstanceState).inflate(
                R.layout.fragment_select_program_header, mListView, false
        );
        mProgressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mListView.addHeaderView(header, TAG, false);
        mListView.setAdapter(mAdapter);

        mOrgUnitButton = (CardTextViewButton) header.findViewById(R.id.select_organisation_unit);
        mProgramButton = (CardTextViewButton) header.findViewById(R.id.select_program);
        mRegisterEventButton = (FloatingActionButton) header.findViewById(R.id.register_new_event);

        mOrgUnitButton.setOnClickListener(this);
        mProgramButton.setOnClickListener(this);
        mRegisterEventButton.setOnClickListener(this);

        mOrgUnitButton.setEnabled(true);
        mProgramButton.setEnabled(false);
        mRegisterEventButton.hide();

        if (savedInstanceState != null &&
                savedInstanceState.getParcelable(STATE) != null) {
            mState = savedInstanceState.getParcelable(STATE);
        }

        if (mState == null) {
            mState = new SelectProgramFragmentState();
        }

        onRestoreState(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventCaptureApplication.getEventBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventCaptureApplication.getEventBus().register(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_select_program, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mNavigationHandler.switchFragment(
                    new SettingsFragment(), SettingsFragment.TAG);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(STATE, mState);
        super.onSaveInstanceState(out);
    }

    public void onRestoreState(boolean hasUnits) {
        mOrgUnitButton.setEnabled(hasUnits);
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

    @Override
    public void onUnitSelected(String orgUnitId, String orgUnitLabel) {
        mOrgUnitButton.setText(orgUnitLabel);
        mProgramButton.setEnabled(true);

        mState.setOrgUnit(orgUnitId, orgUnitLabel);
        mState.resetProgram();
        handleViews(0);
    }

    @Override
    public void onProgramSelected(String programId, String programName) {
        mProgramButton.setText(programName);

        mState.setProgram(programId, programName);
        handleViews(1);

        mProgressBar.setVisibility(View.VISIBLE);
        // this call will trigger onCreateLoader method
        getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
    }

    @Override
    public Loader<List<EventRow>> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            // modelsToTrack.add(Event.class);
            // modelsToTrack.add(FailedItem.class);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack,
                    new SelectProgramFragmentQuery(mState.getOrgUnitId(), mState.getProgramId()));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<EventRow>> loader, List<EventRow> data) {
        if (LOADER_ID == loader.getId()) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.swapData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<EventRow>> loader) {
        mAdapter.swapData(null);
    }

    @Subscribe
    public void onItemClick(OnEventClick eventClick) {
        if (eventClick.isOnDescriptionClick()) {
            DataEntryFragment fragment = DataEntryFragment.newInstance(
                    mState.getOrgUnitId(), mState.getProgramId(),
                    eventClick.getEvent().getLocalId()
            );
            mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG);
        } else {
            switch (eventClick.getStatus()) {
                case SENT:
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.status_sent),
                            getString(R.string.status_sent_description),
                            R.drawable.ic_from_server
                    );
                    break;
                case OFFLINE:
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.status_offline),
                            getString(R.string.status_offline_description),
                            R.drawable.ic_offline
                    );
                    break;
                case ERROR:
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.status_error),
                            getString(R.string.status_error_description),
                            R.drawable.ic_event_error
                    );
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_organisation_unit: {
                OrgUnitDialogFragment fragment = OrgUnitDialogFragment
                        .newInstance(this);
                fragment.show(getChildFragmentManager());
                break;
            }
            case R.id.select_program: {
                ProgramDialogFragment fragment = ProgramDialogFragment
                        .newInstance(this, mState.getOrgUnitId());
                fragment.show(getChildFragmentManager());
                break;
            }
            case R.id.register_new_event: {
                DataEntryFragment fragment2 = DataEntryFragment.newInstance(
                        mState.getOrgUnitId(), mState.getProgramId()
                );
                mNavigationHandler.switchFragment(
                        fragment2, DataEntryFragment.TAG
                );
                break;
            }
        }
    }

    private void handleViews(int level) {
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