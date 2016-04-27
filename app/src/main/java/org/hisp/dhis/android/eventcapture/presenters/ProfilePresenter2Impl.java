package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.android.eventcapture.views.fragments.ProfileView2;
import org.hisp.dhis.client.sdk.android.user.UserAccountInteractor;
import org.hisp.dhis.client.sdk.utils.Logger;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class ProfilePresenter2Impl implements ProfilePresenter2 {
    private static final String TAG = ProfilePresenter2.class.getSimpleName();

    private final UserAccountInteractor userAccountInteractor;
    private final Logger logger;

    private ProfileView2 profileView;

    public ProfilePresenter2Impl(UserAccountInteractor userAccountInteractor, Logger logger) {
        this.userAccountInteractor = userAccountInteractor;
        this.logger = logger;
    }

    @Override
    public void listUserAccountFields() {
        logger.d(TAG, "listUserAccountFields()");
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
    }
}
