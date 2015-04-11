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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.DataValueAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.AutoCompleteRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.CheckBoxRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DatePickerRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.EditTextRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.RadioButtonsRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.SectionRow;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;
import org.hisp.dhis2.android.eventcapture.loaders.Query;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
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

public class DataEntryFragment2 extends Fragment
        implements LoaderManager.LoaderCallbacks<List<DataEntryRow>> {
    public static final String TAG = DataEntryFragment2.class.getSimpleName();
    private static final int LOADER_ID = 1;

    private static final String EXTRA_ARGUMENTS = "extra:Arguments";
    private static final String EXTRA_SAVED_INSTANCE_STATE = "extra:savedInstanceState";

    private static final String ORG_UNIT_ID = "extra:orgUnitId";
    private static final String PROGRAM_ID = "extra:ProgramId";
    private static final String EVENT_ID = "extra:EventId";

    private ListView mListView;
    private ProgressBar mProgressBar;
    private DataValueAdapter mAdapter;

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
        // we need to nullify reference
        // to parent activity in order not to leak it
        mNavigationHandler = null;
        super.onDetach();
    }

    public static DataEntryFragment2 newInstance(String unitId, String programId) {
        DataEntryFragment2 fragment = new DataEntryFragment2();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataEntryFragment2 newInstance(String unitId, String programId,
                                                 long eventId) {
        DataEntryFragment2 fragment = new DataEntryFragment2();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putLong(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mAdapter = new DataValueAdapter(getLayoutInflater(savedInstanceState));
        mListView = (ListView) view.findViewById(R.id.datavalues_listview);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putBundle(EXTRA_ARGUMENTS, getArguments());
        argumentsBundle.putBundle(EXTRA_SAVED_INSTANCE_STATE, savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, argumentsBundle, this);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<DataEntryRow>> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            modelsToTrack.add(DataValue.class);

            Bundle fragmentArguments = args.getBundle(EXTRA_ARGUMENTS);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack, new ProgramQuery(
                    fragmentArguments.getString(ORG_UNIT_ID),
                    fragmentArguments.getString(PROGRAM_ID),
                    -1
            )
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<DataEntryRow>> loader, List<DataEntryRow> data) {
        if (loader.getId() == LOADER_ID) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.swap(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<DataEntryRow>> loader) {
        if (loader.getId() == LOADER_ID) {
            mAdapter.swap(null);
        }
    }

    /*
    static class ProgramHolder {
        public final List<ProgramStageSection> programStageSections;
        public final List<ProgramStageDataElement> programStageDataElements;

        private ProgramHolder(List<ProgramStageSection> programStageSections,
                              List<ProgramStageDataElement> programStageDataElements) {
            this.programStageSections = programStageSections;
            this.programStageDataElements = programStageDataElements;
        }
    }
    */

    static class ProgramQuery implements Query<List<DataEntryRow>> {
        private static final String EMPTY_FIELD = "";
        private final String orgUnitId;
        private final String programId;
        private final long eventId;

        ProgramQuery(String orgUnitId, String programId, long eventId) {
            this.orgUnitId = orgUnitId;
            this.programId = programId;
            this.eventId = eventId;
        }

        @Override
        public List<DataEntryRow> query(Context context) {
            Program program = Select.byId(
                    Program.class, programId
            );

            List<DataEntryRow> rows = new ArrayList<>();
            ProgramStage stage = program.getProgramStages().get(0);
            if (stage == null || stage.getProgramStageSections() == null) {
                return rows;
            }

            List<DataValue> dataValues = new ArrayList<>();
            Event event = new Event();
            if (eventId < 0) {
                event.event = Dhis2.QUEUED + UUID.randomUUID().toString();
                event.fromServer = false;
                event.dueDate = Utils.getCurrentDate();
                event.eventDate = Utils.getCurrentDate();
                event.organisationUnitId = orgUnitId;
                event.programId = programId;
                event.programStageId = stage.id;
                event.status = Event.STATUS_COMPLETED;
                event.lastUpdated = Utils.getCurrentTime();
                event.dataValues = dataValues;
            }

            String username = Dhis2.getUsername(context);
            for (int i = 0; i < stage.getProgramStageSections().size(); i++) {
                ProgramStageSection section = stage.getProgramStageSections().get(i);
                /* if (i != 0) {
                    rows.add(new SectionStubRow());
                } */
                rows.add(new SectionRow(section.getName()));

                if (section.getProgramStageDataElements() == null) {
                    continue;
                }
                for (int j = 0; j < 2; j++)
                    for (ProgramStageDataElement stageDataElement : section.getProgramStageDataElements()) {
                        DataValue dataValue = new DataValue(event.event, EMPTY_FIELD,
                                stageDataElement.dataElement, false, username);
                        dataValues.add(dataValue);

                        DataElement dataElement = MetaDataController.getDataElement(stageDataElement.dataElement);
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
                            Log.d(TAG, "type is: " + dataElement.getType());
                            row = new EditTextRow(dataElement.name, dataValue, DataEntryRowTypes.LONG_TEXT);
                        }

                        rows.add(row);
                    }
            }
            return rows;
        }
    }
}
