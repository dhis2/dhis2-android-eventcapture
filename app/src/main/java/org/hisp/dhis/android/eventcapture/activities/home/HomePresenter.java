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

import org.hisp.dhis.android.eventcapture.datasync.AppAccountManager;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.user.UserAccount;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;
import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class HomePresenter implements IHomePresenter, Action1<UserAccount> {
    private final IHomeView homeView;
    private Subscription subscription;

    public HomePresenter(IHomeView homeView) {
        this.homeView = isNull(homeView, "IHomeView must not be null");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        subscription = D2.me().account()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);

        //init the user account for synchronization:
        AppAccountManager.getInstance().createAccount(homeView.getContext(),
                D2.me().userCredentials().toBlocking().first().getUsername());
    }

    @Override
    public void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = null;
    }

    @Override
    public void call(UserAccount userAccount) {
        String name = "";
        if (!isEmpty(userAccount.getFirstName()) && !isEmpty(userAccount.getSurname())) {
            name = String.valueOf(userAccount.getFirstName().charAt(0)) +
                    String.valueOf(userAccount.getSurname().charAt(0));
        } else if (userAccount.getDisplayName() != null &&
                userAccount.getDisplayName().length() > 1) {
            name = String.valueOf(userAccount.getDisplayName().charAt(0)) +
                    String.valueOf(userAccount.getDisplayName().charAt(1));
        }

        homeView.setUsername(userAccount.getDisplayName());
        homeView.setUserInfo(userAccount.getEmail());
        homeView.setUserLetter(name);
    }
}
