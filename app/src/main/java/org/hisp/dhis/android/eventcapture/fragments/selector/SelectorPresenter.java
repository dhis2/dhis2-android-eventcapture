package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;

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
            synchronizationSubscription = D2.me().programs()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            selectorView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            selectorView.onLoadingError(throwable);
                        }
                    });
        }
    }
}
