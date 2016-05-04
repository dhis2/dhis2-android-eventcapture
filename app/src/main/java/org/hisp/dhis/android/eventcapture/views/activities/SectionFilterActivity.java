/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.SectionFilterPresenter;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SectionFilterActivity extends AppCompatActivity
        implements SectionFilterView, TextWatcher, View.OnClickListener {

    private static final String PROGRAM_STAGE_UID = "extra:programStageUid";

    private List<ProgramStageSection> sectionsList;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private EditText mSearchTextField;
    private String programStageUid;
    private Boolean configChanged = false;

    @Inject
    SectionFilterPresenter sectionFilterPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            configChanged = true;
        }

        ((EventCaptureApp) getApplication()).getUserComponent().inject(this);

        Intent intent = getIntent();
        programStageUid = intent.getStringExtra(PROGRAM_STAGE_UID);
        sectionFilterPresenter.setProgramStageUid(programStageUid);
        sectionFilterPresenter.attachView(this);

        setContentView(R.layout.activity_sctionfilter);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_sectionfilter);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ImageButton mClearButton = (ImageButton) findViewById(R.id.button_search_clear);
        mClearButton.setOnClickListener(this);

        mSearchTextField = (EditText) findViewById(R.id.section_search);
        mSearchTextField.addTextChangedListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (sectionsList != null) {
            ((SectionFilterAdapter) mAdapter).setItems(sectionFilterPresenter.filter(sectionsList, s.toString()));
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sectionFilterPresenter.setProgramStageUid(programStageUid);
        sectionFilterPresenter.attachView(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sectionFilterPresenter.detachView();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        mSearchTextField.setText("");
    }

    @Override
    public void setSectionList(List<ProgramStageSection> sections) {
        sectionsList = new ArrayList<>();
        sectionsList.addAll(sections);
        mAdapter = new SectionFilterActivity.SectionFilterAdapter(sections);
        mRecyclerView.setAdapter(mAdapter);
        if (configChanged) {
            onTextChanged(mSearchTextField.getText().toString(), 0, 0, 0);
        }
    }

    //**********************************************************************************************

    public class SectionFilterAdapter extends RecyclerView.Adapter<SectionFilterAdapter.ViewHolder> {
        private List<ProgramStageSection> mSectionList = new ArrayList<>();

        public SectionFilterAdapter(List<ProgramStageSection> myDataset) {
            mSectionList.addAll(myDataset);
        }

        @Override
        public SectionFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sectionfilter_text_view, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.mTextView.setText(mSectionList.get(position).getName());
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

            public ViewHolder(View view) {
                super(view);
                mTextView = (TextView) view.findViewById(R.id.sectionlist_text);
                mTextView.findViewById(R.id.sectionlist_text).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(FormSectionActivity.PROGRAM_STAGE_UID,
                                        mSectionList.get(getAdapterPosition()).getUId());
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        });
            }
        }
    }
}
