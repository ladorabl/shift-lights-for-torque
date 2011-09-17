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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class LightsView extends View {

    public enum LightsMode {
        eLedMode, eScreenMode, eBarMode, eFlash
    }
    private LightsMode displayMode;
    
    private int limitValue;
    private int currentValue;
    
    private Paint redPaint;
    private Paint greenPaint;
    private Paint yellowPaint;
    private Paint orangePaint;
    private Paint textPaint;
    
    private Bitmap greenBitmap;
    private Bitmap yellowBitmap;
    private Bitmap redBitmap;
    private Bitmap orangeBitmap;

    private boolean showRPM;

    private int cnt;
    
    private static final int kGreenLEDs =  6;
    private static final int kYellowLEDs = 6;
    private static final int kOrangeLEDs = 6;
// implicit    private static final int kRedLEDs    = 2;
    
    public LightsView(Context context) {
        super(context);
        
        initLightsView();
    }
    
    public LightsView( Context context, AttributeSet attrSet ) {
        super( context, attrSet );
        initLightsView();
    }
    public LightsView( Context context, AttributeSet attrs, int defaultStyle ) {
        super( context, attrs, defaultStyle );
        initLightsView();
    } 
    
    //========================================================================
    private void initLightsView() {
         
        redPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        redPaint.setColor( Color.RED );//r.getColor( R.color.redColor )  );
        redPaint.setStyle( Paint.Style.FILL );
        
        textPaint = redPaint;
        textPaint.setStrokeWidth( (float) 20.0 );
        textPaint.setTextSize( textPaint.getTextSize() * 8 );
        textPaint.setTextScaleX( (float) 3.0 );
        
        greenPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        greenPaint.setColor( Color.GREEN );
        
        yellowPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        yellowPaint.setColor( Color.YELLOW );
        
        orangePaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        orangePaint.setColor( Color.rgb( 255, 165, 0 ) );
        
        currentValue = 1;

        // get LED bitmaps
        yellowBitmap = createBitmap( R.drawable.yellow );

        greenBitmap  = createBitmap( R.drawable.green );
        
        redBitmap    = createBitmap( R.drawable.red );

        orangeBitmap = createBitmap( R.drawable.orange );
    }
    
    //=================================================================
    private Bitmap createBitmap( int resId ) {

        Resources r = this.getResources();

        Drawable tmpDrawable = r.getDrawable( resId );
        BitmapDrawable bitmapDrawable = (BitmapDrawable) tmpDrawable;
        return bitmapDrawable.getBitmap();
    }
    
    //========================================================================
    //
    //========================================================================
    @Override
    protected void onMeasure( int wMeasureSpec, int hMeasureSpec ) {
        int measuredWidth = measure( wMeasureSpec );
        int measuredHeight = measure( hMeasureSpec );
        
        setMeasuredDimension( measuredWidth, measuredHeight );

        resizeLEDs();
    }
    
    //=========================================================================
    private int measure( int spec ) {
        
        int result = 0;
        
        int specMode = MeasureSpec.getMode( spec );
        int specSize = MeasureSpec.getSize( spec );
        
        if ( specMode == MeasureSpec.UNSPECIFIED ) {
            result = 200;
        } else {
            result = specSize;
        }
        return result;
    }

    //========================================================================
    void resizeLEDs() {
        
        int measuredWidth = getMeasuredWidth();
        if ( measuredWidth != 0 ) {
            
            int ledWidth = measuredWidth / 20;
            int ledHeight = ledWidth;
            
            if ( yellowBitmap.getWidth() != ledWidth ) {
                yellowBitmap = Bitmap.createScaledBitmap( yellowBitmap, ledHeight, ledWidth, true );
                greenBitmap  = Bitmap.createScaledBitmap( greenBitmap,  ledHeight, ledWidth, true );
                orangeBitmap = Bitmap.createScaledBitmap( orangeBitmap, ledHeight, ledWidth, true );
                redBitmap    = Bitmap.createScaledBitmap( redBitmap,    ledHeight, ledWidth, true );
            }
        }
    }
    
    //=========================================================================
    void setShowRPM( boolean value ) {
        showRPM = value;
    }
    //=========================================================================
    public void setDisplayMode( LightsMode mode ) {
        displayMode = mode;
    }
    
    //=========================================================================
    public LightsMode getDisplayMode() {
        return displayMode;
    }
    
    //=========================================================================
    public void setLimitValue( int value ) {
        limitValue = value;
        postInvalidate();
    }
    
    //=============================================================
    public int getLimitValue() {
        return limitValue;
    }
    
    //==========================================================================
    public void setCurrentValue( int value ) {
        currentValue = value;
        
        int tmp = currentValue * 20 / limitValue;

        if ( tmp != cnt ) {
            cnt = tmp;
            postInvalidate();
        }
    }
    
    //=============================================================================
    private void drawLEDs( Canvas canvas ) {
        
        int measuredHeight = getMeasuredHeight();

        int px = 0;
        int py = measuredHeight / 2;
        
        py -= yellowBitmap.getHeight() / 2;
        
        Matrix matrix = new Matrix();
        matrix.reset();
      
        for ( int i = 0; i < cnt; ++i ) {
            
            Bitmap tmp = null;
            
            if ( i < kGreenLEDs ) { 
                tmp = greenBitmap;
            } else {
                if ( i < kYellowLEDs + kGreenLEDs ) {
                    tmp = yellowBitmap;
                } else {
                    if ( i < kYellowLEDs + kGreenLEDs + kOrangeLEDs ) {
                        tmp = orangeBitmap;
                    } else {
                        tmp = redBitmap;
                    }
                }
            } 
            canvas.drawBitmap( tmp, px, py, null );
            px += tmp.getWidth();
        }
        
        if ( showRPM ) {
            
            String rpm = Integer.toString( currentValue );
            
            px = ( getMeasuredWidth() - (int)textPaint.measureText( rpm ) ) / 2;
            canvas.drawText( rpm, px, py / 2, textPaint );
            
        }
    }
    
    //=============================================================================
    private void drawRPMText( Canvas canvas ) {
        if ( showRPM ) {
            
            String rpm = Integer.toString( currentValue );
            
            int px = ( getMeasuredWidth() - (int)textPaint.measureText( rpm ) ) / 2;
            int py = getMeasuredHeight() / 2;
            canvas.drawText( rpm, px, py, textPaint );
        }
    }
    
    //=============================================================================
    private void drawScreen( Canvas canvas ) {
        
        int col = Color.GRAY;
        
        if ( cnt < kGreenLEDs ) { 
            col = Color.GREEN;
        } else {
            if ( cnt < kYellowLEDs + kGreenLEDs ) {
                col = Color.YELLOW;
            } else {
                if ( cnt < kYellowLEDs + kGreenLEDs + kOrangeLEDs ) {
                    col = Color.rgb( 255, 165, 0 );
                } else {
                    col = Color.RED;
                }
            }
        }
        canvas.drawColor( col );    
        
        drawRPMText( canvas );
    }
    
    //=============================================================================
    private void drawBars( Canvas canvas ) {
        
        int measuredHeight = getMeasuredHeight();
    
        int width = yellowBitmap.getWidth();
        
        float left = 0;
        for ( int i = 0; i < cnt; ++i ) {

            Paint tmp = null;
            
            if ( i < kGreenLEDs ) { 
                tmp = greenPaint;
            } else {
                if ( i < kYellowLEDs + kGreenLEDs ) {
                    tmp = yellowPaint;
                } else {
                    if ( i < kYellowLEDs + kGreenLEDs + kOrangeLEDs ) {
                        tmp = orangePaint;
                    } else {
                        tmp = redPaint;
                    }
                }
            }

            canvas.drawRect( left, 0, left + width, measuredHeight, tmp );
            left += width;
        }

        drawRPMText( canvas );
    }
    
    //=============================================================================    
    private void drawFlash( Canvas canvas ) {
    
        if ( cnt >= kGreenLEDs + kYellowLEDs + kOrangeLEDs ) {
            canvas.drawColor( Color.RED );
        }
        
        drawRPMText( canvas );
    }
    
    //=============================================================================
    // onDraw
    //=============================================================================
    @Override
    protected void onDraw( Canvas canvas ) {
        
        super.onDraw( canvas );
        
        switch ( displayMode ) {
            case eLedMode:
                drawLEDs( canvas );
                break;
            case eScreenMode:
                drawScreen( canvas );
                break;
            case eBarMode:
                drawBars( canvas );
                break;
            case eFlash: 
                drawFlash( canvas );
                break;
        }
    }
}
