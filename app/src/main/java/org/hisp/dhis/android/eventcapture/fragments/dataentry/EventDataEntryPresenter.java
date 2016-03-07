package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class EventDataEntryPresenter extends AbsPresenter
        implements IEventDataEntryPresenter {
    private IEventDataEntryView eventDataEntryView;
    private Subscription programDataEntryRowSubscription;
    private Subscription saveDataEntityValues;

    public EventDataEntryPresenter(IEventDataEntryView eventDataEntryView) {
        this.eventDataEntryView = eventDataEntryView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (programDataEntryRowSubscription != null && !programDataEntryRowSubscription.isUnsubscribed()) {
            programDataEntryRowSubscription.unsubscribe();
        }

        if(saveDataEntityValues != null && !saveDataEntityValues.isUnsubscribed()) {
            saveDataEntityValues.unsubscribe();
        }

        programDataEntryRowSubscription = null;
        saveDataEntityValues = null;
        eventDataEntryView = null;
    }

    @Override
    public void listDataEntryFields(String programStageSectionUid) {
        programDataEntryRowSubscription = D2.programStageSections().get(programStageSectionUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ProgramStageSection, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(ProgramStageSection programStageSection) {
                        return D2.programStageDataElements().list(programStageSection).toBlocking().first();
                    }
                }).map(new Func1<List<ProgramStageDataElement>, List<DataEntity>>() {
                    @Override
                    public List<DataEntity> call(List<ProgramStageDataElement> programStageDataElements) {
                        return transformDataEntryForm(programStageDataElements);
                    }
                }).subscribe(new Action1<List<DataEntity>>() {
                    @Override
                    public void call(List<DataEntity> dataEntities) {
                        if(eventDataEntryView != null) {
                            eventDataEntryView.setDataEntryFields(dataEntities);
                        }
                    }
                });
    }

    @Override
    public void listDataEntryFieldsWithEventValues(String eventUId, final String programStageSectionUId) {
//        programDataEntryRowSubscription
        Observable<List<ProgramStageDataElement>> programStageDataElements = D2.programStageSections().get(programStageSectionUId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ProgramStageSection, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(ProgramStageSection programStageSection) {
                        return D2.programStageDataElements().list(programStageSection).toBlocking().first();
                    }
                });

        Observable<HashMap<String, TrackedEntityDataValue>> programStageDataElementHash = programStageDataElements.zipWith(D2.events().get(eventUId),
                new Func2<List<ProgramStageDataElement>, Event, HashMap<String, TrackedEntityDataValue>>() {
                    @Override
                    public HashMap<String, TrackedEntityDataValue> call(List<ProgramStageDataElement> programStageDataElements, Event event) {
                        HashMap<String, TrackedEntityDataValue> map = new HashMap<>();
                        List<TrackedEntityDataValue> trackedEntityDataValues = event.getTrackedEntityDataValues();
                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                            map.put(programStageDataElement.getUId(), null);
                        }
                        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
                            if (map.containsKey(trackedEntityDataValue.getDataElement())) {
                                map.put(trackedEntityDataValue.getDataElement(), trackedEntityDataValue);
                            }
                        }

                        return map;
                    }
                });
        Observable programStageSections = D2.programStageSections().get(programStageSectionUId);

        Observable<List<DataEntity>> dataEntryForm = Observable.combineLatest(programStageDataElementHash, programStageSections, new Func2<ProgramStageSection,
                HashMap<String, TrackedEntityDataValue>, List<DataEntity>>() {
            @Override
            public List<DataEntity> call(ProgramStageSection section, HashMap<String, TrackedEntityDataValue> valueHashMap) {
                return transformDataEntryFormWithValues(valueHashMap, section);
            }
        });

        programDataEntryRowSubscription = dataEntryForm.subscribe(new Action1<List<DataEntity>>() {
            @Override
            public void call(List<DataEntity> dataEntities) {
                if (eventDataEntryView != null) {
                    eventDataEntryView.setDataEntryFields(dataEntities);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Timber.d(throwable.toString());
            }
        });
    }

    private List<DataEntity> transformDataEntryFormWithValues(HashMap<String, TrackedEntityDataValue> valueHashMap, ProgramStageSection section) {
        List<DataEntity> dataEntities = new ArrayList<>();
        List<ProgramStageDataElement> programStageDataElements = section.getProgramStageDataElements();
        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();

        if(section.getProgramStage().getProgram().isDisplayIncidentDate()) {

        }
        if(section.getProgramStage().isCaptureCoordinates()) {
            //coordinate row
        }

        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);
            dataEntityValueChangedListener.setTrackedEntityDataValue(valueHashMap.get(programStageDataElement.getUId()));

            if(programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.BOOLEAN, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.COORDINATES, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.DATE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.FILE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.INTEGER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.NUMBER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.TEXT, dataEntityValueChangedListener));
            }
        }

        return dataEntities;
    }
    private List<DataEntity> transformDataEntryForm(List<ProgramStageDataElement> programStageDataElements) {
        List<DataEntity> dataEntities = new ArrayList<>();
        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();

        if(programStageDataElements.get(0).getProgramStage().getProgram().isDisplayIncidentDate()) {

        }
        if(programStageDataElements.get(0).getProgramStage().isCaptureCoordinates()) {
            //coordinate row
        }

        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);

            if(programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.BOOLEAN, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.COORDINATES, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.DATE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.FILE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.INTEGER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.NUMBER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), "", DataEntity.Type.TEXT, dataEntityValueChangedListener));
            }
        }

        return dataEntities;
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    private class RxDataEntityValueChangedListener implements OnValueChangeListener<CharSequence, CharSequence> {
        private ProgramStageDataElement programStageDataElement;
        private TrackedEntityDataValue trackedEntityDataValue;


        @Override
        public void onValueChanged(CharSequence key, CharSequence value) {
//            if(!key.equals(programStageDataElement.getDataElement().getDisplayName()) ||
//                    !trackedEntityDataValue.getDataElement()
//                            .equals(programStageDataElement.getDataElement().getUId())) {
//                throw new IllegalArgumentException("Wrong key");
//            }
            trackedEntityDataValue.setValue(value.toString());

//            saveDataEntityValues = D2.me().save(trackedEntityDataValue)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io()).subscribe(
//                    new Action1<Void>() {
//                        @Override
//                        public void call(Void aVoid) {
//                            Timber.d("userAccount successfully saved");
//                        }
//                    }
//                    , new Action1<Throwable>() {
//                        @Override
//                        public void call(Throwable throwable) {
//                            Timber.d("userAccount has failed saving");
//                        }
//                    });

        }

        public void setProgramStageDataElement(ProgramStageDataElement programStageDataElement) {
            this.programStageDataElement = programStageDataElement;
        }

        public void setTrackedEntityDataValue(TrackedEntityDataValue trackedEntityDataValue) {
            this.trackedEntityDataValue = trackedEntityDataValue;
        }
    }
}
