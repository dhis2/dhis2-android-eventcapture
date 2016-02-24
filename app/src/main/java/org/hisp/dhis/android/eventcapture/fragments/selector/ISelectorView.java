package org.hisp.dhis.android.eventcapture.fragments.selector;

import android.support.v4.app.Fragment;

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

import rx.Observable;

public interface ISelectorView {
    void onFinishLoading();

    void onLoadingError();

    void onStartLoading();

    void onPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnitObservable);

    void onPickedProgram(Observable<Program> programObservable);
}
