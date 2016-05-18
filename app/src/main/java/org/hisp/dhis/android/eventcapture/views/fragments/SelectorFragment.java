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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
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
import android.widget.TextView;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.ui.models.ReportEntity;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.views.AbsAnimationListener;
import org.hisp.dhis.android.eventcapture.views.activities.FormSectionActivity;
import org.hisp.dhis.client.sdk.ui.adapters.ReportEntityAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.ReportEntityAdapter.OnReportEntityClickListener;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.adapters.PickerAdapter;
import org.hisp.dhis.client.sdk.ui.adapters.PickerAdapter.OnPickerListChangeListener;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.Picker;
import org.hisp.dhis.client.sdk.ui.views.DividerDecoration;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;
import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class SelectorFragment extends BaseFragment implements SelectorView {
    private static final String TAG = SelectorFragment.class.getSimpleName();

    private static final int ORG_UNIT_PICKER_ID = 0;
    private static final int PROGRAM_UNIT_PICKER_ID = 1;
    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    @Inject
    SelectorPresenter selectorPresenter;

    @Inject
    Logger logger;

    // button which is shown only in case when all pickers are set
    FloatingActionButton createEventButton;
    OnCreateEventButtonClickListener onCreateEventButtonClickListener;

    // pull-to-refresh
    SwipeRefreshLayout swipeRefreshLayout;
    BottomSheetBehavior<CardView> bottomSheetBehavior;

    // bottom sheet layout
    CoordinatorLayout coordinatorLayout;
    CardView bottomSheetView;

    // selected organisation unit and program
    TextView selectedOrganisationUnit;
    TextView selectedProgram;

    // list of pickers
    RecyclerView pickerRecyclerView;
    PickerAdapter pickerAdapter;

    // list of events
    RecyclerView reportEntityRecyclerView;
    ReportEntityAdapter reportEntityAdapter;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupToolbar();
        setupBottomSheet(view, savedInstanceState);
        setupFloatingActionButton(view);
        setupSwipeRefreshLayout(view, savedInstanceState);
        setupPickerRecyclerView(view, savedInstanceState);
        setupReportEntityRecyclerView(view, savedInstanceState);
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

        logger.d(TAG, "onResume()");
        selectorPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.d(TAG, "onPause()");
        selectorPresenter.detachView();
    }

    @Override
    public void showProgressBar() {
        logger.d(SelectorFragment.class.getSimpleName(), "showProgressBar()");

        // this workaround is necessary because of the message queue
        // implementation in android. If you will try to setRefreshing(true) right away,
        // this call will be placed in UI message queue by SwipeRefreshLayout BEFORE
        // message to hide progress bar which probably is created by layout
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
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
    public void showReportEntities(List<ReportEntity> reportEntities) {
        logger.d(TAG, "amount of report entities: " + reportEntities.size());
        reportEntityAdapter.swapData(reportEntities);
    }

    @Override
    public void showNoOrganisationUnitsError() {
        pickerAdapter.swapData(null);
    }

    @Override
    public void navigateToFormSectionActivity(Event event) {
        logger.d(TAG, String.format("Event with uid=%s is created", event.getUId()));
        FormSectionActivity.navigateTo(getActivity(), event.getUId());
    }

    @Override
    public String getPickerLabel(@PickerLabelId String pickerLabelId) {
        isNull(pickerLabelId, "pickerLabelId must not be null");

        switch (pickerLabelId) {
            case ID_CHOOSE_ORGANISATION_UNIT:
                return getString(R.string.choose_organisation_unit);
            case ID_CHOOSE_PROGRAM:
                return getString(R.string.choose_program);
            default:
                throw new IllegalArgumentException("Unsupported PickerLabelId");
        }
    }

    @Override
    public boolean onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        return false;
    }

    private void setupToolbar() {
        Drawable buttonDrawable = DrawableCompat.wrap(ContextCompat
                .getDrawable(getActivity(), R.drawable.ic_menu));
        DrawableCompat.setTint(buttonDrawable, ContextCompat
                .getColor(getContext(), android.R.color.white));

        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_refresh);
            getParentToolbar().setNavigationIcon(buttonDrawable);
            getParentToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return SelectorFragment.this.onMenuItemClick(item);
                }
            });
        }
    }

    private void setupFloatingActionButton(final View rootView) {
        onCreateEventButtonClickListener = new OnCreateEventButtonClickListener();

        createEventButton = (FloatingActionButton) rootView.findViewById(R.id.fab_create_event);
        createEventButton.setOnClickListener(onCreateEventButtonClickListener);

        // button visibility will be changed as soon as pickers are loaded
        createEventButton.setVisibility(View.INVISIBLE);
    }

    private void setupSwipeRefreshLayout(final View rootView, final Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.swiperefreshlayout_selector);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary_default);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                selectorPresenter.sync();
            }
        });

        if (savedInstanceState != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(savedInstanceState
                            .getBoolean(STATE_IS_REFRESHING, false));
                }
            });
        }
    }

    private void setupPickerRecyclerView(final View rootView, final Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        pickerAdapter = new PickerAdapter(getChildFragmentManager(), getActivity());
        pickerAdapter.setOnPickerListChangeListener(new OnPickerListChangeListener() {
            @Override
            public void onPickerListChanged(List<Picker> pickers) {
                SelectorFragment.this.onPickerListChanged(pickers);
            }
        });

        pickerRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_pickers);
        pickerRecyclerView.setLayoutManager(layoutManager);
        pickerRecyclerView.setAdapter(pickerAdapter);
        pickerRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (savedInstanceState != null) {
            pickerAdapter.onRestoreInstanceState(savedInstanceState);
        } else {
            selectorPresenter.listPickers();
        }
    }

    private void setupReportEntityRecyclerView(View view, Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        reportEntityAdapter = new ReportEntityAdapter(getActivity());
        reportEntityAdapter.setOnReportEntityClickListener(new OnReportEntityClickListener() {
            @Override
            public void onReportEntityClicked(ReportEntity reportEntity) {
                SelectorFragment.this.onReportEntityClicked(reportEntity);
            }
        });

        reportEntityRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_events);
        reportEntityRecyclerView.setLayoutManager(layoutManager);
        reportEntityRecyclerView.setAdapter(reportEntityAdapter);
        reportEntityRecyclerView.setItemAnimator(new DefaultItemAnimator());
        reportEntityRecyclerView.addItemDecoration(new DividerDecoration(
                ContextCompat.getDrawable(getActivity(), R.drawable.divider)));
    }

    private void setupBottomSheet(View view, Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorlayout_selector);
        bottomSheetView = (CardView) view.findViewById(R.id.card_view_bottom_sheet);

        selectedOrganisationUnit = (TextView) view.findViewById(R.id.textview_organisation_unit);
        selectedProgram = (TextView) view.findViewById(R.id.textview_program);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setPeekHeight(getResources()
                .getDimensionPixelSize(R.dimen.bottomsheet_peek_height));

        if (savedInstanceState == null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void onReportEntityClicked(ReportEntity reportEntity) {
        FormSectionActivity.navigateTo(getActivity(), reportEntity.getId());
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
        updateLabels(pickers);

        onCreateEventButtonClickListener.setPickers(pickers);
        if (areAllPickersPresent(pickers)) {
            showCreateEventButton();

            // load existing events
            selectorPresenter.listEvents(getOrganisationUnitUid(pickers), getProgramUid(pickers));
        } else {
            hideCreateEventButton();

            // clear out list of existing events
            if (reportEntityAdapter != null) {
                reportEntityAdapter.swapData(null);
            }
        }
        selectorPresenter.onPickersSelectionsChanged(pickers);
    }

    /* check if organisation unit and program are selected */
    private boolean areAllPickersPresent(List<Picker> pickers) {
        return pickers != null && pickers.size() > 1 &&
                pickers.get(ORG_UNIT_PICKER_ID) != null &&
                pickers.get(ORG_UNIT_PICKER_ID).getSelectedChild() != null &&
                pickers.get(PROGRAM_UNIT_PICKER_ID) != null &&
                pickers.get(PROGRAM_UNIT_PICKER_ID).getSelectedChild() != null;
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

        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }, 256);
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

    private void updateLabels(List<Picker> pickers) {
        String orgUnitLabel;
        String programLabel;

        if (!isEmpty(getOrganisationUnitLabel(pickers))) {
            orgUnitLabel = String.format(Locale.getDefault(), "%s: %s",
                    getString(R.string.organisation_unit), getOrganisationUnitLabel(pickers));
        } else {
            orgUnitLabel = String.format(Locale.getDefault(), "%s: %s",
                    getString(R.string.organisation_unit), getString(R.string.none));
        }

        if (!isEmpty(getProgramLabel(pickers))) {
            programLabel = getProgramLabel(pickers);
        } else {
            programLabel = String.format(Locale.getDefault(), "%s: %s",
                    getString(R.string.program), getString(R.string.none));
        }

        selectedOrganisationUnit.setText(orgUnitLabel);
        selectedProgram.setText(programLabel);
    }

    private class OnCreateEventButtonClickListener implements View.OnClickListener {
        private List<Picker> pickers;

        @Override
        public void onClick(View v) {
            String orgUnitUid = getOrganisationUnitUid(pickers);
            String programUid = getProgramUid(pickers);

            if (orgUnitUid != null && programUid != null) {
                selectorPresenter.createEvent(orgUnitUid, programUid);
            }
        }

        public void setPickers(List<Picker> pickers) {
            this.pickers = pickers;
        }
    }

    private static String getOrganisationUnitUid(List<Picker> pickers) {
        if (pickers != null && !pickers.isEmpty() &&
                pickers.get(ORG_UNIT_PICKER_ID).getSelectedChild() != null) {
            return pickers.get(ORG_UNIT_PICKER_ID).getSelectedChild().getId();
        }

        return null;
    }

    private static String getOrganisationUnitLabel(List<Picker> pickers) {
        if (pickers != null && !pickers.isEmpty() &&
                pickers.get(ORG_UNIT_PICKER_ID).getSelectedChild() != null) {
            return pickers.get(ORG_UNIT_PICKER_ID).getSelectedChild().getName();
        }

        return null;
    }

    private static String getProgramUid(List<Picker> pickers) {
        if (pickers != null && pickers.size() > 1 &&
                pickers.get(PROGRAM_UNIT_PICKER_ID).getSelectedChild() != null) {
            return pickers.get(PROGRAM_UNIT_PICKER_ID).getSelectedChild().getId();
        }

        return null;
    }

    private static String getProgramLabel(List<Picker> pickers) {
        if (pickers != null && pickers.size() > 1 &&
                pickers.get(PROGRAM_UNIT_PICKER_ID).getSelectedChild() != null) {
            return pickers.get(PROGRAM_UNIT_PICKER_ID).getSelectedChild().getName();
        }

        return null;
    }
}
