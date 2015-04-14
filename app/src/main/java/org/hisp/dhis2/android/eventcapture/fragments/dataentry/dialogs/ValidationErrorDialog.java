package org.hisp.dhis2.android.eventcapture.fragments.dataentry.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.ValidationErrorAdapter;

import java.util.ArrayList;

public final class ValidationErrorDialog extends DialogFragment
        implements View.OnClickListener {
    private static final String TAG = ValidationErrorDialog.class.getSimpleName();
    private static final String ERRORS_LIST_EXTRA = "extra:ErrorsList";

    private ListView mListView;
    private Button mButton;
    private ValidationErrorAdapter mAdapter;

    public static ValidationErrorDialog newInstance(ArrayList<String> errors) {
        ValidationErrorDialog dialog = new ValidationErrorDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ERRORS_LIST_EXTRA, errors);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.dialog_fragment_validation_errors, container, false
        );
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.simple_listview);
        //mButton = (Button) view.findViewById(R.id.ok_button);

        mAdapter = new ValidationErrorAdapter(
                LayoutInflater.from(getActivity().getBaseContext()));
        mListView.setAdapter(mAdapter);
        //mButton.setOnClickListener(this);
        if (getArguments() != null) {
            mAdapter.swapData(getArguments()
                    .getStringArrayList(ERRORS_LIST_EXTRA));
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
