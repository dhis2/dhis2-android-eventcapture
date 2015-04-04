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

package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.InvalidateEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue$Table;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.AttributeListAdapter;
import org.hisp.dhis2.android.sdk.utils.Utils;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * @author Simen Skogly Russnes on 20.02.15.
 */
public class SelectProgramFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    public static final String TAG = SelectProgramFragment.class.getSimpleName();

    private List<OrganisationUnit> assignedOrganisationUnits;
    private List<Program> programsForSelectedOrganisationUnit;
    private List<Event> displayedExistingEvents;

    //private Button registerButton;
    private CardSpinner organisationUnitSpinner;
    private CardSpinner programSpinner;
    private ListView existingEventsListView;
    private LinearLayout attributeNameContainer;
    private LinearLayout rowContainer;

    /*private int programSelection;
    private int orgunitSelection;*/

    private OrganisationUnit selectedOrganisationUnit;
    private Program selectedProgram;

    private Map<String, String> mOptionToNameMap;

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
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        Dhis2Application.bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Dhis2Application.bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_data_entry, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mNavigationHandler.switchFragment(
                    new SettingsFragment(), SettingsFragment.TAG);
            // showSettingsFragment();
        } else if (id == R.id.action_new_event) {
            showRegisterEventFragment();
            // showRegisterEventFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showRegisterEventFragment() {
        // setTitle("Register Event");
        DataEntryFragment fragment = DataEntryFragment.newInstance(
                selectedOrganisationUnit, selectedProgram
        );
        mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG);
        /* dataEntryFragment = new DataEntryFragment();
        OrganisationUnit organisationUnit = selectProgramFragment.getSelectedOrganisationUnit();
        Program program = selectProgramFragment.getSelectedProgram();
        dataEntryFragment.setSelectedOrganisationUnit(organisationUnit);
        dataEntryFragment.setSelectedProgram(program);
        lastSelectedOrgUnit = selectProgramFragment.getSelectedOrganisationUnitIndex();
        lastSelectedProgram = selectProgramFragment.getSelectedProgramIndex();
        showFragment(dataEntryFragment); */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_program, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        organisationUnitSpinner = (CardSpinner) view.findViewById(R.id.org_unit_spinner);

        programSpinner = (CardSpinner) view.findViewById(R.id.program_spinner);
        //registerButton = (Button) rootView.findViewById(R.id.selectprogram_register_button);
        existingEventsListView = (ListView) view.findViewById(R.id.selectprogram_resultslistview);
        attributeNameContainer = (LinearLayout) view.findViewById(R.id.attributenameslayout);
        rowContainer = (LinearLayout) view.findViewById(R.id.eventrowcontainer);
        assignedOrganisationUnits = Dhis2.getInstance().
                getMetaDataController().getAssignedOrganisationUnits();
        if (assignedOrganisationUnits == null || assignedOrganisationUnits.size() <= 0) {
            existingEventsListView.setAdapter(new AttributeListAdapter(getActivity(), new ArrayList<String[]>()));
            return;
        }

        mOptionToNameMap = new HashMap<>();
        List<Option> options = Select.all(Option.class);
        for (Option option : options) {
            mOptionToNameMap.put(option.getCode(), option.getName());
        }

        List<String> organisationUnitNames = new ArrayList<>();
        for (OrganisationUnit ou : assignedOrganisationUnits) {
            organisationUnitNames.add(ou.getLabel());
        }

        populateSpinner(organisationUnitSpinner, organisationUnitNames);
        organisationUnitSpinner.setOnItemSelectedListener(this);
        programSpinner.setOnItemSelectedListener(this);
        existingEventsListView.setOnItemClickListener(this);

        /*Log.e(TAG, "Setting orgUnit " + orgunitSelection);
        if (assignedOrganisationUnits != null && assignedOrganisationUnits.size() > orgunitSelection) {
            organisationUnitSpinner.setSelection(orgunitSelection);
        }
        Log.e(TAG, "Sat orgUnit " + orgunitSelection);
        if (programsForSelectedOrganisationUnit != null &&
                programsForSelectedOrganisationUnit.size() > programSelection) {
            programSpinner.setSelection(programSelection);
        }
        Log.e(TAG, "Sat program " + programSelection);*/
    }

    public void editEvent(int position) {
        DataEntryFragment fragment = DataEntryFragment.newInstance(
                selectedOrganisationUnit, selectedProgram
        );
        fragment.setEditingEvent(displayedExistingEvents.get(position).localId);
        mNavigationHandler.switchFragment(fragment, DataEntryFragment.TAG);
        /*MessageEvent message = new MessageEvent(BaseEvent.EventType.showDataEntryFragment);
        message.item = displayedExistingEvents.get(position).localId;
        Dhis2Application.bus.post(message);*/
    }

    public void populateSpinner(CardSpinner spinner, List<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_item, list
        );
        spinner.setAdapter(adapter);
    }

    public void invalidate() {
        onProgramSelected(programSpinner.getSelectedItemPosition());
    }

    /* public void showRegisterEventFragment() {
        if (selectedOrganisationUnit == null ||
                programSpinner.getSelectedItemPosition() < 0) {
            return;
        }
        MessageEvent event = new MessageEvent(BaseEvent.EventType.showRegisterEventFragment);
        Dhis2Application.bus.post(event);
    } */

    /*
    public OrganisationUnit getSelectedOrganisationUnit() {
        return selectedOrganisationUnit;
    }

    public Program getSelectedProgram() {
        if (programSpinner.getSelectedItemPosition() < 0) {
            return null;
        }
        Program selectedProgram = programsForSelectedOrganisationUnit.
                get(programSpinner.getSelectedItemPosition());
        return selectedProgram;
    }
    */

    @Subscribe
    public void onReceiveInvalidateMessage(InvalidateEvent event) {
        Log.d(TAG, "invalidate message received");
        if (event.eventType == InvalidateEvent.EventType.event) {
            getActivity().runOnUiThread(new Thread() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    private void onUnitSelected(int position) {
        selectedOrganisationUnit = assignedOrganisationUnits.get(position); //displaying first as default
        selectedProgram = null;
        programsForSelectedOrganisationUnit = Dhis2.getInstance().getMetaDataController().
                getProgramsForOrganisationUnit(selectedOrganisationUnit.getId(),
                        Program.SINGLE_EVENT_WITHOUT_REGISTRATION);
        if (programsForSelectedOrganisationUnit == null ||
                programsForSelectedOrganisationUnit.size() <= 0) {
            populateSpinner(programSpinner, new ArrayList<String>());
            existingEventsListView.setAdapter(new AttributeListAdapter(getActivity(), new ArrayList<String[]>()));
        } else {
            List<String> programNames = new ArrayList<>();
            for (Program p : programsForSelectedOrganisationUnit) {
                programNames.add(p.getName());
            }
            populateSpinner(programSpinner, programNames);
        }
    }

    public void onProgramSelected(int position) {
        if (position < 0) {
            return;
        }
        Log.d(TAG, "onProgramSelected");
        if (programsForSelectedOrganisationUnit != null) {
            selectedProgram = programsForSelectedOrganisationUnit.get(position);
            ProgramStage programStage = selectedProgram.getProgramStages().get(0);
            //since this is single event its only 1 stage

            //get all the events for org unit and program todo: probably should limit it
            displayedExistingEvents = DataValueController.getEvents(selectedOrganisationUnit.id, selectedProgram.id);

            //get data elements to show in list:
            List<ProgramStageDataElement> programStageDataElements =
                    programStage.getProgramStageDataElements();
            List<String> dataElementsToShowInList = new ArrayList<>();
            for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                if (programStageDataElement.displayInReports) {
                    dataElementsToShowInList.add(programStageDataElement.dataElement);
                }
            }

            attributeNameContainer.removeAllViews();
            for (String s : dataElementsToShowInList) {
                TextView tv = new TextView(getActivity());
                tv.setWidth(0);
                tv.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                tv.setText(MetaDataController.getDataElement(s).getName());
                attributeNameContainer.addView(tv);
            }

            //adding invisible imageview to get the same layout width as rows under with status indicator image
            ImageView dummy = new ImageView(getActivity());
            int pxWidth = Utils.getDpPx(20, getResources().getDisplayMetrics());
            dummy.setLayoutParams(new LinearLayout.LayoutParams(pxWidth, pxWidth));
            dummy.setBackgroundResource(R.drawable.ic_server);
            dummy.requestLayout();
            attributeNameContainer.addView(dummy);
            dummy.setVisibility(View.INVISIBLE);


            //get values and show in list
            HashMap<String, String[]> rows = new HashMap<>();
            rowContainer.removeAllViews();
            for (int j = 0; j < displayedExistingEvents.size(); j++) {
                Event event = displayedExistingEvents.get(j);
                String[] row = new String[dataElementsToShowInList.size()];
                LinearLayout v = (LinearLayout) getActivity().getLayoutInflater().inflate(org.hisp.dhis2.android.sdk.R.layout.eventlistlinearlayoutitem, rowContainer, false);
                //v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.getDpPx(50, getResources().getDisplayMetrics())));
                for (int i = 0; i < dataElementsToShowInList.size(); i++) {
                    String dataElement = dataElementsToShowInList.get(i);
                    List<DataValue> result = Select.all(DataValue.class,
                            Condition.column(DataValue$Table.EVENT).is(event.event),
                            Condition.column(DataValue$Table.DATAELEMENT).is(dataElement));
                    if (result != null && !result.isEmpty()) {
                        String code = result.get(0).value;
                        if (!isEmpty(mOptionToNameMap.get(code))) {
                            row[i] = mOptionToNameMap.get(code);
                        } else {
                            row[i] = code;
                        }
                    } else {
                        row[i] = " ";
                    }

                    TextView tv = new TextView(getActivity());
                    tv.setWidth(0);
                    tv.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                    tv.setMaxLines(2);

                    tv.setText(row[i]);
                    v.addView(tv);
                }

                ImageView iv = new ImageView(getActivity());
                iv.setLayoutParams(new LinearLayout.LayoutParams(pxWidth, pxWidth));
                iv.setBackgroundResource(R.drawable.perm_group_display);
                iv.requestLayout();
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dhis2.getInstance().showErrorDialog(getActivity(), getString(R.string.information_message), getString(R.string.offline_item));
                    }
                });
                v.addView(iv);

                if (event.fromServer) {
                    iv.setVisibility(View.INVISIBLE);
                }
                rows.put(event.event, row);

                v.setContentDescription("" + j);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = Integer.parseInt(v.getContentDescription().toString());
                        editEvent(position);
                    }
                });
                rowContainer.addView(getActivity().getLayoutInflater().inflate(R.layout.divider_view, rowContainer, false));
                rowContainer.addView(v);

            }
        } else {
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerId = ((View) parent.getParent()).getId();
        switch (spinnerId) {
            case R.id.org_unit_spinner:
                onUnitSelected(position);
                return;
            case R.id.program_spinner:
                onProgramSelected(position);
                return;
            default:
                throw new IllegalArgumentException("No such spinner");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // stub implementation
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editEvent(position);
    }
}
