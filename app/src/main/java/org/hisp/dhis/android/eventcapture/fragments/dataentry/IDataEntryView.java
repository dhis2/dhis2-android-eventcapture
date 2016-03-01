package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.client.sdk.ui.models.DataEntity;

import java.util.List;

public interface IDataEntryView {
    void setDataEntryFields(List<DataEntity> dataEntities);
}
