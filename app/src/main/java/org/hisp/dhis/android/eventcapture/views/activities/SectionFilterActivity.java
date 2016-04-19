package org.hisp.dhis.android.eventcapture.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.views.fragments.ItemListFragment;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SectionFilterActivity extends FragmentActivity implements TextWatcher, View.OnClickListener {

    private String programStageUid;
    private List<ProgramStageSection> sectionsList;

    private Subscription listProgramStageDataElements;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private EditText mSearchTextField;
    private ImageButton mClearButton;

    public List<ProgramStageSection> getSectionList() {
        return sectionsList;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        programStageUid = intent.getStringExtra(ItemListFragment.PROGRAM_STAGE_UID);

        setContentView(R.layout.activity_sctionfilter);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_sectionfilter);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mClearButton = (ImageButton) findViewById(R.id.button_search_clear);
        mClearButton.setOnClickListener(this);

        mSearchTextField = (EditText) findViewById(R.id.section_search);
        mSearchTextField.addTextChangedListener(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("Sections");
        
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
                        sectionsList = new ArrayList<>();
                        sectionsList.addAll(programStageSections);
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

    private List<ProgramStageSection> filter(List<ProgramStageSection> models, String query) {
        query = query.toLowerCase();
        final List<ProgramStageSection> filteredModelList = new ArrayList<>();
        for (ProgramStageSection model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        List<ProgramStageSection> filteredSections = filter(sectionsList, s.toString());
        ((SectionFilterAdapter) mAdapter).setItems(filteredSections);
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        //clear button clicked.
        mSearchTextField.setText("");
    }

    //**********************************************************************************************

    public class SectionFilterAdapter extends RecyclerView.Adapter<SectionFilterAdapter.ViewHolder> {
        private List<ProgramStageSection> mSectionList = new ArrayList<>();

        public SectionFilterAdapter(List<ProgramStageSection> myDataset) {
            mSectionList.addAll(myDataset);
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
            holder.mTextView.setText(mSectionList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mSectionList.size();
        }

        public void setItems(List<ProgramStageSection> models) {
            mSectionList = models;
            notifyDataSetChanged();
        }

        //******************************************************************************************

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.sectionlist_text);
                mTextView.findViewById(R.id.sectionlist_text).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(DataEntryActivity.PROGRAM_STAGE_UID,
                                        mSectionList.get(getAdapterPosition()).getUId());
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        });
            }
        }
    }
}
