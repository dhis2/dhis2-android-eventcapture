package org.hisp.dhis.android.eventcapture.views.fragments;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

import java.util.List;

public interface DataEntryView extends View {
    void showDataEntryForm(List<FormEntity> formEntities);
}
