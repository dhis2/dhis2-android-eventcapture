package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class DataEntryFragment extends BaseFragment implements DataEntryView {
    private static final String ARG_ORGANISATION_UNIT_ID = "arg:organisationUnitId";
    private static final String ARG_PROGRAM_STAGE_ID = "arg:programStageId";
    private static final String ARG_PROGRAM_STAGE_SECTION_ID = "arg:programStageSectionId";

    @Inject
    DataEntryPresenter dataEntryPresenter;

    RecyclerView recyclerView;

    RowViewAdapter rowViewAdapter;

    public static DataEntryFragment newInstanceForStage(@NonNull String programStageId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_PROGRAM_STAGE_ID, programStageId);

        DataEntryFragment dataEntryFragment = new DataEntryFragment();
        dataEntryFragment.setArguments(arguments);

        return dataEntryFragment;
    }

    public static DataEntryFragment newInstanceForSection(@NonNull String programStageSectionId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_PROGRAM_STAGE_SECTION_ID, programStageSectionId);

        DataEntryFragment dataEntryFragment = new DataEntryFragment();
        dataEntryFragment.setArguments(arguments);

        return dataEntryFragment;
    }

    private String getProgramStageId() {
        return getArguments().getString(ARG_PROGRAM_STAGE_ID, null);
    }

    private String getProgramStageSectionId() {
        return getArguments().getString(ARG_PROGRAM_STAGE_SECTION_ID, null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rowViewAdapter = new RowViewAdapter(getChildFragmentManager());

        // Using ItemDecoration in order to implement divider
        DividerDecoration itemDecoration = new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider));

        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(rowViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // injection point was changed from onCreate() to onActivityCreated()
        // because od stupid fragment lifecycle
        ((EventCaptureApp) getActivity().getApplication()).getFormComponent().inject(this);

        if (!isEmpty(getProgramStageId())) {
            // Pass event id into presenter
            dataEntryPresenter.createDataEntryFormStage("", getProgramStageId());
            return;
        }

        if (!isEmpty(getProgramStageSectionId())) {
            // Pass event id into presenter
            dataEntryPresenter.createDataEntryFormSection("", getProgramStageSectionId());
        }
    }

    @Override
    public void onResume() {
        dataEntryPresenter.attachView(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        dataEntryPresenter.detachView();
        super.onPause();
    }

    @Override
    public void showDataEntryForm(List<FormEntity> formEntities) {
        rowViewAdapter.swap(formEntities);
    }
}
