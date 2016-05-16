package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.RxOnValueChangedListener;
import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryView;
import org.hisp.dhis.client.sdk.android.event.EventInteractor;
import org.hisp.dhis.client.sdk.android.optionset.OptionSetInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.android.trackedentity.TrackedEntityDataValueInteractor;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.optionset.Option;
import org.hisp.dhis.client.sdk.models.optionset.OptionSet;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCharSequence;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCheckBox;
import org.hisp.dhis.client.sdk.ui.models.FormEntityDate;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText.InputType;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.FormEntityRadioButtons;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


public class DataEntryPresenterImpl implements DataEntryPresenter {
    private static final String TAG = DataEntryPresenterImpl.class.getSimpleName();

    private final ProgramStageInteractor stageInteractor;
    private final ProgramStageSectionInteractor sectionInteractor;
    private final ProgramStageDataElementInteractor dataElementInteractor;
    private final OptionSetInteractor optionSetInteractor;

    private final EventInteractor eventInteractor;
    private final TrackedEntityDataValueInteractor dataValueInteractor;

    private final Logger logger;
    private final RxOnValueChangedListener onValueChangedListener;

    private DataEntryView dataEntryView;
    private CompositeSubscription subscription;


    public DataEntryPresenterImpl(ProgramStageInteractor stageInteractor,
                                  ProgramStageSectionInteractor sectionInteractor,
                                  ProgramStageDataElementInteractor dataElementInteractor,
                                  OptionSetInteractor optionSetInteractor,
                                  EventInteractor eventInteractor,
                                  TrackedEntityDataValueInteractor dataValueInteractor,
                                  Logger logger) {
        this.stageInteractor = stageInteractor;
        this.sectionInteractor = sectionInteractor;
        this.dataElementInteractor = dataElementInteractor;
        this.optionSetInteractor = optionSetInteractor;

        this.eventInteractor = eventInteractor;
        this.dataValueInteractor = dataValueInteractor;

        this.logger = logger;
        this.onValueChangedListener = new RxOnValueChangedListener();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dataEntryView = (DataEntryView) view;
    }

