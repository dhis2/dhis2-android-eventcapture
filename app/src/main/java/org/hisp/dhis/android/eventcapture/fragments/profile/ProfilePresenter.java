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

package org.hisp.dhis.android.eventcapture.fragments.profile;

import android.support.v4.util.Pair;

import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.DataEntity.Type;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.Subject;
import timber.log.Timber;

public class ProfilePresenter extends AbsPresenter
        implements IProfilePresenter {

    private IProfileView profileView;
    private Subscription profileSubscription;
    private Subscription saveProfileSubscription;

    public ProfilePresenter(IProfileView profileView) {
        this.profileView = profileView;
    }

    @Override
    public void listUserAccountFields() {
        profileSubscription = D2.me().account()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<UserAccount, List<DataEntity>>() {
                    @Override
                    public List<DataEntity> call(UserAccount userAccount) {
                        return transformUserAccount(userAccount);
                    }
                })
                .subscribe(new Action1<List<DataEntity>>() {
                    @Override
                    public void call(List<DataEntity> dataEntities) {
                        if (profileView != null) {
                            profileView.setProfileFields(dataEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "error reading user account details");
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (profileSubscription != null && !profileSubscription.isUnsubscribed()) {
            profileSubscription.isUnsubscribed();
        }
        if(saveProfileSubscription != null && !saveProfileSubscription.isUnsubscribed()) {
            saveProfileSubscription.unsubscribe();
        }

        profileView = null;
        profileSubscription = null;
        saveProfileSubscription = null;
    }

    @Override
    public String getKey() {
        return ProfilePresenter.class.getSimpleName();
    }

    private List<DataEntity> transformUserAccount(UserAccount account) {
        List<DataEntity> dataEntities = new ArrayList<>();
        RxProfileValueChangedListener onProfileValueChangedListener = new RxProfileValueChangedListener(null);

        dataEntities.add(DataEntity.create("First name", account.getFirstName(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Surname", account.getSurname(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Gender", account.getGender(), Type.AUTO_COMPLETE,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Birthday", account.getBirthday(), Type.DATE,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Introduction", account.getIntroduction(), Type
                .TRUE_ONLY,  onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Education", account.getEducation(), Type.BOOLEAN,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Employer", account.getEmployer(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Interests", account.getIntroduction(), Type
                .COORDINATES,  onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Job title", account.getIntroduction(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Languages", account.getLanguages(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Email", account.getEmail(), Type.TEXT,
                onProfileValueChangedListener));
        dataEntities.add(DataEntity.create("Phone number", account.getPhoneNumber(), Type.TEXT,
                onProfileValueChangedListener));

        return dataEntities;
    }

    static class RxProfileValueChangedListener extends Subject<CharSequence, Pair<CharSequence, CharSequence>> implements OnValueChangeListener<CharSequence> {

        protected RxProfileValueChangedListener(OnSubscribe<Pair<CharSequence, CharSequence>> onSubscribe) {
            super(onSubscribe);
        }

        @Override
        public void onValueChanged(String key, CharSequence value) {
            onNext(value);
        }

        @Override
        public boolean hasObservers() {
            return false;
        }

        @Override
        public void onCompleted() {
            Timber.d("onComplete");
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("onError");
        }

        @Override
        public void onNext(CharSequence charSequence) {
            Timber.d(charSequence.toString());
        }
    }
    private class OnProfileValueChangedListener implements OnValueChangeListener<CharSequence> {
        private UserAccount userAccount;

        @Override
        public void onValueChanged(String key, CharSequence value) {

            saveProfileSubscription = D2.me().save(userAccount)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {

                            Timber.d("ProfilePresenter", "userAcc saved");
                        }
                    });

        }

        public void setUserAccount(UserAccount userAccount) {
            this.userAccount = userAccount;
        }
    }
}
