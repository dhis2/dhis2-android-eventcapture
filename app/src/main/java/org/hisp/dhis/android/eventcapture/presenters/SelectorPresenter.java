package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.common.D2;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SelectorPresenter extends AbsPresenter implements ISelectorPresenter {

    private ISelectorView mSelectorView;
    private Subscription synchronizationSubscription;

    public SelectorPresenter(ISelectorView selectorView) {
        this.mSelectorView = selectorView;
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
        if(synchronizationSubscription != null && !synchronizationSubscription.isUnsubscribed()) {
            synchronizationSubscription.unsubscribe();
            synchronizationSubscription = null;
        }
    }


    @Override
    public void initializeSynchronization() {
        if(synchronizationSubscription == null || synchronizationSubscription.isUnsubscribed()) {
            mSelectorView.onStartLoading();
            synchronizationSubscription = D2.me().syncAssignedPrograms()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mSelectorView.onFinishLoading();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mSelectorView.onLoadingError();
                        }
                    });
        }
    }
}
