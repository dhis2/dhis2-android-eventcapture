package org.hisp.dhis2.android.eventcapture.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

public class SelectProgramFragment2 extends Fragment
        implements View.OnClickListener, OrgUnitDialogFragment.OnOrgUnitSetListener,
        ProgramDialogFragment.OnProgramSetListener {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    private static final String STATE = "state:SelectProgramFragment";

    private CardTextViewButton mOrgUnitButton;
    private CardTextViewButton mProgramButton;

    private SelectProgramFragmentState mState;

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
        mOrgUnitButton = (CardTextViewButton) view.findViewById(R.id.select_organisation_unit);
        mProgramButton = (CardTextViewButton) view.findViewById(R.id.select_program);

        mOrgUnitButton.setOnClickListener(this);
        mProgramButton.setOnClickListener(this);

        mOrgUnitButton.setEnabled(true);
        mProgramButton.setEnabled(false);

        if (savedInstanceState != null &&
                savedInstanceState.getParcelable(STATE) != null) {
            mState = savedInstanceState.getParcelable(STATE);
        }

        if (mState == null) {
            mState = new SelectProgramFragmentState();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_select_program, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(STATE, mState);
        super.onSaveInstanceState(out);
    }

    @Override
    public void onUnitSelected(String orgUnitId, String orgUnitLabel) {
        mOrgUnitButton.setText(orgUnitLabel);
        mProgramButton.setEnabled(true);

        mState.setOrgUnit(orgUnitId, orgUnitLabel);
        mState.resetProgram();
    }

    @Override
    public void onProgramSelected(String programId, String programName) {
        mProgramButton.setText(programName);

        mState.setProgram(programId, programName);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_organisation_unit: {
                OrgUnitDialogFragment fragment = OrgUnitDialogFragment
                        .newInstance(this);
                fragment.show(getChildFragmentManager());
                break;
            }
            case R.id.select_program: {
                ProgramDialogFragment fragment = ProgramDialogFragment
                        .newInstance(this, mState.getOrgUnitId());
                fragment.show(getChildFragmentManager());
                break;
            }
            /*
            case R.id.data_entry_button: {
                startReportEntryActivity();
                break;
            }
            */
        }
    }
}
