package org.hisp.dhis.android.eventcapture.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.eventcapture.EventCaptureApplication;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.network.http.ApiRequestCallback;
import org.hisp.dhis.android.sdk.network.http.Response;
import org.hisp.dhis.android.sdk.utils.APIException;
import org.hisp.dhis.android.sdk.utils.ui.adapters.EventAdapter;
import org.hisp.dhis.android.sdk.utils.ui.adapters.rows.events.EventRow;
import org.hisp.dhis.android.sdk.utils.OnEventClick;
import org.hisp.dhis.android.sdk.fragments.dataentry.DataEntryFragment;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.eventcapture.views.FloatingActionButton;
import org.hisp.dhis.android.sdk.activities.INavigationHandler;
import org.hisp.dhis.android.sdk.controllers.Dhis2;
import org.hisp.dhis.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.utils.ui.dialogs.AutoCompleteDialogFragment;
import org.hisp.dhis.android.sdk.utils.ui.views.CardTextViewButton;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramFragment extends Fragment
        implements View.OnClickListener, AutoCompleteDialogFragment.OnOptionSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<EventRow>> {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    private static final String STATE = "state:SelectProgramFragment";
    private static final int LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private EventAdapter mAdapter;

    private CardTextViewButton mOrgUnitButton;
    private CardTextViewButton mProgramButton;
    private FloatingActionButton mRegisterEventButton;

    private SelectProgramFragmentState mState;
    private SelectProgramFragmentPreferences mPrefs;

    private INavigationHandler mNavigationHandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof INavigationHandler) {
            mNavigationHandler = (INavigationHandler) activity;
        } else {
            throw new IllegalArgumentException("Activity must " +
                    "implement INavigationHandler interface");
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

        setRefreshing(Dhis2.getInstance().isLoading());

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
                    new SettingsFragment(), SettingsFragment.TAG, true);
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
            case OrgUnitDialogFragment.ID: {
                onUnitSelected(id, name);
                break;
            }
            case ProgramDialogFragment.ID: {
                onProgramSelected(id, name);
                break;
            }
        }
    }

    @Override
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

    @Override
    public void onLoadFinished(Loader<List<EventRow>> loader, List<EventRow> data) {
        if (LOADER_ID == loader.getId()) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.swapData(data);
            setRefreshing(false);
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
                    MetaDataController.getProgram(mState.getProgramId()).getProgramStages().get(0).getId(),
                    eventClick.getEvent().getLocalId()
            );
            mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG, true);
        } else {
            switch (eventClick.getStatus()) {
                case SENT:
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.event_sent),
                            getString(R.string.status_sent_description),
                            R.drawable.ic_from_server
                    );
                    break;
                case OFFLINE:
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.event_offline),
                            getString(R.string.status_offline_description),
                            R.drawable.ic_offline
                    );
                    break;
                case ERROR: {
                    String message = getErrorDescription(eventClick.getEvent());
                    Dhis2.getInstance().showErrorDialog(getActivity(),
                            getString(R.string.event_error),
                            message, R.drawable.ic_event_error
                    );
                    break;
                }
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
            Toast.makeText(context, getString(R.string.syncing), Toast.LENGTH_SHORT).show();
            ApiRequestCallback callback = new ApiRequestCallback() {
                @Override
                public void onSuccess(Response response) {
                    onRefreshFinished();
                }

                @Override
                public void onFailure(APIException exception) {
                    onRefreshFinished();
                }
            };
            Dhis2.synchronize(context, callback);
        }
    }

    private void setRefreshing(final boolean refreshing) {
        /* workaround for bug in android support v4 library */
        if (mSwipeRefreshLayout.isRefreshing() != refreshing) {
            System.out.println("VIEW: " + mSwipeRefreshLayout.isRefreshing() +
                    " BOOL: " + refreshing);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(refreshing);
                }
            });
        }
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

    public void onUnitSelected(String orgUnitId, String orgUnitLabel) {
        mOrgUnitButton.setText(orgUnitLabel);
        mProgramButton.setEnabled(true);

        mState.setOrgUnit(orgUnitId, orgUnitLabel);
        mState.resetProgram();

        mPrefs.putOrgUnit(new Pair<>(orgUnitId, orgUnitLabel));
        mPrefs.putProgram(null);

        handleViews(0);
    }

    public void onProgramSelected(String programId, String programName) {
        mProgramButton.setText(programName);

        mState.setProgram(programId, programName);
        mPrefs.putProgram(new Pair<>(programId, programName));
        handleViews(1);

        mProgressBar.setVisibility(View.VISIBLE);
        // this call will trigger onCreateLoader method
        getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
    }

    private String getErrorDescription(Event event) {
        FailedItem failedItem = DataValueController.getFailedItem(FailedItem.EVENT, event.getLocalId());

        if (failedItem != null) {
            if (failedItem.getHttpStatusCode() == 200) {
                if(failedItem.getImportSummary()!=null)
                    return failedItem.getImportSummary().getDescription();
            }
            if (failedItem.getHttpStatusCode() == 401) {
                return getString(R.string.error_401_description);
            }

            if (failedItem.getHttpStatusCode()== 408) {
                return getString(R.string.error_408_description);
            }

            if (failedItem.getHttpStatusCode() >= 400 && failedItem.getHttpStatusCode()< 500) {
                return getString(R.string.error_series_400_description);
            }

            if (failedItem.getHttpStatusCode()>= 500) {
                return failedItem.getErrorMessage();
            }
        }

        return getString(R.string.unknown_error);
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