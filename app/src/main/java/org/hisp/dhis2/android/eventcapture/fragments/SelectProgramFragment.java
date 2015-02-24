package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.controllers.ResponseHolder;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simen Skogly Russnes on 20.02.15.
 */
public class SelectProgramFragment extends Fragment {

    private List<OrganisationUnit> assignedOrganisationUnits;
    private OrganisationUnit selectedOrganisationUnit;
    private List<Program> programsForSelectedOrganisationUnit;

    private Spinner organisationUnitSpinner;
    private Spinner programSpinner;
    private Button registerButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_select_program,
                container, false);
        setupUi(rootView);
        return rootView;
    }

    public void setupUi(View rootView) {
        organisationUnitSpinner = (Spinner) rootView.findViewById(R.id.selectprogram_orgunit_spinner);
        programSpinner = (Spinner) rootView.findViewById(R.id.selectprogram_program_spinner);
        registerButton = (Button) rootView.findViewById(R.id.selectprogram_register_button);
        assignedOrganisationUnits = Dhis2.getInstance().
                getMetaDataController().getAssignedOrganisationUnits();
        if( assignedOrganisationUnits==null || assignedOrganisationUnits.size() <= 0 ) return;

        List<String> organisationUnitNames = new ArrayList<String>();
        for( OrganisationUnit ou: assignedOrganisationUnits )
            organisationUnitNames.add(ou.getLabel());
        populateSpinner(organisationUnitSpinner, organisationUnitNames);

        organisationUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOrganisationUnit = assignedOrganisationUnits.get(position); //displaying first as default
                programsForSelectedOrganisationUnit = Dhis2.getInstance().getMetaDataController().
                        getProgramsForOrganisationUnit(selectedOrganisationUnit.getId());
                if(programsForSelectedOrganisationUnit == null ||
                        programsForSelectedOrganisationUnit.size() <= 0) {
                    populateSpinner(programSpinner, new ArrayList<String>());
                } else {
                    List<String> programNames = new ArrayList<String>();
                    for( Program p: programsForSelectedOrganisationUnit )
                        programNames.add(p.getName());
                    populateSpinner(programSpinner, programNames);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterEventFragment();
            }
        });
    }

    public void populateSpinner( Spinner spinner, List<String> list )
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity(),
                android.R.layout.simple_spinner_dropdown_item, list );
        spinner.setAdapter( adapter );
    }

    public void showRegisterEventFragment() {
        MessageEvent event = new MessageEvent(BaseEvent.EventType.showRegisterEventFragment);
        Dhis2Application.bus.post(event);
    }

    public OrganisationUnit getSelectedOrganisationUnit() {
        return selectedOrganisationUnit;
    }

    public Program getSelectedProgram() {
        Program selectedProgram = programsForSelectedOrganisationUnit.
                get(programSpinner.getSelectedItemPosition());
        return selectedProgram;
    }
}
