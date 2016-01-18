package org.hisp.dhis.android.eventcapture.fragments.selector;

import android.content.Intent;
import android.view.View;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.activities.login.LogInActivity;
import org.hisp.dhis.android.eventcapture.utils.ActivityUtils;
import org.hisp.dhis.client.sdk.ui.fragments.AbsSettingsFragment;

/**
 *
 * Created by Vladislav Georgiev Alfredov on 1/18/16.
 */
public class SettingsFragment extends AbsSettingsFragment {
    public static final String TAG = AbsSettingsFragment.class.getSimpleName();

    public SettingsFragment() {
        mSettingsPresenter = new SettingsPresenter(this);
        setSettingsPresenter(mSettingsPresenter);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        if (view.getId() == R.id.settings_logout_button) {
            ActivityUtils.changeDefaultActivity(getContext(), true);
            startActivity(new Intent(getActivity(), LogInActivity.class));
        }
    }
}