    @Override
    public void detachView() {
        dataEntryView = null;
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void createDataEntryFormStage(String eventId, String programStageId) {
        logger.d(TAG, "ProgramStageId: " + programStageId);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(saveTrackedEntityDataValues());
        subscription.add(Observable.zip(
                eventInteractor.get(eventId),
                stageInteractor.get(programStageId),
                new Func2<Event, ProgramStage, List<FormEntity>>() {
                    @Override
                    public List<FormEntity> call(Event event, ProgramStage stage) {
                        List<ProgramStageDataElement> dataElements = dataElementInteractor
                                .list(stage).toBlocking().first();
                        return transformProgramStageDataElements(event, dataElements);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormEntity>>() {
                    @Override
                    public void call(List<FormEntity> formEntities) {
                        if (dataEntryView != null) {
                            dataEntryView.showDataEntryForm(formEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during form construction", throwable);
                    }
                }));
    }

    @Override
    public void createDataEntryFormSection(String eventId, String programStageSectionId) {
        logger.d(TAG, "ProgramStageSectionId: " + programStageSectionId);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(saveTrackedEntityDataValues());
        subscription.add(Observable.zip(
                eventInteractor.get(eventId),
                sectionInteractor.get(programStageSectionId),
                new Func2<Event, ProgramStageSection, List<FormEntity>>() {
                    @Override
                    public List<FormEntity> call(Event event, ProgramStageSection stageSection) {
                        List<ProgramStageDataElement> dataElements = dataElementInteractor
                                .list(stageSection).toBlocking().first();
                        return transformProgramStageDataElements(event, dataElements);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FormEntity>>() {
                    @Override
                    public void call(List<FormEntity> formEntities) {
                        if (dataEntryView != null) {
                            dataEntryView.showDataEntryForm(formEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Something went wrong during form construction", throwable);
                    }
                }));
    }

    private Subscription saveTrackedEntityDataValues() {
        return Observable.create(onValueChangedListener)
                .debounce(512, TimeUnit.MILLISECONDS)
                .switchMap(new Func1<FormEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(FormEntity formEntity) {
                        return onFormEntityChanged(formEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSaved) {
                        if (isSaved) {
                            logger.d(TAG, "data value is saved successfully");
                        } else {
                            logger.d(TAG, "Failed to save value");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed to save value", throwable);
                    }
                });
    }

    private List<FormEntity> transformProgramStageDataElements(
            Event event, List<ProgramStageDataElement> stageDataElements) {
        if (stageDataElements == null || stageDataElements.isEmpty()) {
            return new ArrayList<>();
        }

        // List<DataElement> dataElements = new ArrayList<>();
        for (ProgramStageDataElement stageDataElement : stageDataElements) {
            DataElement dataElement = stageDataElement.getDataElement();
            if (dataElement == null) {
                throw new RuntimeException("Malformed metadata: Program" +
                        "StageDataElement does not have reference to DataElement");
            }

            OptionSet optionSet = dataElement.getOptionSet();
            if (optionSet != null) {
                List<Option> options = optionSetInteractor.list(
                        dataElement.getOptionSet()).toBlocking().first();
                optionSet.setOptions(options);
            }
        }

        Map<String, TrackedEntityDataValue> dataValueMap = new HashMap<>();
        if (event.getDataValues() != null && !event.getDataValues().isEmpty()) {
            for (TrackedEntityDataValue dataValue : event.getDataValues()) {
                dataValueMap.put(dataValue.getDataElement(), dataValue);
            }
        }

        List<FormEntity> formEntities = new ArrayList<>();
        for (ProgramStageDataElement stageDataElement : stageDataElements) {
            DataElement dataElement = stageDataElement.getDataElement();
            FormEntity formEntity = transformDataElement(
                    event, dataValueMap.get(dataElement.getUId()), stageDataElement);

            if (formEntity != null) {
                formEntities.add(formEntity);
            }
        }

        return formEntities;
    }

    private FormEntity transformDataElement(Event event, TrackedEntityDataValue dataValue,
                                            ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.getDataElement();

        logger.d(TAG, "DataElement: " + dataElement.getDisplayName());
        logger.d(TAG, "ValueType: " + dataElement.getValueType());

        // create TrackedEntityDataValue upfront
        if (dataValue == null) {
            dataValue = new TrackedEntityDataValue();
            dataValue.setEvent(event);
            dataValue.setDataElement(dataElement.getUId());

            // TODO get user name from D2
            dataValue.setStoredBy("android");
        }

        System.out.println("transformDataElement() -> TrackedEntityDataValue: " +
                dataValue + " localId: " + dataValue.getId());

        // in case if we have option set linked to data-element, we
        // need to process it regardless of data-element value type
        if (dataElement.getOptionSet() != null) {
            List<Option> options = dataElement.getOptionSet().getOptions();

            Picker picker = Picker.create(dataElement.getDisplayName());
            if (options != null && !options.isEmpty()) {
                for (Option option : options) {
                    Picker childPicker = Picker.create(
                            option.getCode(), option.getDisplayName(), picker);
                    picker.addChild(childPicker);

                    if (option.getCode().equals(dataValue.getValue())) {
                        picker.setSelectedChild(childPicker);
                    }
                }
            }

            FormEntityFilter formEntityFilter = new FormEntityFilter(dataElement.getUId(),
                    getFormEntityLabel(stageDataElement), dataValue);
            formEntityFilter.setPicker(picker);
            formEntityFilter.setOnFormEntityChangeListener(onValueChangedListener);

            return formEntityFilter;
        }

        switch (dataElement.getValueType()) {
            case TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case LONG_TEXT: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.LONG_TEXT, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case PHONE_NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case EMAIL: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.TEXT, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case NUMBER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.NUMBER, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case INTEGER: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case INTEGER_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_POSITIVE, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case INTEGER_NEGATIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_NEGATIVE, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }
            case INTEGER_ZERO_OR_POSITIVE: {
                FormEntityEditText formEntityEditText = new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), InputType.INTEGER_ZERO_OR_POSITIVE, dataValue);
                formEntityEditText.setOnFormEntityChangeListener(onValueChangedListener);
                formEntityEditText.setValue(dataValue.getValue());
                return formEntityEditText;
            }

            // REVISE WIDGETS
            case BOOLEAN:
                return new FormEntityRadioButtons(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement));
            case TRUE_ONLY:
                return new FormEntityCheckBox(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement));
            case DATE:
                return new FormEntityDate(dataElement.getUId(), getFormEntityLabel(stageDataElement));
            case COORDINATE:
                return null;
            default:
                logger.d(TAG, "Unsupported FormEntity type: " + dataElement.getValueType());
                return null;
        }
    }

    private String getFormEntityLabel(ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.getDataElement();
        String label = isEmpty(dataElement.getDisplayFormName()) ?
                dataElement.getDisplayName() : dataElement.getDisplayFormName();

        if (stageDataElement.isCompulsory()) {
            label = label + " (*)";
        }

        return label;
    }

    private Observable<Boolean> onFormEntityChanged(FormEntity formEntity) {
        return dataValueInteractor.save(mapFormEntityToDataValue(formEntity));
    }

    private TrackedEntityDataValue mapFormEntityToDataValue(FormEntity entity) {
        if (entity instanceof FormEntityFilter) {
            Picker picker = ((FormEntityFilter) entity).getPicker();

            String value = "";
            if (picker != null && picker.getSelectedChild() != null) {
                value = picker.getSelectedChild().getId();
            }

            TrackedEntityDataValue dataValue;
            if (entity.getTag() != null) {
                dataValue = (TrackedEntityDataValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityDataValue must be " +
                        "assigned to FormEntity upfront");
            }

            dataValue.setValue(value);

            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return dataValue;
        } else if (entity instanceof FormEntityCharSequence) {
            String value = ((FormEntityCharSequence) entity).getValue().toString();

            TrackedEntityDataValue dataValue;
            if (entity.getTag() != null) {
                dataValue = (TrackedEntityDataValue) entity.getTag();
            } else {
                throw new IllegalArgumentException("TrackedEntityDataValue must be " +
                        "assigned to FormEntity upfront");
            }

            dataValue.setValue(value);

            logger.d(TAG, "New value " + value + " is emitted for " + entity.getLabel());

            return dataValue;
        }

        return null;
    }
}
