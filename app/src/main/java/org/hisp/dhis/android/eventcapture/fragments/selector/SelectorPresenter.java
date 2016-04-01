package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.datasync.SessionManager;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.utils.ModelUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SelectorPresenter extends AbsPresenter implements ISelectorPresenter {

    private ISelectorView selectorView;
    private CompositeSubscription subscriptions;

    public SelectorPresenter(ISelectorView selectorView) {
        this.selectorView = selectorView;
    }

    @Override
    public void onCreate() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onResume() {
        // stub implementation
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();
    }

    @Override
    public void initializeSynchronization(Boolean force) {
        if (force || !SessionManager.getInstance().isSelectorSynced()) {
            selectorView.onStartLoading();

            subscriptions.add(Observable.zip(
                    D2.me().organisationUnits().sync(), D2.me().programs().sync(),
                    new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                        @Override
                        public List<Program> call(List<OrganisationUnit> organisationUnits,
                                                  List<Program> programs) {
                            return programs;
                        }
                    })
                    .map(new Func1<List<Program>, List<ProgramStage>>() {

                        @Override
                        public List<ProgramStage> call(List<Program> programs) {
                            return loadProgramStages(programs);
                        }
                    })
                    .map(new Func1<List<ProgramStage>, List<ProgramStageSection>>() {

                        @Override
                        public List<ProgramStageSection> call(List<ProgramStage> programStages) {
                            return loadProgramStageSections(programStages);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStageSection>>() {
                        @Override
                        public void call(List<ProgramStageSection> stageSections) {
                            for (ProgramStageSection stageSection : stageSections) {
                                System.out.println(stageSection.getDisplayName());
                            }
                            /*
                            System.out.println(stageSections);
                            for (ProgramStageSection programStageSection : stageSections) {
                                System.out.println("programStageSections: " + programStageSection.getDisplayName());
                                for (ProgramStageDataElement programStageDataElement : programStageSection.getProgramStageDataElements()) {
                                    System.out.println("programStageDataElement: " + programStageDataElement.getUId());
                                    System.out.println("programStageDataElement.getDataElement: " + programStageDataElement.getDataElement().getDisplayName());
                                }
                            }*/
                            SessionManager.getInstance().setSelectorSynced(true);

                            selectorView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {

                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }));

        } else {
            selectorView.onFinishLoading();
        }
    }

    private static List<ProgramStage> loadProgramStages(List<Program> programs) {
        Set<String> stageUids = new HashSet<>();
        for (Program program : programs) {
            Set<String> programStageUids = ModelUtils.toUidSet(
                    program.getProgramStages());
            stageUids.addAll(programStageUids);
        }

        return D2.programStages().sync(stageUids.toArray(
                new String[stageUids.size()])).toBlocking().first();
    }

    private static List<ProgramStageSection> loadProgramStageSections(List<ProgramStage> stages) {
        Set<String> sectionUids = new HashSet<>();
        for (ProgramStage programStage : stages) {
            Set<String> stageSectionUids = ModelUtils.toUidSet(
                    programStage.getProgramStageSections());
            sectionUids.addAll(stageSectionUids);
        }

        return D2.programStageSections().sync(sectionUids.toArray(
                new String[sectionUids.size()])).toBlocking().first();
    }
}
