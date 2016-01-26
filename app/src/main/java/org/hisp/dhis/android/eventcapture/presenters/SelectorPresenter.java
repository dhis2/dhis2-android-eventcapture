package org.hisp.dhis.android.eventcapture.presenters;

import android.support.v4.app.Fragment;

import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.android.eventcapture.fragments.picker.OrganisationUnitProgramPickerFragment;
import org.hisp.dhis.android.eventcapture.fragments.selector.INewButtonActivator;
import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SelectorPresenter extends AbsPresenter implements ISelectorPresenter, IPickableItemClearListener {

    private PickerFragment mPickerFragment;
    private ItemListFragment mItemListFragment;
    private ISelectorView mSelectorView;
    private INewButtonActivator mNewButtonActivator;
    private Subscription synchronizationSubscription;


    public SelectorPresenter(ISelectorView selectorView, INewButtonActivator newButtonActivator) {
        this.mSelectorView = selectorView;
        this.mNewButtonActivator = newButtonActivator;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override

    public Fragment createPickerFragment() {
        OrganisationUnitProgramPickerFragment organisationUnitProgramPickerFragment = new OrganisationUnitProgramPickerFragment();
//        this.registerPickerCallbacks();


        return organisationUnitProgramPickerFragment;
    }

    @Override
    public Fragment createItemListFragment() {
        mItemListFragment = new ItemListFragment();

        return mItemListFragment;
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
    public void registerPickerCallbacks() {
//        mProgramPicker.setListener(
//                new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                        if (mProgramPicker.getPickedItem() != null) {
//                            mNewButtonActivator.activate();
//                        } else {
//                            mNewButtonActivator.deactivate();
//                        }
//
//                    }
//                }
//
//        );
//        mProgramPicker.registerPickedItemClearListener(this);
        //mOrgUnitPicker.registerPickedItemClearListener(this); //implicit in picker.
    }


    @Override
    public void clearedCallback() {
        mNewButtonActivator.deactivate();
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
