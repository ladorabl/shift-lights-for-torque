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

    // determines how the shift lights are drawn
    public enum ELightsMode {
        eLedMode, eScreenMode, eBarMode, eFlash
    }
    private ELightsMode displayMode;
    
    // determines what color is drawn first and which is last (between red and green)
    public enum EColorOrder {
        eGreenFirst, eRedFirst
    }
    private EColorOrder colorOrder = EColorOrder.eGreenFirst;
    
    // determintes the mode of drawing
    public enum EDisplayOrder {
        eLeftToRight, eOutsideIn
    }
    private EDisplayOrder displayOrder = EDisplayOrder.eLeftToRight;
    
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

    private boolean ecoMode;

    
    private static final int kFirstLEDs    = 6;
    private static final int kSecondLEDs = 6;
    private static final int kThirdLEDs = 5;
    // implicit private static final int kGreenLEDs =  3;
    
    
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
        
        greenPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        greenPaint.setColor( Color.GREEN );

        textPaint = new Paint( greenPaint );
        textPaint.setStrokeWidth( (float) 20.0 );
        textPaint.setTextSize( textPaint.getTextSize() * 8 );
        textPaint.setTextScaleX( (float) 3.0 );

        yellowPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        yellowPaint.setColor( Color.YELLOW );
         
        orangePaint = new Paint( Paint.ANTI_ALIAS_FLAG );
        orangePaint.setColor( Color.rgb( 255, 165, 0 ) );
         
        currentValue = 1;
 
        // get LED bitmaps
        yellowBitmap = createBitmap( R.drawable.double_yellow );

        greenBitmap  = createBitmap( R.drawable.double_green );
        
        redBitmap    = createBitmap( R.drawable.double_red );

        orangeBitmap = createBitmap( R.drawable.double_orange );
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
            int ledHeight = ledWidth * 2;
            
            if ( yellowBitmap.getWidth() != ledWidth ) {
                yellowBitmap = Bitmap.createScaledBitmap( yellowBitmap, ledWidth, ledHeight, true );
                greenBitmap  = Bitmap.createScaledBitmap( greenBitmap,  ledWidth, ledHeight, true );
                orangeBitmap = Bitmap.createScaledBitmap( orangeBitmap, ledWidth, ledHeight, true );
                redBitmap    = Bitmap.createScaledBitmap( redBitmap,    ledWidth, ledHeight, true );
            }
        }
    }
    
    //=========================================================================
    void setShowRPM( boolean value ) {
        showRPM = value;
    }
    
    //=========================================================================
    public void setDisplayMode( ELightsMode mode ) {
        displayMode = mode;
    }
    
    //=========================================================================
    public ELightsMode getDisplayMode() {
        return displayMode;
    }
    
    //=========================================================================
    public void setColorOrder( EColorOrder order ) {
        colorOrder = order;
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            textPaint.setColor( Color.RED );
        } else {
            textPaint.setColor( Color.GREEN );
        }
    }
    
    //=========================================================================
    public void setDisplayOrder( EDisplayOrder order ) {
        displayOrder = order;
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
    
    public void setEcoMode( boolean val ) {
        ecoMode = val;
    }
    //==========================================================================
    public void setCurrentValue( int value ) {
        currentValue = value;
        
        if ( limitValue != 0 ) {
            
            int tmp = currentValue * 20 / limitValue;
            
            if ( tmp != cnt || !ecoMode ) {
                cnt = tmp;
                postInvalidate();
            }
        }
    }
    
    //=============================================================================
    // draw the LEDs starting at the ends growing towards the center
    //=============================================================================
    private void drawLEDsOutsideIn( Canvas canvas ) {

        int measuredHeight = getMeasuredHeight();
        int measuredWidth  = getMeasuredWidth();
        
        int iterations = ( cnt + 1 ) / 2;
        
        int px1 = 0;
        int px2 = measuredWidth - yellowBitmap.getWidth();
        
        int py = measuredHeight / 2;
        
        py -= yellowBitmap.getHeight() / 2;
 
        Bitmap firstBitmap;
        Bitmap lastBitmap;
        
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            firstBitmap = greenBitmap;
            lastBitmap  = redBitmap;
        } else {
            firstBitmap = redBitmap;
            lastBitmap  = greenBitmap;
        }

        for ( int i = 0, position = 0; i < iterations; ++i ) {
        
            Bitmap tmp = null;
            
            if ( position < kFirstLEDs ) { 
                tmp = firstBitmap;
            } else {
                if ( position < kSecondLEDs + kFirstLEDs ) {
                    tmp = yellowBitmap;
                } else {
                    if ( position < kFirstLEDs + kSecondLEDs + kThirdLEDs ) {
                        tmp = orangeBitmap;
                    } else {
                        tmp = lastBitmap;
                    }
                }
            } 
            position += 2;
            
            canvas.drawBitmap( tmp, px1, py, null );
            canvas.drawBitmap( tmp, px2, py, null );

            px1 += yellowBitmap.getWidth();
            px2 -= yellowBitmap.getWidth();
        }
    }

    //=============================================================================
    private void drawLEDsLeft2Right( Canvas canvas ) {
    
        int measuredHeight = getMeasuredHeight();

        int px = 0;
        int py = measuredHeight / 2;
        
        py -= yellowBitmap.getHeight() / 2;
        
        Bitmap firstBitmap;
        Bitmap lastBitmap;
        
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            firstBitmap = greenBitmap;
            lastBitmap  = redBitmap;
        } else {
            firstBitmap = redBitmap;
            lastBitmap  = greenBitmap;
        }
        
        for ( int i = 0; i < cnt; ++i ) {
            
            Bitmap tmp = null;
            
            if ( i < kFirstLEDs ) { 
                tmp = firstBitmap;
            } else {
                if ( i < kSecondLEDs + kFirstLEDs ) {
                    tmp = yellowBitmap;
                } else {
                    if ( i < kFirstLEDs + kSecondLEDs + kThirdLEDs ) {
                        tmp = orangeBitmap;
                    } else {
                        tmp = lastBitmap;
                    }
                }
            } 
            canvas.drawBitmap( tmp, px, py, null );
            px += tmp.getWidth();
        }
    }
    
    //=============================================================================
    private void drawLEDs( Canvas canvas ) {
        
        if ( displayOrder == EDisplayOrder.eOutsideIn ) {
            drawLEDsOutsideIn( canvas );
           
        } else {
            drawLEDsLeft2Right( canvas );
        }
        if ( showRPM ) {
            
            String rpm = Integer.toString( currentValue );
            
            int px = ( getMeasuredWidth() - (int)textPaint.measureText( rpm ) ) / 2;
            int py = getMeasuredHeight() / 2;
            py -= yellowBitmap.getHeight() / 2;

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

        int firstColor;
        int lastColor;
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            firstColor = Color.GREEN;
            lastColor  = Color.RED;
        } else {
            firstColor = Color.RED;
            lastColor  = Color.GREEN;
        }

        if ( cnt < kFirstLEDs ) { 
            col = firstColor;
        } else {
            if ( cnt < kSecondLEDs + kFirstLEDs ) {
                col = Color.YELLOW;
            } else {
                if ( cnt < kSecondLEDs + kFirstLEDs + kSecondLEDs ) {
                    col = Color.rgb( 255, 165, 0 );
                } else {
                    col = lastColor;
                }
            }
        }
        canvas.drawColor( col );    
        
        drawRPMText( canvas );
    }
    
    //=============================================================================
    private void  drawBarsOutsideIn( Canvas canvas ) {
        
        int measuredHeight = getMeasuredHeight();
        int measuredWidth  = getMeasuredWidth();
        int width = yellowBitmap.getWidth();
        
        float left = 0;
        float right = measuredWidth - width;
        
        Paint firstPaint;
        Paint lastPaint;
        
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            firstPaint = greenPaint;
            lastPaint  = redPaint;
        } else {
            firstPaint = redPaint;
            lastPaint  = greenPaint;
        }
        int iterations = ( cnt + 1 ) / 2;
        for ( int i = 0, position = 0; i < iterations; ++i ) {
            
            Paint tmp = null;

            if ( position < kFirstLEDs ) { 
                tmp = firstPaint;
            } else {
                if ( position < kSecondLEDs + kFirstLEDs ) {
                    tmp = yellowPaint;
                } else {
                    if ( position < kSecondLEDs + kFirstLEDs + kThirdLEDs ) {
                        tmp = orangePaint;
                    } else {
                        tmp = lastPaint;
                    }
                }
            }

            canvas.drawRect( left, 0, left + width, measuredHeight, tmp );
            canvas.drawRect( right, 0, right + width, measuredHeight, tmp );
            
            left  += width;
            right -= width;

            position += 2;
        }
    }

    //=============================================================================
    private void drawBarssLeft2Right( Canvas canvas ) {
        
        int measuredHeight = getMeasuredHeight();
        
        int width = yellowBitmap.getWidth();
        
        Paint firstPaint;
        Paint lastPaint;
        
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            firstPaint = greenPaint;
            lastPaint  = redPaint;
        } else {
            firstPaint = redPaint;
            lastPaint  = greenPaint;
        }
        float left = 0;
        for ( int i = 0; i < cnt; ++i ) {

            Paint tmp = null;
            
            if ( i < kFirstLEDs ) { 
                tmp = firstPaint;
            } else {
                if ( i < kSecondLEDs + kFirstLEDs ) {
                    tmp = yellowPaint;
                } else {
                    if ( i < kSecondLEDs + kFirstLEDs + kThirdLEDs ) {
                        tmp = orangePaint;
                    } else {
                        tmp = lastPaint;
                    }
                }
            }

            canvas.drawRect( left, 0, left + width, measuredHeight, tmp );
            left += width;
        }
    }

    //=============================================================================
    private void drawBars( Canvas canvas ) {
        
        if ( displayOrder == EDisplayOrder.eOutsideIn ) {
            drawBarsOutsideIn( canvas );
           
        } else {
            drawBarssLeft2Right( canvas );
        }
        drawRPMText( canvas );
    }
    
    //=============================================================================    
    private void drawFlash( Canvas canvas ) {

        int color;
        if ( colorOrder == EColorOrder.eGreenFirst ) {
            color = Color.GREEN;
        } else {
            color = Color.RED;
        }

        if ( cnt >= kFirstLEDs + kSecondLEDs + kThirdLEDs ) {
            canvas.drawColor( color );
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
