package org.hisp.dhis.android.eventcapture.fragments.selector;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.activities.home.DetailsActivity;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.android.eventcapture.fragments.picker.OrganisationUnitProgramPickerFragment;
import org.hisp.dhis.android.eventcapture.utils.RxBus;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import rx.Observable;

public class SelectorFragment extends Fragment implements ISelectorView,
        OnAllPickersSelectedListener, View.OnClickListener {

    public static final String TAG = SelectorFragment.class.getSimpleName();
    public static final String FLOATING_BUTTON_STATE = "state:FloatingButtonState";

    private RxBus rxBus;
    private ISelectorPresenter mSelectorPresenter;
    private CircularProgressBar progressBar;
    private FloatingActionButton mFloatingActionButton;
    private boolean hiddenFloatingActionButton;
    //to save the state of the action button.

    private OrganisationUnitProgramPickerFragment mOrganisationUnitProgramPickerFragment;
    private ItemListFragment mItemListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hiddenFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE,
                    hiddenFloatingActionButton);
        }

        mSelectorPresenter = new SelectorPresenter(this);
        mSelectorPresenter.onCreate();

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rxBus = ((EventCaptureApp) getActivity().getApplication()).getRxBusSingleton();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            //init orgPicker:
            OrganisationUnitProgramPickerFragment organisationUnitProgramPickerFragment =
                    (OrganisationUnitProgramPickerFragment) createPickerFragment();
            attachFragment(R.id.pickerFragment, organisationUnitProgramPickerFragment,
                    OrganisationUnitProgramPickerFragment.TAG);
            //init itemList:
            if (getActivity().findViewById(R.id.item_fragment) != null) {
                mItemListFragment = new ItemListFragment();
                attachFragment(R.id.item_fragment, mItemListFragment,
                        ItemListFragment.TAG);
            }
            hiddenFloatingActionButton = true;
        } else {
            hiddenFloatingActionButton = savedInstanceState.getBoolean(FLOATING_BUTTON_STATE,
                    hiddenFloatingActionButton);
        }

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id
                .floating_action_button);
        mFloatingActionButton.setOnClickListener(this);

        boolean onTablet = getResources().getBoolean(org.hisp.dhis.android.eventcapture.R.bool
                .isTablet);
        if (!onTablet) {
            if (hiddenFloatingActionButton) {
                mFloatingActionButton.hide();
            } else {
                mFloatingActionButton.show();
            }
        } else { //hide it
            mFloatingActionButton.hide();
        }

        progressBar = (CircularProgressBar) view.findViewById(R.id.progress_bar_circular);
        //hideProgress();


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

    public void attachFragment(int resId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(resId, fragment, tag).commit();
    }

    @Override
    public void onFinishLoading() {
        hideProgress();
    }

    @Override
    public void onLoadingError(Throwable throwable) {
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG);
    }

    @Override
    public void onStartLoading() {
        showProgress();
    }

    @Override
    public void onPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnitObservable) {
        rxBus.send(new OnOrganisationUnitPickerValueUpdated(organisationUnitObservable));
    }

    @Override
    public void onPickedProgram(Observable<Program> programObservable) {
        rxBus.send(new OnProgramPickerValueUpdated(programObservable));
    }

    private void hideProgress() {

        //A quick workaround for a null pointer exception that happens after screen rotation.
        //TODO: Vlad : find the cause of getActivity() returning null. (RX call related.)
        Activity a = getActivity();
        if (a != null) {
            FrameLayout progressFrame = (FrameLayout) a.findViewById(R.id.layer_progress_bar);
            progressFrame.setVisibility(View.GONE);
        }

        //spinner:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.out_down);
            progressBar.startAnimation(anim);
        }
        progressBar.setVisibility(View.GONE);
    }

    private void showProgress() {

        FrameLayout progressFrame = (FrameLayout) getActivity().findViewById(R.id
                .layer_progress_bar);
        progressFrame.setVisibility(View.VISIBLE);

        //spinner:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.in_up);
            progressBar.startAnimation(anim);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        // on phone(portrait): go to ItemListFragment

        Intent itemsList = new Intent(getActivity(), DetailsActivity.class); //switch to activity.
        startActivity(itemsList);
    }

    @Override
    public void activate() {
        // only show on phones:
        /* deactivated FAB, as it is currently not necessary.
        if (!getResources().getBoolean(R.bool.isTablet)) {
            mFloatingActionButton.show();
            hiddenFloatingActionButton = false;
        }*/
    }

    @Override
    public void deactivate() {
        mFloatingActionButton.hide();
        hiddenFloatingActionButton = true;
    }

    public Fragment createPickerFragment() {
        mOrganisationUnitProgramPickerFragment = new OrganisationUnitProgramPickerFragment();
        //these callbacks are lost. (part of the problem)
        mOrganisationUnitProgramPickerFragment.setOnPickerClickedListener(this);
        mOrganisationUnitProgramPickerFragment.setSelectorView(this);
        return mOrganisationUnitProgramPickerFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(FLOATING_BUTTON_STATE, hiddenFloatingActionButton);
    }

    public static class OnOrganisationUnitPickerValueUpdated {
        private final Observable<OrganisationUnit> organisationUnitObservable;

        public OnOrganisationUnitPickerValueUpdated(Observable<OrganisationUnit>
                                                            organisationUnitObservable) {
            this.organisationUnitObservable = organisationUnitObservable;
        }

        public Observable<OrganisationUnit> getOrganisationUnitObservable() {
            return this.organisationUnitObservable;
        }
    }

    public static class OnProgramPickerValueUpdated {
        private final Observable<Program> programObservable;

        public OnProgramPickerValueUpdated(Observable<Program> programObservable) {
            this.programObservable = programObservable;
        }

        public Observable<Program> getProgramObservable() {
            return this.programObservable;
        }
    }
}
