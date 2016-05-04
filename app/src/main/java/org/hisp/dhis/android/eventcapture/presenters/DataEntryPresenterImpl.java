package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.DataEntryView;
import org.hisp.dhis.client.sdk.android.program.ProgramStageSectionInteractor;
import org.hisp.dhis.client.sdk.utils.Logger;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DataEntryPresenterImpl implements DataEntryPresenter {
    private final ProgramStageSectionInteractor programStageSectionInteractor;
    private final Logger logger;

    private DataEntryView dataEntryView;

    public DataEntryPresenterImpl(ProgramStageSectionInteractor sectionInteractor, Logger logger) {
        this.programStageSectionInteractor = sectionInteractor;
        this.logger = logger;
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dataEntryView = (DataEntryView) view;
    }

    @Override
    public void detachView() {
        dataEntryView = null;
    }

    @Override
    public void createDataEntryForm(String programStageSectionId) {
        // build the form
    }
}
