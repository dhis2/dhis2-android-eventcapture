package org.hisp.dhis.android.eventcapture.views;

import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Pickable;

import java.util.List;

public interface OrganisationUnitProgramPickerView {

    void renderOrganisationUnitPickables(List<Pickable> organisationUnitPickables);

    void renderProgramPickables(List<Pickable> programPickables);

    void onFinishLoading();

    void onLoadingError();

    void onStartLoading();
}
