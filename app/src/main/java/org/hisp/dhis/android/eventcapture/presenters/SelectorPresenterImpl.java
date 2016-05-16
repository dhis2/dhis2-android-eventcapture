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

import org.hisp.dhis.android.eventcapture.SessionPreferences;
import org.hisp.dhis.android.eventcapture.model.ReportEntity;
import org.hisp.dhis.android.eventcapture.model.SyncDateWrapper;
import org.hisp.dhis.android.eventcapture.model.SyncWrapper;
import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.SelectorView;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.http.HEAD;
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
    private final UserOrganisationUnitInteractor userOrganisationUnitInteractor;
    private final UserProgramInteractor userProgramInteractor;
    private final ProgramStageInteractor programStageInteractor;
    private final EventInteractor eventInteractor;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final SyncWrapper syncWrapper;
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean isSyncedInitially;
    private SelectorView selectorView;

    public SelectorPresenterImpl(UserOrganisationUnitInteractor interactor,
                                 UserProgramInteractor userProgramInteractor,
                                 ProgramStageInteractor programStageInteractor,
                                 SyncWrapper syncWrapper,
                                 EventInteractor eventInteractor,
                                 SessionPreferences sessionPreferences,
                                 SyncDateWrapper syncDateWrapper,
                                 Logger logger) {
        this.userOrganisationUnitInteractor = interactor;
        this.userProgramInteractor = userProgramInteractor;
        this.programStageInteractor = programStageInteractor;
        this.eventInteractor = eventInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.syncWrapper = syncWrapper;
        this.logger = logger;
        this.subscription = new CompositeSubscription();
        this.isSyncedInitially = false;
    }

    public void attachView(View view) {
        isNull(view, "SelectorView must not be null");
        selectorView = (SelectorView) view;

        // check if metadata was synced,
        // if not, sync it
        if (!isSyncedInitially) {
            sync();
        }
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
    public void onPickersSelectionsChanged(List<Picker> pickerList) {
        if (pickerList != null) {
            sessionPreferences.clearSelectedPickers();
            for (int index = 0; index < pickerList.size(); index++) {
                Picker current = pickerList.get(index);
                Picker child = current.getSelectedChild();
                if (child == null) { //done with pickers. exit.
                    return;
                }
                String pickerId = child.getId();
                sessionPreferences.setSelectedPickerUid(index, pickerId);
            }
        }
    }

    @Override
    public void sync() {
        selectorView.showProgressBar();
        syncWrapper.sync()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> stageDataElements) {
                        isSyncedInitially = true;
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
                });
    }

    @Override
    public void listPickers() {
        logger.d(TAG, "listPickers()");
        subscription.add(Observable.zip(
                userOrganisationUnitInteractor.list(),
                userProgramInteractor.list(),
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

    @Override
    public void listEvents(String organisationUnitId, String programId) {
        OrganisationUnit orgUnit = new OrganisationUnit();
        Program program = new Program();

        orgUnit.setUId(organisationUnitId);
        program.setUId(programId);

        subscription.add(eventInteractor.list(orgUnit, program)
                .map(new Func1<List<Event>, List<ReportEntity>>() {
                    @Override
                    public List<ReportEntity> call(List<Event> events) {
                        List<ReportEntity> reportEntities = new ArrayList<>();

                        for (int position = 0; position < events.size(); position++) {
                            Event event = events.get(position);

                            ReportEntity.Status status;
                            if (position % 3 == 0) {
                                status = ReportEntity.Status.SENT;
                            } else if (position % 2 == 0) {
                                status = ReportEntity.Status.OFFLINE;
                            } else {
                                status = ReportEntity.Status.ERROR;
                            }

                            reportEntities.add(new ReportEntity(
                                    event.getUId(), status, "Some event is here",
                                    "Another important line which describes something",
                                    "One more line with some information"));
                        }

                        return reportEntities;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ReportEntity>>() {
                    @Override
                    public void call(List<ReportEntity> reportEntities) {
                        if (selectorView != null) {
                            selectorView.showReportEntities(reportEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed loading events", throwable);
                    }
                }));
    }

    @Override
    public void createEvent(final String orgUnitId, final String programId) {
        final OrganisationUnit orgUnit = new OrganisationUnit();
        final Program program = new Program();

        orgUnit.setUId(orgUnitId);
        program.setUId(programId);

        subscription.add(programStageInteractor.list(program)
                .map(new Func1<List<ProgramStage>, ProgramStage>() {
                    @Override
                    public ProgramStage call(List<ProgramStage> stages) {
                        if (stages != null && !stages.isEmpty()) {
                            return stages.get(0);
                        }

                        return null;
                    }
                })
                .map(new Func1<ProgramStage, Event>() {
                    @Override
                    public Event call(ProgramStage programStage) {
                        if (programStage == null) {
                            throw new IllegalArgumentException("In order to create event, " +
                                    "we need program stage to be in place");
                        }

                        Event event = eventInteractor.create(orgUnit, program,
                                programStage, Event.EventStatus.ACTIVE);
                        eventInteractor.save(event).toBlocking().first();

                        return event;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        if (selectorView != null) {
                            selectorView.navigateToFormSectionActivity(event);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed creating event", throwable);
                    }
                })
        );
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
                    Picker programPicker = Picker.create(assignedProgram.getUId(),
                            assignedProgram.getDisplayName(), organisationUnitPicker);
                    organisationUnitPicker.addChild(programPicker);
                }
            }
            rootPicker.addChild(organisationUnitPicker);
        }

        //Set saved selections or default ones :
        if (sessionPreferences.getSelectedPickerUid(0) != null) {
            traverseAndSetSavedSelection(rootPicker);
        } else {
            // Traverse the tree. If there is a path with nodes
            // which have only one child, set default selection
            traverseAndSetDefaultSelection(rootPicker);
        }
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

    private void traverseAndSetSavedSelection(Picker node) {
        int treeLevel = 0;
        while (node != null) {
            String pickerId = sessionPreferences.getSelectedPickerUid(treeLevel);
            if (pickerId != null) {
                for (Picker child : node.getChildren()) {

                    if (child.getId().equals(pickerId)) {
                        node.setSelectedChild(child);
                        break;
                    }
                }
            }
            treeLevel++;
            node = node.getSelectedChild();
        }
    }
}
