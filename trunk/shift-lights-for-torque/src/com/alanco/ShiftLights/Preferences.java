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


import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnPreferenceChangeListener {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        addPreferencesFromResource( R.xml.preferences );
        
        Resources r = this.getResources();
        
        String rpmKey = r.getString( R.string.showRPM ); 

        findPreference( rpmKey ).setOnPreferenceChangeListener( this );
    }
    
    //====================================================================
    // if RPM setting is true, then eco mode cannot be enabled
    //===================================================================
    @Override
    public boolean onPreferenceChange( Preference pref, Object newValue ) {
     
        final String val = newValue.toString();
        
        if ( val.equals( "true" ) ) {

            Resources r = this.getResources();
    
            String ecoKey = r.getString( R.string.ecoMode );

            CheckBoxPreference cbp = (CheckBoxPreference) findPreference( ecoKey );
            cbp.setChecked( false );
        }
        return true;
    }
 }
