package org.hisp.dhis.android.eventcapture.fragments;

import android.os.Parcel;
import android.os.Parcelable;

import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import rx.Observable;

public class ProgramPickable implements IPickable {
    String mId;
    String mLabel;

    public ProgramPickable(String label, String id) {
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

    public ProgramPickable(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        this.mLabel = data[0];
        this.mId = data[1];
    }

    public static final Parcelable.Creator<ProgramPickable> CREATOR = new Parcelable.Creator<ProgramPickable>() {
        @Override
        public ProgramPickable createFromParcel(Parcel in) {
            return new ProgramPickable(in);
        }

        @Override
        public ProgramPickable[] newArray(int size) {
            return new ProgramPickable[size];
        }
    };

    public Observable<Program> getProgram() {
        return D2.program().get(mId);
    }

}
