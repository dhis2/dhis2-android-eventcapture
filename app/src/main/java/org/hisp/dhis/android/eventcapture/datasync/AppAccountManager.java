package org.hisp.dhis.android.eventcapture.datasync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.hisp.dhis.client.sdk.ui.SettingPreferences;

/**
 * A singleton class to abstract/wrap and simplify interactions with Account in relation to synchronizing.
 */
public class AppAccountManager {

    private static AppAccountManager instance;
    public static final String AUTHORITY = "org.hisp.dhis.android.eventcapture.datasync.provider";
    public static final String ACCOUNT_TYPE = "example.com";
    public static String accountName = "default dhis2 account";

    private static int defaultUpdateFrequency = 30; //in minutes


    private Account mAccount;

    public static AppAccountManager getInstance() {
        if(instance == null) {
            instance = new AppAccountManager();
        }
        return instance;
    }

    private AppAccountManager() {

    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public void createAccount(Context context, String username) {
        if(username != null) {
            accountName = username;
        }
        mAccount = createAccount(context);
        initSyncAccount(context);
    }


    private Account createAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(accountName, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);

        Boolean doesntExist = accountManager.addAccountExplicitly(newAccount, null, null);
        if (doesntExist) {
            /* If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.*/
            return newAccount;
        } else {
            /* The account exists or some other error occurred. Log this, report it,
             * or handle it internally.*/
            Account all[] = accountManager.getAccountsByType(ACCOUNT_TYPE);
            for (Account found : all) {
                if (found.equals(newAccount)) {
                    return found;
                }
            }
        }
        return null; //Not in accounts and existing. this shouldn't happen.
    }

    public void initSyncAccount(Context context) {

        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);

        SettingPreferences.init(context);
        if(SettingPreferences.backgroundSynchronization()) {
            long interval = Long.parseLong(SettingPreferences.synchronizationPeriod());
            Log.d("LoginPresenter", "Initializing sync account, sync interval = " + interval);
            //long interval = 5;

            ContentResolver.addPeriodicSync(
                    mAccount,
                    AUTHORITY,
                    Bundle.EMPTY,
                    interval);
        }
    }

    /**
     * A method to set the sync period for the app.
     *
     * @param interval in seconds
     */
    public void setPeriodicSync(Long interval) {

        //TODO: test if this is nessesary:
        //ContentResolver.removePeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY);

        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                interval);

        /*Account mAccount;
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);

        Account all[] = accountManager.getAccounts();
        for (Account account : all) {
            ContentResolver.addPeriodicSync(
                    account,
                    LogInPresenter.AUTHORITY,
                    Bundle.EMPTY,
                    frequency * 60);
        }*/
    }

    /**
     * Manually sync by calling requestSync().
     * This is an asynchronous operation.
     */
    public void syncNow() {

        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }

}
