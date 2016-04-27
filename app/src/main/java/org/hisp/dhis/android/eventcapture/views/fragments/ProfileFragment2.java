package org.hisp.dhis.android.eventcapture.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.presenters.ProfilePresenter2;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;

import java.util.List;

import javax.inject.Inject;

public class ProfileFragment2 extends Fragment implements ProfileView2 {

    @Inject
    ProfilePresenter2 profilePresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // injection of profile presenter
        ((EventCaptureApp) getActivity().getApplication())
                .getUserComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onResume() {
        super.onResume();
        profilePresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        profilePresenter.detachView();
    }

    @Override
    public void showUserAccountFields(List<DataEntity> dataEntities) {

    }
}
