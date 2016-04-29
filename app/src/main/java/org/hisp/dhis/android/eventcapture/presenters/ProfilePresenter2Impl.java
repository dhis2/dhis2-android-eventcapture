package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.ProfileView2;
import org.hisp.dhis.client.sdk.android.user.UserAccountInteractor;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.models.DataEntity2;
import org.hisp.dhis.client.sdk.ui.models.DataEntity2.Type;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener2;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.Arrays;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class ProfilePresenter2Impl implements ProfilePresenter2 {
    private static final String TAG = ProfilePresenter2.class.getSimpleName();

    private static final String FIRST_NAME = "firstName";
    private static final String SURNAME = "surname";
    private static final String BIRTHDAY = "birthday";
    private static final String INTRODUCTION = "introduction";
    private static final String EDUCATION = "education";
    private static final String EMPLOYER = "employer";
    private static final String INTERESTS = "interests";
    private static final String JOB_TITLE = "jobTitle";
    private static final String LANGUAGES = "languages";
    private static final String EMAIL = "email";
    private static final String PHONE_NUMBER = "phoneNumber";

    private final UserAccountInteractor userAccountInteractor;
    private final Logger logger;

    private ProfileView2 profileView;
    private Subscription subscription;

    public ProfilePresenter2Impl(UserAccountInteractor userAccountInteractor, Logger logger) {
        this.userAccountInteractor = userAccountInteractor;
        this.logger = logger;
    }

    @Override
    public void listUserAccountFields() {
        logger.d(TAG, "listUserAccountFields()");

        subscription = userAccountInteractor.account()
                .map(new Func1<UserAccount, List<DataEntity2<String>>>() {
                    @Override
                    public List<DataEntity2<String>> call(UserAccount userAccount) {
                        return transformUserAccount(userAccount);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<DataEntity2<String>>>() {
                    @Override
                    public void call(List<DataEntity2<String>> entities) {
                        if (profileView != null) {
                            profileView.showUserAccountFields(entities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.d(TAG, throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    public void attachView(View view) {
        isNull(view, "View must not be null");
        profileView = (ProfileView2) view;

        // list account fields as soon as
        // presenter is attached to fragment
        listUserAccountFields();
    }

    @Override
    public void detachView() {
        profileView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private List<DataEntity2<String>> transformUserAccount(UserAccount userAccount) {
        OnValueChangedListener changedListener = new OnValueChangedListener(
                userAccountInteractor, userAccount);

        DataEntity2<String> firstName = new DataEntity2<>(
                FIRST_NAME, "First name", Type.EDITTEXT);
        firstName.setValue(userAccount.getFirstName());
        firstName.setOnValueChangeListener(changedListener);

        DataEntity2<String> surname = new DataEntity2<>(
                SURNAME, "Surname", Type.EDITTEXT);
        surname.setValue(userAccount.getSurname());
        surname.setOnValueChangeListener(changedListener);

        DataEntity2<String> birthday = new DataEntity2<>(
                BIRTHDAY, "Birthday", Type.DATE);
        birthday.setValue(userAccount.getBirthday());
        birthday.setOnValueChangeListener(changedListener);

        DataEntity2<String> introduction = new DataEntity2<>(
                INTRODUCTION, "Introduction", Type.EDITTEXT);
        introduction.setValue(userAccount.getIntroduction());
        introduction.setOnValueChangeListener(changedListener);

        DataEntity2<String> education = new DataEntity2<>(
                EDUCATION, "Education", DataEntity2.Type.EDITTEXT);
        education.setValue(userAccount.getEducation());
        education.setOnValueChangeListener(changedListener);

        DataEntity2<String> employer = new DataEntity2<>(
                EMPLOYER, "Employer", DataEntity2.Type.EDITTEXT);
        employer.setValue(userAccount.getEmployer());
        employer.setOnValueChangeListener(changedListener);

        DataEntity2<String> interests = new DataEntity2<>(
                INTERESTS, "Interests", DataEntity2.Type.EDITTEXT);
        interests.setValue(userAccount.getInterests());
        interests.setOnValueChangeListener(changedListener);

        DataEntity2<String> jobTitle = new DataEntity2<>(
                JOB_TITLE, "Job title", DataEntity2.Type.EDITTEXT);
        jobTitle.setValue(userAccount.getJobTitle());
        jobTitle.setOnValueChangeListener(changedListener);

        DataEntity2<String> languages = new DataEntity2<>(
                LANGUAGES, "Languages", DataEntity2.Type.EDITTEXT);
        languages.setValue(userAccount.getLanguages());
        languages.setOnValueChangeListener(changedListener);

        DataEntity2<String> email = new DataEntity2<>(
                EMAIL, "Email", DataEntity2.Type.EDITTEXT);
        email.setValue(userAccount.getEmail());
        email.setOnValueChangeListener(changedListener);

        DataEntity2<String> phoneNumber = new DataEntity2<>(
                PHONE_NUMBER, "Phone number", DataEntity2.Type.EDITTEXT);
        phoneNumber.setValue(userAccount.getPhoneNumber());
        phoneNumber.setOnValueChangeListener(changedListener);

        return Arrays.asList(
                firstName, surname, birthday, introduction, education, employer,
                interests, jobTitle, languages, email, phoneNumber
        );
    }

    private static class OnValueChangedListener implements OnValueChangeListener2<String> {
        private final UserAccountInteractor userAccountInteractor;
        private final UserAccount userAccount;

        private OnValueChangedListener(UserAccountInteractor userAccountInteractor,
                                       UserAccount userAccount) {
            this.userAccountInteractor = userAccountInteractor;
            this.userAccount = userAccount;
        }

        @Override
        public void onValueChanged(String id, String value) {
            switch (id) {
                case FIRST_NAME: {
                    userAccount.setFirstName(value);
                    break;
                }
                case SURNAME: {
                    userAccount.setSurname(value);
                    break;
                }
                case BIRTHDAY: {
                    userAccount.setBirthday(value);
                    break;
                }
                case INTRODUCTION: {
                    userAccount.setIntroduction(value);
                    break;
                }
                case EDUCATION: {
                    userAccount.setEducation(value);
                    break;
                }
                case EMPLOYER: {
                    userAccount.setEmployer(value);
                    break;
                }
                case INTERESTS: {
                    userAccount.setInterests(value);
                    break;
                }
                case JOB_TITLE: {
                    userAccount.setJobTitle(value);
                    break;
                }
                case LANGUAGES: {
                    userAccount.setLanguages(value);
                    break;
                }
                case EMAIL: {
                    userAccount.setEmail(value);
                    break;
                }
                case PHONE_NUMBER: {
                    userAccount.setPhoneNumber(value);
                    break;
                }
            }

            System.out.println("*** CHANGED ***");
        }
    }
}
