package org.hisp.dhis.android.eventcapture.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.android.user.CurrentUserInteractor;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.utils.Logger;

/**
 * A singleton class to abstract/wrap and simplify interactions with Account in relation to synchronizing.
 */

public class AppAccountManagerImpl implements AppAccountManager {

    private final String TAG = AppAccountManagerImpl.class.getSimpleName();

    private final Logger logger;
    private final Context appContext;
    private final AppPreferences appPreferences;
    private final CurrentUserInteractor currentUserInteractor;

    private String authority;
    private Account account;

    public AppAccountManagerImpl(Context context, AppPreferences appPreferences, CurrentUserInteractor currentUserInteractor, Logger logger) {
        ((EventCaptureApp) context.getApplicationContext()).getUserComponent().inject(this);
        this.appContext = context;
        this.appPreferences = appPreferences;
        this.currentUserInteractor = currentUserInteractor;
        this.logger = logger;
        init(context);
    }

    private void init(Context context) {

        if (currentUserInteractor == null || !currentUserInteractor.isSignedIn().toBlocking().first()) {
            logger.i(TAG, "No syncing performed: User is not signed in. CurrentUserInteractor is null or CurrentUserInteractor.isSignedIn() returned false");
            return;
        }

        String accountType = context.getString(R.string.account_type);

        try {
            account = fetchOrCreateAccount(accountType);
            authority = context.getString(R.string.authority);
            initPeriodicSync();
        } catch (Exception e) {
            logger.i(TAG, "Init error", e);
        }

    }

    private Account fetchOrCreateAccount(String accountType) throws Exception {
        String accountName = currentUserInteractor.userCredentials().toBlocking().first().getUsername();

        Account fetchedAccount = fetchAccount(accountName, accountType);
        if (fetchedAccount == null) {
            fetchedAccount = createAccount(accountName, accountType);
        }

        return fetchedAccount;
    }

    private Account fetchAccount(String accountName, String accountType) {
        Account accounts[] = ((AccountManager) appContext.getSystemService(Context.ACCOUNT_SERVICE)).getAccountsByType(accountType);

        for (Account existingAccount : accounts) {
            if (existingAccount.name.equals(accountName)) {
                return existingAccount;
            }
        }
        // no account with this name exists
        return null;
    }

    private Account createAccount(String accountName, String accountType) throws Exception {
        Account account = new Account(accountName, accountType);
        AccountManager accountManager = (AccountManager) appContext.getSystemService(Context.ACCOUNT_SERVICE);

        Boolean accountAddedSuccessfully = accountManager.addAccountExplicitly(account, null, null);
        if (accountAddedSuccessfully) {
            return account;
        } else {
            throw new Exception("Unable to create Account: AccountManager.addAccountExplicitly returned false");
        }
    }

    private void initPeriodicSync() {

        if (appPreferences.getBackgroundSyncState()) {
            ContentResolver.setIsSyncable(account, authority, 1);
            ContentResolver.setSyncAutomatically(account, authority, true);
            long minutes = (long) appPreferences.getBackgroundSyncFrequency();
            long seconds = minutes * 60;
            ContentResolver.addPeriodicSync(
                    account,
                    authority,
                    Bundle.EMPTY,
                    seconds);
        }
    }

    public void setPeriodicSync(int minutes) {
        Long seconds = ((long) minutes) * 60;
        ContentResolver.addPeriodicSync(
                account,
                authority,
                Bundle.EMPTY,
                seconds);
    }

    public void syncNow() {

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(account, authority, settingsBundle);
    }


    public void removeAccount() {
        if (account != null && appContext != null) {
            AccountManager accountManager =
                    (AccountManager) appContext.getSystemService(Context.ACCOUNT_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                accountManager.removeAccountExplicitly(account);
            } else {
                accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {

                        try {
                            if (!future.getResult()) {
                                throw new Exception("Unable to remove SyncAdapter stub account. User must delete the account in Android system settings.");
                            }
                        } catch (Exception e) {
                            Log.e("SYNC ADAPTER", "Unable to remove SyncAdapter stub account", e);
                        }
                    }
                    // TODO remove magic callback implementation - OK
                }, null);

            }

        }
    }

    public void removePeriodicSync() {
        ContentResolver.removePeriodicSync(account, authority, Bundle.EMPTY);
    }

}