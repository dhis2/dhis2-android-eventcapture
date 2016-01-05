
package org.hisp.dhis.android.eventcapture.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.hisp.dhis.android.eventcapture.BuildConfig;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.android.common.D2;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.models.user.UserAccount;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Timber.d(BuildConfig.SERVER_URL);

        Configuration configuration = new Configuration(BuildConfig.SERVER_URL);
        D2.signIn(configuration, BuildConfig.USERNAME, BuildConfig.PASSWORD)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserAccount>() {
                    @Override
                    public void call(UserAccount userAccount) {
                        Timber.d(userAccount.getFirstName());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }
}
