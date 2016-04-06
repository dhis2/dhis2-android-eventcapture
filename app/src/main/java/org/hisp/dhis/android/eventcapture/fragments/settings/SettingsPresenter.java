package org.hisp.dhis.android.eventcapture.fragments.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.activities.login.LoginActivity;
import org.hisp.dhis.android.eventcapture.datasync.AppAccountManager;
import org.hisp.dhis.client.sdk.android.api.D2;

/**
 * This is the presenter, using MVP.
 * This class controls what is shown in the view. (AbsSettingsFragment).
 * <p>
 * Created by Vladislav Georgiev Alfredov on 1/15/16.
 */
public class SettingsPresenter implements ISettingsPresenter {
    public static final String CLASS_TAG = SettingsPresenter.class.getSimpleName();
    public final static String UPDATE_FREQUENCY = "update_frequency";
    public final static String BACKGROUND_SYNC = "background_sync";
    public final static String CRASH_REPORTS = "crash_reports";
    public final static String PREFS_NAME = "DHIS2";

    //Default values:
    public static final int DEFAULT_UPDATE_FREQUENCY = 1440; //one hour
    public static final Boolean DEFAULT_BACKGROUND_SYNC = true;
    public static final Boolean DEFAULT_CRASH_REPORTS = true;

    SettingsFragment mSettingsFragment;

    public SettingsPresenter(SettingsFragment callback) {
        mSettingsFragment = callback;
    }

    @Override
    public void logout(Context context) {
        // D2.signOut();
        D2.me().signOut();

        // ActivityUtils.changeDefaultActivity(context, true);
        context.startActivity(new Intent(mSettingsFragment.getActivity(), LoginActivity.class));

        //TODO: When loging out functionality works test the following:
        //log in with 1 user, log out and log in with another.
        //Now sync triggers twice, once for each account. But the app is only logged in with one.
        // Maybe we should remove the account before/during logging out ?
        //removeAccountExplicitly(Account account)
    }

    @Override
    public void synchronize(Context context) {
        //Log.d("SettingsPresenter", "Synchronize clicked, synchronizing...");
        AppAccountManager.getInstance().syncNow();
    }

    @Override
    public void setUpdateFrequency(Context context, int frequency) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(UPDATE_FREQUENCY, frequency);
        editor.apply();
        //Log.e(CLASS_TAG, "updateFrequency: " + frequency);

        AppAccountManager.getInstance().setPeriodicSync((long) (frequency * 60));
    }

    @Override
    public int getUpdateFrequency(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int updateFrequency = sharedPreferences.getInt(UPDATE_FREQUENCY, DEFAULT_UPDATE_FREQUENCY);
        //Log.e(CLASS_TAG, "updateFrequency: " + updateFrequency);
        return updateFrequency;
    }

    @Override
    public void setBackgroundSynchronisation(Context context, Boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BACKGROUND_SYNC, enabled);
        editor.apply();
        //Log.e(CLASS_TAG, "backgroundSync: " + enabled);

        if (enabled) {
            if (!ContentResolver.getMasterSyncAutomatically()) {
                //display a notification to the user to enable synchronization globally.
                mSettingsFragment.showMessage(
                        mSettingsFragment.getResources().getString(R.string.sys_sync_disabled_warning));
            }
            synchronize(context);
            AppAccountManager.getInstance().setPeriodicSync((long) getUpdateFrequency(context));
        } else {
            AppAccountManager.getInstance().removePeriodicSync();
        }
    }

    @Override
    public Boolean getBackgroundSynchronisation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Boolean enabled = sharedPreferences.getBoolean(BACKGROUND_SYNC, DEFAULT_BACKGROUND_SYNC);
        //Log.e(CLASS_TAG, "getBackroundSync : " + enabled);
        return enabled;
    }

    @Override
    public Boolean getCrashReports(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Boolean enabled = sharedPreferences.getBoolean(CRASH_REPORTS, DEFAULT_CRASH_REPORTS);
        Log.e(CLASS_TAG, " crash reports : " + enabled);
        return enabled;
    }

    @Override
    public void setCrashReports(Context context, Boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CRASH_REPORTS, enabled);
        editor.apply();
        Log.e(CLASS_TAG, " crash reports: " + enabled);
    }

    public void setSettingsFragment(SettingsFragment s) {
        mSettingsFragment = s;
    }
}
