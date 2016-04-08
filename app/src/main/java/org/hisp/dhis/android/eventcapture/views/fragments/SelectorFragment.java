/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.views.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenterImpl;
import org.hisp.dhis.android.eventcapture.RxBus;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;

import rx.Observable;
import timber.log.Timber;

public class SelectorFragment extends BaseFragment implements SelectorView, OnAllPickersSelectedListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = SelectorFragment.class.getSimpleName();

    private RxBus rxBus;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SelectorPresenter mSelectorPresenter;

    private OrganisationUnitProgramPickerFragment mOrganisationUnitProgramPickerFragment;
    private ItemListFragment mItemListFragment;
    boolean ranOnStartLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectorPresenter = new SelectorPresenterImpl(this);
        setOnMenuItemClickListener(this);
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
            showRefreshButton();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_selector);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        //set the circular progress bar color:
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_primary_default);

        mSelectorPresenter.initializeSynchronization(false);
    }

    @Override
    public void onRefresh() {
        System.out.println("On refresh selector fragment.");
        mSelectorPresenter.initializeSynchronization(true);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        System.out.println("On refresh selector fragment.");
        mSelectorPresenter.initializeSynchronization(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void attachFragment(int resId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(resId, fragment, tag).commit();
    }

    @Override
    public void onFinishLoading() {

        if (mSwipeRefreshLayout == null && getView() != null) {
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView()
                    .findViewById(R.id.swipe_refresh_selector);
            if (mSwipeRefreshLayout == null) {
                Timber.e("mSwipeRefreshLayout is NULL");
                return;
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
        //A quick workaround for a null pointer exception that happens after screen rotation.
        //TODO: Vlad : find the cause of getActivity() returning null. (RX call related.)
        Activity a = getActivity();
        if (a != null) {
            FrameLayout progressFrame = (FrameLayout) a.findViewById(R.id.layer_progress_bar);
            progressFrame.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStartLoading() {
        FrameLayout progressFrame = (FrameLayout) getActivity().findViewById(R.id
                .layer_progress_bar);
        progressFrame.setVisibility(View.VISIBLE);
        View v = getView();
        if (mSwipeRefreshLayout == null && getView() != null) {
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView()
                    .findViewById(R.id.swipe_refresh_selector);
            if (mSwipeRefreshLayout == null) {
                Timber.e("mSwipeRefreshLayout is NULL");
                return;
            }
        }
        //mSwipeRefreshLayout.setRefreshing(true);
        //A workaround, to the above. It seems to work when started on a new thread.
        //
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onLoadingError(Throwable throwable) {
        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG);
    }


    @Override
    public void onPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnitObservable) {
        rxBus.send(new OnOrganisationUnitPickerValueUpdated(organisationUnitObservable));
    }

    @Override
    public void onPickedProgram(Observable<Program> programObservable) {
        rxBus.send(new OnProgramPickerValueUpdated(programObservable));
    }

    public Fragment createPickerFragment() {
        mOrganisationUnitProgramPickerFragment = new OrganisationUnitProgramPickerFragment();
        mOrganisationUnitProgramPickerFragment.setSelectorView(this);
        mOrganisationUnitProgramPickerFragment.setOnPickerClickedListener(this);
        return mOrganisationUnitProgramPickerFragment;
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    public static class OnOrganisationUnitPickerValueUpdated {
        private final Observable<OrganisationUnit> organisationUnitObservable;

        public OnOrganisationUnitPickerValueUpdated(
                Observable<OrganisationUnit> organisationUnitObservable) {
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
