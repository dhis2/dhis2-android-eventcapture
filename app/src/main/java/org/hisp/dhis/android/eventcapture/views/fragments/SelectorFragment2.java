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

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.hisp.dhis.android.eventcapture.presenters.SelectorPresenter2;
import org.hisp.dhis.android.eventcapture.views.SelectorView2;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.models.picker.Picker;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SelectorFragment2 extends BaseFragment
        implements SelectorView2, OnMenuItemClickListener {

    @Inject
    SelectorPresenter2 selectorPresenter;

    @Inject
    Logger logger;

    PickerAdapter pickerAdapter;

    RecyclerView pickerRecyclerView;

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
        return inflater.inflate(R.layout.fragment_selector_2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (getParentToolbar() != null) {
            getParentToolbar().inflateMenu(R.menu.menu_main);
            getParentToolbar().setOnMenuItemClickListener(this);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        pickerAdapter = new PickerAdapter();

        pickerRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_pickers);
        pickerRecyclerView.setLayoutManager(layoutManager);
        pickerRecyclerView.setAdapter(pickerAdapter);

        selectorPresenter.listPickers();
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
        logger.d(SelectorFragment2.class.getSimpleName(), "showProgressBar()");
    }

    @Override
    public void hideProgressBar() {
        logger.d(SelectorFragment2.class.getSimpleName(), "hideProgressBar()");
    }

    @Override
    public void showPickers(Picker picker) {
        pickerAdapter.swapData(picker);

        System.out.println("### LABEL ###: " + picker.getLabel());
        for (Picker pickerItem : picker.getItems()) {
            System.out.println("### ITEM ###: " + pickerItem.getDescendant());
        }
    }

    @Override
    public void showNoOrganisationUnitsError() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(getActivity(), "CLICK", Toast.LENGTH_LONG).show();
        logger.d(SelectorFragment2.class.getSimpleName(), "onMenuItemClick()");
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                selectorPresenter.sync();
                return true;
            }
        }

        return false;
    }

    private class PickerAdapter extends RecyclerView.Adapter {
        private final LayoutInflater layoutInflater;
        private final List<Picker> pickers;

        public PickerAdapter() {
            layoutInflater = LayoutInflater.from(getActivity());
            pickers = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PickerViewHolder(
                    layoutInflater.inflate(R.layout.recyclerview_picker, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            PickerViewHolder pickerViewHolder = (PickerViewHolder) holder;
            pickerViewHolder.pickerLabel.setText(pickers.get(position).getLabel());
        }

        @Override
        public int getItemCount() {
            return pickers.size();
        }

        public void swapData(Picker picker) {
            pickers.clear();
            System.out.println("Picker: " + picker);

            // flattening the picker tree
            if (picker != null) {
                Picker node = picker;

                do {
                    pickers.add(node);
                } while ((node = node.getDescendant()) != null);
            }

            notifyDataSetChanged();
        }

        private class PickerViewHolder extends RecyclerView.ViewHolder {
            final TextView pickerLabel;
            final ImageView cancel;
            final View.OnClickListener onClickListener;

            public PickerViewHolder(View itemView) {
                super(itemView);

                onClickListener = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // stub implementation
                    }
                };

                pickerLabel = (TextView) itemView.findViewById(R.id.textview_picker);
                cancel = (ImageView) itemView.findViewById(R.id.imageview_cancel);

                pickerLabel.setOnClickListener(onClickListener);
                cancel.setOnClickListener(onClickListener);
            }
        }
    }
}
