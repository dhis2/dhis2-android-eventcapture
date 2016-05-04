package org.hisp.dhis.android.eventcapture.views.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.FormSectionPresenter;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryFragment;
import org.hisp.dhis.client.sdk.ui.models.FormSection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class FormSectionsActivity extends AppCompatActivity implements FormSectionView {
    private static final String ARG_ORGANISATION_UNIT_ID = "arg:organisationUnitId";
    private static final String ARG_PROGRAM_ID = "arg:programId";

    @Inject
    FormSectionPresenter formSectionPresenter;

    CollapsingToolbarLayout collapsingToolbarLayout;

    // section tabs
    TabLayout tabLayout;

    // view pager
    ViewPager viewPager;
    FormSectionsAdapter viewPagerAdapter;

    public static void navigateTo(Activity activity, String organisationUnitId, String programId) {
        isNull(activity, "activity must not be null");

        Intent intent = new Intent(activity, FormSectionsActivity.class);
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
        setContentView(R.layout.activity_form_sections);

        // injecting dependencies into FormSectionsActivity
        ((EventCaptureApp) getApplication())
                .getUserComponent().inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        tabLayout = (TabLayout) findViewById(R.id.tablayout_data_entry);
        viewPager = (ViewPager) findViewById(R.id.viewpager_dataentry);
        viewPagerAdapter = new FormSectionsAdapter(getSupportFragmentManager());

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        formSectionPresenter.createDataEntryForm(
                getOrganisationUnitId(), getProgramId());
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
    public void showFormSections(List<FormSection> formSections) {
        viewPagerAdapter.swapData(formSections);
    }

    @Override
    public void showTitle(String title) {
        collapsingToolbarLayout.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class FormSectionsAdapter extends FragmentStatePagerAdapter {
        private final List<FormSection> formSections;

        public FormSectionsAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.formSections = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return new DataEntryFragment();
        }

        @Override
        public int getCount() {
            return formSections.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return formSections.get(position).getLabel();
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
