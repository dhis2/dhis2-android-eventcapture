package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.datasync.SessionManager;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramRule;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleAction;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.utils.ModelUtils;

import java.util.ArrayList;
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
                    .map(new Func1<List<Program>, List<ProgramStageDataElement>>() {

                        @Override
                        public List<ProgramStageDataElement> call(List<Program> programs) {
                            List<ProgramStage> programStages =
                                    loadProgramStages(programs);
                            List<ProgramStageSection> stageSections =
                                    loadProgramStageSections(programStages);
                            List<ProgramRule> programRules = loadProgramRules(programs);
                            List<ProgramRuleAction> programRuleActions = loadProgramRuleActions(programRules);
                            for(ProgramRuleAction programRuleAction : programRuleActions) {
                                System.out.println("ProgramRuleAction: " +
                                programRuleAction.getUId());
                            }

                            return loadProgramStageDataElements(programStages, stageSections);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStageDataElement>>() {

                        @Override
                        public void call(List<ProgramStageDataElement> stageDataElements) {

                            for (ProgramStageDataElement stageDataElement : stageDataElements) {
                                System.out.println("ProgramStageDataElement: " +
                                        stageDataElement.getDataElement().getUId());
                            }

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

    private static List<ProgramStageDataElement> loadProgramStageDataElements(
            List<ProgramStage> programStages, List<ProgramStageSection> stageSections) {

        if (programStages == null || programStages.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> stageDataElementUids = new HashSet<>();
        for (ProgramStage stage : programStages) {
            stageDataElementUids.addAll(ModelUtils.toUidSet(
                    stage.getProgramStageDataElements()));
        }

        for (ProgramStageSection programStageSection : stageSections) {
            stageDataElementUids.addAll(ModelUtils.toUidSet(
                    programStageSection.getProgramStageDataElements()));
        }

        return D2.programStageDataElements().sync(stageDataElementUids
                .toArray(new String[stageDataElementUids.size()])).toBlocking().first();
    }

    private static List<ProgramRule> loadProgramRules(List<Program> programs) {

        return D2.programRules().sync(SyncStrategy.DEFAULT, programs).toBlocking().first();
    }
    private static List<ProgramRuleAction> loadProgramRuleActions(List<ProgramRule> programRules) {
        Set<String> programRuleActionUids = new HashSet<>();

        for(ProgramRule programRule : programRules) {
            programRuleActionUids.addAll(ModelUtils.toUidSet(
                    programRule.getProgramRuleActions()));
        }
        return D2.programRuleActions().sync(SyncStrategy.DEFAULT, programRuleActionUids)
                .toBlocking().first();
    }
}
