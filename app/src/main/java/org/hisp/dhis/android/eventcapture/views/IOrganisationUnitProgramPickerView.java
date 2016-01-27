package org.hisp.dhis.android.eventcapture.views;

import android.content.Context;

import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import java.util.List;

public interface IOrganisationUnitProgramPickerView {

    void renderOrganisationUnitPickables(List<IPickable> organisationUnitPickables);

    void renderProgramPickables(List<IPickable> programPickables);
}
