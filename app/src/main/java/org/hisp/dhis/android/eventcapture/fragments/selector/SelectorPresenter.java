package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.datasync.SessionManager;
import org.hisp.dhis.android.eventcapture.datasync.SyncManager;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.utils.ModelUtils.ModelAction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.models.utils.ModelUtils.toUidSet;

public class SelectorPresenter implements ISelectorPresenter {
    private ISelectorView selectorView;
    private CompositeSubscription subscriptions;

    public SelectorPresenter(ISelectorView selectorView) {
        this.selectorView = selectorView;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void initializeSynchronization(Boolean force) {
        if (force || !SessionManager.getInstance().isSelectorSynced()) {
            selectorView.onStartLoading();

            subscriptions.add(Observable.zip(
                    D2.me().organisationUnits().pull(), D2.me().programs().pull(),
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
                            List<ProgramStage> programStages
                                    = loadProgramStages(programs);
                            List<ProgramStageSection> stageSections
                                    = loadProgramStageSections(programStages);
                            return loadProgramStageDataElements(programStages, stageSections);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStageDataElement>>() {
                        @Override
                        public void call(List<ProgramStageDataElement> stageDataElements) {
                            SessionManager.getInstance().setSelectorSynced(true);
                            SyncManager.getInstance().setLastSyncedNow();

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
        Set<String> stageUids = toUidSet(programs, new ModelAction<Program>() {
            @Override
            public Collection<String> getUids(Program program) {
                return toUidSet(program.getProgramStages());
            }
        });

        return D2.programStages().pull(stageUids).toBlocking().first();
    }

    private static List<ProgramStageSection> loadProgramStageSections(List<ProgramStage> stages) {
        Set<String> sectionUids = toUidSet(stages, new ModelAction<ProgramStage>() {
            @Override
            public Collection<String> getUids(ProgramStage programStage) {
                return toUidSet(programStage.getProgramStageSections());
            }
        });

        return D2.programStageSections().pull(sectionUids).toBlocking().first();
    }

    private static List<ProgramStageDataElement> loadProgramStageDataElements(
            List<ProgramStage> stages, List<ProgramStageSection> sections) {

        Set<String> stageElementUids = toUidSet(stages, new ModelAction<ProgramStage>() {
            @Override
            public Collection<String> getUids(ProgramStage model) {
                return toUidSet(model.getProgramStageDataElements());
            }
        });

        Set<String> sectionElementUids = toUidSet(sections, new ModelAction<ProgramStageSection>() {
            @Override
            public Collection<String> getUids(ProgramStageSection model) {
                return toUidSet(model.getProgramStageDataElements());
            }
        });

        stageElementUids.addAll(sectionElementUids);
        return D2.programStageDataElements().pull(sectionElementUids).toBlocking().first();
    }
}
