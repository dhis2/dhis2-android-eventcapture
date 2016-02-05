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

package org.hisp.dhis.android.eventcapture.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.android.eventcapture.fragments.itemlist.ItemListFragment;
import org.hisp.dhis.android.eventcapture.fragments.selector.ContainerFragment;
import org.hisp.dhis.android.eventcapture.fragments.settings.SettingsFragment;
import org.hisp.dhis.client.sdk.ui.activities.INavigationCallback;

import static org.hisp.dhis.client.sdk.models.utils.Preconditions.isNull;

public class WrapperFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_TITLE = "arg:title";
    private static final String ARG_NESTED_FRAGMENT = "arg:nestedFragment";

    private static final String ARG_PROFILE = "arg:profile";
    private static final String ARG_SETTINGS = "arg:settings";
    private static final String ARG_SELECTOR = "arg:selector";
    private static final String ARG_ITEMSLIST = "arg:itemlist";

    INavigationCallback mNavigationCallback;

    @NonNull
    public static WrapperFragment newInstanceWithSettingsFragment(@NonNull Context context) {
        return newInstance(context, R.string.drawer_settings, ARG_SETTINGS);
    }

    @NonNull
    public static WrapperFragment newInstanceWithProfileFragment(@NonNull Context context) {
        return newInstance(context, R.string.drawer_profile, ARG_PROFILE);
    }

    @NonNull
    public static WrapperFragment newInstanceWithSelectorFragment(@NonNull Context context) {
        return newInstance(context, R.string.drawer_profile, ARG_SELECTOR);
    }

    @NonNull
    public static WrapperFragment newInstanceWithItemlistFragment(@NonNull Context context) {
        return newInstance(context, R.string.drawer_profile, ARG_ITEMSLIST);
    }

    private static WrapperFragment newInstance(@NonNull Context context, @StringRes int titleId,
                                                 String fragment) {
        isNull(context, "context must bot be null");

        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, context.getString(titleId));
        arguments.putString(ARG_NESTED_FRAGMENT, fragment);

        WrapperFragment wrapperFragment = new WrapperFragment();
        wrapperFragment.setArguments(arguments);

        return wrapperFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof INavigationCallback) {
            mNavigationCallback = (INavigationCallback) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wrapper, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        final Drawable buttonDrawable = DrawableCompat.wrap(ContextCompat
                .getDrawable(getActivity(), R.drawable.ic_menu));

        DrawableCompat.setTint(buttonDrawable, ContextCompat
                .getColor(getContext(), R.color.white));

        toolbar.setNavigationIcon(buttonDrawable);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setTitle(getTitle());

        switch (getFragment()) {
            case ARG_PROFILE: {
                toolbar.inflateMenu(R.menu.menu_profile);
                attachFragment(new ProfileFragment());
                break;
            }
            case ARG_SETTINGS: {
                attachFragment(new SettingsFragment());
                break;
            }
            case ARG_SELECTOR: {
                attachFragment(new ContainerFragment());
                break;
            }
            case ARG_ITEMSLIST: {
                attachFragment(new ItemListFragment());
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mNavigationCallback != null) {
            mNavigationCallback.toggleNavigationDrawer();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mNavigationCallback = null;
    }

    @NonNull
    private String getTitle() {
        if (isAdded() && getArguments() != null) {
            return getArguments().getString(ARG_TITLE, "");
        }

        return "";
    }

    @NonNull
    private String getFragment() {
        if (isAdded() && getArguments() != null) {
            return getArguments().getString(ARG_NESTED_FRAGMENT, "");
        }

        return "";
    }

    private void attachFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container_fragment_frame, fragment)
                .commit();
    }
}
