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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.views.AbsAnimationListener;
import org.hisp.dhis.android.eventcapture.views.SelectorView;
import org.hisp.dhis.client.sdk.ui.adapters.PickerAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.PickerAdapter.OnPickerListChangeListener;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

public class SelectorFragment extends BaseFragment implements SelectorView {
    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    @Inject
    SelectorPresenter selectorPresenter;

    @Inject
    Logger logger;

    // button which is shown only in case
    // when all pickers are set
    FloatingActionButton createEventButton;

    // pull-to-refresh
    SwipeRefreshLayout swipeRefreshLayout;

    // list of pickers
    RecyclerView pickerRecyclerView;
    PickerAdapter pickerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EventCaptureApp) getActivity().getApplication())
                .getUserComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selector, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_refresh);
            getParentToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return SelectorFragment.this.onMenuItemClick(item);
                }
            });
        }

        createEventButton = (FloatingActionButton) view
                .findViewById(R.id.fab_create_event);
        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swiperefreshlayout_selector);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.color_primary_default);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                selectorPresenter.sync();
            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        "Creating event", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        pickerAdapter = new PickerAdapter(getChildFragmentManager(), getActivity());
        pickerAdapter.setOnPickerListChangeListener(new OnPickerListChangeListener() {
            @Override
            public void onPickerListChanged(List<Picker> pickers) {
                SelectorFragment.this.onPickerListChanged(pickers);
            }
        });

        pickerRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_pickers);
        pickerRecyclerView.setLayoutManager(layoutManager);
        pickerRecyclerView.setAdapter(pickerAdapter);
        pickerRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (savedInstanceState != null) {
            pickerAdapter.onRestoreInstanceState(savedInstanceState);

            // this workaround is necessary because of the message queue
            // implementation in android. If you will try to setRefreshing(true) right away,
            // this call will be placed in UI message queue by SwipeRefreshLayout BEFORE
            // message to hide progress bar which probably is created by layout
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(savedInstanceState
                            .getBoolean(STATE_IS_REFRESHING, false));
                }
            });
        } else {
            selectorPresenter.listPickers();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (pickerAdapter != null) {
            pickerAdapter.onSaveInstanceState(outState);
        }

        outState.putBoolean(STATE_IS_REFRESHING, swipeRefreshLayout.isRefreshing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        selectorPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        selectorPresenter.detachView();
    }

    @Override
    public void showProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "showProgressBar()");
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "hideProgressBar()");
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showPickers(Picker pickerTree) {
        pickerAdapter.swapData(pickerTree);
    }

    @Override
    public void showNoOrganisationUnitsError() {
        pickerAdapter.swapData(null);
    }

    private boolean onMenuItemClick(MenuItem item) {
        logger.d(SelectorFragment.class.getSimpleName(), "onMenuItemClick()");

        switch (item.getItemId()) {
            case R.id.action_refresh: {
                selectorPresenter.sync();
                return true;
            }
        }

        return false;
    }

    /* change visibility of floating action button*/
    private void onPickerListChanged(List<Picker> pickers) {
        if (areAllPickersPresent(pickers)) {
            showCreateEventButton();
        } else {
            hideCreateEventButton();
        }
    }

    /* check if organisation unit and program are selected */
    private boolean areAllPickersPresent(List<Picker> pickers) {
        return pickers != null && pickers.size() > 1 &&
                pickers.get(0) != null && pickers.get(0).getSelectedChild() != null &&
                pickers.get(1) != null && pickers.get(1).getSelectedChild() != null;
    }

    private void showCreateEventButton() {
        if (!createEventButton.isShown()) {
            createEventButton.setVisibility(View.VISIBLE);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(createEventButton, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(createEventButton, "scaleY", 0, 1);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(new OvershootInterpolator());
            animSetXY.setDuration(256);
            animSetXY.start();
        }
    }

    private void hideCreateEventButton() {
        if (createEventButton.isShown()) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(createEventButton, "scaleX", 1, 0);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(createEventButton, "scaleY", 1, 0);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(new AccelerateInterpolator());
            animSetXY.setDuration(256);
            animSetXY.addListener(new AbsAnimationListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    createEventButton.setVisibility(View.INVISIBLE);
                }
            });
            animSetXY.start();
        }
    }
}
