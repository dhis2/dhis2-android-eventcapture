package org.hisp.dhis.android.eventcapture.fragments.picker;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.fragments.selector.OnAllPickersSelectedListener;
import org.hisp.dhis.android.eventcapture.presenters.OrganisationUnitProgramPickerPresenter;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitPickableListener;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class OrganisationUnitProgramPickerFragment extends PickerFragment implements IOrganisationUnitProgramPickerView {
    private OrganisationUnitProgramPickerPresenter mOrganisationUnitProgramPickerPresenter;
    private Picker mProgramPicker;
    private Picker mOrganisationUnitPicker;
    private OnAllPickersSelectedListener onPickerClickedListener;
    private ISelectorView selectorView;
    private IOrganisationUnitPickableListener organisationUnitPickableListener;


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
        mOrganisationUnitPicker.setListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mOrganisationUnitPicker.getPickedItem() != null) {
                    OrganisationUnitPickable organisationUnitPickable = (OrganisationUnitPickable) mOrganisationUnitPicker.getPickableItems();
                    mOrganisationUnitProgramPickerPresenter.loadPrograms(organisationUnitPickable.getOrganisationUnit().toBlocking().first());
                }
            }
        });
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

    @Override
    public void onFinishLoading() {
        Timber.d("onFinishLoading from orgUnitProgramFrag");
        selectorView.onFinishLoading();
    }

    @Override
    public void onLoadingError() {
        Timber.d("onLoadingError from orgUnitProgramFrag");
        selectorView.onLoadingError();
    }

    @Override
    public void onStartLoading() {
        Timber.d("onStartLoading from orgUnitProgramFrag");
        selectorView.onStartLoading();
    }


    public void setOnPickerClickedListener(OnAllPickersSelectedListener onPickerClickedListener) {
        this.onPickerClickedListener = onPickerClickedListener;
    }

    public void setSelectorView(ISelectorView selectorView) {
        this.selectorView = selectorView;
    }

}
