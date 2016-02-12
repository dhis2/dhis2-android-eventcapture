/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.activities.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.hisp.dhis.android.eventcapture.fragments.settings.SettingsPresenter;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.SettingPreferences;

import java.net.HttpURLConnection;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LogInPresenter extends AbsPresenter implements ILogInPresenter, IOnLogInFinishedListener {
    public static final String TAG = LogInPresenter.class.getSimpleName();

    public static final String AUTHORITY = "org.hisp.dhis.android.eventcapture.datasync.provider";
    public static final String ACCOUNT_TYPE = "example.com";

    public static String accountName = "dummyaccount";

    public static Account mAccount;
    private static int defaultUpdateFrequency = 30;

    private final ILogInView mLoginView;
    private Subscription mLoginSubscription;

    public LogInPresenter(ILogInView loginView) {
        this.mLoginView = loginView;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSyncAccount(); //TODO: remove this when app starts fine without special run configuratoins
    }

    @Override
    public void validateCredentials(String serverUrl, String username, String password) {
        Configuration configuration = new Configuration(serverUrl);

        accountName = username;

        mLoginView.showProgress();
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoginView.hideProgress();
            }
        }, 3000);

        mLoginSubscription = D2.signIn(configuration, username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserAccount>() {
                    @Override
                    public void call(UserAccount userAccount) {
                        onSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleError(throwable);
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (mLoginSubscription != null) {
            mLoginSubscription.unsubscribe();
        }
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public void onResume() {
        D2.isSignedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isSignedIn) {
                        if (isSignedIn != null && isSignedIn) {
                            onSuccess();
                        }
                    }
                });
    }

    @Override
    public void onServerError(String message) {
        mLoginView.showServerError(message);
    }

    @Override
    public void onUnexpectedError(String message) {
        mLoginView.showUnexpectedError(message);
    }

    @Override
    public void onInvalidCredentialsError() {
        mLoginView.showInvalidCredentialsError();
    }

    @Override
    public void onSuccess() {
        mLoginView.hideProgress();

        initSyncAccount();

        mLoginView.navigateToHome();
    }

    private void handleError(final Throwable throwable) {
        if (throwable instanceof ApiException) {
            ApiException apiException = (ApiException) throwable;
            switch (apiException.getKind()) {
                case CONVERSION:
                    onUnexpectedError(apiException.getMessage());
                    break;
                case HTTP: {
                    if (apiException.getResponse() != null) {
                        switch (apiException.getResponse().getStatus()) {
                            case HttpURLConnection.HTTP_UNAUTHORIZED: {
                                onInvalidCredentialsError();
                                break;
                            }
                            default: {
                                onUnexpectedError(throwable.getMessage());
                            }
                        }
                    }
                    break;
                }
                case NETWORK: {
                    onServerError(apiException.getMessage());
                    break;
                }
                case UNEXPECTED: {
                    onUnexpectedError(throwable.getMessage());
                    break;
                }
            }
        } else {
            onUnexpectedError(throwable.getMessage());
        }
    }

    void initSyncAccount() {
        mAccount = createSyncAccount(mLoginView.getContext());

        ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);

        SettingPreferences.init(mLoginView.getContext());
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
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context) {
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
}
