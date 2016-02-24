package org.hisp.dhis.android.eventcapture.presenters;


import android.support.v4.app.Fragment;
import android.view.View;

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.Picker;

import rx.Observable;

public interface ISelectorPresenter {
    void onCreate();

    void onResume();

    void initializeSynchronization();
}
