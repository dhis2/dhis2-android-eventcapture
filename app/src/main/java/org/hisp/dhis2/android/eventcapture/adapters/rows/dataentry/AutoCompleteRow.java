/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteRow implements DataEntryRow {
    private String mLabel;
    private BaseValue mBaseValue;
    private List<String> mOptions;

    public AutoCompleteRow(BaseValue value, OptionSet optionset) {
        mBaseValue = value;
        mOptions = new ArrayList<>();

        if (optionset != null && optionset.getOptions() != null &&
                optionset.getOptions().size() > 0) {
            for (Option option : optionset.getOptions()) {
                mOptions.add(option.getName());
            }
        }
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view = null;
        /* AutoCompleteRowHolder holder;

        if (convertView == null) {
            View root = inflater.inflate(
                    R.layout.listview_row_autocomplete, container, false);
            TextView textLabel = (TextView)
                    root.findViewById(R.id.text_label);
            AutoCompleteValueEntryView autoComplete = (AutoCompleteValueEntryView)
                    root.findViewById(R.id.find_option);

            ValueSetListener listener = new ValueSetListener();
            holder = new AutoCompleteRowHolder(textLabel, autoComplete, listener);

            root.setTag(holder);
            view = root;
        } else {
            view = convertView;
            holder = (AutoCompleteRowHolder) view.getTag();
        } */

        //holder.updateViews(mField, mListener);
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.AUTO_COMPLETE.ordinal();
    }

    /* private class AutoCompleteRowHolder {
        final TextView textLabel;
        final AutoCompleteValueEntryView autoComplete;
        final ValueSetListener listener;

        public AutoCompleteRowHolder(TextView textLabel,
                                     AutoCompleteValueEntryView autoComplete,
                                     ValueSetListener listener) {
            this.textLabel = textLabel;
            this.autoComplete = autoComplete;
            this.listener = listener;
        }

        public void updateViews(DbRow<Field> field,
                                OnFieldValueSetListener onFieldValueSetListener) {
            textLabel.setText(field.getItem().getLabel());

            listener.setField(field);
            listener.setListener(onFieldValueSetListener);

            System.out.println("updateViews(): " + mField.getItem().getValue());
            autoComplete.setOnValueSetListener(listener);
            autoComplete.swapData(mOptions);
            autoComplete.setText(mField.getItem().getValue());
            autoComplete.resetView();
        }
    } */
}