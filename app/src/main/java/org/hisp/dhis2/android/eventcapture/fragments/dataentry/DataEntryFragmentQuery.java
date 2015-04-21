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

package org.hisp.dhis2.android.eventcapture.fragments.dataentry;

import android.content.Context;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.AutoCompleteRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.CheckBoxRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DatePickerRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.EditTextRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.RadioButtonsRow;
import org.hisp.dhis2.android.eventcapture.loaders.Query;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.Event;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramIndicator;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis2.android.sdk.utils.Utils;
import org.hisp.dhis2.android.sdk.utils.services.ProgramIndicatorService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController.getDataElement;

class DataEntryFragmentQuery implements Query<DataEntryFragmentForm> {

    private static final String CLASS_TAG = DataEntryFragmentQuery.class.getSimpleName();

    private static final String EMPTY_FIELD = "";
    private static final String DEFAULT_SECTION = "defaultSection";

    private final String orgUnitId;
    private final String programId;
    private final long eventId;

    DataEntryFragmentQuery(String orgUnitId, String programId, long eventId) {
        this.orgUnitId = orgUnitId;
        this.programId = programId;
        this.eventId = eventId;
    }

    @Override
    public DataEntryFragmentForm query(Context context) {
        final Program program = Select.byId(Program.class, programId);
        final ProgramStage stage = program.getProgramStages().get(0);
        final DataEntryFragmentForm form = new DataEntryFragmentForm();

        if (stage == null || stage.getProgramStageSections() == null) {
            return form;
        }

        final String username = Dhis2.getUsername(context);
        final Event event = getEvent(
                orgUnitId, programId, eventId, stage, username
        );

        form.setEvent(event);
        form.setStage(stage);
        form.setSections(new ArrayList<DataEntryFragmentSection>());
        form.setDataElementNames(new HashMap<String, String>());
        form.setDataValues(new HashMap<String, DataValue>());
        form.setIndicatorRows(new ArrayList<IndicatorRow>());

        if (stage.getProgramStageSections() == null || stage.getProgramStageSections().isEmpty()) {
            List<DataEntryRow> rows = new ArrayList<>();
            populateDataEntryRows(form, stage.getProgramStageDataElements(), rows, username);
            populateIndicatorRows(form, stage.getProgramIndicators(), rows);
            form.getSections().add(new DataEntryFragmentSection(DEFAULT_SECTION, rows));
        } else {
            for (int i = 0; i < stage.getProgramStageSections().size(); i++) {

                ProgramStageSection section = stage.getProgramStageSections().get(i);
                if (section.getProgramStageDataElements() == null) {
                    continue;
                }

                List<DataEntryRow> rows = new ArrayList<>();
                populateDataEntryRows(form, section.getProgramStageDataElements(), rows, username);
                populateIndicatorRows(form, section.getProgramIndicators(), rows);
                form.getSections().add(new DataEntryFragmentSection(section.getName(), rows));
            }
        }

        return form;
    }

    private static void populateDataEntryRows(DataEntryFragmentForm form,
                                              List<ProgramStageDataElement> dataElements,
                                              List<DataEntryRow> rows, String username) {
        for (ProgramStageDataElement stageDataElement : dataElements) {
            DataValue dataValue = getDataValue(stageDataElement.dataElement, form.getEvent(), username);
            DataValue copyDataValue = dataValue.clone();
            DataElement dataElement = getDataElement(stageDataElement.dataElement);

            form.getDataValues().put(copyDataValue.dataElement, copyDataValue.clone());
            form.getDataElementNames().put(stageDataElement.dataElement, dataElement.name);
            rows.add(createDataEntryRow(dataElement, dataValue));
        }
    }

    private static void populateIndicatorRows(DataEntryFragmentForm form,
                                              List<ProgramIndicator> indicators,
                                              List<DataEntryRow> rows) {
        for (ProgramIndicator programIndicator : indicators) {
            String value = ProgramIndicatorService
                    .getProgramIndicatorValue(form.getEvent(), programIndicator);
            IndicatorRow indicatorRow = new IndicatorRow(programIndicator, value);
            rows.add(indicatorRow);
            form.getIndicatorRows().add(indicatorRow);
        }
    }

    private Event getEvent(String orgUnitId, String programId, long eventId,
                           ProgramStage programStage, String username) {
        Event event;
        if (eventId < 0) {
            event = new Event();
            event.setEvent(Dhis2.QUEUED + UUID.randomUUID().toString());
            event.setFromServer(false);
            event.setDueDate(Utils.getCurrentDate());
            event.setEventDate(Utils.getCurrentDate());
            event.setOrganisationUnitId(orgUnitId);
            event.setProgramId(programId);
            event.setProgramStageId(programStage.getId());
            event.setStatus(Event.STATUS_COMPLETED);
            event.setLastUpdated(Utils.getCurrentTime());

            List<DataValue> dataValues = new ArrayList<>();
            for (ProgramStageDataElement dataElement : programStage.getProgramStageDataElements()) {
                dataValues.add(
                        new DataValue(event.event, EMPTY_FIELD, dataElement.dataElement, false, username)
                );
            }
            event.setDataValues(dataValues);
        } else {
            event = DataValueController.getEvent(eventId);
        }

        return event;
    }

    private static DataEntryRow createDataEntryRow(DataElement dataElement, DataValue dataValue) {
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

    public static DataValue getDataValue(String dataElement, Event event,
                                         String username) {
        for (DataValue dataValue : event.getDataValues()) {
            if (dataValue.getDataElement().equals(dataElement)) {
                return dataValue;
            }
        }

        // The DataValue didn't exist for some reason. Create a new one.
        DataValue dataValue = new DataValue(
                event.getEvent(), EMPTY_FIELD, dataElement, false, username
        );
        event.getDataValues().add(dataValue);
        return dataValue;
    }
}