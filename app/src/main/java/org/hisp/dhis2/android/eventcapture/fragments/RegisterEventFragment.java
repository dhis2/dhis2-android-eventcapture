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

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.utils.views.BoolDataElementView;
import org.hisp.dhis2.android.sdk.utils.views.DataElementAdapterViewAbstract;
import org.hisp.dhis2.android.sdk.utils.views.DatePickerDataElementView;
import org.hisp.dhis2.android.sdk.utils.views.NumberDataElementView;
import org.hisp.dhis2.android.sdk.utils.views.OptionSetDataElementView;
import org.hisp.dhis2.android.sdk.utils.views.TextDataElementView;
import org.hisp.dhis2.android.sdk.utils.views.TrueOnlyDataElementView;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Simen Skogly Russnes on 20.02.15.
 */
public class RegisterEventFragment extends Fragment {

    private static final String CLASS_TAG = "RegisterEventFragment";

    private OrganisationUnit selectedOrganisationUnit;
    private Program selectedProgram;

    private TextView organisationUnitLabel;
    private TextView programLabel;
    private Button submitButton;
    private Event event;
    private List<DataValue> dataValues;
    private List<ProgramStageDataElement> programStageDataElements;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_register_event,
                container, false);
        setupUi(rootView);
        return rootView;
    }

    public void setupUi(View rootView) {
        organisationUnitLabel = (TextView) rootView.findViewById(R.id.dataentry_orgunitlabel);
        programLabel = (TextView) rootView.findViewById(R.id.dataentry_programlabel);
        submitButton = (Button) rootView.findViewById(R.id.dataentry_submitbutton);

        if(selectedOrganisationUnit == null || selectedProgram == null) return;

        organisationUnitLabel.setText(selectedOrganisationUnit.getLabel());
        programLabel.setText(selectedProgram.getName());

        LinearLayout dataElementContainer = (LinearLayout) rootView.
                findViewById(R.id.dataentry_dataElementContainer);
        setupDataEntryForm(dataElementContainer);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    public void setupDataEntryForm(LinearLayout dataElementContainer) {
        programStageDataElements =
                selectedProgram.getProgramStages().get(0).getProgramStageDataElements();
        event = new Event();
        event.event = Dhis2.QUEUED + UUID.randomUUID().toString();
        event.fromServer = false;
        event.dueDate = Utils.getCurrentDate();
        event.eventDate = Utils.getCurrentDate();
        event.organisationUnitId = selectedOrganisationUnit.getId();
        event.programId = selectedProgram.id;
        event.programStageId = selectedProgram.getProgramStages().get(0).id;
        event.status = Event.STATUS_COMPLETED;
        dataValues = new ArrayList<DataValue>();
        for(int i = 0; i<programStageDataElements.size(); i++) {

            ProgramStageDataElement programStageDataElement = programStageDataElements.get(i);
            dataValues.add(new DataValue(event.event, "",
                    programStageDataElement.dataElement, false,
                    Dhis2.getInstance().getUsername(getActivity())));
            View view = createDataElementView(programStageDataElement, dataValues.get(i));
            dataElementContainer.addView(view);
        }
    }

    public View createDataElementView(ProgramStageDataElement programStageDataElement,
                                      DataValue dataValue) {
        DataElement dataElement = MetaDataController.
                getDataElement(programStageDataElement.getDataElement());
        DataElementAdapterViewAbstract dataElementViewAbstract = null;
        String dataType = dataElement.getType();
        if ( dataElement.getOptionSet() != null )
        {
            OptionSet optionSet = MetaDataController.getOptionSet(dataElement.getOptionSet());
            if ( optionSet == null )
            {
                dataElementViewAbstract = new TextDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
            else
            {
                dataElementViewAbstract = new OptionSetDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
        }
        else
        {
            if ( dataType.equalsIgnoreCase( DataElement.VALUE_TYPE_BOOL ) )
            {
                dataElementViewAbstract = new BoolDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
            else if ( dataType.equalsIgnoreCase( DataElement.VALUE_TYPE_DATE ) )
            {
                dataElementViewAbstract = new DatePickerDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
            else if ( dataType.equalsIgnoreCase( DataElement.VALUE_TYPE_TRUE_ONLY ) )
            {
                dataElementViewAbstract = new TrueOnlyDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
            else if ( dataType.equalsIgnoreCase( DataElement.VALUE_TYPE_NUMBER ) || dataType.equalsIgnoreCase( DataElement.VALUE_TYPE_INT ) )
            {
                dataElementViewAbstract = new NumberDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
            else
            {
                dataElementViewAbstract = new TextDataElementView( getActivity(),
                        dataElement, dataValue, programStageDataElement.isCompulsory() );
            }
        }
        return dataElementViewAbstract.getView();
    }

    /**
     * saves the current data values as a registered event.
     */
    public void submit() {
        boolean valid = true;
        //go through each data element and check that they are valid
        //i.e. all compulsory are not empty
        for(int i = 0; i<dataValues.size(); i++) {
            ProgramStageDataElement programStageDataElement = programStageDataElements.get(i);
            if( programStageDataElement.isCompulsory() ) {
                DataValue dataValue = dataValues.get(i);
                if(dataValue.value == null || dataValue.value.length() <= 0) {
                    valid = false;
                }
            }
        }

        if(!valid) {
            Dhis2.getInstance().showErrorDialog(getActivity(), "Validation error",
                    "Some compulsory fields are empty, please fill them in");
        } else {
            saveEvent();
            showSelectProgramFragment();
        }
    }

    public void saveEvent() {
        event.save(false);
        for(DataValue dataValue: dataValues) {
            dataValue.save(false);
        }
    }

    public void showSelectProgramFragment() {
        MessageEvent event = new MessageEvent(BaseEvent.EventType.showSelectProgramFragment);
        Dhis2Application.bus.post(event);
    }

    public OrganisationUnit getSelectedOrganisationUnit() {
        return selectedOrganisationUnit;
    }

    public void setSelectedOrganisationUnit(OrganisationUnit selectedOrganisationUnit) {
        this.selectedOrganisationUnit = selectedOrganisationUnit;
    }

    public Program getSelectedProgram() {
        return selectedProgram;
    }

    public void setSelectedProgram(Program selectedProgram) {
        this.selectedProgram = selectedProgram;
    }
}
