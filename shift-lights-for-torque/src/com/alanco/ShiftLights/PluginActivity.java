/* ShiftLights - a plug-in for Torque Android application
    Copyright (C) 2011  Alex Bakaev

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.alanco.ShiftLights;

import org.prowl.torque.remote.ITorqueService;

import com.alanco.ShiftLights.LightsView.LightsMode;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;


//import android.app.Service;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.IBinder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;

import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class PluginActivity extends Activity  {
 
    private static final String TAG = "ShiftLightsActivityLog";
    private static final String ProfileString = "com.alanco.ShiftLights.RPM";
    private static final int RPM_PID = 0xC;
    private static final int kShiftDialogId = 0;
    
   
    private ITorqueService torqueService;
    private Timer updateTimer;

    private boolean useDebugRPM;
    
    private Random generator;
    
    //private int shiftRPM;
    
    private LightsView lightsView;
    
    //===========================================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
       super.onCreate(savedInstanceState);
       
       lightsView = new LightsView( this );
       setContentView( lightsView );
            
       activatePreferences();
       
       setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
       
       generator = new Random();
    }
    
    //===============================================================================
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate( R.menu.menu, menu );
        return true;
    }

    //================================================================================
    void activatePreferences() {
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
        
        Resources r = this.getResources();

        String name = r.getString( R.string.showRPM ); 
        boolean showRPM = preferences.getBoolean( name , false );
        
        lightsView.setShowRPM( showRPM );

        name = r.getString( R.string.listPref );
        int selectedPosition = Integer.parseInt( preferences.getString( name, "0" ) );
        
        LightsMode mode = lightsView.getDisplayMode();
                
        switch ( selectedPosition ) {
            case 0:
                mode = LightsMode.eLedMode;
                break;
            case 1:
                mode = LightsMode.eScreenMode;
                break;
            case 2:
                mode = LightsMode.eBarMode;
                break;
            case 3:
                mode = LightsMode.eFlash;
                break;
        }
        lightsView.setDisplayMode( mode );
        
        name = r.getString( R.string.debugMode );
        useDebugRPM = preferences.getBoolean( name, false );
        
        name = r.getString( R.string.ecoMode );
        lightsView.setEcoMode( preferences.getBoolean( name, false ) );
    }
    
    //================================================================================
    void displayPreferences() {
        
        Intent preferencesIntent = new Intent( this, Preferences.class );
        //startActivity( preferencesIntent );
        startActivityForResult( preferencesIntent, 20 );
        
        /*
        final String items [] = { "LEDs", "Entire Screen", "Bars" };

        LightsMode mode = lightsView.getDisplayMode();
        int initialIndex = mode.ordinal();
        
        AlertDialog.Builder dialog = new AlertDialog.Builder( this );
        dialog.setTitle( R.string.prefDialogTitle )
      
        .setSingleChoiceItems( items, initialIndex, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        } )
        
        .setPositiveButton( "OK", new DialogInterface.OnClickListener() {
             
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
                dialog.dismiss();
                
                // this seems to be a safe trick (cast) to use
                // an alternative is to implement a local (to the activity) OnClickListener for the
                // setSingleChoiceItems() method and set a class variable that can be used here
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                
                LightsMode mode = LightsMode.eLedMode;
                switch ( selectedPosition ) {
                    case 1:
                        mode = LightsMode.eScreenMode;
                        break;
                    case 2:
                        mode = LightsMode.eBarMode;
                        break;
                }
                lightsView.setDisplayMode( mode );
            }
        } )
        
        .setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        } )
        
        .show();
        */
        
    }
    
    //======================================================================================
    //
    //======================================================================================
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        
       super.onActivityResult( requestCode, resultCode, data );
       activatePreferences();
    }

    //======================================================================================
    //
    //======================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch ( item.getItemId() ) {
        case R.id.shiftValueStr:
            enterShiftValue();
            break;
        case R.id.prefsButton:
            displayPreferences();
            break;
        }
        return true;
    }
    
    //======================================================================================
    //
    //======================================================================================
    @Override
    protected void onResume() {
        
       super.onResume();

       // Bind to the torque service
       Intent intent = new Intent();
       intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService");
       boolean successfulBind = bindService(intent, connection, 0);

       if (successfulBind) {

           updateTimer = new Timer();
           updateTimer.schedule(new TimerTask() { public void run() {
             updateLights();
          }}, 50, 50 );
       }
    }

    //============================================================================================
    //
    //============================================================================================
    @Override
    protected void onPause() {
       super.onPause();
       updateTimer.cancel();
       if ( connection != null ) {
           unbindService( connection );
       }
    }

    //==============================================================================================
    // onCreateDialog
    //==============================================================================================
    @Override
    protected Dialog onCreateDialog( int id ) {
        if ( id == kShiftDialogId ) {
            final RPMInputDialog dlg = new RPMInputDialog( this, lightsView.getLimitValue() );
             dlg.setOnDismissListener( dlgIntf );
             return dlg;
        }
        return null;
    }
    
    //==============================================================================================
    //
    //==============================================================================================
    private DialogInterface.OnDismissListener dlgIntf = new DialogInterface.OnDismissListener() {
        
        @Override
        public void onDismiss(DialogInterface dialog) {
            RPMInputDialog rpmDlg = (RPMInputDialog)dialog;
            int rpmValue = rpmDlg.getRpmValue();
            if ( rpmValue < 1000 ) {
                Toast.makeText( getBaseContext(), "Really? Enter valid (1000+) number", Toast.LENGTH_SHORT ).show();
            } else {
                lightsView.setLimitValue( rpmValue );
                if ( torqueService != null ) {
                    try {
                        torqueService.storeInProfile( PluginActivity.ProfileString, Float.toHexString( (float)rpmValue ), true );
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }; 
    
    //==============================================================================================
    //enterShiftValue
    //==============================================================================================
    private void enterShiftValue() {
        
        showDialog( kShiftDialogId );
        
        /* commented out - an example of a manual way to create an input dialog
        LinearLayout layout = new LinearLayout( this );
        
        EditText inputBox = new EditText( this );
        inputBox.setText( "7500" );
        layout.addView( inputBox );

        Button okBtn = new Button( this );
        okBtn.setText( "OK" );
        okBtn.setOnClickListener( new View.OnClickListener() { 

            @Override
            public void onClick(View arg0) {
                String value = inputBox.getText().toString();
                setContentView( R.layout.main );
            }
        } );
        
        layout.addView( okBtn );
        
        ScrollView scrollView = new ScrollView( this );

        scrollView.addView( layout );
        
        setContentView( scrollView );
         
        */
    }
    
    //==============================================================================================
    // updateLights
    //==============================================================================================
    private void updateLights() {
        
        if ( torqueService != null ) {
            
            try {
                
                float rpms = torqueService.getValueForPid( RPM_PID, true );
                        
                if ( rpms != 0 || !useDebugRPM ) {
                    lightsView.setCurrentValue( (int) rpms );
                } else {
                    int val = generator.nextInt();
                    if ( val < 0 ) {
                        val = -val;
                    }
                    
                    lightsView.setCurrentValue( val % lightsView.getLimitValue() );
                }
            }
            catch( RemoteException e ) {
                Log.e( getClass().getCanonicalName(), e.getMessage(), e );
            }
        }
    }
    
 
    /**
     * Bits of service code. You usually won't need to change this.
     */
    private ServiceConnection connection = new ServiceConnection() {
       public void onServiceConnected(ComponentName arg0, IBinder service) {
          torqueService = ITorqueService.Stub.asInterface( service );
           
          if ( torqueService != null ) {
             int rpmValue = 6000;
             try {
                String rpm = torqueService.retrieveProfileData( ProfileString );
                if ( rpm == null || rpm.isEmpty() ) {
                    String profile [] = torqueService.getVehicleProfileInformation();
                    rpm = profile [5];
                }
                if ( rpm.isEmpty() ) {
                    rpm = "6000.0";
                }
                rpmValue = (int) Float.parseFloat( rpm );
             } catch (RemoteException e) {
                Log.i( TAG, "failed to get profile" );
             }
             lightsView.setLimitValue( rpmValue );
          }
       };
       public void onServiceDisconnected(ComponentName name) {
          torqueService = null;
       };
    };
}