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

import org.hisp.dhis.android.eventcapture.model.SessionManager;
import org.hisp.dhis.android.eventcapture.model.SyncManager;
import org.hisp.dhis.android.eventcapture.views.fragments.SelectorView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.core.common.utils.ModelUtils.ModelAction;
import static org.hisp.dhis.client.sdk.core.common.utils.ModelUtils.toUidSet;


public class SelectorPresenterImpl implements SelectorPresenter {
    private SelectorView selectorView;
    private CompositeSubscription subscriptions;

    public SelectorPresenterImpl(SelectorView selectorView) {
        this.selectorView = selectorView;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void initializeSynchronization(Boolean force) {
        if (force || !SessionManager.getInstance().isSelectorSynced()) {
            selectorView.onStartLoading();

            subscriptions.add(Observable.zip(
                    D2.me().organisationUnits().pull(), D2.me().programs().pull(),
                    new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                        @Override
                        public List<Program> call(List<OrganisationUnit> organisationUnits,
                                                  List<Program> programs) {
                            return programs;
                        }
                    })
                    .map(new Func1<List<Program>, List<ProgramStageDataElement>>() {
                        @Override
                        public List<ProgramStageDataElement> call(List<Program> programs) {
                            List<ProgramStage> programStages
                                    = loadProgramStages(programs);
                            List<ProgramStageSection> stageSections
                                    = loadProgramStageSections(programStages);
                            return loadProgramStageDataElements(programStages, stageSections);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStageDataElement>>() {
                        @Override
                        public void call(List<ProgramStageDataElement> stageDataElements) {
                            SessionManager.getInstance().setSelectorSynced(true);
                            // SyncManager.getInstance().setLastSyncedNow();

                            selectorView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }));
        } else {
            selectorView.onFinishLoading();
        }
    }

    private static List<ProgramStage> loadProgramStages(List<Program> programs) {
        Set<String> stageUids = toUidSet(programs, new ModelAction<Program>() {
            @Override
            public Collection<String> getUids(Program program) {
                return toUidSet(program.getProgramStages());
            }
        });

        return D2.programStages().pull(stageUids).toBlocking().first();
    }

    private static List<ProgramStageSection> loadProgramStageSections(List<ProgramStage> stages) {
        Set<String> sectionUids = toUidSet(stages, new ModelAction<ProgramStage>() {
            @Override
            public Collection<String> getUids(ProgramStage programStage) {
                return toUidSet(programStage.getProgramStageSections());
            }
        });

        return D2.programStageSections().pull(sectionUids).toBlocking().first();
    }

    private static List<ProgramStageDataElement> loadProgramStageDataElements(
            List<ProgramStage> stages, List<ProgramStageSection> sections) {

        Set<String> stageElementUids = toUidSet(stages, new ModelAction<ProgramStage>() {
            @Override
            public Collection<String> getUids(ProgramStage model) {
                return toUidSet(model.getProgramStageDataElements());
            }
        });

        Set<String> sectionElementUids = toUidSet(sections, new ModelAction<ProgramStageSection>() {
            @Override
            public Collection<String> getUids(ProgramStageSection model) {
                return toUidSet(model.getProgramStageDataElements());
            }
        });

        stageElementUids.addAll(sectionElementUids);
        return D2.programStageDataElements().pull(sectionElementUids).toBlocking().first();
    }
}
