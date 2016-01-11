package org.hisp.dhis.android.eventcapture.fragments;

import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Pickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.List;

public class SelectorPresenter implements ISelectorPresenter {
    private PickerFragment mPickerFragment;
    private ISelectorView mSelectorView;

    public SelectorPresenter(ISelectorView selectorView) {
        this.mSelectorView = selectorView;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public Fragment createPickerFragment() {

        //        Observable<List<OrganisationUnit>> organisationUnits = Observable.create(new Observable.OnSubscribe<List<OrganisationUnit>>() {
//            @Override
//            public void call(final Subscriber<? super List<OrganisationUnit>> subscriber) {
//                subscriber.onNext(D2.organisationUnit().list());
//            }
//        });
        List<Picker> pickerList = new ArrayList<>();
        List<IPickable> orgUnitPickables = new ArrayList<>();
        orgUnitPickables.add(new Pickable("Matabeleland South", "0"));
        orgUnitPickables.add(new Pickable("Mash west", "1"));
        orgUnitPickables.add(new Pickable("Matabeleland North", "2"));

        List<IPickable> programPickables = new ArrayList<>();
        programPickables.add(new Pickable("Malaria Programme", "01"));
        programPickables.add(new Pickable("TB Programme", "02"));
        programPickables.add(new Pickable("Entolomogical Investigation", "03"));

        Picker orgUnitPicker = new Picker(orgUnitPickables, "Organisation Units", OrganisationUnit.class.getName());
        Picker programPicker = new Picker(programPickables, "Programs", Program.class.getName());

        orgUnitPicker.setNextLinkedSibling(programPicker);

        pickerList.add(orgUnitPicker);

        return PickerFragment.newInstance(pickerList);
    }

    @Override
    public void onResume() {

    }
}
