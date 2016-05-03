package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

public class ProfileFragment extends BaseFragment implements ProfileView {
    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    @Inject
    ProfilePresenter profilePresenter;

    @Inject
    Logger logger;

    // pull-to-refresh
    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView recyclerView;

    RowViewAdapter rowViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // injection of profile presenter
        ((EventCaptureApp) getActivity().getApplication())
                .getUserComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_profile);
            getParentToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return ProfileFragment.this.onMenuItemClick(item);
                }
            });
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swiperefreshlayout_profile);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary_default);
        recyclerView = (RecyclerView) view
                .findViewById(R.id.recyclerview_profile);

        // we want RecyclerView to behave like ListView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Using ItemDecoration in order to implement divider
        DividerDecoration itemDecoration = new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider));

        rowViewAdapter = new RowViewAdapter(getChildFragmentManager());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(rowViewAdapter);

        if (savedInstanceState != null) {
            // this workaround is necessary because of the message queue
            // implementation in android. If you will try to setRefreshing(true) right away,
            // this call will be placed in UI message queue by SwipeRefreshLayout BEFORE
            // message to hide progress bar which probably is created by layout
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(savedInstanceState
                            .getBoolean(STATE_IS_REFRESHING, false));
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_REFRESHING, swipeRefreshLayout.isRefreshing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        profilePresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        profilePresenter.detachView();
    }

    @Override
    public void showProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "showProgressBar()");
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "hideProgressBar()");
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showUserAccountForm(List<FormEntity> formEntities) {
        rowViewAdapter.swap(formEntities);
    }

    @Override
    public String getUserAccountFieldLabel(@NonNull @UserAccountFieldId String fieldId) {
        switch (fieldId) {
            case ID_FIRST_NAME:
                return getString(R.string.first_name);
            case ID_SURNAME:
                return getString(R.string.surname);
            case ID_GENDER:
                return getString(R.string.gender);
            case ID_GENDER_MALE:
                return getString(R.string.gender_male);
            case ID_GENDER_FEMALE:
                return getString(R.string.gender_female);
            case ID_GENDER_OTHER:
                return getString(R.string.gender_other);
            case ID_BIRTHDAY:
                return getString(R.string.birthday);
            case ID_INTRODUCTION:
                return getString(R.string.introduction);
            case ID_EDUCATION:
                return getString(R.string.education);
            case ID_EMPLOYER:
                return getString(R.string.employer);
            case ID_INTERESTS:
                return getString(R.string.interests);
            case ID_JOB_TITLE:
                return getString(R.string.job_title);
            case ID_LANGUAGES:
                return getString(R.string.languages);
            case ID_EMAIL:
                return getString(R.string.email);
            case ID_PHONE_NUMBER:
                return getString(R.string.phone_number);
            default:
                throw new IllegalArgumentException("Unsupported prompt");
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        logger.d(SelectorFragment.class.getSimpleName(), "onMenuItemClick()");

        switch (item.getItemId()) {
            case R.id.action_refresh: {
                Toast.makeText(getActivity(), "Sync()",
                        Toast.LENGTH_SHORT).show();
                profilePresenter.sync();
                return true;
            }
        }

        return false;
    }
}
