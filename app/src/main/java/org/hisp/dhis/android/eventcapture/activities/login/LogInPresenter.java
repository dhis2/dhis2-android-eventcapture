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
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.models.user.UserAccount;

import java.net.HttpURLConnection;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LogInPresenter extends AbsPresenter implements ILogInPresenter, IOnLogInFinishedListener {
    private final ILogInView mLoginView;
    private Subscription mLoginSubscription;

    public LogInPresenter(ILogInView loginView) {
        this.mLoginView = loginView;
    }

    @Override
    public void validateCredentials(String serverUrl, String username, String password) {
        Configuration configuration = new Configuration(serverUrl);

        mLoginView.showProgress();
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
        return LogInPresenter.class.getSimpleName();
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
}
