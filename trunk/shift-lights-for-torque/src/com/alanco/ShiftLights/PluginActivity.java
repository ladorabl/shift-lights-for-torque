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

import com.alanco.ShiftLights.LightsView.EColorOrder;
import com.alanco.ShiftLights.LightsView.EDisplayOrder;
import com.alanco.ShiftLights.LightsView.ELightsMode;

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
import android.view.WindowManager;
import android.widget.Toast;


public class PluginActivity extends Activity  {
 
    private static final String TAG = "ShiftLightsActivityLog";
    private static final String ProfileString = "com.alanco.ShiftLights.RPM";
    private static final int RPM_PID = 0xC;
    private static final int kShiftDialogId = 0;
    
    private float oldBrightness;

    private ITorqueService torqueService;
    private Timer updateTimer;

    private boolean useDebugRPM;
    
    private Random generator;
    
    private int shiftRPM;
    
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
       
       String rpmTip = getResources().getString( R.string.enterRPMtip );
       Toast.makeText( this, rpmTip, Toast.LENGTH_SHORT ).show();
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
        
        ELightsMode mode = ELightsMode.eLedMode;
           
        try {
            mode = ELightsMode.values() [selectedPosition];
        }
        catch ( ArrayIndexOutOfBoundsException  e ) {
            e.printStackTrace();
            mode = lightsView.getDisplayMode();
        }
        
        lightsView.setDisplayMode( mode );
        
        name = r.getString( R.string.debugMode );
        useDebugRPM = preferences.getBoolean( name, false );
        
        name = r.getString( R.string.ecoMode );
        lightsView.setEcoMode( preferences.getBoolean( name, false ) );
        
        String tmp = preferences.getString( "colorOrder", "0" );
        selectedPosition = Integer.parseInt( tmp );
        EColorOrder colorOrder = EColorOrder.eGreenFirst;
        try {
            colorOrder = EColorOrder.values() [selectedPosition];
        }
        catch ( ArrayIndexOutOfBoundsException e ) {
            e.printStackTrace();
        }
        lightsView.setColorOrder( colorOrder );
        
        tmp = preferences.getString( "prefDisplayOrder", "0" );
        selectedPosition = Integer.parseInt( tmp );
        EDisplayOrder displayOrder = EDisplayOrder.eLeftToRight;
        try {
            displayOrder = EDisplayOrder.values() [selectedPosition];
        }
        catch ( ArrayIndexOutOfBoundsException e ) {
            e.printStackTrace();
        }
        lightsView.setDisplayOrder( displayOrder );
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

       if (successfulBind ) {

           WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
           
           // make as bright as possible
           oldBrightness = layoutParams.screenBrightness;
           layoutParams.screenBrightness = (float) 1.0;//WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;

           getWindow().setAttributes( layoutParams );
           
           getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
           
       }
    }

    //============================================================================================
    //
    //============================================================================================
    @Override
    protected void onPause() {
       super.onPause();
       synchronized ( this ) {
           
           if ( torqueService != null ) {
               updateTimer.cancel();
           }
    
           if ( connection != null ) {
               
               unbindService( connection );
               
               getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
               
               WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
               layoutParams.screenBrightness = oldBrightness;
               getWindow().setAttributes( layoutParams );
           }
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
                
                shiftRPM = rpmValue;
                
                lightsView.setLimitValue( rpmValue );

                synchronized( this ) {
                    
                    if ( torqueService != null ) {
                        
                        try {
                            torqueService.storeInProfile( PluginActivity.ProfileString, Float.toString( (float)rpmValue ), true );
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
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
    }
    
    //==============================================================================================
    // updateLights
    //==============================================================================================
    private void updateLights() {
        
        synchronized( this ) {
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
                        
                        int limit = lightsView.getLimitValue();
                        if ( limit <= 0 ) {
                            limit = 1;
                        }
                        lightsView.setCurrentValue( val % limit );
                    }
                }
                catch( RemoteException e ) {
                    Log.e( getClass().getCanonicalName(), e.getMessage(), e );
                }
            }
        }
    }
    
 
    /**
     * Bits of service code. You usually won't need to change this.
     */
    private ServiceConnection connection = new ServiceConnection() {
       public void onServiceConnected(ComponentName arg0, IBinder service) {
           
          if ( updateTimer != null ) {
              updateTimer.cancel();
          }
 
          ITorqueService tmpService = ITorqueService.Stub.asInterface( service );
           
          if ( shiftRPM == 0 ) {
              
              shiftRPM = 6000;
              
              if ( tmpService != null ) {
                 
                 // get the max RPM value from the vehicle profile, it it exists,
                 // to use as the initial shift value (when private info is absent) 
                 try {
                     
                     int apiVersion = tmpService.getVersion();
                     if ( apiVersion < 6 ) {
                         Toast.makeText( getApplicationContext(), R.string.wrongAPI, Toast.LENGTH_LONG ).show();
                         torqueService = null;
                         return;
                     }
                     
                     // try the private profile data first
                    String rpm = tmpService.retrieveProfileData( ProfileString );
                    
                    if ( rpm == null || rpm.length() == 0 ) {
                        
                        // nope, no private - get from the vehicle profile
                        String profile [] = tmpService.getVehicleProfileInformation();
                        
                        if ( profile != null && profile.length >= 5 ) {
                            rpm = profile [5];
                        }
    
                    }
                    if ( rpm.length() == 0 ) {
                        rpm = "6000.0";
                    }
                    try {
                        shiftRPM = (int) Float.parseFloat( rpm );
                    }
                    catch ( NumberFormatException e ) {
                        e.printStackTrace();
                    }
                 } catch (RemoteException e) {
                    Log.i( TAG, "failed to get profile" );
                 }
              }
              
              lightsView.setLimitValue( shiftRPM );
          }
          
          synchronized( this ) {
              torqueService = tmpService;
              updateTimer = new Timer();
              updateTimer.schedule(
                  new TimerTask() { 
                      public void run() {
                          updateLights();
                      }
                  }, 
                  50, 50 );
          }
          
       };
       //===============================================================================
       public void onServiceDisconnected( ComponentName name ) {
           synchronized( this ) {
               torqueService = null;
               if ( updateTimer != null ) {
                   updateTimer.cancel();
               }
           }
       };
    };
}