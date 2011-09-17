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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RPMInputDialog extends Dialog {

    private int rpmValue;
    
    public RPMInputDialog( Context context, int value ) {
     
        super( context );

        rpmValue = value;
    }
    
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.dialog );

        EditText text = (EditText) findViewById( R.id.editText1 );
        text.setText( Integer.toString( rpmValue ) );
        
        Button button = (Button) findViewById( R.id.button1 );
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                EditText text = (EditText) findViewById( R.id.editText1 );
                rpmValue = Integer.parseInt( text.getText().toString() ); 
                dismiss();
            }
        });
    }
    
    public int getRpmValue() {
        return rpmValue;
    }
}
