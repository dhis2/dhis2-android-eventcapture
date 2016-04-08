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

import org.hisp.dhis.android.eventcapture.views.fragments.IDataEntryView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.models.user.UserAccount;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DataEntryPresenter implements IDataEntryPresenter {
    private IDataEntryView dataEntryView;
    private Subscription listProgramStageDataElements;
    private Subscription programStageSubscription;

    public DataEntryPresenter(IDataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;
    }

    @Override
    public void listProgramStageSections(String programId) {
        listProgramStageDataElements = D2.programs().get(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Program, List<ProgramStage>>() {
                    @Override
                    public List<ProgramStage> call(Program program) {
                        return D2.programStages().list(program).toBlocking().first();
                    }
                }).map(new Func1<List<ProgramStage>, List<ProgramStageSection>>() {
                    @Override
                    public List<ProgramStageSection> call(List<ProgramStage> programStages) {
                        ProgramStage stage = programStages.get(0);
                        return D2.programStageSections().list(stage).toBlocking().first();
                    }
                }).subscribe(new Action1<List<ProgramStageSection>>() {
                    @Override
                    public void call(List<ProgramStageSection> programStageSections) {
                        if (dataEntryView != null) {
                            dataEntryView.initializeViewPager(programStageSections);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });

    }

    @Override
    public void createNewEvent(final String organisationUnitId, final String programId) {
        programStageSubscription = D2.programs().get(programId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Program, List<ProgramStage>>() {
                    @Override
                    public List<ProgramStage> call(Program program) {
                        return D2.programStages().list(program).toBlocking().first();
                    }
                }).zipWith(D2.me().account(), new Func2<List<ProgramStage>, UserAccount, Event>() {
                    @Override
                    public Event call(List<ProgramStage> programStages, UserAccount userAccount) {
                        ProgramStage currentProgramStage = programStages.get(0); //only one stage in event capture
//                        Event event = D2.events().create(
//                                organisationUnitId, programId,

//                                currentProgramStage.getUId(), Event.EventStatus.ACTIVE).toBlocking().first();
//                        setEmptyTrackedEntityDataValues(event, currentProgramStage, userAccount);
//                        return event;
                        //TODO fix this stuff

//                                currentProgramStage.getUId(), Event.STATUS_ACTIVE).toBlocking().first();
                        Event event = new Event();
                        setEmptyTrackedEntityDataValues(event, currentProgramStage, userAccount);
                        return event;

                    }
                }).subscribe(new Action1<Event>() {
                    @Override
                    public void call(Event event) {
                        if (dataEntryView != null) {
                            dataEntryView.setEvent(event);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });
    }

    private void setEmptyTrackedEntityDataValues(Event event, ProgramStage programStage, UserAccount userAccount) {
        String EMPTY_FIELD = "";
        List<ProgramStageDataElement> programStageDataElements =
                D2.programStageDataElements().list(programStage).toBlocking().first();
        List<TrackedEntityDataValue> trackedEntityDataValues = new ArrayList<>();

        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {

//            TrackedEntityDataValue trackedEntityDataValue = TrackedEntityDataValue.create(
//                    event, programStageDataElement.getDataElement().getUId(), EMPTY_FIELD,
//                    userAccount.getDisplayName(), false);


//            trackedEntityDataValues.add(trackedEntityDataValue);
        }
        event.setDataValues(trackedEntityDataValues);
        //TODO fix this stuff
            TrackedEntityDataValue trackedEntityDataValue = new TrackedEntityDataValue();
            trackedEntityDataValues.add(trackedEntityDataValue);

        // event.setTrackedEntityDataValues(trackedEntityDataValues);

    }





    @Override
    public Event getEvent(String eventUId) {
        Event event = D2.events().get(eventUId).toBlocking().first();
        return event;
    }

//    @Override
//    public void onDestroy() {
//        if (listProgramStageDataElements != null && !listProgramStageDataElements.isUnsubscribed()) {
//            listProgramStageDataElements.unsubscribe();
//        }
//
//        if (programStageSubscription != null && !programStageSubscription.isUnsubscribed()) {
//            programStageSubscription.unsubscribe();
//        }
//
//        listProgramStageDataElements = null;
//        programStageSubscription = null;
//        dataEntryView = null;
//    }
//
//    @Override
//    public String getKey() {
//        return this.getClass().getSimpleName();
//    }
}
