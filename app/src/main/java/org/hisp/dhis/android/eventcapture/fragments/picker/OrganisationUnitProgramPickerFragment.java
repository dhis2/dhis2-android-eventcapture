package org.hisp.dhis.android.eventcapture.fragments.picker;

import android.os.Bundle;
import android.view.View;

import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.fragments.selector.OnAllPickersSelectedListener;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.android.eventcapture.views.ProgramPickable;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.dialogs.AutoCompleteDialogFragment;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrganisationUnitProgramPickerFragment extends PickerFragment implements IOrganisationUnitProgramPickerView, AutoCompleteDialogFragment.OnOptionSelectedListener {
    public static final String TAG = OrganisationUnitProgramPickerFragment.class.getSimpleName();
    private OrganisationUnitProgramPickerPresenter mOrganisationUnitProgramPickerPresenter;
    private Picker mProgramPicker;
    private Picker mOrganisationUnitPicker;
    private OnAllPickersSelectedListener onPickerClickedListener;
    private ISelectorView selectorView;

    public OrganisationUnitProgramPickerFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOrganisationUnitProgramPickerPresenter = new OrganisationUnitProgramPickerPresenter();
        mOrganisationUnitProgramPickerPresenter.setOrganisationUnitProgramPickerView(this);

        this.createPickers();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void createPickers() {
        mOrganisationUnitPicker = new Picker(new ArrayList<IPickable>(), OrganisationUnit.class.getSimpleName(), OrganisationUnit.class.getName());
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

        mOrganisationUnitPicker.registerPickedItemClearListener(new IPickableItemClearListener() {
            @Override
            public void clearedCallback() {
                selectorView.onPickedOrganisationUnit(null);
            }
        });

        mProgramPicker = new Picker(new ArrayList<IPickable>(), Program.class.getSimpleName(), Program.class.getName());
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

        mProgramPicker.registerPickedItemClearListener(new IPickableItemClearListener() {
            @Override
            public void clearedCallback() {
                selectorView.onPickedProgram(null);

            }
        });
        mOrganisationUnitPicker.setNextLinkedSibling(mProgramPicker);

        super.setRootPickerList(Collections.singletonList(mOrganisationUnitPicker));
    }

    @Override
    public void renderOrganisationUnitPickables(List<IPickable> organisationUnitPickables) {
        mOrganisationUnitPicker.setPickableItems(organisationUnitPickables);
    }

    @Override
    public void renderProgramPickables(List<IPickable> programPickables) {
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

    public void setSelectorView(ISelectorView selectorView) {
        this.selectorView = selectorView;
    }

    @Override
    public void onOptionSelected(IPickable pickable) {
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

    public void showDialog(String title, List<IPickable> pickables,
                           AutoCompleteDialogFragment.OnOptionSelectedListener
                                   onOptionSelectedListener) {
        AutoCompleteDialogFragment.newInstance(title, pickables, onOptionSelectedListener)
        .show(getFragmentManager(), TAG + title);
    }
}
