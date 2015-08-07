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

package org.hisp.dhis.android.eventcapture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.eventcapture.fragments.SelectProgramFragment;
import org.hisp.dhis.android.sdk.activities.INavigationHandler;
import org.hisp.dhis.android.sdk.activities.LoginActivity;
import org.hisp.dhis.android.sdk.activities.OnBackPressedListener;
import org.hisp.dhis.android.sdk.controllers.Dhis2;
import org.hisp.dhis.android.sdk.controllers.ResponseHolder;
import org.hisp.dhis.android.sdk.events.BaseEvent;
import org.hisp.dhis.android.sdk.events.MessageEvent;
import org.hisp.dhis.android.sdk.fragments.LoadingFragment;
import org.hisp.dhis.android.sdk.network.http.ApiRequestCallback;
import org.hisp.dhis.android.sdk.network.managers.NetworkManager;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

public class MainActivity extends AppCompatActivity implements INavigationHandler {
    public final static String TAG = MainActivity.class.getSimpleName();
    private OnBackPressedListener mBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dhis2.getInstance().enableLoading(this, Dhis2.LOAD_EVENTCAPTURE);
        NetworkManager.getInstance().setCredentials(Dhis2.getCredentials(this));
        NetworkManager.getInstance().setServerUrl(Dhis2.getServer(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Dhis2.activatePeriodicSynchronizer(this);
        if (Dhis2.isInitialDataLoaded(this)) {
            showSelectProgramFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Dhis2Application.getEventBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Dhis2Application.getEventBus().register(this);
        if (Dhis2.isInitialDataLoaded(this)) {
            if(Dhis2.getInstance().isBlocking()) {
                showLoadingFragment();
            } else {

            }
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
        ApiRequestCallback callback = new ApiRequestCallback() {
            @Override
            public void onSuccess(ResponseHolder holder) {
                FlowContentObserver observer = Dhis2.getFlowContentObserverForAllTables();
                String message = getString(org.hisp.dhis.android.sdk.R.string.finishing_up);
                Dhis2.postProgressMessage(message);
                ApiRequestCallback callback = new ApiRequestCallback() {
                    @Override
                    public void onSuccess(ResponseHolder holder) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showSelectProgramFragment();
                            }
                        });
                    }

                    @Override
                    public void onFailure(ResponseHolder holder) {
                        showSelectProgramFragment();
                    }
                };
                Dhis2.BlockThread blockThread = new Dhis2.BlockThread(observer, callback);
                Dhis2.BlockingModelChangeListener listener = new Dhis2.BlockingModelChangeListener(blockThread);
                observer.addModelChangeListener(listener);
                blockThread.start();
            }

            @Override
            public void onFailure(ResponseHolder holder) {
                //todo: notify the user that data is missing and request to try to re-load.
                showSelectProgramFragment();
            }
        };
        Dhis2.loadInitialData(this, callback);
    }

    @Subscribe
    public void onReceiveMessage(MessageEvent event) {
        Log.d(TAG, "onReceiveMessage");
        if (event.eventType == BaseEvent.EventType.onLoadingInitialDataFinished) {
            if (Dhis2.isInitialDataLoaded(this)) {
                showSelectProgramFragment();
            } else {
                //todo: notify the user that data is missing and request to try to re-load.
                showSelectProgramFragment();
            }
        } else if (event.eventType == BaseEvent.EventType.loadInitialDataFailed) {
            startActivity(new Intent(MainActivity.this,
                    LoginActivity.class));
            finish();
        }
    }

    public void showLoadingFragment() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle("Loading initial data");
            }
        });
        switchFragment(new LoadingFragment(), LoadingFragment.TAG, false);
    }

    public void showSelectProgramFragment() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle("Event Capture");
            }
        });
        switchFragment(new SelectProgramFragment(), SelectProgramFragment.TAG, true);
    }

    @Override
    public void onBackPressed() {
        if (mBackPressedListener != null) {
            mBackPressedListener.doBack();
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    public void setBackPressedListener(OnBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    @Override
    public void switchFragment(Fragment fragment, String fragmentTag, boolean addToBackStack) {
        if (fragment != null) {
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();

            transaction
                    .setCustomAnimations(R.anim.open_enter, R.anim.open_exit)
                    .replace(R.id.fragment_container, fragment);
            if (addToBackStack) {
                transaction = transaction
                        .addToBackStack(fragmentTag);
            }

            transaction.commitAllowingStateLoss();
        }
    }
}
