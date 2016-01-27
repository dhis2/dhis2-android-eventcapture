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
}
