package org.hisp.dhis.android.eventcapture.views.fragments;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.DataEntity2;

import java.util.List;

public interface ProfileView2 extends View {
    void showUserAccountFields(List<DataEntity2<String>> dataEntities);
}
