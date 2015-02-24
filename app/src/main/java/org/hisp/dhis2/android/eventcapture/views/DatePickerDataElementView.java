package org.hisp.dhis2.android.eventcapture.views;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;

public class DatePickerDataElementView
    extends DataElementAdapterViewAbstract
    implements TextWatcher, OnClickListener, OnDateSetListener, DialogInterface.OnClickListener
{
    /**
     * 
     */
    private EditText dataElementEditText;

    /**
     * 
     */
    private DatePickerDialog datePickerDialog;

    public DatePickerDataElementView( Context context, ProgramStageDataElement programStageDataElement,
        DataElement dataElement, DataValue dataValue )
    {
        super( context, programStageDataElement, dataElement, dataValue );
    }

    @Override
    public void beforeTextChanged( CharSequence s, int start, int count, int after )
    {
        dataValue.value = this.dataElementEditText.getText().toString();
    }

    @Override
    public void onTextChanged( CharSequence s, int start, int before, int count )
    {
        dataValue.value = this.dataElementEditText.getText().toString();
    }

    @Override
    public void afterTextChanged( Editable s )
    {
        dataValue.value = this.dataElementEditText.getText().toString();
    }

    @Override
    public View getView()
    {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        View view = inflater.inflate( R.layout.listview_item_datepicker, null );

        TextView tv = (TextView) view.findViewById( R.id.dateNameTextView );
        tv.setText( dataElement.getName() );

        TextView mandatoryEt = (TextView) view.findViewById( R.id.datePickerMandatoryTextView );
        if ( programStageDataElement.isCompulsory() )
        {
            mandatoryEt.setVisibility( View.VISIBLE );
        }
        else
        {
            mandatoryEt.setVisibility( View.INVISIBLE );
        }
        this.getDatePickerDialog();
        dataElementEditText = (EditText) view.findViewById( R.id.dateEditText );
        dataElementEditText.addTextChangedListener( this );
        dataElementEditText.setClickable( true );
        dataElementEditText.setOnClickListener( this );
        dataElementEditText.setHint( "YYYY-MM-DD" );
        return view;
    }

    @Override
    public void onClick( View v )
    {
        this.getDatePickerDialog().show();
    }

    public EditText getDataElementEditText()
    {
        return dataElementEditText;
    }

    public void setDataElementEditText( EditText dataElementEditText )
    {
        this.dataElementEditText = dataElementEditText;
    }

    public DatePickerDialog getDatePickerDialog()
    {
        if ( datePickerDialog == null )
        {
            Calendar calendar = Calendar.getInstance();
            int mYear, mMonth, mDay;
            mYear = calendar.get( Calendar.YEAR );
            mMonth = calendar.get( Calendar.MONTH );
            mDay = calendar.get( Calendar.DAY_OF_MONTH );
            datePickerDialog = new DatePickerDialog( getContext(), this, mYear, mMonth, mDay );
            datePickerDialog.setCancelable( true );
            datePickerDialog.setButton( DialogInterface.BUTTON_NEGATIVE, getContext().getString( R.string.cancel ),
                this );
            datePickerDialog.setButton( DialogInterface.BUTTON_POSITIVE, getContext().getString( R.string.set ), this );
        }
        return datePickerDialog;
    }

    public void setDatePickerDialog( DatePickerDialog datePickerDialog )
    {
        this.datePickerDialog = datePickerDialog;
    }

    @Override
    public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
    {

    }

    @Override
    public void onClick( DialogInterface dialog, int which )
    {
        if ( which == DialogInterface.BUTTON_NEGATIVE )
        {
            getDatePickerDialog().cancel();
        }
        else
        {
            DatePicker datePicker = getDatePickerDialog().getDatePicker();

            int year = datePicker.getYear();
            int dayOfMonth = datePicker.getDayOfMonth();
            int monthOfYear = datePicker.getMonth();

            String month = monthOfYear + 1 + "";
            if ( month.length() == 1 )
            {
                month = "0" + month;
            }

            String date = String.valueOf( dayOfMonth );

            if ( date.length() == 1 )
            {
                date = "0" + date;
            }

            dataElementEditText.setText( year + "-" + month + "-" + date );
        }

    }

}
