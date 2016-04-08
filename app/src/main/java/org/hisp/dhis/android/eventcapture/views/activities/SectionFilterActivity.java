package org.hisp.dhis.android.eventcapture.views.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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

    private Subscription listProgramStageDataElements;
    private RecyclerView sectionRecyclerView;
    private String programStageUid;
    private List<ProgramStageSection> sectionsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        programStageUid = intent.getStringExtra(ItemListFragment.PROGRAM_STAGE_UID);

        Log.d("SectionFilterActivity", "started");
        Log.d("SectionFilterActivity", "PROGRAM_STAGE_UID: " + programStageUid);

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
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });
    }

    public List<ProgramStageSection> getSectionList() {
        return sectionsList;
    }

}
