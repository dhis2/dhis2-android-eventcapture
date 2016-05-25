package org.hisp.dhis.android.eventcapture.views.activities;


import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.FormComponent;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryFragment;
import org.hisp.dhis.client.sdk.ui.adapters.OnPickerItemClickListener;
import org.hisp.dhis.client.sdk.ui.fragments.DatePickerDialogFragment;
import org.hisp.dhis.client.sdk.ui.fragments.FilterableDialogFragment;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


// TODO check if configuration changes are handled properly
public class FormSectionActivity extends AppCompatActivity implements FormSectionView {
    private static final String ARG_EVENT_UID = "arg:eventUid";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Inject
    FormSectionPresenter formSectionPresenter;

    // collapsing toolbar views
    TextView textViewReportDate;
    LinearLayout linearLayoutCoordinates;
    EditText editTextLatitude;
    EditText editTextLongitude;

    // section tabs and view pager
    TabLayout tabLayout;
    ViewPager viewPager;

    FilterableDialogFragment sectionDialogFragment;

    public static void navigateTo(Activity activity, String eventUid) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, FormSectionActivity.class);
        intent.putExtra(ARG_EVENT_UID, eventUid);
        activity.startActivity(intent);
    }

    private String getEventUid() {
        if (getIntent().getExtras() == null || getIntent().getExtras()
                .getString(ARG_EVENT_UID, null) == null) {
            throw new IllegalArgumentException("You must pass event uid in intent extras");
        }

        return getIntent().getExtras().getString(ARG_EVENT_UID, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_sections);

        setupToolbar();
        setupPickers();
        setupViewPager();

        // attach listener if dialog opened (post-configuration change)
        attachListenerToExistingFragment();

        FormComponent formComponent = ((EventCaptureApp) getApplication()).getFormComponent();

        // first time activity is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (formComponent != null) {
                // create new instance of component
                ((EventCaptureApp) getApplication()).releaseFormComponent();
            }

            formComponent = ((EventCaptureApp) getApplication()).createFormComponent();
        } else {
            formComponent = ((EventCaptureApp) getApplication()).getFormComponent();
        }

        // inject dependencies
        formComponent.inject(this);

        // start building the form
        formSectionPresenter.createDataEntryForm(getEventUid());
    }

    @Override
    protected void onResume() {
        formSectionPresenter.attachView(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        formSectionPresenter.detachView();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (sectionDialogFragment != null) {
            getMenuInflater().inflate(R.menu.menu_form_sections, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                super.onBackPressed();
                return true;
            }
            case R.id.filter_button:
                if (sectionDialogFragment != null) {
                    sectionDialogFragment.show(getSupportFragmentManager(), FilterableDialogFragment.TAG);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showFormDefaultSection(String formSectionId) {
        FormSingleSectionAdapter viewPagerAdapter =
                new FormSingleSectionAdapter(getSupportFragmentManager());
        viewPagerAdapter.swapData(getEventUid(), formSectionId);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // hide tab layout
        tabLayout.setVisibility(View.GONE);

        // if we don't have sections, we don't need to show navigation drawer
        // sectionsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

        // we also need to hide filter icon
        supportInvalidateOptionsMenu();
    }

    @Override
    public void showFormSections(List<FormSection> formSections) {
        FormSectionsAdapter viewPagerAdapter =
                new FormSectionsAdapter(getSupportFragmentManager());
        viewPagerAdapter.swapData(getEventUid(), formSections);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // hide tab layout
        tabLayout.setVisibility(View.VISIBLE);

        // TabLayout will fail on you, if ViewPager which is going to be
        // attached does not contain ViewPagerAdapter set to it.
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void setFormSectionsPicker(Picker picker) {
        sectionDialogFragment = FilterableDialogFragment.newInstance(picker);
        sectionDialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());

        supportInvalidateOptionsMenu();
    }

    @Override
    public void showReportDatePicker(String hint, String value) {
        String dateLabel = isEmpty(hint) ? getString(R.string.report_date) : hint;
        textViewReportDate.setHint(dateLabel);

        if (!isEmpty(value)) {
            textViewReportDate.setText(String.format(Locale.getDefault(),
                    "%s: %s", dateLabel, value));
        }
    }

    @Override
    public void showCoordinatesPicker(String latitude, String longitude) {
        if (linearLayoutCoordinates.getVisibility() == View.INVISIBLE ||
                linearLayoutCoordinates.getVisibility() == View.GONE) {
            linearLayoutCoordinates.setVisibility(View.VISIBLE);
        }

        if (!isEmpty(latitude)) {
            editTextLatitude.setText(latitude);
        }

        if (!isEmpty(longitude)) {
            editTextLongitude.setText(longitude);
        }
    }

    private void attachListenerToExistingFragment() {
        FilterableDialogFragment dialogFragment = (FilterableDialogFragment)
                getSupportFragmentManager().findFragmentByTag(FilterableDialogFragment.TAG);

        // if we don't have fragment attached to activity,
        // we don't want to do anything else
        if (dialogFragment != null) {
            dialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupPickers() {
        textViewReportDate = (TextView) findViewById(R.id.textview_report_date);
        linearLayoutCoordinates = (LinearLayout) findViewById(R.id.linearlayout_coordinates);
        editTextLatitude = (EditText) findViewById(R.id.edittext_latitude);
        editTextLongitude = (EditText) findViewById(R.id.edittext_longitude);

        final OnDateSetListener onDateSetListener = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String stringDate = (new SimpleDateFormat(DATE_FORMAT, Locale.US))
                        .format(calendar.getTime());
                String newValue = String.format(Locale.getDefault(), "%s: %s",
                        getString(R.string.report_date), stringDate);
                textViewReportDate.setText(newValue);

                DateTime dateTime = DateTime.parse(stringDate);
                formSectionPresenter.saveEventDate(getEventUid(), dateTime);
            }
        };

        // set on click listener to text view report date
        textViewReportDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment datePickerDialogFragment =
                        DatePickerDialogFragment.newInstance(false);
                datePickerDialogFragment.setOnDateSetListener(onDateSetListener);
                datePickerDialogFragment.show(getSupportFragmentManager());
            }
        });

        // since coordinates are optional, initially they should be hidden
        linearLayoutCoordinates.setVisibility(View.GONE);
    }

    private void setupViewPager() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout_data_entry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_dataentry);

        // hide tab layout initially in order to prevent UI
        // jumps in cases when we don't have sections
        tabLayout.setVisibility(View.GONE);
    }

    private class OnSearchSectionsClickListener implements OnPickerItemClickListener {
        @Override
        public void onPickerItemClickListener(Picker selectedPicker) {
            PagerAdapter pagerAdapter = viewPager.getAdapter();

            if (pagerAdapter != null && (pagerAdapter instanceof FormSectionsAdapter)) {
                FormSectionsAdapter sectionsAdapter = (FormSectionsAdapter) pagerAdapter;
                List<FormSection> formSections = sectionsAdapter.getData();

                for (int position = 0; position < formSections.size(); position++) {
                    FormSection formSection = formSections.get(position);

                    if (formSection.getId().equals(selectedPicker.getId())) {
                        viewPager.setCurrentItem(position);
                        break;
                    }
                }
            }
        }
    }

    /*
    *
    * This adapter exists only in order to satisfy cases when there is no
    * sections assigned to program stage. As the result, we have to
    * use program stage itself as section
    *
    */
    private static class FormSingleSectionAdapter extends FragmentStatePagerAdapter {
        private static final int DEFAULT_STAGE_COUNT = 1;
        private static final int DEFAULT_STAGE_POSITION = 0;
        private String eventId;
        private String formSectionId;

        public FormSingleSectionAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (DEFAULT_STAGE_POSITION == position && !isEmpty(formSectionId)) {
                return DataEntryFragment.newInstanceForStage(eventId, formSectionId);
            }

            return null;
        }

        @Override
        public int getCount() {
            return isEmpty(formSectionId) ? 0 : DEFAULT_STAGE_COUNT;
        }

        public void swapData(String eventId, String programStageId) {
            this.eventId = eventId;
            this.formSectionId = programStageId;
            this.notifyDataSetChanged();
        }
    }

    private static class FormSectionsAdapter extends FragmentStatePagerAdapter {
        private final List<FormSection> formSections;
        private String eventId;

        public FormSectionsAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.formSections = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            FormSection formSection = formSections.get(position);
            return DataEntryFragment.newInstanceForSection(eventId, formSection.getId());
        }

        @Override
        public int getCount() {
            return formSections.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            FormSection formSection = formSections.get(position);
            return formSection.getLabel();
        }

        @NonNull
        public List<FormSection> getData() {
            return formSections;
        }

        public void swapData(String eventId, List<FormSection> formSections) {
            this.eventId = eventId;
            this.formSections.clear();

            if (formSections != null) {
                this.formSections.addAll(formSections);
            }

            notifyDataSetChanged();
        }
    }
}
