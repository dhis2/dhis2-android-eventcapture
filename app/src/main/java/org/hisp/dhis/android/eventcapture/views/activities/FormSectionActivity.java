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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.FormComponent;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryFragment;
import org.hisp.dhis.client.sdk.ui.adapters.OnPickerItemClickListener;
import org.hisp.dhis.client.sdk.ui.adapters.PickerItemAdapter;
import org.hisp.dhis.client.sdk.ui.models.FormSection;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.ui.views.AbsTextWatcher;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


// TODO check if configuration changes are handled properly
public class FormSectionActivity extends AppCompatActivity implements FormSectionView {
    private static final String ARG_EVENT_UID = "arg:eventUid";

    @Inject
    FormSectionPresenter formSectionPresenter;

    // collapsing toolbar views
    TextView textViewOrganisationUnit;
    TextView textViewProgram;

    // Drawer layout with section filtering
    DrawerLayout sectionsDrawer;
    EditText sectionFilterEditText;
    RecyclerView recyclerViewSections;
    PickerItemAdapter recyclerViewItemAdapter;

    // section tabs and view pager
    TabLayout tabLayout;
    ViewPager viewPager;

    // prompts
    String organisationUnit;
    String program;

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
        setContentView(R.layout.activity_filter_sections);

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

        setupToolbar();
        setupLabels();
        setupViewPager();
        setupNavigationDrawer();

        // start building the form
        formSectionPresenter.createDataEntryForm(getEventUid());
    }

//    @Override
//    protected void onDestroy() {
//        // release component
//        // ((EventCaptureApp) getApplication()).releaseFormComponent();
//
//        super.onDestroy();
//    }

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
        if (sectionsDrawer.getDrawerLockMode(GravityCompat.END) !=
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
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
                sectionsDrawer.openDrawer(GravityCompat.END);
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

        // if we don't have sections, we don't need to show navigation drawer
        sectionsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

        // we also need to hide filter icon
        supportInvalidateOptionsMenu();
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

    @Override
    public void showDrawerSections(Picker picker) {
        recyclerViewItemAdapter.swapData(picker);
        recyclerViewSections.setAdapter(recyclerViewItemAdapter);

        // in case if configuration change happened and we already had filter string set
        // to field, we have to perform filtering afterwards
        if (!isEmpty(sectionFilterEditText.getText())) {
            recyclerViewItemAdapter.filter(sectionFilterEditText.getText().toString());
        }
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
        if (sectionsDrawer.isDrawerOpen(GravityCompat.END)) {
            sectionsDrawer.closeDrawer(GravityCompat.END);
            return;
        }

        super.onBackPressed();
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

    private void setupLabels() {
        textViewOrganisationUnit = (TextView) findViewById(R.id.textview_organisation_unit);
        textViewProgram = (TextView) findViewById(R.id.textview_program);

        organisationUnit = getString(R.string.organisation_unit);
        program = getString(R.string.program);
    }

    private void setupViewPager() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout_data_entry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_dataentry);
    }

    private void setupNavigationDrawer() {
        sectionsDrawer = (DrawerLayout) findViewById(R.id.drawerlayout_form_sections);

        recyclerViewItemAdapter = new PickerItemAdapter(this);
        recyclerViewItemAdapter.setOnPickerItemClickListener(new OnPickerItemClickListener() {

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

                sectionsDrawer.closeDrawer(GravityCompat.END);
            }
        });

        recyclerViewSections = (RecyclerView) findViewById(R.id.recyclerview_sectionfilter);
        if (recyclerViewSections != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            recyclerViewSections.setLayoutManager(layoutManager);
            recyclerViewSections.addItemDecoration(new DividerDecoration(
                    ContextCompat.getDrawable(this, R.drawable.divider)));
            recyclerViewSections.setAdapter(recyclerViewItemAdapter);
        }

        sectionFilterEditText = (EditText) findViewById(R.id.edittext_filter_picker_items);
        if (sectionFilterEditText != null) {
            sectionFilterEditText.addTextChangedListener(new AbsTextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                    recyclerViewItemAdapter.filter(editable.toString());
                }
            });
        }
    }

    //********************************************************************************
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

    //********************************************************************************
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
