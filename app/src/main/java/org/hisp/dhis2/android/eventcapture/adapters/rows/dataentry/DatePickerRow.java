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

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.persistence.models.BaseValue;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class DatePickerRow implements DataEntryRow {
    private static final String EMPTY_FIELD = "";

    private String mLabel;
    private BaseValue mValue;

    public DatePickerRow(String label, BaseValue value) {
        mLabel = label;
        mValue = value;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        DatePickerRowHolder holder;

        if (convertView == null) {
            View root = inflater.inflate(
                    R.layout.listview_row_datepicker, container, false);

            TextView textLabel = (TextView)
                    root.findViewById(R.id.text_label);
            ImageButton clearButton = (ImageButton)
                    root.findViewById(R.id.clear_edit_text);
            EditText pickerInvoker = (EditText)
                    root.findViewById(R.id.date_picker_edit_text);

            DateSetListener dateSetListener = new DateSetListener();
            OnEditTextClickListener invokerListener = new OnEditTextClickListener(inflater.getContext());
            ClearButtonListener clearButtonListener = new ClearButtonListener();

            holder = new DatePickerRowHolder(textLabel, pickerInvoker, clearButton,
                    clearButtonListener, dateSetListener, invokerListener);

            root.setTag(holder);
            view = root;
        } else {
            view = convertView;
            holder = (DatePickerRowHolder) view.getTag();
        }

        holder.updateViews(mLabel, mValue);
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.DATE.ordinal();
    }

    private class DatePickerRowHolder {
        final TextView textLabel;
        final EditText editText;
        final ImageButton clearButton;

        final DateSetListener dateSetListener;
        final OnEditTextClickListener invokerListener;
        final ClearButtonListener clearButtonListener;

        public DatePickerRowHolder(TextView textLabel, EditText editText,
                                   ImageButton clearButton, ClearButtonListener clearButtonListener,
                                   DateSetListener dateSetListener, OnEditTextClickListener invokerListener) {
            this.textLabel = textLabel;
            this.editText = editText;
            this.clearButton = clearButton;

            this.dateSetListener = dateSetListener;
            this.invokerListener = invokerListener;
            this.clearButtonListener = clearButtonListener;
        }

        public void updateViews(String label, BaseValue baseValue) {
            textLabel.setText(label);

            dateSetListener.setBaseValue(baseValue);
            dateSetListener.setEditText(editText);
            invokerListener.setListener(dateSetListener);

            editText.setText(label);
            editText.setOnClickListener(invokerListener);

            clearButtonListener.setEditText(editText);
            clearButtonListener.setBaseValue(baseValue);
            clearButton.setOnClickListener(clearButtonListener);
        }
    }

    private static class OnEditTextClickListener implements OnClickListener {
        private DateSetListener listener;
        private LocalDate currentDate;
        private Context context;

        public OnEditTextClickListener(Context context) {
            this.context = context;
            currentDate = new LocalDate();
        }

        public void setListener(DateSetListener listener) {
            this.listener = listener;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            DatePickerDialog picker = new DatePickerDialog(context, listener,
                    currentDate.getYear(), currentDate.getMonthOfYear() - 1, currentDate.getDayOfMonth());
            picker.getDatePicker().setMaxDate(DateTime.now().getMillis());
            picker.show();
        }
    }

    private static class ClearButtonListener implements OnClickListener {
        private EditText editText;
        private BaseValue value;

        public void setEditText(EditText editText) {
            this.editText = editText;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onClick(View view) {
            editText.setText(EMPTY_FIELD);
            value.value = EMPTY_FIELD;
        }
    }

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private static final String DATE_FORMAT = "YYYY-MM-dd";
        private BaseValue value;
        private EditText editText;

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        public void setEditText(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
            String newValue = date.toString(DATE_FORMAT);
            editText.setText(newValue);
            value.value = newValue;
        }
    }
}
