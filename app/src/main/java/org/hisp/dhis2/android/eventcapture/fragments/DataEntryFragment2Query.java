/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.fragments;

import android.content.Context;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.AutoCompleteRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.CheckBoxRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DatePickerRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.EditTextRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.RadioButtonsRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.SectionRow;
import org.hisp.dhis2.android.eventcapture.loaders.Query;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis2.android.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DataEntryFragment2Query implements Query<DataEntryFragment2Form> {
    private static final String EMPTY_FIELD = "";
    private final String orgUnitId;
    private final String programId;
    private final long eventId;

    DataEntryFragment2Query(String orgUnitId,
                            String programId,
                            long eventId) {
        this.orgUnitId = orgUnitId;
        this.programId = programId;
        this.eventId = eventId;
    }

    @Override
    public DataEntryFragment2Form query(Context context) {
        Program program = Select.byId(Program.class, programId);
        ProgramStage stage = program.getProgramStages().get(0);
        DataEntryFragment2Form form = new DataEntryFragment2Form();

        if (stage == null || stage.getProgramStageSections() == null) {
            return form;
        }

        List<DataValue> dataValues = new ArrayList<>();
        List<DataEntryRow> rows = new ArrayList<>();
        Event event = createEvent(orgUnitId, programId, stage.getId(), eventId);

        String username = Dhis2.getUsername(context);
        for (int i = 0; i < stage.getProgramStageSections().size(); i++) {
            ProgramStageSection section = stage.getProgramStageSections().get(i);
            rows.add(new SectionRow(section.getName()));

            if (section.getProgramStageDataElements() == null) {
                continue;
            }

            for (ProgramStageDataElement stageDataElement : section.getProgramStageDataElements()) {
                DataValue dataValue = new DataValue(event.event, EMPTY_FIELD,
                        stageDataElement.dataElement, false, username);
                dataValues.add(dataValue);

                DataElement dataElement = MetaDataController.getDataElement(stageDataElement.dataElement);
                rows.add(createRow(dataElement, dataValue));
            }
        }

        form.setEvent(event);
        form.setRows(rows);

        return form;
    }

    private static Event createEvent(String orgUnitId, String programId,
                                     String programStageId, long eventId) {
        Event event;
        if (eventId < 0) {
            event = new Event();
            event.setEvent(Dhis2.QUEUED + UUID.randomUUID().toString());
            event.setFromServer(false);
            event.setDueDate(Utils.getCurrentDate());
            event.setEventDate(Utils.getCurrentDate());
            event.setOrganisationUnitId(orgUnitId);
            event.setProgramId(programId);
            event.setProgramStageId(programStageId);
            event.setStatus(Event.STATUS_COMPLETED);
            event.setLastUpdated(Utils.getCurrentTime());
            // event.dataValues = dataValues;
        } else {
            event = DataValueController.getEvent(eventId);
        }

        return event;
    }

    private static DataEntryRow createRow(DataElement dataElement, DataValue dataValue) {
        DataEntryRow row;
        if (dataElement.getOptionSet() != null) {
            OptionSet optionSet = MetaDataController.getOptionSet(dataElement.optionSet);
            if (optionSet == null) {
                row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.TEXT);
            } else {
                row = new AutoCompleteRow(dataElement.name, dataValue, optionSet);
            }
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_TEXT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.TEXT);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_LONG_TEXT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.LONG_TEXT);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_NUMBER)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.NUMBER);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_INT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.INTEGER);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.INTEGER_ZERO_OR_POSITIVE);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_POSITIVE_INT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.INTEGER_POSITIVE);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_NEGATIVE_INT)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.INTEGER_NEGATIVE);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_BOOL)) {
            row = new RadioButtonsRow(dataElement.name, dataValue, DataEntryRowTypes.BOOLEAN);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_TRUE_ONLY)) {
            row = new CheckBoxRow(dataElement.name, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_DATE)) {
            row = new DatePickerRow(dataElement.name, dataValue);
        } else if (dataElement.getType().equalsIgnoreCase(DataElement.VALUE_TYPE_STRING)) {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.LONG_TEXT);
        } else {
            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.LONG_TEXT);
        }
        return row;
    }
}