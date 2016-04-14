package org.hisp.dhis.android.eventcapture.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.views.fragments.ItemListFragment;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SectionFilterActivity extends FragmentActivity {
    public static final String PROGRAM_STAGE_IX = "extra:ProgramStageIx";
    private String programStageUid;
    private List<ProgramStageSection> sectionsList;

    private Subscription listProgramStageDataElements;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public List<ProgramStageSection> getSectionList() {
        return sectionsList;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        programStageUid = intent.getStringExtra(ItemListFragment.PROGRAM_STAGE_UID);

        Log.d("SectionFilterActivity", "started");
        Log.d("SectionFilterActivity", "PROGRAM_STAGE_UID: " + programStageUid);

        setContentView(R.layout.activity_sctionfilter);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_sectionfilter);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //initializes the data and adapter.
        initSectionList();
    }

    public void initSectionList() {
        listProgramStageDataElements = D2.programStages().get(programStageUid)
                .map(new Func1<ProgramStage, List<ProgramStageSection>>() {
                    @Override
                    public List<ProgramStageSection> call(ProgramStage programStage) {
                        return D2.programStageSections().list(programStage).toBlocking().first();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageSection>>() {
                    @Override
                    public void call(List<ProgramStageSection> programStageSections) {
                        //update the program stage section list here :
                        sectionsList = programStageSections;
                        sectionsList.add(2, sectionsList.get(0));
                        sectionsList.add(3, sectionsList.get(0));
                        sectionsList.add(4, sectionsList.get(1));
                        sectionsList.add(5, sectionsList.get(1));
                        // specify an adapter (see also next example)
                        mAdapter = new SectionFilterAdapter(sectionsList);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });
    }



    public class SectionFilterAdapter extends RecyclerView.Adapter<SectionFilterAdapter.ViewHolder> {
        private List<ProgramStageSection> mSectionList;

        public SectionFilterAdapter(List<ProgramStageSection> myDataset) {
            mSectionList = myDataset;
        }

        @Override
        public SectionFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sectionfilter_text_view, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //System.out.println("Showing SectionList ix: " + position);
            holder.mTextView.setText(mSectionList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            //System.out.println("SectionList size:" + mSectionList.size());
            return mSectionList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.sectionlist_text);
                mTextView.findViewById(R.id.sectionlist_text).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("Clicked!!");
                        Intent intent = new Intent(getApplicationContext(), DataEntryActivity.class);
                        intent.putExtra(SectionFilterActivity.PROGRAM_STAGE_IX, getAdapterPosition());
                        //startActivity(intent);
                    }
                });
            }
        }
    }y 
}
