package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryView;
import org.hisp.dhis.client.sdk.android.program.ProgramStageDataElementInteractor;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityDate;
import org.hisp.dhis.client.sdk.ui.models.FormEntityEditText;
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

public class DataEntryPresenterImpl implements DataEntryPresenter {
    private static final String TAG = DataEntryPresenterImpl.class.getSimpleName();

    private final ProgramStageSectionInteractor sectionInteractor;
    private final ProgramStageDataElementInteractor dataElementInteractor;
    private final Logger logger;

    private DataEntryView dataEntryView;
    private CompositeSubscription subscription;

    public DataEntryPresenterImpl(ProgramStageSectionInteractor sectionInteractor,
                                  ProgramStageDataElementInteractor dataElementInteractor,
                                  Logger logger) {
        this.sectionInteractor = sectionInteractor;
        this.dataElementInteractor = dataElementInteractor;
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
    public void createDataEntryForm(String programStageSectionId) {
        logger.d(TAG, "ProgramStageSectionId: " + programStageSectionId);

        subscription = new CompositeSubscription();
        subscription.add(sectionInteractor.get(programStageSectionId)
                .switchMap(new Func1<ProgramStageSection, Observable<List<ProgramStageDataElement>>>() {
                    @Override
                    public Observable<List<ProgramStageDataElement>> call(ProgramStageSection stage) {
                        return dataElementInteractor.list(stage);
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

    // TODO consider creating new scope for data-entry screen (otherwise, we can run into weird-ass bugs)
    private List<FormEntity> transformDataElementsToFormEntities(
            List<ProgramStageDataElement> stageDataElements) {
        List<FormEntity> formEntities = new ArrayList<>();

        if (stageDataElements != null && !stageDataElements.isEmpty()) {
            for (int i = 0; i < 3; i++)
            for (ProgramStageDataElement stageDataElement : stageDataElements) {
                if (stageDataElement.getDataElement() == null) {
                    throw new RuntimeException("Malformed meta-data: program stage data element" +
                            " does not have reference to data element");
                }

                DataElement dataElement = stageDataElement.getDataElement();
                logger.d(TAG, "ValueType: " + dataElement.getValueType());

                switch (dataElement.getValueType()) {
                    case TEXT: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.TEXT));
                        break;
                    }
                    case LONG_TEXT: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.LONG_TEXT));
                        break;
                    }
                    case PHONE_NUMBER: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.TEXT));
                        break;
                    }
                    case EMAIL: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.TEXT));
                        break;
                    }
                    case BOOLEAN: {
                        break;
                    }
                    case TRUE_ONLY: {
                        break;
                    }
                    case DATE: {
                        formEntities.add(new FormEntityDate(
                                dataElement.getUId(), getFormEntityLabel(dataElement)));
                        break;
                    }
                    case NUMBER: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.NUMBER));
                        break;
                    }
                    case INTEGER: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.INTEGER));
                        break;
                    }
                    case INTEGER_POSITIVE: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.INTEGER_POSITIVE));
                        break;
                    }
                    case INTEGER_NEGATIVE: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.INTEGER_NEGATIVE));
                        break;
                    }
                    case INTEGER_ZERO_OR_POSITIVE: {
                        formEntities.add(new FormEntityEditText(
                                dataElement.getUId(), getFormEntityLabel(dataElement),
                                FormEntityEditText.InputType.INTEGER_ZERO_OR_POSITIVE));
                        break;
                    }
                    case COORDINATE: {
                        break;
                    }
                    default:
                        logger.d(TAG, "Unsupported FormEntity type: " + dataElement.getValueType());
                }
            }
        }

        return formEntities;
    }

    private String getFormEntityLabel(DataElement dataElement) {
        return isEmpty(dataElement.getDisplayFormName()) ?
                dataElement.getDisplayName() : dataElement.getDisplayFormName();
    }
}
