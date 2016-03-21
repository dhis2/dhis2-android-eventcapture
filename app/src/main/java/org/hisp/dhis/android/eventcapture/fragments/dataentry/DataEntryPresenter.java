package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
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
                        return null;
//                        return D2.programStageSections().list(stage).toBlocking().first();
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
                        Event event = Event.create(organisationUnitId, programId, currentProgramStage.getUId(), Event.STATUS_ACTIVE);
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
        List<ProgramStageDataElement> programStageDataElements = programStage.getProgramStageDataElements();
        List<TrackedEntityDataValue> trackedEntityDataValues = new ArrayList<>();

        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {

            TrackedEntityDataValue trackedEntityDataValue = TrackedEntityDataValue.create(
                    event, programStageDataElement.getDataElement().getUId(), EMPTY_FIELD,
                    userAccount.getDisplayName(), false);

            trackedEntityDataValues.add(trackedEntityDataValue);
        }
        event.setTrackedEntityDataValues(trackedEntityDataValues);
    }

    @Override
    public Event getEvent(String eventUId) {
        Event event = D2.events().get(eventUId).toBlocking().first();
        return event;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        if (listProgramStageDataElements != null && !listProgramStageDataElements.isUnsubscribed()) {
            listProgramStageDataElements.unsubscribe();
        }

        if(programStageSubscription != null && !programStageSubscription.isUnsubscribed()) {
            programStageSubscription.unsubscribe();
        }

        listProgramStageDataElements = null;
        programStageSubscription = null;
        dataEntryView = null;
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }


}
