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

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.EditTextValueChangedEvent;
import org.hisp.dhis2.android.eventcapture.EventCaptureApplication;
import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.MainActivity;
import org.hisp.dhis2.android.eventcapture.OnBackPressedListener;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.DataValueAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.SectionAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.rows.AbsTextWatcher;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis2.android.eventcapture.loaders.DbLoader;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis2.android.sdk.utils.Utils;
import org.hisp.dhis2.android.sdk.utils.services.ProgramIndicatorService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class DataEntryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<DataEntryFragmentForm>,
        OnBackPressedListener, AdapterView.OnItemSelectedListener {
    public static final String TAG = DataEntryFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;

    private static final String EXTRA_ARGUMENTS = "extra:Arguments";
    private static final String EXTRA_SAVED_INSTANCE_STATE = "extra:savedInstanceState";

    private static final String ORG_UNIT_ID = "extra:orgUnitId";
    private static final String PROGRAM_ID = "extra:ProgramId";
    private static final String EVENT_ID = "extra:EventId";

    private ListView mListView;
    private ProgressBar mProgressBar;

    private View mSpinnerContainer;
    private Spinner mSpinner;

    private EditText mLatitude;
    private EditText mLongitude;
    private ImageButton mCaptureCoords;

    private SectionAdapter mSpinnerAdapter;
    private DataValueAdapter mListViewAdapter;

    private INavigationHandler mNavigationHandler;
    private DataEntryFragmentForm mForm;

    public static DataEntryFragment newInstance(String unitId, String programId) {
        DataEntryFragment fragment = new DataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataEntryFragment newInstance(String unitId, String programId,
                                                long eventId) {
        DataEntryFragment fragment = new DataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putLong(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    private static Map<String, ProgramStageDataElement> toMap(List<ProgramStageDataElement> dataElements) {
        Map<String, ProgramStageDataElement> dataElementMap = new HashMap<>();
        if (dataElements != null && !dataElements.isEmpty()) {
            for (ProgramStageDataElement dataElement : dataElements) {
                dataElementMap.put(dataElement.dataElement, dataElement);
            }
        }
        return dataElementMap;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof ActionBarActivity) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowTitleEnabled(false);
        }

        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setBackPressedListener(this);
        }

        if (activity instanceof INavigationHandler) {
            mNavigationHandler = (INavigationHandler) activity;
        } else {
            throw new IllegalArgumentException("Activity must implement INavigationHandler interface");
        }
    }

    @Override
    public void onDetach() {
        if (getActivity() != null &&
                getActivity() instanceof ActionBarActivity) {
            getActionBar().setHomeButtonEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);
        }

        // we need to nullify reference
        // to parent activity in order not to leak it
        if (getActivity() != null &&
                getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBackPressedListener(null);
        }

        Dhis2.disableGps();
        mNavigationHandler = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventCaptureApplication.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventCaptureApplication.getEventBus().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_data_entry, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mListViewAdapter = new DataValueAdapter(getLayoutInflater(savedInstanceState));
        mListView = (ListView) view.findViewById(R.id.datavalues_listview);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(mListViewAdapter);
    }

    @Override
    public void onDestroyView() {
        detachSpinner();
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            doBack();
            return true;
        } else if (menuItem.getItemId() == R.id.action_new_event) {
            submitEvent();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putBundle(EXTRA_ARGUMENTS, getArguments());
        argumentsBundle.putBundle(EXTRA_SAVED_INSTANCE_STATE, savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, argumentsBundle, this);

        mProgressBar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<DataEntryFragmentForm> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            // Adding Tables for tracking here is dangerous (since MetaData updates in background
            // can trigger reload of values from db which will reset all fields).
            // Hence, it would be more safe not to track any changes in any tables
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            Bundle fragmentArguments = args.getBundle(EXTRA_ARGUMENTS);
            return new DbLoader<>(
                    getActivity().getBaseContext(), modelsToTrack, new DataEntryFragmentQuery(
                    fragmentArguments.getString(ORG_UNIT_ID, null),
                    fragmentArguments.getString(PROGRAM_ID, null),
                    fragmentArguments.getLong(EVENT_ID, -1)
            )
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<DataEntryFragmentForm> loader, DataEntryFragmentForm data) {
        if (loader.getId() == LOADER_ID && isAdded()) {
            mProgressBar.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mForm = data;
            if (data.getStage() != null &&
                    data.getStage().captureCoordinates) {
                Double latitude = data.getEvent().getLatitude();
                Double longitude = data.getEvent().getLongitude();
                attachCoordinatePicker(latitude, longitude);
            }

            if (!data.getSections().isEmpty()) {
                if (data.getSections().size() > 1) {
                    attachSpinner();
                    mSpinnerAdapter.swapData(data.getSections());
                } else {
                    DataEntryFragmentSection section = data.getSections().get(0);
                    mListViewAdapter.swapData(section.getRows());
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<DataEntryFragmentForm> loader) {
        if (loader.getId() == LOADER_ID) {
            if (mSpinnerAdapter != null) {
                mSpinnerAdapter.swapData(null);
            }
            if (mListViewAdapter != null) {
                mListViewAdapter.swapData(null);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DataEntryFragmentSection section = (DataEntryFragmentSection)
                mSpinnerAdapter.getItem(position);

        if (section != null) {
            mListViewAdapter.swapData(section.getRows());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // stub implementation
    }

    @Override
    public void doBack() {
        getFragmentManager().popBackStack();
    }

    @Subscribe
    public void onRowValueChanged(EditTextValueChangedEvent event) {
        if (mForm == null || mForm.getIndicatorRows() == null) {
            return;
        }

        /*
        * updating indicator values in rows
        * */
        for (IndicatorRow indicatorRow : mForm.getIndicatorRows()) {
            String newValue = ProgramIndicatorService.
                    getProgramIndicatorValue(mForm.getEvent(), indicatorRow.getIndicator());
            if (!newValue.equals(indicatorRow.getValue())) {
                indicatorRow.updateValue(newValue);
            }
        }

        /*
        * Calling adapter's getView in order to render changes in visible IndicatorRows
        * */
        int start = mListView.getFirstVisiblePosition();
        int end = mListView.getLastVisiblePosition();
        for (int pos = start; pos <= end; pos++) {
            if (mListViewAdapter.getCount() > pos &&
                    mListViewAdapter.getItemViewType(pos)
                            == DataEntryRowTypes.INDICATOR.ordinal()) {
                View view = mListView.getChildAt(pos);
                mListViewAdapter.getView(pos, view, mListView);
            }
        }
    }

    private ActionBar getActionBar() {
        if (getActivity() != null &&
                getActivity() instanceof ActionBarActivity) {
            return ((ActionBarActivity) getActivity()).getSupportActionBar();
        } else {
            throw new IllegalArgumentException("Fragment should be attached to ActionBarActivity");
        }
    }

    private Toolbar getActionBarToolbar() {
        if (isAdded() && getActivity() != null &&
                getActivity() instanceof MainActivity) {
            return (Toolbar) getActivity().findViewById(R.id.toolbar);
        } else {
            throw new IllegalArgumentException("Fragment should be attached to MainActivity");
        }
    }

    private void attachCoordinatePicker(Double latitude, Double longitude) {
        // Prepare GPS for work. Note, we should use base
        // context in order not to leak activity
        Dhis2.activateGps(getActivity().getBaseContext());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(
                R.layout.fragment_data_entry_header, mListView, false);

        mLatitude = (EditText) view.findViewById(R.id.latitude_edittext);
        mLongitude = (EditText) view.findViewById(R.id.longitude_edittext);
        mCaptureCoords = (ImageButton) view.findViewById(R.id.capture_coordinates);

        if (latitude != null) {
            mLatitude.setText(String.valueOf(latitude));
        }

        if (longitude != null) {
            mLongitude.setText(String.valueOf(longitude));
        }

        final String latitudeMessage = getString(R.string.latitude_error_message);
        final String longitudeMessage = getString(R.string.longitude_error_message);

        mLatitude.addTextChangedListener(new AbsTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    double value = Double.parseDouble(s.toString());
                    if (value < -90 || value > 90) {
                        mLatitude.setError(latitudeMessage);
                    }
                    mForm.getEvent().setLatitude(Double.valueOf(value));
                }
            }
        });

        mLongitude.addTextChangedListener(new AbsTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    double value = Double.parseDouble(s.toString());
                    if (value < -180 || value > 180) {
                        mLongitude.setError(longitudeMessage);
                    }
                    mForm.getEvent().setLongitude(Double.valueOf(value));
                }
            }
        });

        mCaptureCoords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = Dhis2.getLocation(getActivity().getBaseContext());

                mLatitude.setText(String.valueOf(location.getLatitude()));
                mLongitude.setText(String.valueOf(location.getLongitude()));
            }
        });

        mListView.addHeaderView(view);
    }

    private void attachSpinner() {
        if (!isSpinnerAttached()) {
            Toolbar toolbar = getActionBarToolbar();

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            mSpinnerContainer = inflater.inflate(
                    R.layout.toolbar_spinner, toolbar, false);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);

            mSpinnerAdapter = new SectionAdapter(inflater);

            mSpinner = (Spinner) mSpinnerContainer.findViewById(R.id.toolbar_spinner);
            mSpinner.setAdapter(mSpinnerAdapter);
            mSpinner.setOnItemSelectedListener(this);
        }
    }

    private void detachSpinner() {
        if (isSpinnerAttached()) {
            if (mSpinnerContainer != null) {
                ((ViewGroup) mSpinnerContainer.getParent()).removeView(mSpinnerContainer);
                mSpinnerContainer = null;
                mSpinner = null;
                if (mSpinnerAdapter != null) {
                    mSpinnerAdapter.swapData(null);
                    mSpinnerAdapter = null;
                }
            }
        }
    }

    private boolean isSpinnerAttached() {
        return mSpinnerContainer != null;
    }

    private void submitEvent() {
        if (mForm != null && isAdded()) {
            ArrayList<String> errors = isEventValid();
            if (errors.isEmpty()) {
                mForm.getEvent().setFromServer(false);
                mForm.getEvent().setLastUpdated(Utils.getCurrentTime());
                mForm.getEvent().save(true);

                Dhis2.sendLocalData(getActivity().getBaseContext());
                doBack();
            } else {
                ValidationErrorDialog dialog = ValidationErrorDialog
                        .newInstance(errors);
                dialog.show(getChildFragmentManager());
            }
        }
    }

    private ArrayList<String> isEventValid() {
        Map<String, ProgramStageDataElement> dataElements = toMap(
                mForm.getStage().getProgramStageDataElements()
        );

        ArrayList<String> errors = new ArrayList<>();
        for (DataValue dataValue : mForm.getEvent().getDataValues()) {
            ProgramStageDataElement dataElement = dataElements.get(dataValue.dataElement);
            if (dataElement.compulsory && isEmpty(dataValue.getValue())) {
                errors.add(mForm.getDataElementNames().get(dataValue.dataElement));
            }
        }

        return errors;
    }
}