package org.hisp.dhis.android.eventcapture.fragments.selector;


import android.content.Context;
import android.content.SharedPreferences;

import static org.hisp.dhis.client.sdk.ui.utils.Preconditions.isNull;

public final class SelectorFragmentPreferences {
    private final SharedPreferences mPrefs;
    private static final String PROGRAM_FRAGMENT_PREFERENCES = "preferences:programFragment";

    private static final String ORG_UNIT_ID = "key:orgUnitId";
    private static final String ORG_UNIT_LABEL = "key:orgUnitLabel";

    private static final String PROGRAM_ID = "key:programId";
    private static final String PROGRAM_LABEL = "key:programLabel";

    public SelectorFragmentPreferences(Context context) {
        isNull(context, "Context object must not be null");
        mPrefs = context.getSharedPreferences(
                PROGRAM_FRAGMENT_PREFERENCES, Context.MODE_PRIVATE);
    }

}
