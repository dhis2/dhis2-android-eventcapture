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
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.AutoCompleteAdapter;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoCompleteRow2 implements DataEntryRow {
    private static final String EMPTY_FIELD = "";

    private final String mLabel;
    private final BaseValue mValue;
    private final OptionSet mOptionSet;
    private final Map<String, Option> mCodeToOptionMap;
    private AutoCompleteAdapter mAdapter;

    public AutoCompleteRow2(String label,
                            BaseValue value,
                            OptionSet optionSet) {
        mLabel = label;
        mValue = value;
        mOptionSet = optionSet;
        mCodeToOptionMap = new HashMap<>();

        if (optionSet.getOptions() != null) {
            for (Option option : optionSet.getOptions()) {
                mCodeToOptionMap.put(option.getCode(), option);
            }
        }
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        ViewHolder holder;

        if (mAdapter == null) {
            mAdapter = new AutoCompleteAdapter(inflater);
        }

        if (convertView == null) {
            view = inflater.inflate(R.layout.listview_row_autocomplete, container, false);
            holder = new ViewHolder(
                    (TextView) view.findViewById(R.id.text_label),
                    (AutoCompleteTextView) view.findViewById(R.id.choose_option),
                    (ImageButton) view.findViewById(R.id.show_drop_down_list)
            );
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.handleViews(mLabel, mValue);
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.AUTO_COMPLETE.ordinal();
    }

    private static class ViewHolder {
        public final TextView textView;
        public final AutoCompleteTextView autoCompleteTextView;
        public final ImageButton imageButton;

        private ViewHolder(TextView textView,
                           AutoCompleteTextView autoCompleteTextView,
                           ImageButton imageButton) {
            this.textView = textView;
            this.autoCompleteTextView = autoCompleteTextView;
            this.imageButton = imageButton;
        }

        public void handleViews(String label, BaseValue value) {

        }
    }

    private void setValue(BaseValue value) {
        // find matching option by value
        // extract name (show it)
    }

    private class DropDownButtonListener implements View.OnClickListener {
        private AutoCompleteTextView autoComplete;

        public void setAutoComplete(AutoCompleteTextView autoComplete) {
            this.autoComplete = autoComplete;
        }

        @Override
        public void onClick(View v) {
            autoComplete.showDropDown();
        }

    }

    private class OnFocusListener implements View.OnFocusChangeListener {
        private AutoCompleteTextView autoComplete;
        private List<String> options;

        public OnFocusListener(AutoCompleteTextView autoComplete,
                               List<String> options) {
            this.autoComplete = autoComplete;
            this.options = options;
        }

        public void setValues(AutoCompleteTextView autoComplete,
                              List<String> options) {
            this.autoComplete = autoComplete;
            this.options = options;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                String choice = autoComplete.getText().toString();
                if (!options.contains(choice)) {
                    autoComplete.setText(EMPTY_FIELD);
                }
            }
        }
    }
}
