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

import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

public final class AutoCompleteRow implements DataEntryRow {
    private static final String EMPTY_FIELD = "";

    private final String mLabel;
    private final BaseValue mValue;
    private final OptionSet mOptionSet;
    private final List<String> mOptions;
    private AutoCompleteAdapter mAdapter;
    //private final Map<String, String> mCodeToNameMap;

    public AutoCompleteRow(String label,
                           BaseValue value,
                           OptionSet optionSet) {
        mLabel = label;
        mValue = value;
        mOptionSet = optionSet;
        // mCodeToNameMap = new HashMap<>();

        mOptions = new ArrayList<>();
        if (optionSet.getOptions() != null) {
            for (Option option : optionSet.getOptions()) {
                mOptions.add(option.getCode());
                // mCodeToNameMap.put(option.getCode(), option.getName());
            }
        }
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            OnFocusListener onFocusListener = new OnFocusListener();
            OnTextChangedListener onTextChangedListener = new OnTextChangedListener();
            DropDownButtonListener dropButtonListener = new DropDownButtonListener();

            view = inflater.inflate(R.layout.listview_row_autocomplete, container, false);
            holder = new ViewHolder(
                    (TextView) view.findViewById(R.id.text_label),
                    (AutoCompleteTextView) view.findViewById(R.id.choose_option),
                    (ImageButton) view.findViewById(R.id.show_drop_down_list),
                    onFocusListener, onTextChangedListener
            );

            onFocusListener.setAutoComplete(holder.autoCompleteTextView);
            dropButtonListener.setAutoComplete(holder.autoCompleteTextView);

            holder.imageButton.setOnClickListener(dropButtonListener);
            holder.autoCompleteTextView.addTextChangedListener(onTextChangedListener);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        if (mAdapter == null) {
            mAdapter = new AutoCompleteAdapter(inflater);
            mAdapter.swapData(mOptions);
        }

        holder.textView.setText(mLabel);
        holder.onFocusListener.setOptions(mOptions);

        holder.onTextChangedListener.setBaseValue(mValue);
        holder.onTextChangedListener.setOptions(mOptions);

        holder.autoCompleteTextView.setText(mValue.getValue());
        holder.autoCompleteTextView.setAdapter(mAdapter);
        holder.autoCompleteTextView.clearFocus();

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
        public final OnFocusListener onFocusListener;
        public final OnTextChangedListener onTextChangedListener;

        private ViewHolder(TextView textView, AutoCompleteTextView autoCompleteTextView,
                           ImageButton imageButton, OnFocusListener onFocusListener,
                           OnTextChangedListener onTextChangedListener) {
            this.textView = textView;
            this.autoCompleteTextView = autoCompleteTextView;
            this.imageButton = imageButton;
            this.onFocusListener = onFocusListener;
            this.onTextChangedListener = onTextChangedListener;
        }
    }

    private static class DropDownButtonListener implements View.OnClickListener {
        private AutoCompleteTextView autoComplete;

        public void setAutoComplete(AutoCompleteTextView autoComplete) {
            this.autoComplete = autoComplete;
        }

        @Override
        public void onClick(View v) {
            autoComplete.showDropDown();
        }
    }

    private static class OnTextChangedListener implements TextWatcher {
        private BaseValue value;
        private List<String> options;

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null) {
                String name = s.toString();
                if (options.contains(name)) {
                    value.setValue(name);
                }
            }
        }
    }

    private class OnFocusListener implements View.OnFocusChangeListener {
        private AutoCompleteTextView autoComplete;
        private List<String> options;

        public void setAutoComplete(AutoCompleteTextView autoComplete) {
            this.autoComplete = autoComplete;
        }

        public void setOptions(List<String> options) {
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
