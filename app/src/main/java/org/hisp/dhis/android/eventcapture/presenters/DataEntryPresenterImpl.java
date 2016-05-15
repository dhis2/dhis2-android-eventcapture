package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryView;
import org.hisp.dhis.client.sdk.android.optionset.OptionSetInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.optionset.Option;
import org.hisp.dhis.client.sdk.models.optionset.OptionSet;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityCheckBox;
import org.hisp.dhis.client.sdk.ui.models.FormEntityDate;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText;
import org.hisp.dhis.client.sdk.ui.models.FormEntityFilter;
import org.hisp.dhis.client.sdk.ui.models.FormEntityRadioButtons;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


// TODO Improve performance by syncing only programs based on type (WITH OR WITHOUT REGISTRATION)
public class DataEntryPresenterImpl implements DataEntryPresenter {
    private static final String TAG = DataEntryPresenterImpl.class.getSimpleName();

    private final ProgramStageInteractor stageInteractor;
    private final ProgramStageSectionInteractor sectionInteractor;
    private final ProgramStageDataElementInteractor dataElementInteractor;
    private final OptionSetInteractor optionSetInteractor;
    private final Logger logger;

    private DataEntryView dataEntryView;
    private CompositeSubscription subscription;

    public DataEntryPresenterImpl(ProgramStageInteractor stageInteractor,
                                  ProgramStageSectionInteractor sectionInteractor,
                                  ProgramStageDataElementInteractor dataElementInteractor,
                                  OptionSetInteractor optionSetInteractor, Logger logger) {
        this.stageInteractor = stageInteractor;
        this.sectionInteractor = sectionInteractor;
        this.dataElementInteractor = dataElementInteractor;
        this.optionSetInteractor = optionSetInteractor;
        this.logger = logger;
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
        subscription.add(stageInteractor.get(programStageId)
                .switchMap(new Func1<ProgramStage, Observable<List<ProgramStageDataElement>>>() {
                    @Override
                    public Observable<List<ProgramStageDataElement>> call(ProgramStage stage) {
                        return dataElementInteractor.list(stage);
                    }
                })
                .map(new Func1<List<ProgramStageDataElement>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<ProgramStageDataElement> stageDataElements) {
                        return transformProgramStageDataElements(stageDataElements);
                    }
                })
                .map(new Func1<List<ProgramStageDataElement>, List<FormEntity>>() {

                    @Override
                    public List<FormEntity> call(List<ProgramStageDataElement> dataElements) {
                        return transformDataElementsToFormEntities(dataElements);
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

    /*
    * 1) User first gets into FormSectionActivity:
    *    - We get organisation unit, program ids in as parameters (and, event optionally)
    *         If we don't have event, we should create one. DataEntry views should not
    *         know anything about event creation.
    */

    @Override
    public void createDataEntryFormSection(String eventId, String programStageSectionId) {
        logger.d(TAG, "ProgramStageSectionId: " + programStageSectionId);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }

        subscription = new CompositeSubscription();
        subscription.add(sectionInteractor.get(programStageSectionId)
                .switchMap(new Func1<ProgramStageSection, Observable<List<ProgramStageDataElement>>>() {
                    @Override
                    public Observable<List<ProgramStageDataElement>> call(ProgramStageSection stage) {
                        return dataElementInteractor.list(stage);
                    }
                })
                .map(new Func1<List<ProgramStageDataElement>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<ProgramStageDataElement> elements) {
                        return transformProgramStageDataElements(elements);
                    }
                })
                .map(new Func1<List<ProgramStageDataElement>, List<FormEntity>>() {
                    @Override
                    public List<FormEntity> call(List<ProgramStageDataElement> dataElements) {
                        return transformDataElementsToFormEntities(dataElements);
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

    private List<ProgramStageDataElement> transformProgramStageDataElements(
            List<ProgramStageDataElement> stageDataElements) {
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

        return stageDataElements;
    }

    private List<FormEntity> transformDataElementsToFormEntities(
            List<ProgramStageDataElement> dataElements) {
        List<FormEntity> formEntities = new ArrayList<>();

        for (ProgramStageDataElement dataElement : dataElements) {
            FormEntity formEntity = transformDataElement(dataElement);

            if (formEntity != null) {
                formEntities.add(formEntity);
            }
        }

        return formEntities;
    }

    private FormEntity transformDataElement(ProgramStageDataElement stageDataElement) {
        DataElement dataElement = stageDataElement.getDataElement();

        logger.d(TAG, "DataElement: " + dataElement.getDisplayName());
        logger.d(TAG, "ValueType: " + dataElement.getValueType());

        // in case if we have option set linked to data-element, we
        // need to process it regardless of data-element value type
        if (dataElement.getOptionSet() != null) {
            List<Option> options = dataElement.getOptionSet().getOptions();

            Picker picker = Picker.create(dataElement.getDisplayName());
            if (options != null && !options.isEmpty()) {
                for (Option option : options) {
                    picker.addChild(Picker.create(
                            option.getCode(), option.getDisplayName(), picker));
                }
            }

            FormEntityFilter formEntityFilter = new FormEntityFilter(
                    dataElement.getUId(), getFormEntityLabel(stageDataElement));
            formEntityFilter.setPicker(picker);

            return formEntityFilter;
        }

        switch (dataElement.getValueType()) {
            // GO THROUGH WIDGETS
            case TEXT:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT);
            case LONG_TEXT:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.LONG_TEXT);
            case PHONE_NUMBER:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT);
            case EMAIL:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.TEXT);
            case NUMBER:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.NUMBER);
            case INTEGER:
                return new FormEntityEditText(dataElement.getUId(),
                        getFormEntityLabel(stageDataElement), FormEntityEditText.InputType.INTEGER);
            case INTEGER_POSITIVE:
                return new FormEntityEditText(dataElement.getUId(), getFormEntityLabel(stageDataElement),
                        FormEntityEditText.InputType.INTEGER_POSITIVE);
            case INTEGER_NEGATIVE:
                return new FormEntityEditText(dataElement.getUId(), getFormEntityLabel(stageDataElement),
                        FormEntityEditText.InputType.INTEGER_NEGATIVE);
            case INTEGER_ZERO_OR_POSITIVE:
                return new FormEntityEditText(dataElement.getUId(), getFormEntityLabel(stageDataElement),
                        FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE);

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
}
