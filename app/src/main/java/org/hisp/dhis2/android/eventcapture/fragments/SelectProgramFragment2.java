package org.hisp.dhis2.android.eventcapture.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.hisp.dhis2.android.eventcapture.INavigationHandler;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.utils.ui.views.CardTextViewButton;

public class SelectProgramFragment2 extends Fragment
        implements View.OnClickListener, OrgUnitDialogFragment.OnOrgUnitSetListener,
        ProgramDialogFragment.OnProgramSetListener {
    public static final String TAG = SelectProgramFragment.class.getSimpleName();
    private static final String STATE = "state:SelectProgramFragment";

    private CardTextViewButton mOrgUnitButton;
    private CardTextViewButton mProgramButton;
    private Button mRegisterEventButton;

    private SelectProgramFragmentState mState;

    private INavigationHandler mNavigationHandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof INavigationHandler) {
            mNavigationHandler = (INavigationHandler) activity;
        } else {
            throw new IllegalArgumentException("Activity must implement INavigationHandler interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // we need to nullify reference
        // to parent activity in order not to leak it
        mNavigationHandler = null;
    }

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
        mRegisterEventButton = (Button) view.findViewById(R.id.register_new_event);

        mOrgUnitButton.setOnClickListener(this);
        mProgramButton.setOnClickListener(this);

        mOrgUnitButton.setEnabled(true);
        mProgramButton.setEnabled(false);
        mRegisterEventButton.setEnabled(false);

        if (savedInstanceState != null &&
                savedInstanceState.getParcelable(STATE) != null) {
            mState = savedInstanceState.getParcelable(STATE);
        }

        if (mState == null) {
            mState = new SelectProgramFragmentState();
        }

        onRestoreState(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_select_program, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mNavigationHandler.switchFragment(
                    new SettingsFragment(), SettingsFragment.TAG);
        } else if (id == R.id.action_new_event) {
            Toast.makeText(getActivity(), "Another button", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(STATE, mState);
        super.onSaveInstanceState(out);
    }

    public void onRestoreState(boolean hasUnits) {
        mOrgUnitButton.setEnabled(hasUnits);
        if (!hasUnits) {
            return;
        }

        SelectProgramFragmentState backedUpState = new SelectProgramFragmentState(mState);
        if (!backedUpState.isOrgUnitEmpty()) {
            onUnitSelected(
                    backedUpState.getOrgUnitId(),
                    backedUpState.getOrgUnitLabel()
            );

            if (!backedUpState.isProgramEmpty()) {
                onProgramSelected(
                        backedUpState.getProgramId(),
                        backedUpState.getProgramName()
                );
            }
        }
    }

    @Override
    public void onUnitSelected(String orgUnitId, String orgUnitLabel) {
        mOrgUnitButton.setText(orgUnitLabel);
        mProgramButton.setEnabled(true);

        mState.setOrgUnit(orgUnitId, orgUnitLabel);
        mState.resetProgram();
        handleViews(0);
    }

    @Override
    public void onProgramSelected(String programId, String programName) {
        mProgramButton.setText(programName);

        mState.setProgram(programId, programName);
        handleViews(1);
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

    private void handleViews(int level) {
        switch (level) {
            case 0:
                mRegisterEventButton.setEnabled(false);
                break;
            case 1:
                mRegisterEventButton.setEnabled(true);
        }
    }
}
