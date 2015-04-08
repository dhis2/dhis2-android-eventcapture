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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;

import static android.text.TextUtils.isEmpty;

public class CheckBoxRow implements DataEntryRow {
    private static final String TRUE = "true";
    private static final String EMPTY_FIELD = "";

    private String mLabel;
    private BaseValue mBaseValue;

    public CheckBoxRow(String label, BaseValue baseValue) {
        mLabel = label;
        mBaseValue = baseValue;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        CheckBoxHolder holder;

        if (convertView == null) {
            View root = inflater.inflate(R.layout.listview_row_checkbox, container, false);
            TextView textLabel = (TextView) root.findViewById(R.id.text_label);
            CheckBox checkBox = (CheckBox) root.findViewById(R.id.checkbox);

            CheckBoxListener listener = new CheckBoxListener();
            holder = new CheckBoxHolder(textLabel, checkBox, listener);

            root.setTag(holder);
            view = root;
        } else {
            view = convertView;
            holder = (CheckBoxHolder) view.getTag();
        }

        holder.updateViews(mLabel, mBaseValue);
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.TRUE_ONLY.ordinal();
    }

    private static class CheckBoxListener implements OnCheckedChangeListener {
        private BaseValue value;

        public void setValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                value.value = TRUE;
            } else {
                value.value = EMPTY_FIELD;
            }
        }
    }

    private static class CheckBoxHolder {
        final TextView textLabel;
        final CheckBox checkBox;
        final CheckBoxListener listener;

        public CheckBoxHolder(TextView textLabel, CheckBox checkBox,
                              CheckBoxListener listener) {
            this.textLabel = textLabel;
            this.checkBox = checkBox;
            this.listener = listener;
        }

        public void updateViews(String valueLabel, BaseValue value) {
            listener.setValue(value);
            textLabel.setText(valueLabel);
            checkBox.setOnCheckedChangeListener(listener);

            String stringValue = value.value;
            if (TRUE.equalsIgnoreCase(stringValue)) {
                checkBox.setChecked(true);
                return;
            }

            if (isEmpty(stringValue)) {
                checkBox.setChecked(false);
            }
        }
    }
}


