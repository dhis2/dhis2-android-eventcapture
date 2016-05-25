package org.hisp.dhis.android.eventcapture.views.fragments;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.rules.RuleEffect;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;
import org.hisp.dhis.client.sdk.ui.models.FormEntityAction;

import java.util.List;

public interface DataEntryView extends View {
    void showDataEntryForm(List<FormEntity> formEntities, List<FormEntityAction> actions);

    void updateDataEntryForm(List<FormEntityAction> formEntityActions);
}
