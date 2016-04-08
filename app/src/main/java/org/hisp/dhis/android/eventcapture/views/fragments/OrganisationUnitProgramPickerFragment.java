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
import android.view.View;

import org.hisp.dhis.android.eventcapture.presenters.OrganisationUnitProgramPickerPresenter;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitProgramPickerView;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.android.eventcapture.views.ProgramPickable;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.dialogs.AutoCompleteDialogFragment;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Pickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.PickableItemClearListener;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrganisationUnitProgramPickerFragment extends PickerFragment implements OrganisationUnitProgramPickerView, AutoCompleteDialogFragment.OnOptionSelectedListener {
    public static final String TAG = OrganisationUnitProgramPickerFragment.class.getSimpleName();
    private OrganisationUnitProgramPickerPresenter mOrganisationUnitProgramPickerPresenter;
    private Picker mProgramPicker;
    private Picker mOrganisationUnitPicker;
    private OnAllPickersSelectedListener onPickerClickedListener;
    private SelectorView selectorView;

    public OrganisationUnitProgramPickerFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOrganisationUnitProgramPickerPresenter = new OrganisationUnitProgramPickerPresenter(this);
        mOrganisationUnitProgramPickerPresenter.setOrganisationUnitProgramPickerView(this);

        this.createPickers();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void createPickers() {
        mOrganisationUnitPicker = new Picker(new ArrayList<Pickable>(), OrganisationUnit.class.getSimpleName(), OrganisationUnit.class.getName());
        mOrganisationUnitPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(OrganisationUnit.class.getSimpleName(),
                        mOrganisationUnitPicker.getPickableItems(),
                        OrganisationUnitProgramPickerFragment.this);
            }
        });
        mOrganisationUnitPicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDialog(OrganisationUnit.class.getSimpleName(),
                            mOrganisationUnitPicker.getPickableItems(),
                            OrganisationUnitProgramPickerFragment.this);
                }
            }
        });

        mOrganisationUnitPicker.registerPickedItemClearListener(new PickableItemClearListener() {
            @Override
            public void clearedCallback() {
                selectorView.onPickedOrganisationUnit(null);
            }
        });

        mProgramPicker = new Picker(new ArrayList<Pickable>(), Program.class.getSimpleName(), Program.class.getName());
        mProgramPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(Program.class.getSimpleName(),
                        mProgramPicker.getPickableItems(),
                        OrganisationUnitProgramPickerFragment.this);
            }
        });
        mProgramPicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    showDialog(Program.class.getSimpleName(),
                            mProgramPicker.getPickableItems(),
                            OrganisationUnitProgramPickerFragment.this);
                }
            }
        });

        mProgramPicker.registerPickedItemClearListener(new PickableItemClearListener() {
            @Override
            public void clearedCallback() {
                selectorView.onPickedProgram(null);

            }
        });
        mOrganisationUnitPicker.setNextLinkedSibling(mProgramPicker);

        super.setRootPickerList(Collections.singletonList(mOrganisationUnitPicker));
    }

    @Override
    public void renderOrganisationUnitPickables(List<Pickable> organisationUnitPickables) {
        mOrganisationUnitPicker.setPickableItems(organisationUnitPickables);
    }

    @Override
    public void renderProgramPickables(List<Pickable> programPickables) {
        mProgramPicker.setPickableItems(programPickables);
    }

    @Override
    public void onFinishLoading() {
        /* TODO: Find another way to show/hide the spinners instead of callbacks.
        if (selectorView != null)
            selectorView.onFinishLoading();*/
    }

    @Override
    public void onLoadingError() {
        /*if(selectorView != null)
            selectorView.onLoadingError();*/
    }

    @Override
    public void onStartLoading() {
        /*if(selectorView != null)
            selectorView.onStartLoading();*/
    }

    public void setOnPickerClickedListener(OnAllPickersSelectedListener onPickerClickedListener) {
        this.onPickerClickedListener = onPickerClickedListener;
    }

    public void setSelectorView(SelectorView selectorView) {
        this.selectorView = selectorView;
    }

    @Override
    public void onOptionSelected(Pickable pickable) {
        if(pickable instanceof OrganisationUnitPickable) {
            OrganisationUnitPickable organisationUnitPickable = (OrganisationUnitPickable) pickable;

            mOrganisationUnitProgramPickerPresenter.setPickedOrganisationUnit(organisationUnitPickable.getOrganisationUnit());
            selectorView.onPickedOrganisationUnit(organisationUnitPickable.getOrganisationUnit());
            mOrganisationUnitPicker.setPickedItem(organisationUnitPickable);
            mOrganisationUnitPicker.showNext();
        }
        else if(pickable instanceof ProgramPickable) {
            ProgramPickable programPickable = (ProgramPickable) pickable;

            selectorView.onPickedProgram(programPickable.getProgram());
            mProgramPicker.setPickedItem(programPickable);
            onPickerClickedListener.activate();
        }
    }

    public void showDialog(String title, List<Pickable> pickables,
                           AutoCompleteDialogFragment.OnOptionSelectedListener
                                   onOptionSelectedListener) {
        AutoCompleteDialogFragment.newInstance(title, pickables, onOptionSelectedListener)
        .show(getFragmentManager(), TAG + title);
    }
}
