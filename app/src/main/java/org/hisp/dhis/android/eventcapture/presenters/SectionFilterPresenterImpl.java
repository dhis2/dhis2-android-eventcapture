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

package org.hisp.dhis.android.eventcapture.presenters;


import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.activities.SectionFilterView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class SectionFilterPresenterImpl implements SectionFilterPresenter {

    private SectionFilterView sectionFilterView;
    private String programStageUid;

    @Override
    public void attachView(View view) {
        isNull(view, "SectionFilterView must not be null");

        this.sectionFilterView = (SectionFilterView) view;
        initSectionList();
    }

    @Override
    public void detachView() {
        sectionFilterView = null;
    }

    public SectionFilterPresenterImpl() {
    }

    public void setProgramStageUid(String uid) {
        this.programStageUid = uid;
    }

    public void initSectionList() {
        D2.programStages().get(programStageUid)
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
                        sectionFilterView.setSectionList(programStageSections);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });
    }

    public List<ProgramStageSection> filter(List<ProgramStageSection> models, String query) {
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
}
