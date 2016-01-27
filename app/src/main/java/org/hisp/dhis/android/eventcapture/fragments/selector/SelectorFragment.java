package org.hisp.dhis.android.eventcapture.fragments.selector;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.hisp.dhis.android.eventcapture.fragments.picker.OrganisationUnitProgramPickerFragment;
import org.hisp.dhis.android.eventcapture.presenters.ISelectorPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.client.sdk.ui.R;
import org.hisp.dhis.client.sdk.ui.fragments.AbsSelectorFragment;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class SelectorFragment extends AbsSelectorFragment implements ISelectorView, INewButtonActivator, View.OnClickListener {
    public static final String TAG = SelectorFragment.class.getSimpleName();
    private ISelectorPresenter mSelectorPresenter;
    private CircularProgressBar progressBar;


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
        mSelectorPresenter = new SelectorPresenter(this);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            OrganisationUnitProgramPickerFragment organisationUnitProgramPickerFragment = (OrganisationUnitProgramPickerFragment) createPickerFragment();
            attachFragment(R.id.pickerFragment, organisationUnitProgramPickerFragment, OrganisationUnitProgramPickerFragment.TAG);
            hiddenFloatingActionButton = true;
        } else {
            hiddenFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
        }

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(this);

        if (hiddenFloatingActionButton) {
            mFloatingActionButton.hide();
        } else {
            mFloatingActionButton.show();
        }

        progressBar = (CircularProgressBar) view.findViewById(R.id.progress_bar_circular);
        hideProgress();
        mSelectorPresenter.initializeSynchronization();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void attachFragment(int resId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(resId, fragment, tag).commit();
    }

    @Override
    public void onFinishLoading() {
        hideProgress();
    }

    @Override
    public void onLoadingError() {
        throw new RuntimeException("loading error");
    }

    @Override
    public void onStartLoading() {
        showProgress();
    }

    private void hideProgress() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.out_down);
            progressBar.startAnimation(anim);
        }
        progressBar.setVisibility(View.GONE);
    }

    private void showProgress() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.in_up);
            progressBar.startAnimation(anim);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        // on phone(portrait): go to ItemListFragment
        // on phone(landscape): hide FAbutton (if it doesn't look good, go to ItemListFragment)
        // on tablet(portrait): go to ItemListFragment
        // on tablet(landscape): hide FAbutton
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

    public Fragment createPickerFragment() {
        OrganisationUnitProgramPickerFragment organisationUnitProgramPickerFragment = new OrganisationUnitProgramPickerFragment();
        organisationUnitProgramPickerFragment.setOnPickerClickedListener(this);

        return organisationUnitProgramPickerFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
    }
}
