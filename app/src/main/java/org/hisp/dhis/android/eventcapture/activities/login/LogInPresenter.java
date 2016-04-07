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

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.ILogger;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.models.user.UserAccount;

import java.net.HttpURLConnection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

// TODO create new module for utilities (commons)
// TODO revise MVP/PassiveView/SupervisorController patterns
public class LoginPresenter extends AbsPresenter implements ILoginPresenter, IOnLoginFinishedListener {
    public static final String TAG = LoginPresenter.class.getSimpleName();

    private final ILoginView loginView;
    private final ILogger logger;
    private final CompositeSubscription subscription;

    public LoginPresenter(ILoginView loginView, ILogger logger) {
        this.loginView = loginView;
        this.logger = logger;
        this.subscription = new CompositeSubscription();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void validateCredentials(
            final String serverUrl, final String username, final String password) {

        loginView.showProgress();
        subscription.add(
                D2.configure(new Configuration(serverUrl))
                        .flatMap(new Func1<Void, Observable<UserAccount>>() {
                            @Override
                            public Observable<UserAccount> call(Void aVoid) {
                                return D2.me().signIn(username, password);
                            }
                        })
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
                                throwable.printStackTrace();
                                handleError(throwable);
                            }
                        }));
    }

    @Override
    public void onDestroy() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public void onResume() {
        if (D2.isConfigured() && D2.me().isSignedIn().toBlocking().first()) {
            onSuccess();
        }
    }

    @Override
    public void onServerError(final String message) {
        loginView.hideProgress(new ILoginView.OnProgressFinishedListener() {

            @Override
            public void onProgressFinished() {
                loginView.showServerError(message);
            }
        });
    }

    @Override
    public void onUnexpectedError(final String message) {
        loginView.hideProgress(new ILoginView.OnProgressFinishedListener() {

            @Override
            public void onProgressFinished() {
                loginView.showUnexpectedError(message);
            }
        });
    }

    @Override
    public void onInvalidCredentialsError() {
        loginView.showInvalidCredentialsError();
    }

    @Override
    public void onSuccess() {
        loginView.navigateToHome();
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
}
