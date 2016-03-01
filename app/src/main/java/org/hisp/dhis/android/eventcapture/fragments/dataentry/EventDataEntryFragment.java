package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.profile.IProfilePresenter;
import org.hisp.dhis.android.eventcapture.fragments.profile.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

public class EventDataEntryFragment extends Fragment implements IEventDataEntryView {
    private RowViewAdapter rowViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eventdataentry, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rowViewAdapter = new RowViewAdapter(getFragmentManager());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_eventdataentry);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rowViewAdapter);
        recyclerView.addItemDecoration(new DividerDecoration(getContext()));

        IEventDataEntryPresenter eventDataEntryPresenter = new EventDataEntryPresenter(this);
        eventDataEntryPresenter.onCreate();
        eventDataEntryPresenter.listDataEntryFields();
    }

    @Override
    public void setDataEntryFields(List<DataEntity> dataEntities) {
        rowViewAdapter.swap(dataEntities);
    }
}
