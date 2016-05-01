package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.ProfileView;
import org.hisp.dhis.client.sdk.android.user.UserAccountInteractor;
import org.hisp.dhis.client.sdk.models.user.UserAccount;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.DataEntityEditText;
import org.hisp.dhis.client.sdk.ui.models.DataEntityEditText.InputType;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class ProfilePresenterImpl implements ProfilePresenter {
    private static final String TAG = ProfilePresenter.class.getSimpleName();

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

    private ProfileView profileView;
    private Subscription subscription;

    public ProfilePresenterImpl(UserAccountInteractor userAccountInteractor, Logger logger) {
        this.userAccountInteractor = userAccountInteractor;
        this.logger = logger;
    }

    @Override
    public void listUserAccountFields() {
        logger.d(TAG, "listUserAccountFields()");

        subscription = userAccountInteractor.account()
                .map(new Func1<UserAccount, List<DataEntity>>() {
                    @Override
                    public List<DataEntity> call(UserAccount userAccount) {
                        return transformUserAccount(userAccount);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<DataEntity>>() {
                    @Override
                    public void call(List<DataEntity> entities) {
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
        profileView = (ProfileView) view;

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

    private List<DataEntity> transformUserAccount(UserAccount userAccount) {
        List<DataEntity> dataEntities = new ArrayList<>();

        OnValueChangedListener changedListener = new OnValueChangedListener(
                userAccountInteractor, userAccount);

        DataEntityEditText firstName = new DataEntityEditText(
                FIRST_NAME, "First name", "Enter text", InputType.TEXT);
        firstName.setValue(userAccount.getFirstName());
        firstName.setOnValueChangeListener(changedListener);
        dataEntities.add(firstName);

        DataEntityEditText surname = new DataEntityEditText(
                SURNAME, "Surname", "Enter text", InputType.TEXT);
        surname.setValue(userAccount.getSurname());
        surname.setOnValueChangeListener(changedListener);
        dataEntities.add(surname);

        DataEntityEditText introduction = new DataEntityEditText(
                INTRODUCTION, "Introduction", "Enter text", InputType.TEXT);
        introduction.setValue(userAccount.getIntroduction());
        introduction.setOnValueChangeListener(changedListener);
        dataEntities.add(introduction);

        return dataEntities;

//
//        DataEntityEditText birthday = new DataEntityDate(
//                BIRTHDAY, "Birthday", "Enter text", InputType.);
//        birthday.setValue(userAccount.getBirthday());
//        birthday.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> education = new DataEntity2<>(
//                EDUCATION, "Education", DataEntity2.Type.EDITTEXT);
//        education.setValue(userAccount.getEducation());
//        education.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> employer = new DataEntity2<>(
//                EMPLOYER, "Employer", DataEntity2.Type.EDITTEXT);
//        employer.setValue(userAccount.getEmployer());
//        employer.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> interests = new DataEntity2<>(
//                INTERESTS, "Interests", DataEntity2.Type.EDITTEXT);
//        interests.setValue(userAccount.getInterests());
//        interests.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> jobTitle = new DataEntity2<>(
//                JOB_TITLE, "Job title", DataEntity2.Type.EDITTEXT);
//        jobTitle.setValue(userAccount.getJobTitle());
//        jobTitle.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> languages = new DataEntity2<>(
//                LANGUAGES, "Languages", DataEntity2.Type.EDITTEXT);
//        languages.setValue(userAccount.getLanguages());
//        languages.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> email = new DataEntity2<>(
//                EMAIL, "Email", DataEntity2.Type.EDITTEXT);
//        email.setValue(userAccount.getEmail());
//        email.setOnValueChangeListener(changedListener);
//
//        DataEntity2<String> phoneNumber = new DataEntity2<>(
//                PHONE_NUMBER, "Phone number", DataEntity2.Type.EDITTEXT);
//        phoneNumber.setValue(userAccount.getPhoneNumber());
//        phoneNumber.setOnValueChangeListener(changedListener);
    }

    private static class OnValueChangedListener implements OnValueChangeListener<String> {
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
