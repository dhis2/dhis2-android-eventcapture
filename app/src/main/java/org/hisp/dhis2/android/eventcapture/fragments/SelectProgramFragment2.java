package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.EventAdapter;
import org.hisp.dhis2.android.eventcapture.models.EventItem;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue$Table;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramFragment2 extends Fragment
        implements View.OnClickListener, OrgUnitDialogFragment.OnOrgUnitSetListener,
        ProgramDialogFragment.OnProgramSetListener {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    private static final String STATE = "state:SelectProgramFragment";

    private ListView mListView;
    private EventAdapter mAdapter;

    private CardTextViewButton mOrgUnitButton;
    private CardTextViewButton mProgramButton;
    private Button mRegisterEventButton;

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
        mListView.addHeaderView(header);
        mListView.setAdapter(mAdapter);

        mOrgUnitButton = (CardTextViewButton) header.findViewById(R.id.select_organisation_unit);
        mProgramButton = (CardTextViewButton) header.findViewById(R.id.select_program);
        mRegisterEventButton = (Button) header.findViewById(R.id.register_new_event);

        mOrgUnitButton.setOnClickListener(this);
        mProgramButton.setOnClickListener(this);

        mOrgUnitButton.setEnabled(true);
        mProgramButton.setEnabled(false);
        mRegisterEventButton.setEnabled(false);

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
        } else if (id == R.id.action_new_event) {
            Toast.makeText(getActivity(), "Another button", Toast.LENGTH_SHORT).show();
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
        prepareEventsList();
        handleViews(1);
    }

    private void prepareEventsList() {
        // create a list of EventItems
        Program selectedProgram = Select.byId(Program.class, mState.getProgramId());
        if (selectedProgram == null || isListEmpty(selectedProgram.getProgramStages())) {
            return;
        }

        // since this is single event its only 1 stage
        ProgramStage programStage = selectedProgram.getProgramStages().get(0);
        if (programStage == null || isListEmpty(programStage.getProgramStageDataElements())) {
            return;
        }

        List<ProgramStageDataElement> stageElements = programStage
                .getProgramStageDataElements();
        if (isListEmpty(stageElements)) {
            return;
        }

        List<String> elementsToShow = new ArrayList<>();
        for (ProgramStageDataElement stageElement : stageElements) {
            if (stageElement.displayInReports) {
                elementsToShow.add(stageElement.dataElement);
            }
        }

        List<Event> events = DataValueController.getEvents(
                mState.getOrgUnitId(), mState.getProgramId()
        );
        if (isListEmpty(events)) {
            return;
        }

        List<EventItem> eventItems = new ArrayList<>();
        for (Event event : events) {
            eventItems.add(createEventItem(event, elementsToShow));
        }
        System.out.println("EventItems: " + eventItems.size());
        mAdapter.swapData(eventItems);
    }

    private EventItem createEventItem(Event event, List<String> elementsToShow) {
        EventItem eventItem = new EventItem();
        for (int i = 0; i < 3; i++) {
            String dataElement = elementsToShow.get(i);
            System.out.println("DataElement: " + dataElement);
            if (dataElement != null) {
                DataValue dataValue = getDataValue(event, dataElement);
                if (dataValue == null) {
                    System.out.println("EMPTY!!!");
                    continue;
                }
                System.out.println("VALUE: " + dataValue.value);
                if (i == 0) {
                    eventItem.setFirstItem(dataValue.value);
                } else if (i == 1) {
                    eventItem.setSecondItem(dataValue.value);
                } else if (i == 2) {
                    eventItem.setThirdItem(dataValue.value);
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
            /*
            case R.id.data_entry_button: {
                startReportEntryActivity();
                break;
            }
            */
        }
    }

    private void handleViews(int level) {
        switch (level) {
            case 0:
                mAdapter.swapData(null);
                mRegisterEventButton.setEnabled(false);
                break;
            case 1:
                mRegisterEventButton.setEnabled(true);
        }
    }

    private static <T> boolean isListEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }
}
