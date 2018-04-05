/*
 *  Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.android.eventcapture.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.eventcapture.export.ExportData;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.ui.views.FontButton;
import org.hisp.dhis.android.sdk.ui.views.FontCheckBox;

import java.io.IOException;

/**
 * Basic settings Fragment giving users options to change update frequency to the server,
 * and logging out.
 *
 * @author Simen Skogly Russnes on 02.03.15.
 */
public class SettingsFragment extends
        org.hisp.dhis.android.sdk.ui.fragments.settings.SettingsFragment {

    private FontButton exportDataButton;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        exportDataButton = (FontButton) view.findViewById(R.id.settings_export_data);
        exportDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExportDataClick();
            }
        });
        FontCheckBox fontCheckBox = (FontCheckBox) view.findViewById(
                R.id.checkbox_developers_options);

        Context context = getActivity().getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        fontCheckBox.setChecked(sharedPreferences.getBoolean(
                getActivity().getApplicationContext().getResources().getString(
                        R.string.developer_option_key), false));
        toggleOptions(fontCheckBox.isChecked());
        fontCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                        getActivity().getApplicationContext());
                SharedPreferences.Editor prefEditor =
                        sharedPref.edit(); // Get preference in editor mode
                prefEditor.putBoolean(
                        getActivity().getApplicationContext().getResources().getString(
                                R.string.developer_option_key), value);
                prefEditor.commit();
                toggleOptions(value);
            }
        });

    }

    private void toggleOptions(boolean value) {
        if (value) {
            exportDataButton.setVisibility(View.VISIBLE);
        } else {
            exportDataButton.setVisibility(View.INVISIBLE);
        }
    }

    public void onExportDataClick() {
        ExportData exportData = new ExportData();
        Intent emailIntent = null;
        try {
            emailIntent = exportData.dumpAndSendToAIntent(getActivity());
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_exporting_data,
                    Toast.LENGTH_LONG).show();
        }
        if (emailIntent != null) {
            startActivity(emailIntent);
        }
    }

    @Subscribe
    public void onSynchronizationFinishedEvent(final UiEvent event)
    {
        super.onSynchronizationFinishedEvent(event);
    }

    @Subscribe
    public void onLoadingMessageEvent(final LoadingMessageEvent event) {
        super.onLoadingMessageEvent(event);
    }
}
