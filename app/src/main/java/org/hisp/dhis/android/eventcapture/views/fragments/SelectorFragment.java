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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter;
import org.hisp.dhis.android.eventcapture.views.SelectorView2;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.picker.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SelectorFragment extends BaseFragment
        implements SelectorView2, OnMenuItemClickListener {

    private static final String STATE_IS_REFRESHING = "state:isRefreshing";

    @Inject
    SelectorPresenter selectorPresenter;

    @Inject
    Logger logger;

    SwipeRefreshLayout swipeRefreshLayout;
    PickerAdapter pickerAdapter;
    RecyclerView pickerRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EventCaptureApp) getActivity().getApplication())
                .getUserComponent().inject(this);
        System.out.println("onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selector_2, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_main);
            getParentToolbar().setOnMenuItemClickListener(this);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swiperefreshlayout_selector);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        pickerAdapter = new PickerAdapter(getChildFragmentManager(), getActivity());

        pickerRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_pickers);
        pickerRecyclerView.setLayoutManager(layoutManager);
        pickerRecyclerView.setAdapter(pickerAdapter);

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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(getActivity(), "CLICK", Toast.LENGTH_LONG).show();
        logger.d(SelectorFragment.class.getSimpleName(), "onMenuItemClick()");

        switch (item.getItemId()) {
            case R.id.action_refresh: {
                selectorPresenter.sync();
                return true;
            }
        }

        return false;
    }

    // TODO implement filter in dialog (also think about preserving its state)
    // TODO show animation when amount of pickers change
    // TODO better APIs to build the tree
    // TODO provide callbacks to client code (notify clients about changes on each selection)
    private static class PickerAdapter extends RecyclerView.Adapter {
        private static final String PICKER_ADAPTER_STATE = "state:pickerAdapter";

        private final FragmentManager fragmentManager;
        private final LayoutInflater layoutInflater;
        private final List<Picker> pickers;

        private Picker pickerTree;

        public PickerAdapter(FragmentManager fragmentManager, Context context) {
            this.fragmentManager = fragmentManager;
            this.layoutInflater = LayoutInflater.from(context);
            this.pickers = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PickerViewHolder(layoutInflater.inflate(
                    R.layout.recyclerview_picker, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((PickerViewHolder) holder).update(pickers.get(position));
        }

        @Override
        public int getItemCount() {
            int itemCount = pickers.size();

            if (itemCount > 0) {
                Picker lastPicker = pickers.get(itemCount - 1);

                // if last picker does not gave any items, we don't want
                // to show it as picker in the list, but use it as value for parent
                if (lastPicker.getItems().isEmpty()) {
                    itemCount = itemCount - 1;
                }
            }

            return itemCount;
        }

        public void onSaveInstanceState(Bundle outState) {
            if (outState != null && pickerTree != null) {
                outState.putParcelable(PICKER_ADAPTER_STATE, pickerTree);
            }
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                Picker pickerTree = savedInstanceState
                        .getParcelable(PICKER_ADAPTER_STATE);
                swapData(pickerTree);
            }
        }

        public void swapData(Picker pickerTree) {
            this.pickerTree = pickerTree;
            this.pickers.clear();

            if (pickerTree != null) {

                // flattening the picker tree into list
                Picker node = getRootNode(pickerTree);
                do {
                    // we don't want to add leaf nodes to list
                    if (!node.getItems().isEmpty()) {
                        pickers.add(node);
                    }
                } while ((node = node.getSelectedItem()) != null);
            }

            notifyDataSetChanged();
        }

        private Picker getRootNode(Picker picker) {
            Picker node = picker;

            // walk up the tree
            while (node.getParent() != null) {
                node = node.getParent();
            }

            return node;
        }

        private class OnItemClickedListener implements
                FilterableDialogFragment.OnPickerItemClickListener {

            @Override
            public void onPickerItemClickListener(Picker selectedPicker) {
                if (selectedPicker.getParent() != null) {
                    selectedPicker.getParent().setSelectedChild(selectedPicker);
                }

                // re-render the tree
                swapData(selectedPicker);
            }
        }

        private class PickerViewHolder extends RecyclerView.ViewHolder {
            private final TextView pickerLabel;
            private final ImageView cancel;

            public PickerViewHolder(View itemView) {
                super(itemView);

                pickerLabel = (TextView) itemView.findViewById(R.id.textview_picker);
                cancel = (ImageView) itemView.findViewById(R.id.imageview_cancel);
            }

            public void update(Picker picker) {
                if (picker.getSelectedItem() != null) {
                    pickerLabel.setText(picker.getSelectedItem().getName());
                } else {
                    pickerLabel.setText(picker.getHint());
                }

                OnClickListener listener = new OnClickListener(picker);
                pickerLabel.setOnClickListener(listener);
                cancel.setOnClickListener(listener);

                attachListenerToExistingFragment(picker);
            }

            private void attachListenerToExistingFragment(Picker picker) {
                FilterableDialogFragment fragment = (FilterableDialogFragment)
                        fragmentManager.findFragmentByTag(FilterableDialogFragment.TAG);

                // if we don't have fragment attached to activity,
                // we don't want to do anything else
                if (fragment == null) {
                    return;
                }

                // get the arguments bundle out from fragment
                Bundle arguments = fragment.getArguments();

                // if we don't have picker set to fragment, we can't distinguish
                // the fragment which we need to update
                if (arguments == null || !arguments
                        .containsKey(FilterableDialogFragment.ARGS_PICKER)) {
                    return;
                }

                Picker existingPicker = arguments
                        .getParcelable(FilterableDialogFragment.ARGS_PICKER);
                if (picker.equals(existingPicker)) {
                    FilterableDialogFragment.OnPickerItemClickListener listener =
                            new OnItemClickedListener();
                    fragment.setOnPickerItemClickListener(listener);
                }
            }
        }

        private class OnClickListener implements View.OnClickListener {
            private final Picker picker;

            private OnClickListener(Picker picker) {
                this.picker = picker;
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.textview_picker: {
                        attachFragment();
                        break;
                    }
                    case R.id.imageview_cancel: {
                        clearSelection();
                        break;
                    }
                }
            }

            private void attachFragment() {
                FilterableDialogFragment.OnPickerItemClickListener listener =
                        new OnItemClickedListener();
                FilterableDialogFragment dialogFragment =
                        FilterableDialogFragment.newInstance(picker);

                dialogFragment.setOnPickerItemClickListener(listener);
                dialogFragment.show(fragmentManager, FilterableDialogFragment.TAG);
            }

            private void clearSelection() {
                picker.setSelectedChild(null);
                swapData(picker);
            }
        }
    }
}
