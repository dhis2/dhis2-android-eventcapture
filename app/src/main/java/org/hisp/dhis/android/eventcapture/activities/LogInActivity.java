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

package org.hisp.dhis.android.eventcapture.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

public class LogInActivity extends AbsLoginActivity implements ILogInView {

    private ILogInPresenter loginPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginPresenter = new LogInPresenter(this);
        loginPresenter.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        loginPresenter.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loginPresenter.onResume();
    }

    @Override
    protected void onLogInButtonClicked(Editable server, Editable username, Editable password) {
        loginPresenter.validateCredentials(server.toString(), username.toString(),
                password.toString());
    }

    @Override
    public void showProgress() {
        onStartLoading();
    }

    @Override
    public void hideProgress() {
        onFinishLoading();
    }

    @Override
    public void showServerError(String message) {
        showError(message);
    }

    @Override
    public void showInvalidCredentialsError() {
        showError(getString(R.string.invalid_credentials_error));
    }

    @Override
    public void showUnexpectedError(String message) {
        showError(message);
    }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
        AlertDialog alertDialog = builder.setTitle(getString(R.string.error))
                .setMessage(message).show();
        alertDialog.show();
    }
}
