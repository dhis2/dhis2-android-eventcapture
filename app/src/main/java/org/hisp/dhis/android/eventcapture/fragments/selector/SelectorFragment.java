package org.hisp.dhis.android.eventcapture.fragments.selector;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.android.eventcapture.presenters.ISelectorPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.client.sdk.ui.R;
import org.hisp.dhis.client.sdk.ui.fragments.AbsSelectorFragment;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

public class SelectorFragment extends AbsSelectorFragment implements ISelectorView, INewButtonActivator, View.OnClickListener {
    public static final String TAG = SelectorFragment.class.getSimpleName();
    private FrameLayout mPickerFrameLayout;
    private PickerFragment mPickerFragment;
    private ISelectorPresenter mSelectorPresenter;

    private FloatingActionButton mFloatingActionButton;
    private boolean hiddenFloatingActionButton; //to save the state of the action button.
    public static final String FLOATING_BUTTON_STATE = "extra:FloatingButtonState";

    public SelectorFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hiddenFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
            //restore mSelectorPresenter ! (instead of getting the instance...)
        }
        mSelectorPresenter = new SelectorPresenter(this, this);
        mSelectorPresenter.onCreate();

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            PickerFragment pickerFragment = (PickerFragment) mSelectorPresenter.createPickerFragment();
            attachFragment(R.id.pickerFragment, pickerFragment, PickerFragment.TAG);

            ItemListFragment itemListFragment = (ItemListFragment) mSelectorPresenter.createItemListFragment();
            attachFragment(R.id.itemFragment, itemListFragment, ItemListFragment.TAG);
            hiddenFloatingActionButton = true;
        } else {
            hiddenFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
            mSelectorPresenter.registerPickerCallbacks();
        }

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(this);

        if (hiddenFloatingActionButton) {
            mFloatingActionButton.hide();
        } else {
            mFloatingActionButton.show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            mNavigationHandler.switchFragment(
//                    new SettingsFragment(), SettingsFragment.TAG, true);
//        }
//        else if (id == android.R.id.home) {
//            getFragmentManager().popBackStack();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public PickerFragment getPickerFragment() {
        return mPickerFragment = (PickerFragment) getFragmentManager().findFragmentByTag(PickerFragment.TAG);
    }

    @Override
    public void attachFragment(int resId, Fragment fragment, String tag) {
        mNavigationHandler.addFragmentToLayout(resId, fragment, tag);
    }

    @Override
    public void onClick(View v) {
        //Log.d("FloatingActionButton", "onClick");
        // Add new event for orgUnit and program
        //mSelectorPresenter.getOrganisationUnitPicker().getPickedItem();
    }

    @Override
    public void activate() {
        mFloatingActionButton.show();
        hiddenFloatingActionButton = false;
    }

    @Override
    public void deactivate() {
        mFloatingActionButton.hide();
        hiddenFloatingActionButton = true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
    }
}
