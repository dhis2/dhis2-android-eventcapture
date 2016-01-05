package org.hisp.dhis.android.eventcapture.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.ui.activities.AbsLoginActivity;

public class LogInActivity extends AbsLoginActivity implements ILoginView {

    private ILoginPresenter loginPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginPresenter = new LoginPresenter(this);
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
        startActivity(new Intent(this, MainActivity.class));
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
        AlertDialog alertDialog = builder.setTitle(getString(R.string.error))
                .setMessage(message).show();
        alertDialog.show();
    }
}
