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
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.IDataEntity;
import org.hisp.dhis.client.sdk.ui.rows.RowViewAdapter;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;

import java.util.List;

public class EventDataEntryFragment extends Fragment implements IEventDataEntryView {
    private RowViewAdapter rowViewAdapter;
    private static String EXTRA_SECTION_UID = "extra:SectionUid";
    private static String EXTRA_EVENT_UID = "extra:EventUid";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventDataEntryFragment newInstance(String eventUId, String sectionUid) {
        EventDataEntryFragment eventDataEntryFragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_EVENT_UID, eventUId);
        args.putString(EXTRA_SECTION_UID, sectionUid);
        eventDataEntryFragment.setArguments(args);

        return eventDataEntryFragment;
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

        String eventUId = getArguments().getString(EXTRA_EVENT_UID);
        String sectionUid = getArguments().getString(EXTRA_SECTION_UID);

        IEventDataEntryPresenter eventDataEntryPresenter = new EventDataEntryPresenter(this);
        eventDataEntryPresenter.listDataEntryFieldsWithEventValues(eventUId, sectionUid);
    }

    @Override
    public void setDataEntryFields(List<IDataEntity> dataEntities) {
        rowViewAdapter.swap(dataEntities);
    }
}
