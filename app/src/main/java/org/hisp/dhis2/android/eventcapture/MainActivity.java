package org.hisp.dhis2.android.eventcapture;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.fragments.RegisterEventFragment;
import org.hisp.dhis2.android.eventcapture.fragments.SelectProgramFragment;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;


public class MainActivity extends ActionBarActivity {

    private CharSequence title;

    private SelectProgramFragment selectProgramFragment;
    private RegisterEventFragment registerEventFragment;

    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dhis2Application.bus.register(this);
        setContentView(R.layout.activity_main);
        showSelectProgramFragment();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle( CharSequence title )
    {
        this.title = title;
        getSupportActionBar().setTitle( this.title );
    }

    @Subscribe
    public void onReceiveMessage(MessageEvent event) {
        if(event.eventType == BaseEvent.EventType.showRegisterEventFragment) {
            showRegisterEventFragment();
        } else if(event.eventType == BaseEvent.EventType.showSelectProgramFragment) {
            showSelectProgramFragment();
        }
    }

    public void showRegisterEventFragment() {
        setTitle("Register Event");
        if(registerEventFragment == null) registerEventFragment = new RegisterEventFragment();
        OrganisationUnit organisationUnit = selectProgramFragment.getSelectedOrganisationUnit();
        Program program = selectProgramFragment.getSelectedProgram();
        registerEventFragment.setSelectedOrganisationUnit(organisationUnit);
        registerEventFragment.setSelectedProgram(program);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace
                ( R.id.fragment_container, registerEventFragment ).commit();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.commit();
        currentPosition = 1;
    }

    public void showSelectProgramFragment() {
        setTitle("Event Capture");
        if(selectProgramFragment == null) selectProgramFragment = new SelectProgramFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace
                ( R.id.fragment_container, selectProgramFragment ).commit();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.commit();
        currentPosition = 0;
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if ( (keyCode == KeyEvent.KEYCODE_BACK) )
        {
            if ( currentPosition == 0 )
            {
                Dhis2.getInstance().showConfirmDialog(this, getString(R.string.confirm),
                        getString(R.string.exit_confirmation), getString(R.string.yes_option),
                        getString(R.string.no_option),
                 new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        finish();
                        System.exit( 0 );
                    }
                } );
            }
            else
            {
                Dhis2.getInstance().showConfirmDialog(this, getString(R.string.discard),
                        getString(R.string.discard_confirm), getString(R.string.yes_option),
                        getString(R.string.no_option),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialog, int which )
                            {
                                showSelectProgramFragment();
                                registerEventFragment = null;
                            }
                        } );
            }
            return true;
        }

        return super.onKeyDown( keyCode, event );
    }
}
