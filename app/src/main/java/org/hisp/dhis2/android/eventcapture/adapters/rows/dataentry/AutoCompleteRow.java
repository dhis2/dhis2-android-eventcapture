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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.EditTextValueChangedEvent;
import org.hisp.dhis2.android.eventcapture.EventCaptureApplication;
import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.eventcapture.adapters.AutoCompleteAdapter;
import org.hisp.dhis2.android.eventcapture.adapters.rows.AbsTextWatcher;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public final class AutoCompleteRow implements DataEntryRow {
    private static final String EMPTY_FIELD = "";

    private final String mLabel;
    private final BaseValue mValue;
    private final AutoCompleteAdapter mAdapter;

    private final Map<String, String> mCodeToNameMap;
    private final Map<String, String> mNameToCodeMap;

    public AutoCompleteRow(String label, BaseValue value,
                           OptionSet optionSet) {
        mLabel = label;
        mValue = value;

        mCodeToNameMap = new LinkedHashMap<>();
        mNameToCodeMap = new LinkedHashMap<>();

        if (optionSet.getOptions() != null) {
            for (Option option : optionSet.getOptions()) {
                mCodeToNameMap.put(option.getCode(), option.getName());
                mNameToCodeMap.put(option.getName(), option.getCode());
            }
        }

        mAdapter = new AutoCompleteAdapter();
        mAdapter.swapData(new ArrayList<>(mNameToCodeMap.keySet()));
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = inflater.inflate(R.layout.listview_row_autocomplete, container, false);

            TextView textLabel = (TextView) view.findViewById(R.id.text_label);
            AutoCompleteTextView autoComplete = (AutoCompleteTextView) view.findViewById(R.id.choose_option);
            ImageButton imageButton = (ImageButton) view.findViewById(R.id.show_drop_down_list);

            OnFocusListener onFocusListener = new OnFocusListener(autoComplete);
            OnTextChangedListener onTextChangedListener = new OnTextChangedListener();
            DropDownButtonListener dropButtonListener = new DropDownButtonListener(autoComplete);

            holder = new ViewHolder(
                    textLabel, autoComplete, imageButton, onFocusListener, onTextChangedListener
            );

            imageButton.setOnClickListener(dropButtonListener);
            autoComplete.addTextChangedListener(onTextChangedListener);
            autoComplete.setOnFocusChangeListener(onFocusListener);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        mAdapter.setLayoutInflater(inflater);

        holder.textView.setText(mLabel);
        holder.onFocusListener.setOptions(mNameToCodeMap);

        holder.onTextChangedListener.setBaseValue(mValue);
        holder.onTextChangedListener.setOptions(mNameToCodeMap);

        String name;
        if (mCodeToNameMap.containsKey(mValue.getValue())) {
            name = mCodeToNameMap.get(mValue.getValue());
        } else {
            name = EMPTY_FIELD;
        }

        holder.autoCompleteTextView.setText(name);
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
        private final AutoCompleteTextView autoComplete;

        private DropDownButtonListener(AutoCompleteTextView autoComplete) {
            this.autoComplete = autoComplete;
        }

        @Override
        public void onClick(View v) {
            autoComplete.showDropDown();
        }
    }

    private static class OnTextChangedListener extends AbsTextWatcher {
        private BaseValue value;
        private Map<String, String> nameToCodeMap;

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        public void setOptions(Map<String, String> nameToCodeMap) {
            this.nameToCodeMap = nameToCodeMap;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String name;
            if (s != null) {
                name = s.toString();
            } else {
                name = EMPTY_FIELD;
            }

            if (isEmpty(name) || nameToCodeMap.containsKey(name)) {
                value.setValue(nameToCodeMap.get(name));
            }

            EventCaptureApplication.getEventBus()
                    .post(new EditTextValueChangedEvent());
        }
    }

    private class OnFocusListener implements View.OnFocusChangeListener {
        private final AutoCompleteTextView autoComplete;
        private Map<String, String> nameToCodeMap;

        private OnFocusListener(AutoCompleteTextView autoComplete) {
            this.autoComplete = autoComplete;
        }

        public void setOptions(Map<String, String> nameToCodeMap) {
            this.nameToCodeMap = nameToCodeMap;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                String choice = autoComplete.getText().toString();
                if (!nameToCodeMap.containsKey(choice)) {
                    autoComplete.setText(EMPTY_FIELD);
                }
            }
        }
    }
}
