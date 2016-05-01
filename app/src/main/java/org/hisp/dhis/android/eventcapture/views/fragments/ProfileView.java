package org.hisp.dhis.android.eventcapture.views.fragments;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;

import java.util.List;

public interface ProfileView extends View {
    void showUserAccountFields(List<DataEntity> dataEntities);
}
