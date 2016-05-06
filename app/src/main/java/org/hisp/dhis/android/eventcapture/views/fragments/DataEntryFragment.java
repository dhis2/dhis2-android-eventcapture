package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
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
import org.hisp.dhis.android.eventcapture.presenters.DataEntryPresenterImpl;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.utils.LoggerImpl;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

import javax.inject.Inject;

public class DataEntryFragment extends BaseFragment implements DataEntryView {
    private static final String ARG_PROGRAM_STAGE_SECTION_ID = "arg:programStageSectionId";

    @Inject
    DataEntryPresenter dataEntryPresenter;

    RecyclerView recyclerView;

    RowViewAdapter rowViewAdapter;

    public static DataEntryFragment newInstance(String programStageSectionId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_PROGRAM_STAGE_SECTION_ID, programStageSectionId);

        DataEntryFragment dataEntryFragment = new DataEntryFragment();
        dataEntryFragment.setArguments(arguments);

        return dataEntryFragment;
    }

    private String getProgramStageSectionId() {
        return getArguments().getString(ARG_PROGRAM_STAGE_SECTION_ID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inject dependencies
        ((EventCaptureApp) getActivity().getApplication())
                .getFormComponent().inject(this);
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

        dataEntryPresenter.createDataEntryForm(getProgramStageSectionId());
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
