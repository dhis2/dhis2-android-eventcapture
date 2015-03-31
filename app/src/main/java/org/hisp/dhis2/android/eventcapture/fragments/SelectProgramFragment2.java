package org.hisp.dhis2.android.eventcapture.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

public class SelectProgramFragment2 extends Fragment {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();

    private CardTextViewButton mSelectOrgUnitButton;
    private CardTextViewButton mSelectProgramButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_program_2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mSelectOrgUnitButton = (CardTextViewButton) view.findViewById(R.id.select_organisation_unit);
        mSelectProgramButton = (CardTextViewButton) view.findViewById(R.id.select_program);
    }
}
