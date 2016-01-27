package org.hisp.dhis.android.eventcapture.fragments.picker;

import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;

import org.hisp.dhis.android.eventcapture.fragments.selector.INewButtonActivator;
import org.hisp.dhis.android.eventcapture.presenters.OrganisationUnitProgramPickerPresenter;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrganisationUnitProgramPickerFragment extends PickerFragment implements IOrganisationUnitProgramPickerView {
    private OrganisationUnitProgramPickerPresenter mOrganisationUnitProgramPickerPresenter;
    private Picker mProgramPicker;
    private Picker mOrganisationUnitPicker;
    private INewButtonActivator onPickerClickedListener;

    public OrganisationUnitProgramPickerFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.createPickers();

        mOrganisationUnitProgramPickerPresenter = new OrganisationUnitProgramPickerPresenter();
        mOrganisationUnitProgramPickerPresenter.setOrganisationUnitProgramPickerView(this);

        mOrganisationUnitProgramPickerPresenter.onCreate();
    }


    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void createPickers() {
        mOrganisationUnitPicker = new Picker(new ArrayList<IPickable>(), OrganisationUnit.class.getSimpleName(), OrganisationUnit.class.getName());
        mProgramPicker = new Picker(new ArrayList<IPickable>(), Program.class.getSimpleName(), Program.class.getName());
        mProgramPicker.setListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (mProgramPicker.getPickedItem() != null) {
                            onPickerClickedListener.activate();
                        } else {
                            onPickerClickedListener.deactivate();
                        }

                    }
                }

        );
        mProgramPicker.registerPickedItemClearListener(new IPickableItemClearListener() {
            @Override
            public void clearedCallback() {
                onPickerClickedListener.deactivate();
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

    public void setOnPickerClickedListener(INewButtonActivator onPickerClickedListener) {
        this.onPickerClickedListener = onPickerClickedListener;
    }
}
