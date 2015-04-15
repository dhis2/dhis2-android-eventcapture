package org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramIndicator;

public final class IndicatorRow implements DataEntryRow {
    private static final String EMPTY_FIELD = "";

    private final ProgramIndicator mIndicator;
    private String mValue;

    public IndicatorRow(ProgramIndicator indicator, String value) {
        mIndicator = indicator;
        mValue = value;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        ValueEntryHolder holder;
        if (convertView == null) {
            View root = inflater.inflate(
                    R.layout.listview_row_indicator, container, false);
            holder = new ValueEntryHolder(
                    (TextView) root.findViewById(R.id.text_label),
                    (TextView) root.findViewById(R.id.indicator_row)
            );

            root.setTag(holder);
            view = root;
        } else {
            view = convertView;
            holder = (ValueEntryHolder) view.getTag();
        }

        if (mIndicator.name != null) {
            holder.textLabel.setText(mIndicator.name);
        } else {
            holder.textLabel.setText(EMPTY_FIELD);
        }

        holder.textValue.setText(mValue);
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.INDICATOR.ordinal();
    }

    public void updateValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    public ProgramIndicator getIndicator() {
        return mIndicator;
    }

    private static class ValueEntryHolder {
        final TextView textLabel;
        final TextView textValue;

        public ValueEntryHolder(TextView textLabel,
                                TextView textValue) {
            this.textLabel = textLabel;
            this.textValue = textValue;
        }
    }
    //android:inputType="number|numberDecimal|numberSigned"
}
