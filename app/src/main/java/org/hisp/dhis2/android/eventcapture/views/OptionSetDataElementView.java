package org.hisp.dhis2.android.eventcapture.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.hisp.dhis2.android.eventcapture.R;
import org.hisp.dhis2.android.sdk.controllers.MetaDataController;
import org.hisp.dhis2.android.sdk.persistence.models.DataElement;
import org.hisp.dhis2.android.sdk.persistence.models.DataValue;
import org.hisp.dhis2.android.sdk.persistence.models.Option;
import org.hisp.dhis2.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis2.android.sdk.persistence.models.ProgramStageDataElement;

public class OptionSetDataElementView
    extends DataElementAdapterViewAbstract
    implements OnItemSelectedListener
{
    
    /**
     * 
     */
    private Spinner spinner;

    /**
     * 
     */
    private OptionSet optionSet;

    /**
     * 
     */
    private ArrayAdapter<String> adapter;
    
    public OptionSetDataElementView( Context context, ProgramStageDataElement programStageDataElement,
            DataElement dataElement, DataValue dataValue)
        {
            super( context, programStageDataElement, dataElement, dataValue);
        }

    @Override
    public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
    {
        String please_select = this.getContext().getString( R.string.please_select );
        String value = (String) spinner.getItemAtPosition( position );
        this.dataValue.value = value;
    }

    @Override
    public void onNothingSelected( AdapterView<?> parent )
    {
    }

    @Override
    public View getView()
    {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        View view = inflater.inflate( R.layout.listview_item_optionset, null );

        TextView tv = (TextView) view.findViewById( R.id.optionSetNameTextView );
        tv.setText( dataElement.getName() );

        TextView mandatoryEt = (TextView) view.findViewById( R.id.optionSetMandatoryTextView );

        if ( programStageDataElement.isCompulsory() )
        {
            mandatoryEt.setVisibility(View.VISIBLE);
        }
        else
        {
            mandatoryEt.setVisibility(View.INVISIBLE);
        }

        spinner = (Spinner) view.findViewById( R.id.optionSetValueSpinner );
        spinner.setOnItemSelectedListener( this );

        this.optionSet = MetaDataController.getOptionSet(dataElement.getOptionSet());

        List<Option> options = optionSet.getOptions();
        List<String> optionNames = new ArrayList<String>();
        for(Option option: options) {
            optionNames.add(option.getName());
        }

        //options.add( this.getContext().getString( R.string.please_select ) );

        adapter = new ArrayAdapter<String>( this.getContext(),
                R.layout.spinner_item, optionNames );

        spinner.setAdapter( adapter );

        return view;
    }

    /**
     * 
     * @return
     */
    public Spinner getSpinner()
    {
        return spinner;
    }

    /**
     * 
     * @param spinner
     */
    public void setSpinner( Spinner spinner )
    {
        this.spinner = spinner;
    }

    /**
     * 
     * @return
     */
    public OptionSet getOptionSet()
    {
        return optionSet;
    }

    /**
     * 
     * @param optionSet
     */
    public void setOptionSet( OptionSet optionSet )
    {
        this.optionSet = optionSet;
    }
}
