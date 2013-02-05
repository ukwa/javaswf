/****************************************************************
 * Copyright (c) 2001, David N. Main, All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the 
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the following 
 * disclaimer. 
 * 
 * 2. Redistributions in binary form must reproduce the above 
 * copyright notice, this list of conditions and the following 
 * disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or 
 * promote products derived from this software without specific 
 * prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ****************************************************************/
package com.anotherbigidea.flash.movie;

import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.SWFConstants;

/**
 * A Transformation matrix that has translation coordinates in pixels
 */
public class Transform extends Matrix 
{
    /**
     * Copy an existing matrix
     */
    public Transform( Matrix matrix )
    {
        super( matrix );
    }
    
    /**
     * An identity transform
     */
    public Transform() {}
    
    /**
     * A transform that only translates
     */
    public Transform( double translateX, double translateY )
    {
        super( translateX, translateY );
    }    
    
    /**
     * A transform that rotates and translates
     */
    public Transform( double radians, double translateX, double translateY )
    {
        this( radians, 1.0, 1.0, translateX, translateY );
    }    

    /**
     * A transform that scales and translates
     */
    public Transform( double scaleX, double scaleY, 
                      double translateX, double translateY )
    {
        super( scaleX, scaleY, 0.0, 0.0, translateX, translateY );
    }        
    
    /**
     * A transform that rotates, scales and translates
     */
    public Transform( double radians,
                      double scaleX, double scaleY,  
                      double translateX, double translateY )
    {
        super( scaleX * Math.cos( radians ),
               scaleY * Math.cos( radians ),
               Math.sin( radians ),
               -Math.sin( radians ),
               translateX, translateY );
    }    
    
    /**
     * Specify all the matrix components
     */
    public Transform( double scaleX,  double scaleY, 
                      double skew0,   double skew1,
                      double translateX, double translateY )
    {
        super( scaleX, scaleY, skew0, skew1, translateX, translateY );
    }    
    
    public double getTranslateX() { return translateX / (double)SWFConstants.TWIPS; }
    public double getTranslateY() { return translateY / (double)SWFConstants.TWIPS; }    
    
    public void setTranslateX( double translateX ) 
    { 
        this.translateX = translateX * (double)SWFConstants.TWIPS; 
    }
    
    public void setTranslateY( double translateY ) 
    {
        this.translateY = translateY * (double)SWFConstants.TWIPS; 
    }
}
