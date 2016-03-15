package org.hisp.dhis.android.eventcapture.fragments.selector;

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;

import rx.Observable;

public interface ISelectorView {
    void onFinishLoading();

    void onLoadingError(Throwable throwable);

    void onStartLoading();

    void onPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnitObservable);

    void onPickedProgram(Observable<Program> programObservable);
}
