package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.program.Program;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SelectorPresenter extends AbsPresenter implements ISelectorPresenter {

    private ISelectorView selectorView;
    private Subscription synchronizationSubscription;
    private Subscription pickedOrganisationUnitSubscription;
    private Subscription pickedProgramSubscription;

    public SelectorPresenter(ISelectorView selectorView) {
        this.selectorView = selectorView;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (synchronizationSubscription != null && !synchronizationSubscription.isUnsubscribed()) {
            synchronizationSubscription.unsubscribe();
            synchronizationSubscription = null;
        }
        if (pickedOrganisationUnitSubscription != null && !pickedOrganisationUnitSubscription
                .isUnsubscribed()) {
            pickedOrganisationUnitSubscription.unsubscribe();
            pickedOrganisationUnitSubscription = null;
        }
        if (pickedProgramSubscription != null && !pickedProgramSubscription.isUnsubscribed()) {
            pickedProgramSubscription.unsubscribe();
            pickedProgramSubscription = null;
        }
    }

    @Override
    public void initializeSynchronization() {
        if (synchronizationSubscription == null || synchronizationSubscription.isUnsubscribed()) {
            selectorView.onStartLoading();

            synchronizationSubscription = D2.me().programs().sync()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Program>>() {

                        @Override
                        public void call(List<Program> assignedPrograms) {
                            selectorView.onFinishLoading();

                            for (Program program : assignedPrograms) {
                                System.out.println("Program list item: " +
                                        program.getDisplayName() + " isAssigned: " +
                                        program.isAssignedToUser());
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            selectorView.onLoadingError(throwable);
                        }
                    });

            // TODO Do something with this
//            Observable.zip(Arrays.asList(D2.me().programs().sync(),
//                    D2.me().organisationUnits().sync()), new FuncN<List<OrganisationUnit>>() {
//
//                @Override
//                public List<OrganisationUnit> call(Object... args) {
//                    return null;
//                }
//            });
        }
    }
}
