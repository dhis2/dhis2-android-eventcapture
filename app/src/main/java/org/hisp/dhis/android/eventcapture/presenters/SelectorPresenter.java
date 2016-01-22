package org.hisp.dhis.android.eventcapture.presenters;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

import org.hisp.dhis.android.eventcapture.fragments.selector.INewButtonActivator;
import org.hisp.dhis.android.eventcapture.fragments.selector.ISelectorView;
import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.android.eventcapture.views.ProgramPickable;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.ItemListFragment;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickableItemClearListener;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.ArrayList;
import java.util.List;

public class SelectorPresenter implements ISelectorPresenter, IPickableItemClearListener {

    private PickerFragment mPickerFragment;
    private ItemListFragment mItemListFragment;
    private ISelectorView mSelectorView;
    private INewButtonActivator mNewButtonActivator;

    private static Picker mOrgUnitPicker;
    private static Picker mProgramPicker;
    private List<IPickable> mOrgUnitPickables;
    private List<IPickable> mProgramPickables;


    public SelectorPresenter(ISelectorView selectorView, INewButtonActivator newButtonActivator) {
        this.mSelectorView = selectorView;
        this.mNewButtonActivator = newButtonActivator;
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
        OrganisationUnit matSouth = new OrganisationUnit();
        matSouth.setName("Matabeleland South");
        matSouth.setUId("abc123");

        OrganisationUnit mashWest = new OrganisationUnit();
        mashWest.setName("Mash west");
        mashWest.setUId("def345");

        OrganisationUnit matNorth = new OrganisationUnit();
        matNorth.setName("Matabeleland North");
        matNorth.setUId("ghi678");
        mOrgUnitPickables = new ArrayList<>();
        mOrgUnitPickables.add(new OrganisationUnitPickable(matSouth.getName(), matSouth.getUId()));
        mOrgUnitPickables.add(new OrganisationUnitPickable(mashWest.getName(), mashWest.getUId()));
        mOrgUnitPickables.add(new OrganisationUnitPickable(matNorth.getName(), matNorth.getUId()));
//        mOrgUnitPickables.add(new Pickable("Matabeleland South", "0"));
//        mOrgUnitPickables.add(new Pickable("Mash west", "1"));
//        mOrgUnitPickables.add(new Pickable("Matabeleland North", "2"));

        Program malariaProgramme = new Program();
        malariaProgramme.setName("Malaria Programme");
        malariaProgramme.setUId("aaa123");

        Program tbProgramme = new Program();
        tbProgramme.setName("TB Programme");
        tbProgramme.setUId("bbb123");

        Program entolomogicalInvestigation = new Program();
        entolomogicalInvestigation.setName("Entolomogical Investigation");
        entolomogicalInvestigation.setUId("ccc123");

        mProgramPickables = new ArrayList<>();
        mProgramPickables.add(new ProgramPickable(malariaProgramme.getName(), malariaProgramme.getUId()));
        mProgramPickables.add(new ProgramPickable(tbProgramme.getName(), tbProgramme.getUId()));
        mProgramPickables.add(new ProgramPickable(entolomogicalInvestigation.getName(), entolomogicalInvestigation.getUId()));
//        mProgramPickables.add(new Pickable("Malaria Programme", "01"));
//        mProgramPickables.add(new Pickable("TB Programme", "02"));
//        mProgramPickables.add(new Pickable("Entolomogical Investigation", "03"));

        mOrgUnitPicker = new Picker(mOrgUnitPickables, "Organisation Units", OrganisationUnit.class.getName());

        mProgramPicker = new Picker(mProgramPickables, "Programs", Program.class.getName());

        this.registerPickerCallbacks();

        mOrgUnitPicker.setNextLinkedSibling(mProgramPicker);
        pickerList.add(mOrgUnitPicker);

        mPickerFragment = PickerFragment.newInstance(pickerList);

        return mPickerFragment;
    }

    @Override
    public Fragment createItemListFragment() {
        mItemListFragment = ItemListFragment.newInstance();


        return mItemListFragment;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void registerPickerCallbacks() {
        mProgramPicker.setListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (mProgramPicker.getPickedItem() != null) {
                            mNewButtonActivator.activate();
                        } else {
                            mNewButtonActivator.deactivate();
                        }

                    }
                }

        );
        mProgramPicker.registerPickedItemClearListener(this);
        //mOrgUnitPicker.registerPickedItemClearListener(this); //implicit in picker.
    }

    @Override
    public Picker getProgramPicker() {
        return mProgramPicker;
    }
    @Override
    public Picker getOrganisationUnitPicker() {
        return mOrgUnitPicker;
    }

    @Override
    public void clearedCallback() {
        mNewButtonActivator.deactivate();
    }

}
