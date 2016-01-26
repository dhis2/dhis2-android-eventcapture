package org.hisp.dhis.android.eventcapture.presenters;

import android.support.v4.app.Fragment;

import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.android.eventcapture.fragments.picker.OrganisationUnitProgramPickerFragment;
import org.hisp.dhis.android.eventcapture.fragments.selector.INewButtonActivator;
import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;

public class SelectorPresenter extends AbsPresenter implements ISelectorPresenter, IPickableItemClearListener {

    private PickerFragment mPickerFragment;
    private ItemListFragment mItemListFragment;
    private ISelectorView mSelectorView;
    private INewButtonActivator mNewButtonActivator;


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

}
