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

package org.hisp.dhis.android.eventcapture.activities.home;

<<<<<<< HEAD
import android.content.pm.PackageManager;
=======
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
>>>>>>> 42df37cc08728f4d7b118a9d10b31a3fbbd63b91
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.WrapperFragment;
import org.hisp.dhis.client.sdk.ui.activities.AbsHomeActivity;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

public class HomeActivity extends AbsHomeActivity implements IHomeView {
    private IHomePresenter homePresenter;
    // Constants
    public static final String AUTHORITY = "org.hisp.dhis.android.eventcapture.datasync.provider";
    public static final String ACCOUNT_TYPE = "example.com";
    public static final String ACCOUNT_NAME = "dummyaccount";
    // Instance fields
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the dummy account
        mAccount = createSyncAccount(this);

        homePresenter = new HomePresenter(this);
        homePresenter.onCreate(savedInstanceState);

        onNavigationItemSelected(getNavigationView()
                .getMenu().findItem(R.id.drawer_selector));
<<<<<<< HEAD
        addAppsToMenu();
=======

>>>>>>> 42df37cc08728f4d7b118a9d10b31a3fbbd63b91
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        homePresenter.onDestroy();
    }

    @Override
    protected int getNavigationMenu() {
        return R.menu.menu_drawer;
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawer_selector: {
                attachFragmentDelayed(WrapperFragment
                        .newInstanceWithSelectorFragment(this));
                break;
            }
            case R.id.drawer_settings: {
                attachFragmentDelayed(WrapperFragment
                        .newInstanceWithSettingsFragment(this));
                break;
            }
            case R.id.drawer_profile: {
                attachFragmentDelayed(WrapperFragment
                        .newInstanceWithProfileFragment(this));
                break;
            }
        }

        return true;
    }

    @Override
    public void setUsername(CharSequence username) {
        getUsernameTextView().setText(username);
    }

    @Override
    public void setUserInfo(CharSequence userInfo) {
        getUserInfoTextView().setText(userInfo);
    }

    @Override
    public void setUserLetter(CharSequence userLetters) {
        getUsernameLetterTextView().setText(userLetters);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            PickerFragment pickerFragment = (PickerFragment)
                    getSupportFragmentManager().findFragmentByTag(PickerFragment.TAG);

            if (pickerFragment != null) {
                pickerFragment.dispatchTouchEvent(event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

<<<<<<< HEAD
    private void addAppsToMenu() {
        if (getAppsMenu() != null) {

            if (isAppInstalled("org.hisp.dhis.android.dashboard")) {
                MenuItem menuItem = getAppsMenu().add("Dashboard");
                menuItem.setIcon(R.drawable.ic_dashboard);
            }

            if (isAppInstalled("org.hisp.dhis.android.eventcapture")) {
                MenuItem menuItem = getAppsMenu().add("Event capture");
                menuItem.setIcon(R.drawable.ic_event_capture);
            }

            if (isAppInstalled("org.hisp.dhis.android.trackercapture")) {
                MenuItem menuItem = getAppsMenu().add("Tracker capture");
                menuItem.setIcon(R.drawable.ic_tracker_capture);
            }

            if (isAppInstalled("org.dhis2.mobile")) {
                MenuItem menuItem = getAppsMenu().add("Data capture");
                menuItem.setIcon(R.drawable.ic_data_capture);
            }
        }
    }

    private boolean isAppInstalled(String packageName) {
        String currentApp = getApplicationContext().getPackageName();
        if (currentApp.equals(packageName)) {
            return false;
        }

        PackageManager packageManager = getBaseContext().getPackageManager();
        try {
            // using side effect of calling getPackageInfo() method
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Nullable
    private Menu getAppsMenu() {
        Menu menu = getNavigationView().getMenu();
        if (menu == null) {
            return null;
        }

        for (int index = 0; index < menu.size(); index++) {
            MenuItem item = menu.getItem(index);
            if (item.getItemId() == R.id.drawer_section_apps) {
                return item.getSubMenu();
            }
        }

=======
    //Since this will be used only once maybe it is best to not define this ?
    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
>>>>>>> 42df37cc08728f4d7b118a9d10b31a3fbbd63b91
        return null;
    }
}
