package org.hisp.dhis.android.eventcapture.fragments;


import android.content.Context;
import android.content.SharedPreferences;

import static org.hisp.dhis.client.sdk.ui.utils.Preconditions.isNull;

public final class SelectorFragmentPreferences {
    private final SharedPreferences mPrefs;
    private static final String PROGRAM_FRAGMENT_PREFERENCES = "preferences:programFragment";

    private static final String ORG_UNIT_ID = "key:orgUnitId";
    private static final String ORG_UNIT_LABEL = "key:orgUnitLabel";

    private static final String ORG_UNIT_SELECTED_ID = "orgUnitSelectedId";
    private static final String ORG_UNIT_SELECTED_VALUE = "orgUnitSelectedValue";

    private static final String PROGRAM_ID = "key:programId";
    private static final String PROGRAM_LABEL = "key:programLabel";

    private static final String PROGRAM_SELECTED_ID = "programSelectedId";
    private static final String PROGRAM_SELECTED_VALUE = "programSelectedValue";

    public SelectorFragmentPreferences(Context context) {
        isNull(context, "Context object must not be null");
        mPrefs = context.getSharedPreferences(
                PROGRAM_FRAGMENT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String getOrgUnitSelectedId() {
        return mPrefs.getString(ORG_UNIT_SELECTED_ID, "");
    }

    public void setOrgUnitSelectedId(String id) {
        mPrefs.edit().putString(ORG_UNIT_SELECTED_ID, id).apply();
    }

    public String getOrgUnitSelectedValue() {
        return mPrefs.getString(ORG_UNIT_SELECTED_VALUE, "");
    }

    public void setOrgUnitSelectedValue(String value) {
        mPrefs.edit().putString(ORG_UNIT_SELECTED_VALUE, value).apply();
    }

    public String getProgramSelectedId() {
        return mPrefs.getString(PROGRAM_SELECTED_ID, "");
    }

    public void setProgramSelectedId(String id) {
        mPrefs.edit().putString(PROGRAM_SELECTED_ID, id).apply();
    }

    public String getProgramSelectedValue() {
        return mPrefs.getString(PROGRAM_SELECTED_VALUE, "");
    }

    public void setProgramSelectedValue(String value) {
        mPrefs.edit().putString(PROGRAM_SELECTED_VALUE, value).apply();
    }
}
