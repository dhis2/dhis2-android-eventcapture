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

package org.hisp.dhis.android.eventcapture.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;

import org.hisp.dhis.android.eventcapture.BuildConfig;
import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.LoginPresenter;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

import javax.inject.Inject;

public class LoginActivity extends AbsLoginActivity implements LoginView {

    private AlertDialog alertDialog;
    @Inject
    LoginPresenter loginPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EventCaptureApp) getApplication()).getUserComponent().inject(this);

        getServerUrl().setText(BuildConfig.SERVER_URL);
        getUsername().setText(BuildConfig.USERNAME);
        getPassword().setText(BuildConfig.PASSWORD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //To avoid leaks on configuration changes:
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        loginPresenter.detachView();
    }

    @Override
    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        ((EventCaptureApp) getApplication())
                .createUserComponent(server.toString()).inject(this);

        // since we have re-instantiated LoginPresenter, we
        // also have to re-attach view to it
        loginPresenter.attachView(this);

        loginPresenter.validateCredentials(
                server.toString(), username.toString(), password.toString());
    }

    @Override
    public void showProgress() {
        onStartLoading();
    }

    @Override
    public void hideProgress(final OnProgressFinishedListener listener) {
        onFinishLoading(new OnAnimationFinishListener() {
            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onProgressFinished();
                }
            }
        });
    }

    @Override
    public void showServerError(String message) {
        //TODO: Highlight url field and show a toast instead of dialog.
        showErrorDialog(message);
    }

    @Override
    public void showInvalidCredentialsError(String message) {
        //TODO: Highlight credentials in red ? and show a toast instead.
        showErrorDialog(message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(message);
    }

    @Override
    public void navigateToHome() {
        navigateTo(HomeActivity.class);
    }

    private void showErrorDialog(String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(getString(R.string.error));
        alertDialog.setMessage(message);
        alertDialog.show();
    }
}
