package org.hisp.dhis.android.eventcapture.fragments.dataentry;

import org.hisp.dhis.client.sdk.ui.models.IDataEntity;

import java.util.List;

public interface IEventDataEntryView {
    void setDataEntryFields(List<IDataEntity> dataEntities);
}
