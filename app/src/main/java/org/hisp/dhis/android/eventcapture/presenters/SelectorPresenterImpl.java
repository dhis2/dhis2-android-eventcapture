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

import org.hisp.dhis.android.eventcapture.model.SyncDateWrapper;
import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.SelectorView;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;


public class SelectorPresenterImpl implements SelectorPresenter {
    private static final String TAG = SelectorPresenterImpl.class.getSimpleName();

    private final UserOrganisationUnitInteractor organisationUnitInteractor;
    private final UserProgramInteractor programInteractor;
    private final ProgramStageInteractor programStageInteractor;
    private final ProgramStageSectionInteractor programStageSectionInteractor;
    private final ProgramStageDataElementInteractor programStageDataElementInteractor;
    private final SyncDateWrapper syncDateWrapper;
    private final Logger logger;

    private CompositeSubscription subscription;
    private SelectorView selectorView;

    public SelectorPresenterImpl(UserOrganisationUnitInteractor interactor,
                                 UserProgramInteractor programInteractor,
                                 ProgramStageInteractor programStageInteractor,
                                 ProgramStageSectionInteractor programStageSectionInteractor,
                                 ProgramStageDataElementInteractor stageDataElementInteractor,
                                 SyncDateWrapper syncDateWrapper,
                                 Logger logger) {
        this.organisationUnitInteractor = interactor;
        this.programInteractor = programInteractor;
        this.programStageInteractor = programStageInteractor;
        this.programStageSectionInteractor = programStageSectionInteractor;
        this.programStageDataElementInteractor = stageDataElementInteractor;
        this.subscription = new CompositeSubscription();
        this.syncDateWrapper = syncDateWrapper;
        this.logger = logger;
    }

    public void attachView(View view) {
        isNull(view, "SelectorView must not be null");
        selectorView = (SelectorView) view;
    }

    @Override
    public void detachView() {
        selectorView = null;
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = new CompositeSubscription();
        }
    }

    @Override
    public void sync() {
        selectorView.showProgressBar();

        subscription.add(Observable.zip(
                organisationUnitInteractor.pull(), programInteractor.pull(),
                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                    @Override
                    public List<Program> call(List<OrganisationUnit> units, List<Program> programs) {
                        return programs;
                    }
                })
                .map(new Func1<List<Program>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<Program> programs) {
                        List<Program> programsWithoutRegistration = new ArrayList<>();

                        if (programs != null && !programs.isEmpty()) {
                            for (Program program : programs) {
                                if (ProgramType.WITHOUT_REGISTRATION
                                        .equals(program.getProgramType())) {
                                    programsWithoutRegistration.add(program);
                                }
                            }
                        }

                        List<ProgramStage> programStages =
                                loadProgramStages(programsWithoutRegistration);
                        List<ProgramStageSection> programStageSections =
                                loadProgramStageSections(programStages);

                        return loadProgramStageDataElements(programStages, programStageSections);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> stageDataElements) {
                        syncDateWrapper.setLastSyncedNow();

                        if (selectorView != null) {
                            selectorView.hideProgressBar();
                        }

                        listPickers();
                    }
                }, new Action1<Throwable>() {

                    @Override
                    public void call(Throwable throwable) {
                        if (selectorView != null) {
                            selectorView.hideProgressBar();
                        }

                        throwable.printStackTrace();
                    }
                }));
    }

    @Override
    public void listPickers() {
        logger.d(TAG, "listPickers()");
        subscription.add(Observable.zip(organisationUnitInteractor.list(), programInteractor.list(),
                new Func2<List<OrganisationUnit>, List<Program>, Picker>() {
                    @Override
                    public Picker call(List<OrganisationUnit> units, List<Program> programs) {
                        return createPickerTree(units, programs);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Picker>() {
                    @Override
                    public void call(Picker picker) {
                        if (selectorView != null) {
                            selectorView.showPickers(picker);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    /*
     * Goes through given organisation units and programs and builds Picker tree
     */
    private Picker createPickerTree(List<OrganisationUnit> units, List<Program> programs) {
        Map<String, OrganisationUnit> organisationUnitMap = ModelUtils.toMap(units);
        Map<String, Program> assignedProgramsMap = ModelUtils.toMap(programs);

        String chooseOrganisationUnit = selectorView != null ? selectorView
                .getPickerLabel(SelectorView.ID_CHOOSE_ORGANISATION_UNIT) : "";
        String chooseProgram = selectorView != null ? selectorView
                .getPickerLabel(SelectorView.ID_CHOOSE_PROGRAM) : "";

        Picker rootPicker = Picker.create(chooseOrganisationUnit);
        for (String unitKey : organisationUnitMap.keySet()) {

            // Creating organisation unit picker items
            OrganisationUnit organisationUnit = organisationUnitMap.get(unitKey);
            Picker organisationUnitPicker = Picker.create(
                    organisationUnit.getUId(), organisationUnit.getDisplayName(),
                    chooseProgram, rootPicker);

            for (Program program : organisationUnit.getPrograms()) {
                Program assignedProgram = assignedProgramsMap.get(program.getUId());

                if (assignedProgram != null && ProgramType.WITHOUT_REGISTRATION
                        .equals(assignedProgram.getProgramType())) {
                    Picker programPicker = Picker.create(
                            assignedProgram.getUId(),
                            assignedProgram.getDisplayName(),
                            organisationUnitPicker);
                    organisationUnitPicker.addChild(programPicker);
                }
            }
            rootPicker.addChild(organisationUnitPicker);
        }

        // Traverse the tree. If there is a path with nodes
        // which have only one child, set default selection
        traverseAndSetDefaultSelection(rootPicker);
        return rootPicker;
    }

    private static void traverseAndSetDefaultSelection(Picker tree) {
        if (tree != null) {

            Picker node = tree;
            do {
                if (node.getChildren().size() == 1) {
                    // get the only child node and set it as selected
                    Picker singleChild = node.getChildren().get(0);
                    node.setSelectedChild(singleChild);
                }
            } while ((node = node.getSelectedChild()) != null);
        }
    }

    private List<ProgramStage> loadProgramStages(List<Program> programs) {
        Set<String> stageUids = new HashSet<>();

        for (Program program : programs) {
            Set<String> programStageUids = ModelUtils.toUidSet(
                    program.getProgramStages());
            stageUids.addAll(programStageUids);
        }

        return programStageInteractor.pull(stageUids).toBlocking().first();
    }

    private List<ProgramStageSection> loadProgramStageSections(List<ProgramStage> stages) {
        Set<String> sectionUids = new HashSet<>();

        for (ProgramStage programStage : stages) {
            Set<String> stageSectionUids = ModelUtils.toUidSet(
                    programStage.getProgramStageSections());
            sectionUids.addAll(stageSectionUids);
        }

        return programStageSectionInteractor.pull(sectionUids).toBlocking().first();
    }

    private List<ProgramStageDataElement> loadProgramStageDataElements(
            List<ProgramStage> stages, List<ProgramStageSection> programStageSections) {
        Set<String> dataElementUids = new HashSet<>();

        for (ProgramStage programStage : stages) {
            Set<String> stageDataElementUids = ModelUtils.toUidSet(
                    programStage.getProgramStageDataElements());
            dataElementUids.addAll(stageDataElementUids);
        }

        for (ProgramStageSection programStageSection : programStageSections) {
            Set<String> stageSectionElements = ModelUtils.toUidSet(
                    programStageSection.getProgramStageDataElements());
            dataElementUids.addAll(stageSectionElements);
        }

        return programStageDataElementInteractor.pull(dataElementUids).toBlocking().first();
    }
}
