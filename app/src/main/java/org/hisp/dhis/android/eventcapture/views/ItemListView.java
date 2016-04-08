package org.hisp.dhis.android.eventcapture.views;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;

import java.util.List;

public interface ItemListView {

    /**
     * Render itemrowlist in the UI.
     *
     * @param itemListRowCollection The collection of {@link ItemListRow} that will be shown.
     */
    void renderItemRowList(List<ItemListRow> itemListRowCollection);

    /**
     * View and edit an {@link Event} data entry model.
     *
     * @param object The user that will be shown.
     */
    void viewEditEvent(Object object);

    void hideViewRetry();

    void showViewRetry();

    void hideViewLoading();

    void showViewLoading();

    void showErrorMessage(String error);

}
