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

import android.support.v4.util.Pair;

import org.hisp.dhis.android.eventcapture.model.SyncWrapper;
import org.hisp.dhis.android.eventcapture.views.SelectorView;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.UserProgramInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.common.state.State;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.client.sdk.utils.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class SelectorPresenterImpl implements SelectorPresenter {
    private static final String TAG = SelectorPresenterImpl.class.getSimpleName();
    private final UserOrganisationUnitInteractor userOrganisationUnitInteractor;
    private final UserProgramInteractor userProgramInteractor;
    private final ProgramStageInteractor programStageInteractor;
    private final ProgramStageDataElementInteractor programStageDataElementInteractor;
    private final EventInteractor eventInteractor;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
    private final SyncWrapper syncWrapper;
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean hasSyncedBefore;
    private SelectorView selectorView;
    private boolean isSyncing;
    private HashMap reportEntityDataElementFilter;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public SelectorPresenterImpl(UserOrganisationUnitInteractor interactor,
                                 UserProgramInteractor userProgramInteractor,
                                 ProgramStageInteractor programStageInteractor,
                                 ProgramStageDataElementInteractor stageDataElementInteractor,
                                 EventInteractor eventInteractor,
                                 SessionPreferences sessionPreferences,
                                 SyncDateWrapper syncDateWrapper,
                                 SyncWrapper syncWrapper,
                                 ApiExceptionHandler apiExceptionHandler,
                                 Logger logger) {
        this.userOrganisationUnitInteractor = interactor;
        this.userProgramInteractor = userProgramInteractor;
        this.programStageInteractor = programStageInteractor;
        this.programStageDataElementInteractor = stageDataElementInteractor;
        this.eventInteractor = eventInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
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

    public void attachView(View view) {
        isNull(view, "SelectorView must not be null");

        selectorView = (SelectorView) view;

        if (isSyncing) {
            selectorView.showProgressBar();
        } else {
            selectorView.hideProgressBar();
        }

        // check if metadata was synced,
        // if not, sync it
        if (!isSyncing && !hasSyncedBefore) {
            sync();
        }

        listPickers();
    }

    @Override
    public void detachView() {
        selectorView.hideProgressBar();
        selectorView = null;
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
        isSyncing = true;
        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> stageDataElements) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        syncDateWrapper.setLastSyncedNow();

                        if (selectorView != null) {
                            selectorView.hideProgressBar();
                        }
                        listPickers();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (selectorView != null) {
                            selectorView.hideProgressBar();
                        }
                        handleError(throwable);
                    }
                }));
        subscription.add(syncWrapper.syncData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        listPickers();

                        logger.d(TAG, "Synced events successfully");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to sync events", throwable);
                    }
                }));
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
                        logger.e(TAG, "Failed listing pickers.", throwable);
                    }
                }));
    }

    @Override
    public void listEvents(String organisationUnitId, final String programId) {
        final OrganisationUnit orgUnit = new OrganisationUnit();
        final Program program = new Program();

        orgUnit.setUId(organisationUnitId);
        program.setUId(programId);

        subscription.add(programStageInteractor.list(program)
                .switchMap(new Func1<List<ProgramStage>, Observable<List<ReportEntity>>>() {
                    @Override
                    public Observable<List<ReportEntity>> call(List<ProgramStage> stages) {
                        if (stages == null || stages.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Program should contain at least one program stage");
                        }

                        Observable<List<ProgramStageDataElement>> stageDataElements =
                                programStageDataElementInteractor.list(stages.get(0));

                        return Observable.zip(
                                stageDataElements, eventInteractor.list(orgUnit, program),
                                new Func2<List<ProgramStageDataElement>, List<Event>, List<ReportEntity>>() {

                                    @Override
                                    public List<ReportEntity> call(List<ProgramStageDataElement> stageDataElements,
                                                                   List<Event> events) {
                                        reportEntityDataElementFilter = sessionPreferences.getReportEntityDataModelFilters(
                                                programId,
                                                mapDataElementNameToDefaultViewSetting(stageDataElements));
                                        return transformEvents(stageDataElements, events);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ReportEntity>>() {
                    @Override
                    public void call(List<ReportEntity> reportEntities) {

                        if (selectorView != null) {
                            selectorView.setReportEntityLabelFilters(reportEntityDataElementFilter);
                            selectorView.showFilterOptionItem(true);
                            selectorView.showReportEntities(reportEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (selectorView != null) {
                            selectorView.showFilterOptionItem(false);
                        }
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
                        event.setEventDate(DateTime.now());
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

    @Override
    public void deleteEvent(final ReportEntity reportEntity) {
        subscription.add(eventInteractor.get(reportEntity.getId())
                .switchMap(new Func1<Event, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Event event) {
                        return eventInteractor.remove(event);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        logger.d(TAG, "Event deleted");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Error deleting event: " + reportEntity, throwable);
                        if (selectorView != null) {
                            selectorView.onReportEntityDeletionError(reportEntity);
                        }
                    }
                }));
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        selectorView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        selectorView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        selectorView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    @Override
    public void setReportEntityDataElementFilters(String programId, HashMap<String, Pair<String, Boolean>> filters) {
        sessionPreferences.setReportEntityDataModelFilters(programId, filters);
    }

    private List<ReportEntity> transformEvents(List<ProgramStageDataElement> dataElements,
                                               List<Event> events) {
        List<ReportEntity> reportEntities = new ArrayList<>();

        for (Event event : events) {

            // status of event
            ReportEntity.Status status;
            // TODO remove hack
            // get state of event from database
            State state = eventInteractor.get(event).toBlocking().first();

            logger.d(TAG, "State action for event " + event + " is " + state.getAction());
            switch (state.getAction()) {
                case SYNCED: {
                    status = ReportEntity.Status.SENT;
                    break;
                }
                case TO_POST: {
                    status = ReportEntity.Status.TO_POST;
                    break;
                }
                case TO_UPDATE: {
                    status = ReportEntity.Status.TO_UPDATE;
                    break;
                }
                case ERROR: {
                    status = ReportEntity.Status.ERROR;
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                            "Unsupported event state: " + state.getAction());
                }
            }

            Map<String, String> dataElementToValueMap =
                    mapDataElementToValue(event.getDataValues());

            dataElementToValueMap.put("Event date",
                    event.getEventDate().toString(DateTimeFormat.forPattern(DATE_FORMAT)));
            dataElementToValueMap.put("Status", event.getStatus().toString());

            reportEntities.add(
                    new ReportEntity(
                            event.getUId(),
                            status,
                            dataElementToValueMap));
        }

        return reportEntities;
    }

    private HashMap<String, Pair<String, Boolean>> mapDataElementNameToDefaultViewSetting(
            List<ProgramStageDataElement> dataElements) {

        HashMap<String, Pair<String, Boolean>> map = new HashMap<>();
        if (dataElements != null && !dataElements.isEmpty()) {
            for (ProgramStageDataElement dataElementWrapper : dataElements) {

                DataElement dataElement = dataElementWrapper.getDataElement();

                String name = dataElement.getFormName() != null ?
                        dataElement.getFormName() : dataElement.getDisplayName();

                boolean defaultViewSetting = dataElementWrapper.isDisplayInReports();

                map.put(dataElement.getUId(), new Pair<String, Boolean>(name, defaultViewSetting));
            }
        }

        map.put("Event date", new Pair<String, Boolean>("Event date", true));
        map.put("Status", new Pair<String, Boolean>("Status", true));

        return map;
    }

    private Map<String, String> mapDataElementToValue(List<TrackedEntityDataValue> dataValues) {

        Map<String, String> dataElementToValueMap = new HashMap<>();

        if (dataValues != null && !dataValues.isEmpty()) {
            for (TrackedEntityDataValue dataValue : dataValues) {

                String value = !isEmpty(dataValue.getValue()) ? dataValue.getValue() : "";
                dataElementToValueMap.put(dataValue.getDataElement(), value);
            }
        }
        return dataElementToValueMap;
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

        if (selectorView != null &&
                (organisationUnitMap == null || organisationUnitMap.isEmpty())) {
            selectorView.showNoOrganisationUnitsError();
        }

        Picker rootPicker = Picker.create(chooseOrganisationUnit);
        for (String unitKey : organisationUnitMap.keySet()) {

            // Creating organisation unit picker items
            OrganisationUnit organisationUnit = organisationUnitMap.get(unitKey);
            Picker organisationUnitPicker = Picker.create(
                    organisationUnit.getUId(), organisationUnit.getDisplayName(),
                    chooseProgram, rootPicker);

            if (organisationUnit.getPrograms() != null && !organisationUnit.getPrograms().isEmpty()) {
                for (Program program : organisationUnit.getPrograms()) {
                    Program assignedProgram = assignedProgramsMap.get(program.getUId());

                    if (assignedProgram != null && ProgramType.WITHOUT_REGISTRATION
                            .equals(assignedProgram.getProgramType())) {
                        Picker programPicker = Picker.create(assignedProgram.getUId(),
                                assignedProgram.getDisplayName(), organisationUnitPicker);
                        organisationUnitPicker.addChild(programPicker);
                    }
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
