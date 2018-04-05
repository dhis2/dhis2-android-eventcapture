package org.hisp.dhis.android.eventcapture.fragments.dialogs;

import android.os.Bundle;

import org.hisp.dhis.android.sdk.persistence.models.BaseSerializableModel;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;

/**
 * Created by erling on 9/21/15.
 */
public class ItemStatusDialogFragment extends org.hisp.dhis.android.sdk.ui.dialogs.ItemStatusDialogFragment
{


    public static ItemStatusDialogFragment newInstance(BaseSerializableModel item) {
        ItemStatusDialogFragment dialogFragment = new ItemStatusDialogFragment();
        Bundle args = new Bundle();

        args.putLong(EXTRA_ID, item.getLocalId());
        if (item instanceof Event) {
            args.putString(EXTRA_TYPE, FailedItem.EVENT);
        }

        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void sendToServer(BaseSerializableModel item, org.hisp.dhis.android.sdk.ui.dialogs.ItemStatusDialogFragment fragment) {
        if (item instanceof Event) {
            Event event = (Event) item;
            sendEvent(event);
        }
    }
}
