package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.mapper.OrganisationUnitPickableMapper;
import org.hisp.dhis.android.eventcapture.mapper.ProgramPickableMapper;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import java.util.List;

import rx.Observable;

public class OrganisationUnitProgramPickerPresenter extends AbsPresenter {

    private IOrganisationUnitProgramPickerView mOrganisationUnitProgramPickerView;
    private OrganisationUnitPickableMapper mOrganisationUnitPickableMapper;
    private ProgramPickableMapper mProgramPickableMapper;


    public OrganisationUnitProgramPickerPresenter() {
        mOrganisationUnitPickableMapper = new OrganisationUnitPickableMapper();
        mProgramPickableMapper = new ProgramPickableMapper();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.loadOrganisationUnits();
        this.loadPrograms();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }



    public void loadOrganisationUnits() {
        Observable<List<OrganisationUnit>> organisationUnits = D2.organisationUnits().list();
        setOrganisationUnitPickables(organisationUnits);
    }

    public void loadPrograms() {
        Observable<List<Program>> programs = D2.programs().list();
        setProgramPickables(programs);
    }

    public void setOrganisationUnitPickables(Observable<List<OrganisationUnit>> organisationUnits) {
        List<IPickable> organisationUnitPickables = mOrganisationUnitPickableMapper.transform(organisationUnits);
        mOrganisationUnitProgramPickerView.renderOrganisationUnitPickables(organisationUnitPickables);
//        mOrganisationUnitPicker.setPickableItems(organisationUnitPickables);
    }

    public void setProgramPickables(Observable<List<Program>> programs) {
        List<IPickable> programPickables = mProgramPickableMapper.transform(programs);
        mOrganisationUnitProgramPickerView.renderProgramPickables(programPickables);
//        mProgramPicker.setPickableItems(programPickables);
    }

    public void setOrganisationUnitProgramPickerView(IOrganisationUnitProgramPickerView mOrganisationUnitProgramPickerView) {
        this.mOrganisationUnitProgramPickerView = mOrganisationUnitProgramPickerView;
    }

}
