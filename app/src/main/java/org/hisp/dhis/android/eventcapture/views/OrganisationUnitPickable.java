/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.android.eventcapture.views;

import android.os.Parcel;

import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import rx.Observable;

public class OrganisationUnitPickable implements IPickable {

    String mId;
    String mLabel;

    public OrganisationUnitPickable(String label, String id) {
        this.mLabel = label;
        this.mId = id;
    }

    @Override
    public String toString() {
        return mLabel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{mLabel, mId});
    }

    public OrganisationUnitPickable(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        this.mLabel = data[0];
        this.mId = data[1];
    }

    public static final Creator<OrganisationUnitPickable> CREATOR = new Creator<OrganisationUnitPickable>() {
        @Override
        public OrganisationUnitPickable createFromParcel(Parcel in) {
            return new OrganisationUnitPickable(in);
        }

        @Override
        public OrganisationUnitPickable[] newArray(int size) {
            return new OrganisationUnitPickable[size];
        }
    };

    public Observable<OrganisationUnit> getOrganisationUnit() {
        // return D2.organisationUnits().get(mId);
        return null;
    }
}
