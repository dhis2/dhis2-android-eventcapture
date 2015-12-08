/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.fragments.selectprogram;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import org.hisp.dhis.android.sdk.common.D2;
import org.hisp.dhis.java.sdk.models.organisationunit.OrganisationUnit;

import rx.Observable;

import static org.hisp.dhis.java.sdk.models.utils.Preconditions.isNull;

public final class SelectProgramFragmentPreferences {
    private static final String PROGRAM_FRAGMENT_PREFERENCES = "preferences:programFragment";

    private static final String ORG_UNIT_ID = "key:orgUnitId";
    private static final String ORG_UNIT_LABEL = "key:orgUnitLabel";

    private static final String PROGRAM_ID = "key:programId";
    private static final String PROGRAM_LABEL = "key:programLabel";

    private final SharedPreferences mPrefs;

    public SelectProgramFragmentPreferences(Context context) {
        isNull(context, "Context object must not be null");
        mPrefs = context.getSharedPreferences(
                PROGRAM_FRAGMENT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void putOrgUnit(Pair<String, String> orgUnit) {
        if (orgUnit != null) {
            put(ORG_UNIT_ID, orgUnit.first);
            put(ORG_UNIT_LABEL, orgUnit.second);
        } else {
            remove(ORG_UNIT_ID);
            remove(ORG_UNIT_LABEL);
        }
    }

    public Pair<String, String> getOrgUnit() {
        String orgUnitId = get(ORG_UNIT_ID);
        String orgUnitLabel = get(ORG_UNIT_LABEL);

        // we need to make sure that last selected
        // organisation unit still exists in database
        Observable<OrganisationUnit> organisationUnitObservable = D2.organisationUnits().get(orgUnitId);
        OrganisationUnit organisationUnit = null;
        if (organisationUnit != null) {
            return new Pair<>(orgUnitId, orgUnitLabel);
        } else {
            putOrgUnit(null);
            putProgram(null);
            return null;
        }
    }

    public void putProgram(Pair<String, String> program) {
        if (program != null) {
            put(PROGRAM_ID, program.first);
            put(PROGRAM_LABEL, program.second);
        } else {
            remove(PROGRAM_ID);
            remove(PROGRAM_LABEL);
        }
    }

    public Pair<String, String> getProgram() {
        String orgUnitId = get(ORG_UNIT_ID);
        String programId = get(PROGRAM_ID);
        String programLabel = get(PROGRAM_LABEL);

        // we need to make sure that last selected program for particular
        // organisation unit is still in database and assigned to selected organisation unit
        long count = 1;
        if (count > 0) {
            return new Pair<>(programId, programLabel);
        } else {
            putProgram(null);
            return null;
        }
    }

    private void put(String key, String value) {
        mPrefs.edit().putString(key, value).apply();
    }

    private String get(String key) {
        return mPrefs.getString(key, null);
    }

    private void delete() {
        mPrefs.edit().clear().apply();
    }

    private void remove(String key) {
        mPrefs.edit().remove(key).apply();
    }
}
