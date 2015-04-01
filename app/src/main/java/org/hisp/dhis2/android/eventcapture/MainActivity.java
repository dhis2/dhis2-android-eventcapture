/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.eventcapture;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.fragments.SelectProgramFragment;
import org.hisp.dhis2.android.sdk.activities.LoginActivity;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.fragments.LoadingFragment;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.network.managers.NetworkManager;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;


public class MainActivity extends ActionBarActivity implements INavigationHandler {
    public final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dhis2.getInstance().enableLoading(this, Dhis2.LOAD_EVENTCAPTURE);
        NetworkManager.getInstance().setCredentials(Dhis2.getCredentials(this));
        NetworkManager.getInstance().setServerUrl(Dhis2.getServer(this));
        Dhis2Application.bus.register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Dhis2.activatePeriodicSynchronizer(this);

        if (Dhis2.isInitialDataLoaded(this)) {
            showSelectProgramFragment();
        } else if (Dhis2.isLoadingInitial()) {
            showLoadingFragment();
        } else {
            loadInitialData();
        }
    }

    public void loadInitialData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingFragment();
            }
        });
        Dhis2.loadInitialData(this);
    }

    @Subscribe
    public void onReceiveMessage(MessageEvent event) {
        Log.d(TAG, "onReceiveMessage");
        if (event.eventType == BaseEvent.EventType.showRegisterEventFragment) {
            //showRegisterEventFragment();
        } else if (event.eventType == BaseEvent.EventType.showSelectProgramFragment) {
            //showSelectProgramFragment();
        } else if (event.eventType == BaseEvent.EventType.logout) {
            //logout();
        } else if (event.eventType == BaseEvent.EventType.onLoadingInitialDataFinished) {
            if (Dhis2.isInitialDataLoaded(this)) {
                showSelectProgramFragment();
            } else {
                //todo: notify the user that data is missing and request to try to re-load.
            }
        } else if (event.eventType == BaseEvent.EventType.showDataEntryFragment) {
            if (event.item != null) {
                long localEventId = (long) event.item;
                // showEditEventFragment(localEventId);
            }
        } else if (event.eventType == BaseEvent.EventType.loadInitialDataFailed) {
            startActivity(new Intent(MainActivity.this,
                    LoginActivity.class));
            finish();
        }
    }

    public void showLoadingFragment() {
        setTitle("Loading initial data");
        // if (loadingFragment == null) loadingFragment = new LoadingFragment();
        switchFragment(new LoadingFragment(), LoadingFragment.TAG);
    }

    public void showSelectProgramFragment() {
        setTitle("Event Capture");
        // if (selectProgramFragment == null) selectProgramFragment = new SelectProgramFragment();
        //showFragment(selectProgramFragment);
        switchFragment(new SelectProgramFragment(), SelectProgramFragment.TAG);
        //selectProgramFragment.setSelection(lastSelectedOrgUnit, lastSelectedProgram);
    }

    public void showSettingsFragment() {
        setTitle("Settings");
        switchFragment(new SettingsFragment(), SettingsFragment.TAG);
        /*
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        */
        /*
        if (selectProgramFragment != null) {
            lastSelectedOrgUnit = selectProgramFragment.getSelectedOrganisationUnitIndex();
            lastSelectedProgram = selectProgramFragment.getSelectedProgramIndex();
        }
        */

    }

    /*
    public void showEditEventFragment(long localEventId) {
        setTitle("Edit Event");
        Fragment fragment = DataEntryFragment.newInstance(
                selectProgramFragment.getSelectedOrganisationUnit(),
                selectProgramFragment.getSelectedProgram(),
                localEventId
        );
        switchFragment(fragment, DataEntryFragment.TAG);
        /* dataEntryFragment = new DataEntryFragment();
        OrganisationUnit organisationUnit = selectProgramFragment.getSelectedOrganisationUnit();
        Program program = selectProgramFragment.getSelectedProgram();
        dataEntryFragment.setSelectedOrganisationUnit(organisationUnit);
        dataEntryFragment.setSelectedProgram(program);
        dataEntryFragment.setEditingEvent(localEventId);
        lastSelectedOrgUnit = selectProgramFragment.getSelectedOrganisationUnitIndex();
        lastSelectedProgram = selectProgramFragment.getSelectedProgramIndex();
        showFragment(dataEntryFragment); */
    // }

    /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_new_event);
        item.setVisible(true);
        if(currentFragment.equals(settingsFragment))
            item.setVisible(false);
        else */
        /* if (currentFragment == selectProgramFragment)
            item.setIcon(getResources().getDrawable(R.drawable.ic_new));
        else if (currentFragment == dataEntryFragment)
            item.setIcon(getResources().getDrawable(R.drawable.ic_save));
        else if(currentFragment.equals(loadingFragment))
            item.setVisible(false);

        return true;
    } */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {

            Toast.makeText(
                    getApplicationContext(), "onBackPressed() -> Exit " + getSupportFragmentManager().getBackStackEntryCount(), Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (currentFragment == selectProgramFragment) {
                Dhis2.getInstance().showConfirmDialog(this, getString(R.string.confirm),
                        getString(R.string.exit_confirmation), getString(R.string.yes_option),
                        getString(R.string.no_option),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(0);
                            }
                        });
            } else if (currentFragment == dataEntryFragment) {
                String message = null;
                if (dataEntryFragment.isEditing()) {
                    message = getString(R.string.discard_confirm_changes);
                } else {
                    message = getString(R.string.discard_confirm);
                }
                if (dataEntryFragment.hasEdited()) {
                    Dhis2.getInstance().showConfirmDialog(this, getString(R.string.discard),
                            message, getString(R.string.yes_option),
                            getString(R.string.no_option),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showSelectProgramFragment();
                                }
                            });
                } else {
                    showSelectProgramFragment();
                    dataEntryFragment = null;
                }
            } else if ( currentFragment.equals(settingsFragment )) {
                if(previousFragment == null) showSelectProgramFragment();
                else showFragment(previousFragment);
            } */
        /*    return true;
        }

        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dhis2Application.bus.unregister(this);
    }

    public void switchFragment(Fragment fragment, String fragmentTag) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(fragmentTag)
                    .commit();
        }
    }
}
