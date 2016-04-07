package org.hisp.dhis.android.eventcapture.fragments.dataentry;


import android.support.v4.util.Pair;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.IDataEntity;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventDataEntryPresenter implements IEventDataEntryPresenter {
    private IEventDataEntryView eventDataEntryView;
    private Subscription programDataEntryRowSubscription;
    private Subscription saveDataEntityValues;

    public EventDataEntryPresenter(IEventDataEntryView eventDataEntryView) {
        this.eventDataEntryView = eventDataEntryView;
    }

//    public void onDestroy() {
//        if (programDataEntryRowSubscription != null && !programDataEntryRowSubscription.isUnsubscribed()) {
//            programDataEntryRowSubscription.unsubscribe();
//        }
//
//        if(saveDataEntityValues != null && !saveDataEntityValues.isUnsubscribed()) {
//            saveDataEntityValues.unsubscribe();
//        }
//
//        programDataEntryRowSubscription = null;
//        saveDataEntityValues = null;
//        eventDataEntryView = null;
//    }

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
                })
                .map(new Func1<List<ProgramStageDataElement>, List<IDataEntity>>() {
                    @Override
                    public List<IDataEntity> call(List<ProgramStageDataElement> programStageDataElements) {
                        return transformDataEntryForm(programStageDataElements);
                    }
                })
                .subscribe(new Action1<List<IDataEntity>>() {
                    @Override
                    public void call(List<IDataEntity> dataEntities) {
                        if (eventDataEntryView != null) {
                            eventDataEntryView.setDataEntryFields(dataEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void listDataEntryFieldsWithEventValues(final String eventUId, final String programStageSectionUId) {

//        Observable<List<ProgramStageDataElement>> programStageDataElements = D2.programStageSections().get(programStageSectionUId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Func1<ProgramStageSection, List<ProgramStageDataElement>>() {
//                    @Override
//                    public List<ProgramStageDataElement> call(ProgramStageSection programStageSection) {
//                        return D2.programStageDataElements().list(programStageSection).toBlocking().first();
//                    }
//                });
//
//        Observable<HashMap<String, TrackedEntityDataValue>> programStageDataElementHash = programStageDataElements.zipWith(D2.events().get(eventUId),
//                new Func2<List<ProgramStageDataElement>, Event, HashMap<String, TrackedEntityDataValue>>() {
//                    @Override
//                    public HashMap<String, TrackedEntityDataValue> call(List<ProgramStageDataElement> programStageDataElements, Event event) {
//                        HashMap<String, TrackedEntityDataValue> map = new HashMap<>();
//                        List<TrackedEntityDataValue> trackedEntityDataValues = event.getTrackedEntityDataValues();
//                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
//                            map.put(programStageDataElement.getUId(), null);
//                        }
//                        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
//                            if (map.containsKey(trackedEntityDataValue.getDataElement())) {
//                                map.put(trackedEntityDataValue.getDataElement(), trackedEntityDataValue);
//                            }
//                        }
//
//                        return map;
//                    }
//                });
//        Observable programStageSections = D2.programStageSections().get(programStageSectionUId);
//
//        Observable<List<IDataEntity>> dataEntryForm = Observable.combineLatest(programStageDataElementHash, programStageSections, new Func2<ProgramStageSection,
//                HashMap<String, TrackedEntityDataValue>, List<IDataEntity>>() {
//            @Override
//            public List<IDataEntity> call(ProgramStageSection section, HashMap<String, TrackedEntityDataValue> valueHashMap) {
//                return transformDataEntryFormWithValues(valueHashMap, section, D2.events().get(eventUId).toBlocking().first());
//            }
//        });
//
//
//        programDataEntryRowSubscription = dataEntryForm.subscribe(new Action1<List<IDataEntity>>() {
//            @Override
//            public void call(List<IDataEntity> dataEntities) {
//                if (eventDataEntryView != null) {
//                    eventDataEntryView.setDataEntryFields(dataEntities);
//                }
//            }
//        }, new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//                Timber.d(throwable.toString());
//            }
//        });
    }

    private List<IDataEntity> transformDataEntryForm(List<ProgramStageDataElement> programStageDataElements) {
        List<IDataEntity> dataEntities = new ArrayList<>();

        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();
        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);
//            TrackedEntityDataValue trackedEntityDataValue = valueHashMap.get(programStageDataElement.getUId());
//            dataEntityValueChangedListener.setTrackedEntityDataValue(trackedEntityDataValue);

            if(programStageDataElement.getDataElement().getOptionSet() != null) {
                dataEntities.add(null);
            }
            if(programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.BOOLEAN, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.COORDINATES, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.DATE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.FILE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.INTEGER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.NUMBER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        "",//trackedEntityDataValue.getValue(),
                        DataEntity.Type.TEXT, dataEntityValueChangedListener));
            }
        }

        return dataEntities;
    }

    private List<IDataEntity> transformDataEntryFormWithValues(
            HashMap<String, TrackedEntityDataValue> valueHashMap, ProgramStageSection section, Event event) {

        List<IDataEntity> dataEntities = new ArrayList<>();
        List<ProgramStageDataElement> programStageDataElements = section.getProgramStageDataElements();
        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();
        dataEntityValueChangedListener.setEvent(event);

        if(section.getProgramStage().getReportDateDescription() != null) {
            dataEntities.add(DataEntity.create(
                    section.getProgramStage().getReportDateDescription(),
                    event.getEventDate().toString(), DataEntity.Type.DATE, dataEntityValueChangedListener));
        }
        if(section.getProgramStage().isCaptureCoordinates()) {
            //coordinate row
            // TODO create onvaluechangedlistener with getting coordinates



            // dataEntities.add(DataEntityCoordinate.create("Capture Coordinates", event.getCoordinate(), DataEntity.Type.COORDINATES));
        }

        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);
            TrackedEntityDataValue trackedEntityDataValue = valueHashMap.get(programStageDataElement.getUId());
            dataEntityValueChangedListener.setTrackedEntityDataValue(trackedEntityDataValue);

            if(programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.BOOLEAN, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.COORDINATES, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.DATE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.FILE, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.INTEGER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.NUMBER, dataEntityValueChangedListener));
            }
            else if(programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntity.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntity.Type.TEXT, dataEntityValueChangedListener));
            }
        }

        return dataEntities;
    }

    private class RxDataEntityValueChangedListener implements OnValueChangeListener<Pair<CharSequence, CharSequence>> {
        private ProgramStageDataElement programStageDataElement;
        private TrackedEntityDataValue trackedEntityDataValue;
        private Event event;
        private Observable saveValueObservable;


        @Override
        public void onValueChanged(Pair<CharSequence, CharSequence> keyValuePair) {

//            if(programStageDataElement.getProgramStage().getReportDateDescription().equals(keyValuePair.first)) {
//                // update report date in Event and save
//                event.setEventDate(new DateTime(keyValuePair.second.toString()));
//                saveValueObservable = D2.events().save(event);
//            }
//            else if("Capture Coordinates".equals(keyValuePair.first)) {
//                // update coordinates in Event and save
//                // needs special onvaluechangedlistener
//
//            }
//            else if(programStageDataElement.getDataElement().getDisplayName().equals(keyValuePair.first)) {
//                // save trackedEntityDataValue
//                trackedEntityDataValue.setValue(keyValuePair.second.toString());
//                saveValueObservable = D2.trackedEntityDataValues().save(trackedEntityDataValue);
//            }
//
//
//            saveDataEntityValues = D2.trackedEntityDataValues().save(trackedEntityDataValue)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .debounce(250, TimeUnit.MILLISECONDS)
//                    .map(new Func1<Boolean, List<ProgramRule>>() {
//                        @Override
//                        public List<ProgramRule> call(Boolean aBoolean) {
//                            return D2.programRules().list(programStageDataElement.getProgramStage()).toBlocking().first();
//                        }
//                    }).subscribe(new Action1<List<ProgramRule>>() {
//                        @Override
//                        public void call(List<ProgramRule> programRules) {
//
//                        }
//                    });

                // trigger update of program rules
        }

        public void setProgramStageDataElement(ProgramStageDataElement programStageDataElement) {
            this.programStageDataElement = programStageDataElement;
        }

        public void setTrackedEntityDataValue(TrackedEntityDataValue trackedEntityDataValue) {
            this.trackedEntityDataValue = trackedEntityDataValue;
        }

        public void setEvent(Event event) {
            this.event = event;
        }
    }
}
