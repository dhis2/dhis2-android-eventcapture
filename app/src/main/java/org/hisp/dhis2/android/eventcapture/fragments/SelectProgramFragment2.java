package org.hisp.dhis2.android.eventcapture.fragments;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.EventAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.rows.ColumnNamesRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.EventItemRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.EventItemStatus;
import org.hisp.dhis2.android.eventcapture.adapters.rows.Row;
import org.hisp.dhis2.android.eventcapture.fragments.dialogs.OrgUnitDialogFragment;
import org.hisp.dhis2.android.eventcapture.fragments.dialogs.ProgramDialogFragment;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;
import org.hisp.dhis2.android.eventcapture.loaders.Query;
import org.hisp.dhis2.android.eventcapture.views.FloatingActionButton;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue$Table;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis2.android.sdk.persistence.models.FailedItem$Table;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectProgramFragment2 extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener,
        OrgUnitDialogFragment.OnOrgUnitSetListener,
        ProgramDialogFragment.OnProgramSetListener,
        LoaderManager.LoaderCallbacks<List<Row>> {
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
        return inflater.inflate(R.layout.fragment_select_program_2, container, false);
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

        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

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
    public Loader<List<Row>> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            return new DbLoader<>(
                    getActivity().getBaseContext(), Event.class,
                    new EventListQuery(mState.getOrgUnitId(), mState.getProgramId()));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Row>> loader, List<Row> data) {
        if (LOADER_ID == loader.getId()) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.swapData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Row>> loader) {
        mAdapter.swapData(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "ID: " + id, Toast.LENGTH_SHORT).show();
        DataEntryFragment4 fragment2 = DataEntryFragment4.newInstance(
                mState.getOrgUnitId(), mState.getProgramId(), id
        );
        mNavigationHandler.switchFragment(fragment2, DataEntryFragment4.TAG);
    }

    private static class EventListQuery implements Query<List<Row>> {
        private final String mOrgUnitId;
        private final String mProgramId;

        public EventListQuery(String orgUnitId, String programId) {
            mOrgUnitId = orgUnitId;
            mProgramId = programId;
        }

        @Override
        public List<Row> query() {
            List<Row> eventRows = new ArrayList<>();

            // create a list of EventItems
            Program selectedProgram = Select.byId(Program.class, mProgramId);
            if (selectedProgram == null || isListEmpty(selectedProgram.getProgramStages())) {
                return eventRows;
            }

            // since this is single event its only 1 stage
            ProgramStage programStage = selectedProgram.getProgramStages().get(0);
            if (programStage == null || isListEmpty(programStage.getProgramStageDataElements())) {
                return eventRows;
            }

            List<ProgramStageDataElement> stageElements = programStage
                    .getProgramStageDataElements();
            if (isListEmpty(stageElements)) {
                return eventRows;
            }

            List<String> elementsToShow = new ArrayList<>();
            ColumnNamesRow columnNames = new ColumnNamesRow();
            for (ProgramStageDataElement stageElement : stageElements) {
                if (stageElement.displayInReports && elementsToShow.size() < 3) {
                    elementsToShow.add(stageElement.dataElement);
                    String name = stageElement.getDataElement().getName();
                    if (elementsToShow.size() == 1) {
                        columnNames.setFirstItem(name);
                    } else if (elementsToShow.size() == 2) {
                        columnNames.setSecondItem(name);
                    } else if (elementsToShow.size() == 3) {
                        columnNames.setThirdItem(name);
                    }
                }
            }
            eventRows.add(columnNames);

            List<Event> events = DataValueController.getEvents(
                    mOrgUnitId, mProgramId
            );
            if (isListEmpty(events)) {
                return eventRows;
            }

            List<Option> options = Select.all(Option.class);
            Map<String, String> codeToName = new HashMap<>();
            for (Option option : options) {
                codeToName.put(option.getCode(), option.getName());
            }

            List<FailedItem> failedEvents = Select.all(
                    FailedItem.class, Condition
                            .column(FailedItem$Table.ITEMTYPE)
                            .is(FailedItem.EVENT)
            );

            Set<String> failedEventIds = new HashSet<>();
            for (FailedItem failedItem : failedEvents) {
                Event event = (Event) failedItem.getItem();
                failedEventIds.add(event.getEvent());
            }

            for (Event event : events) {
                eventRows.add(createEventItem(event, elementsToShow,
                        codeToName, failedEventIds));
            }

            return eventRows;
        }

        private EventItemRow createEventItem(Event event, List<String> elementsToShow,
                                             Map<String, String> codeToName,
                                             Set<String> failedEventIds) {
            EventItemRow eventItem = new EventItemRow();
            System.out.println("EventID: " + event.localId);
            eventItem.setEventId(event.localId);

            if (event.fromServer) {
                eventItem.setStatus(EventItemStatus.SENT);
            } else if (failedEventIds.contains(event.getEvent())) {
                eventItem.setStatus(EventItemStatus.ERROR);
            } else {
                eventItem.setStatus(EventItemStatus.OFFLINE);
            }

            for (int i = 0; i < 3; i++) {
                String dataElement = elementsToShow.get(i);
                if (dataElement != null) {
                    DataValue dataValue = getDataValue(event, dataElement);
                    if (dataValue == null) {
                        continue;
                    }

                    String code = dataValue.value;
                    String name = codeToName.get(code) == null ? code : codeToName.get(code);

                    if (i == 0) {
                        eventItem.setFirstItem(name);
                    } else if (i == 1) {
                        eventItem.setSecondItem(name);
                    } else if (i == 2) {
                        eventItem.setThirdItem(name);
                    }
                }
            }
            return eventItem;
        }

        private DataValue getDataValue(Event event, String dataElement) {
            List<DataValue> dataValues = Select.all(
                    DataValue.class,
                    Condition.column(DataValue$Table.EVENT).is(event.event),
                    Condition.column(DataValue$Table.DATAELEMENT).is(dataElement)
            );

            if (dataValues != null && !dataValues.isEmpty()) {
                return dataValues.get(0);
            } else {
                return null;
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
                DataEntryFragment4 fragment2 = DataEntryFragment4.newInstance(
                        mState.getOrgUnitId(), mState.getProgramId()
                );
                mNavigationHandler.switchFragment(
                        fragment2, DataEntryFragment4.TAG
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

    private static <T> boolean isListEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }
}