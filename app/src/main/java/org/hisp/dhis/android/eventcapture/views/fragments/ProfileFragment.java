package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

import javax.inject.Inject;

public class ProfileFragment extends Fragment implements ProfileView {

    @Inject
    ProfilePresenter profilePresenter;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // we want RecyclerView to behave like ListView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Using ItemDecoration in order to implement divider
        DividerDecoration itemDecoration = new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider));

        rowViewAdapter = new RowViewAdapter(getChildFragmentManager());

        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(rowViewAdapter);
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
}
