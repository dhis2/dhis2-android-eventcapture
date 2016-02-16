package org.hisp.dhis.android.eventcapture.fragments.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.activities.login.LogInActivity;
import org.hisp.dhis.android.eventcapture.activities.login.LogInPresenter;
import org.hisp.dhis.android.eventcapture.datasync.AppAccountManager;
import org.hisp.dhis.android.eventcapture.utils.ActivityUtils;
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.ui.fragments.AbsSettingsFragment;

/**
 * This is the presenter, using MVP.
 * This class controls what is shown in the view. (AbsSettingsFragment).
 * <p/>
 * Created by Vladislav Georgiev Alfredov on 1/15/16.
 */
public class SettingsPresenter implements ISettingsPresenter {
    public static final String CLASS_TAG = SettingsPresenter.class.getSimpleName();
    public final static String UPDATE_FREQUENCY = "update_frequency";
    public final static String BACKGROUND_SYNC = "background_sync";
    public final static String CRASH_REPORTS = "crash_reports";
    public final static String PREFS_NAME = "DHIS2";

    public static final int FREQUENCY_ONE_MINUTE = 0;
    public static final int FREQUENCY_15_MINTUES = 1;
    public static final int FREQUENCY_ONE_HOUR = 2;
    public static final int FREQUENCY_ONE_DAY = 3;
    public static final int FREQUENCY_DISABLED = 4;
    public static final int DEFAULT_UPDATE_FREQUENCY = FREQUENCY_ONE_HOUR;
    public static final Boolean DEFAULT_BACKGROUND_SYNC = true;
    public static final Boolean DEFAULT_CRASH_REPORTS = true;

    AbsSettingsFragment mSettingsFragment;

    public SettingsPresenter(AbsSettingsFragment callback) {
        mSettingsFragment = callback;
    }

    @Override
    public void logout(Context context) {
        D2.signOut();
        ActivityUtils.changeDefaultActivity(context, true);
        context.startActivity(new Intent(mSettingsFragment.getActivity(), LogInActivity.class));
    }

    @Override
    public void synchronize(Context context) {
        Log.d("SettingsPresenter", "Synchronize clicked.");
    }

    @Override
    public void setUpdateFrequency(Context context, int frequency) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(UPDATE_FREQUENCY, frequency);
        editor.apply();
        Log.e(CLASS_TAG, "updateFrequency: " + frequency);

        AppAccountManager.getInstance().setPeriodicSync((long) frequency * 60);

        Log.d("SettingsFragmeent", "sync period = " + frequency + " in seconds: " + (frequency * 60));

    }

    @Override
    public int getUpdateFrequency(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int updateFrequency = sharedPreferences.getInt(UPDATE_FREQUENCY, DEFAULT_UPDATE_FREQUENCY);
        Log.e(CLASS_TAG, "updateFrequency: " + updateFrequency);
        return updateFrequency;
    }

    @Override
    public void setBackgroundSynchronisation(Context context, Boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BACKGROUND_SYNC, enabled);
        editor.apply();
        Log.e(CLASS_TAG, "backgroundSync: " + enabled);
    }

    @Override
    public Boolean getBackgroundSynchronisation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Boolean enabled = sharedPreferences.getBoolean(BACKGROUND_SYNC, DEFAULT_BACKGROUND_SYNC);
        Log.e(CLASS_TAG, "getBackroundSync : " + enabled);
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

    public void setSettingsFragment(AbsSettingsFragment s) {
        mSettingsFragment = s;
    }
}
