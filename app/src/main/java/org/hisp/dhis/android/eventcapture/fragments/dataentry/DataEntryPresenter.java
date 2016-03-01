package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DataEntryPresenter implements IDataEntryPresenter {
    private IDataEntryView dataEntryView;
    private Subscription listProgramStageDataElements;
    private Subscription getProgramPojoSubscription;

    public DataEntryPresenter(IDataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;
    }

    @Override
    public void listDataEntryFields(String programId, final int sectionNumber) {
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
                        return D2.programStageSections().list(programStages.get(0)).toBlocking().first(); //only one stage in EventCapture
                    }
                }).map(new Func1<List<ProgramStageSection>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<ProgramStageSection> programStageSections) {
                        return D2.programStageDataElements().list(programStageSections.get(sectionNumber)).toBlocking().first();
                    }
                }).subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> programStageDataElements) {
                        if (dataEntryView != null)
                            dataEntryView.setDataEntryFields(transformDataEntryForm(programStageDataElements));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.d(throwable.toString());
                    }
                });

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        if (listProgramStageDataElements != null && !listProgramStageDataElements.isUnsubscribed()) {
            listProgramStageDataElements.unsubscribe();
        }

        listProgramStageDataElements = null;
        dataEntryView = null;
    }

    @Override
    public String getKey() {
        return null;
    }

    private List<DataEntity> transformDataEntryForm(List<ProgramStageDataElement> programStageDataElements) {
        List<DataEntity> dataEntities = new ArrayList<>();
        if(programStageDataElements.get(0).getProgramStage().getProgram().isDisplayIncidentDate()) {

        }
        if(programStageDataElements.get(0).getProgramStage().isCaptureCoordinates()) {
            //coordinate row
        }

        for(ProgramStageDataElement programStageDataElement : programStageDataElements) {

            if(programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.BOOLEAN));
            }
            else if(programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.COORDINATES));
            }
            else if(programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.DATE));
            }
            else if(programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.FILE));
            }
            else if(programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.INTEGER));
            }
            else if(programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.NUMBER));
            }
            else if(programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntity.create(programStageDataElement.getDataElement().getDisplayName(), DataEntity.Type.TEXT));
            }

        }

        return dataEntities;
    }
}
