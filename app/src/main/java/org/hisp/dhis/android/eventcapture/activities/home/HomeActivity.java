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

import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.WrapperFragment;
import org.hisp.dhis.android.eventcapture.fragments.selector.ContainerFragment;
import org.hisp.dhis.client.sdk.ui.activities.AbsHomeActivity;
import org.hisp.dhis.client.sdk.ui.fragments.PickerFragment;

public class HomeActivity extends AbsHomeActivity implements IHomeView {
    private IHomePresenter homePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homePresenter = new HomePresenter(this);
        homePresenter.onCreate(savedInstanceState);

        onNavigationItemSelected(getNavigationView()
                .getMenu().findItem(R.id.drawer_selector));
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
}
