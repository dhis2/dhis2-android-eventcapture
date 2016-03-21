package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.datasync.SessionManager;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
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

        D2.me().programs().list()
                .subscribe(new Action1<List<Program>>() {

                    @Override
                    public void call(List<Program> programs) {
                        for (Program program : programs) {
                            System.out.println("OrgUnits: " + program.getOrganisationUnits());
                        }
                    }
                });
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
        if (!SessionManager.getInstance().isSelectorSynced()) {
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
                            Set<String> stageUids = new HashSet<>();
                            for (Program program : programs) {
                                Set<String> programStageUids = ModelUtils.toUidSet(
                                        program.getProgramStages());
                                stageUids.addAll(programStageUids);

                                System.out.println("ProgramName: " + program.getDisplayName()
                                        + " stages: " + programStageUids);
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ProgramStage>>() {
                        @Override
                        public void call(List<ProgramStage> programStages) {
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
}
