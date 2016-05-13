package org.hisp.dhis.android.eventcapture.views.activities;


import android.app.Activity;
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
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryFragment;
import org.hisp.dhis.client.sdk.ui.adapters.OnPickerItemClickListener;
import org.hisp.dhis.client.sdk.ui.fragments.FilterableDialogFragment;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


// TODO disable filtering in cases when we don't have sections
// TODO check if configuration changes are handled properly
// TODO solve performance issues with RX
public class FormSectionActivity extends AppCompatActivity implements FormSectionView {
    private static final String ARG_ORGANISATION_UNIT_ID = "arg:organisationUnitId";
    private static final String ARG_PROGRAM_ID = "arg:programId";

    @Inject
    FormSectionPresenter formSectionPresenter;

    // collapsing toolbar views
    TextView textViewOrganisationUnit;
    TextView textViewProgram;

    //local storage for:
    Picker formSectionsPicker;

    // section tabs and view pager
    TabLayout tabLayout;
    ViewPager viewPager;

    // prompts
    String organisationUnit;
    String program;

    public static void navigateTo(Activity activity, String organisationUnitId, String programId) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, FormSectionActivity.class);
        intent.putExtra(ARG_ORGANISATION_UNIT_ID, organisationUnitId);
        intent.putExtra(ARG_PROGRAM_ID, programId);
        activity.startActivity(intent);
    }

    private String getOrganisationUnitId() {
        return getIntent().getExtras().getString(ARG_ORGANISATION_UNIT_ID, null);
    }

    private String getProgramId() {
        return getIntent().getExtras().getString(ARG_PROGRAM_ID, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_sections);

        // injecting dependencies
        ((EventCaptureApp) getApplication()).createFormComponent().inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        textViewOrganisationUnit = (TextView) findViewById(R.id.textview_organisation_unit);
        textViewProgram = (TextView) findViewById(R.id.textview_program);

        tabLayout = (TabLayout) findViewById(R.id.tablayout_data_entry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_dataentry);

        // start building the form
        formSectionPresenter.createDataEntryForm(getOrganisationUnitId(), getProgramId());

        organisationUnit = getString(R.string.organisation_unit);
        program = getString(R.string.program);

        // attach listener if dialog opened (post-configuration change)
        attachListenerToExistingFragment();
    }


    @Override
    protected void onDestroy() {
        // release component
        ((EventCaptureApp) getApplication()).releaseFormComponent();

        super.onDestroy();
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
        getMenuInflater().inflate(R.menu.menu_form_sections, menu);
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
                showFilterableDialog(formSectionsPicker);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showFormDefaultSection(String formSectionId) {
        FormSingleSectionAdapter viewPagerAdapter =
                new FormSingleSectionAdapter(getSupportFragmentManager());
        viewPagerAdapter.swapData(formSectionId);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // hide tab layout
        tabLayout.setVisibility(View.GONE);
    }

    @Override
    public void showFormSections(List<FormSection> formSections) {
        FormSectionsAdapter viewPagerAdapter =
                new FormSectionsAdapter(getSupportFragmentManager());
        viewPagerAdapter.swapData(formSections);

        // in order not to loose state of ViewPager, first we
        // have to fill FormSectionsAdapter with data, and only then set it to ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        // TabLayout will fail on you, if ViewPager which is going to be
        // attached does not contain ViewPagerAdapter set to it.
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showFilterableDialog(Picker picker) {
        FilterableDialogFragment dialogFragment = FilterableDialogFragment.newInstance(picker);
        dialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());
        dialogFragment.show(getSupportFragmentManager(), FilterableDialogFragment.TAG);
    }

    @Override
    public void setFormSectionsPicker(Picker picker) {
        formSectionsPicker = picker;
    }

    private void attachListenerToExistingFragment() {
        FilterableDialogFragment dialogFragment = (FilterableDialogFragment) getSupportFragmentManager().findFragmentByTag(FilterableDialogFragment.TAG);
        // if we don't have fragment attached to activity,
        // we don't want to do anything else
        if (dialogFragment == null) {
            return;
        }
        dialogFragment.setOnPickerItemClickListener(new OnSearchSectionsClickListener());
    }

    @Override
    public void showTitle(String title) {
        Spanned spannedTitle = Html.fromHtml(String.format(
                "<b>%s</b>:<br/><u>%s</u>", organisationUnit, title));
        textViewOrganisationUnit.setText(spannedTitle);
    }

    @Override
    public void showSubtitle(String subtitle) {
        Spanned spannedSubtitle = Html.fromHtml(String.format(
                "<b>%s</b>:<br/><u>%s</u>", program, subtitle));
        textViewProgram.setText(spannedSubtitle);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //**********************************************************************************************
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

    //**********************************************************************************************
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
        private String formSectionId;

        public FormSingleSectionAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (DEFAULT_STAGE_POSITION == position && !isEmpty(formSectionId)) {
                return DataEntryFragment.newInstanceForStage(formSectionId);
            }

            return null;
        }

        @Override
        public int getCount() {
            return isEmpty(formSectionId) ? 0 : DEFAULT_STAGE_COUNT;
        }

        public void swapData(String programStageId) {
            this.formSectionId = programStageId;
            this.notifyDataSetChanged();
        }
    }

    //**********************************************************************************************
    private static class FormSectionsAdapter extends FragmentStatePagerAdapter {
        private final List<FormSection> formSections;

        public FormSectionsAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.formSections = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            FormSection formSection = formSections.get(position);
            return DataEntryFragment.newInstanceForSection(formSection.getId());
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

        public void swapData(List<FormSection> formSections) {
            this.formSections.clear();

            if (formSections != null) {
                this.formSections.addAll(formSections);
            }
            notifyDataSetChanged();
        }
    }
}
