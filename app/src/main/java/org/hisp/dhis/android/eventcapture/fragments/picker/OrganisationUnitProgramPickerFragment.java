package org.hisp.dhis.android.eventcapture.fragments.picker;

import android.content.Context;
import android.os.Bundle;

import org.hisp.dhis.android.eventcapture.presenters.OrganisationUnitProgramPickerPresenter;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.SelectorAdapter;

import java.util.List;

public class OrganisationUnitProgramPickerFragment extends PickerFragment implements IOrganisationUnitProgramPickerView {
    private OrganisationUnitProgramPickerPresenter mOrganisationUnitProgramPickerPresenter;
    private SelectorAdapter mSelectorAdapter;


    public OrganisationUnitProgramPickerFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrganisationUnitProgramPickerPresenter = new OrganisationUnitProgramPickerPresenter();
        mOrganisationUnitProgramPickerPresenter.setOrganisationUnitProgramPickerView(this);

        mOrganisationUnitProgramPickerPresenter.onCreate();
    }

    @Override
    public void renderPickers(List<Picker> pickers) {
        // super.setRootPickerList(pickers);
    }


    @Override
    public Context getContext() {
        return this.getContext();
    }
}
