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

package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.activities.LoginView;
import org.hisp.dhis.android.eventcapture.views.activities.OnLoginFinishedListener;
import org.hisp.dhis.client.sdk.android.user.CurrentUserInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class LoginPresenterImpl implements LoginPresenter, OnLoginFinishedListener {
    private final CurrentUserInteractor userAccountInteractor;
    private final CompositeSubscription subscription;
    private final Logger logger;

    private LoginView loginView;

    public LoginPresenterImpl(CurrentUserInteractor userAccountInteractor, Logger logger) {
        this.userAccountInteractor = userAccountInteractor;
        this.subscription = new CompositeSubscription();
        this.logger = logger;
    }

    @Override
    public void attachView(View view) {
        isNull(view, "LoginView must not be null");
        loginView = (LoginView) view;

        if (userAccountInteractor != null &&
                userAccountInteractor.isSignedIn().toBlocking().first()) {
            onSuccess();
        }
    }

    @Override
    public void detachView() {
        loginView = null;
    }

    @Override
    public void validateCredentials(final String serverUrl, final String username,
                                    final String password) {
        loginView.showProgress();
        subscription.add(userAccountInteractor.signIn(username, password)
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
    public void onServerError(final String message) {
        loginView.hideProgress(new LoginView.OnProgressFinishedListener() {
            @Override
            public void onProgressFinished() {
                loginView.showServerError(message);
            }
        });
    }

    @Override
    public void onUnexpectedError(final String message) {
        loginView.hideProgress(new LoginView.OnProgressFinishedListener() {
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
                        System.out.println("STATUS: " + apiException.getResponse().getStatus());
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
