package org.hisp.dhis.android.eventcapture.presenters;

public interface ProfilePresenter extends Presenter {
    void createUserAccountForm();

    void sync();

    void logout();
}
