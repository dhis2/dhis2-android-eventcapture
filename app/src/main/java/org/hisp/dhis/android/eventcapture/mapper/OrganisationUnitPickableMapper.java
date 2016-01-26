package org.hisp.dhis.android.eventcapture.mapper;


import org.hisp.dhis.android.eventcapture.views.OrganisationUnitPickable;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hisp.dhis.client.sdk.ui.utils.Preconditions.isNull;

public class OrganisationUnitPickableMapper {


    public IPickable transform(OrganisationUnit organisationUnit) {
        isNull(organisationUnit, "Org unit must not be null");

        IPickable organisationUnitPickable = new OrganisationUnitPickable(organisationUnit.getName(), organisationUnit.getUId());
        return organisationUnitPickable;
    }

    public List<IPickable> transform(List<OrganisationUnit> organisationUnits) {
        List<IPickable> organisationUnitPickables = new ArrayList<>();

        for(OrganisationUnit organisationUnit : organisationUnits) {
            IPickable organisationUnitPickable = transform(organisationUnit);
            organisationUnitPickables.add(organisationUnitPickable);
        }

        return organisationUnitPickables;
    }

    public List<IPickable> transform(Observable<List<OrganisationUnit>> organisationUnits) {
        List<IPickable> organisationUnitPickables = new ArrayList<>();

        OrganisationUnit matSouth = new OrganisationUnit();
        matSouth.setName("Matabeleland South");
        matSouth.setUId("abc123");

        OrganisationUnit mashWest = new OrganisationUnit();
        mashWest.setName("Mash west");
        mashWest.setUId("def345");

        OrganisationUnit matNorth = new OrganisationUnit();
        matNorth.setName("Matabeleland North");
        matNorth.setUId("ghi678");

        organisationUnitPickables.add(transform(matNorth));
        organisationUnitPickables.add(transform(matSouth));
        organisationUnitPickables.add(transform(mashWest));

        return organisationUnitPickables;
    }
}
