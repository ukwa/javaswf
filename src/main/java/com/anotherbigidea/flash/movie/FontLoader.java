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

import java.io.*;
import java.util.*;
import com.anotherbigidea.flash.interfaces.*;
import com.anotherbigidea.flash.writers.*;
import com.anotherbigidea.flash.readers.*;
import com.anotherbigidea.flash.structs.*;
import com.anotherbigidea.flash.SWFConstants;

/**
 * Font loading utilities
 */
public class FontLoader extends SWFTagTypesImpl 
{
    protected FontDefinition fontDef;
    
    public FontLoader()
    {
        super( null );
    }
    
    /**
     * Load the first font from the given Flash movie
     * 
     * @return null if no font was found
     */
    public static FontDefinition loadFont( String filename )
        throws IOException
    {
        FileInputStream in = new FileInputStream( filename );
        
        FontDefinition def = loadFont(in); 
        
        in.close();
        
        return def;
    }
    
    /**
     * Load the first font from the given Flash movie
     * 
     * @return null if no font was found
     */
    public static FontDefinition loadFont( InputStream flashMovie )
        throws IOException
    {
        FontLoader fontloader = new FontLoader();
        SWFTags   swfparser = new TagParser( fontloader );
        SWFReader swfreader = new SWFReader( swfparser, flashMovie );
        
        swfreader.readFile();
        
        return fontloader.fontDef;
    }
    
    /**
     * SWFTagTypes Interface
     */
    public SWFVectors tagDefineFont2( int id, int flags, String name, int numGlyphs,
                                      int ascent, int descent, int leading,
                                      int[] codes, int[] advances, Rect[] bounds,
                                      int[] kernCodes1, int[] kernCodes2,
                                      int[] kernAdjustments ) throws IOException
    {
        if( fontDef != null ) return null;  //only load first font
        
        double twips = (double)SWFConstants.TWIPS;
        
        fontDef = new FontDefinition( name, 
                                      ((double)ascent)/twips,
                                      ((double)descent)/twips,
                                      ((double)leading)/twips,
                                      (flags & SWFConstants.FONT2_UNICODE   ) != 0,
                                      (flags & SWFConstants.FONT2_SHIFTJIS  ) != 0,
                                      (flags & SWFConstants.FONT2_ANSI      ) != 0,
                                      (flags & SWFConstants.FONT2_ITALIC    ) != 0,
                                      (flags & SWFConstants.FONT2_BOLD      ) != 0,
                                      (flags & SWFConstants.FONT2_HAS_LAYOUT) != 0 );
        
        if( kernCodes1 != null && kernCodes1.length > 0 )
        {
            //System.out.println( "Number of Kernings --> " + kernCodes1.length );
            
            ArrayList kerns = fontDef.getKerningPairList();
        
            for( int i = 0; i < kernCodes1.length; i++ )
            {
                FontDefinition.KerningPair pair = 
                    new FontDefinition.KerningPair( 
                        kernCodes1[i],
                        kernCodes2[i],
                        ((double)kernAdjustments[i])/twips );
                
                kerns.add( pair );
            }
        }
        
        return new VectorImpl( codes, advances, bounds );
    }
    
    protected class VectorImpl implements SWFVectors 
    {
        protected int[] codes;
        protected int[] advances;
        protected Rect[] bounds;
        protected int i;
        protected Shape shape;
        protected int currx;
        protected int curry;
        protected double twips = (double)SWFConstants.TWIPS;
        
        protected VectorImpl( int[] codes, int[] advances, Rect[] bounds )
        {
            this.codes    = codes;
            this.advances = advances;
            this.bounds   = bounds;
            i = 0;
            
            shape = new Shape();
        }
        
        public void done()
        {
            //System.out.println( "------------" );
            double advance = (advances == null) ? 0.0 : ((double)advances[i])/twips;
            int code = codes[i];
            
            Rect rect = bounds[i];
            shape.minX = ((double)rect.getMinX())/twips;
            shape.minY = ((double)rect.getMinY())/twips;
            shape.maxX = ((double)rect.getMaxX())/twips;
            shape.maxY = ((double)rect.getMaxY())/twips;
            
            FontDefinition.Glyph g = new FontDefinition.Glyph( shape, advance, code );
            
            fontDef.getGlyphList().add( g );
            
            i++;
            
            if( i < codes.length ) shape = new Shape();
            
            currx = curry = 0;
        }
    
        public void line( int dx, int dy )
        {
            currx += dx;
            curry += dy;
            
            shape.line( currx/twips, curry/twips );
            
            //System.out.println( "L: " + dx + " " + dy + "     " + currx + " " + curry );            
        }
    
        public void curve( int cx, int cy, int dx, int dy )
        {
            cx += currx;
            cy += curry;
            dx += cx;
            dy += cy;
            
            currx = dx;
            curry = dy;            
                        
            shape.curve( dx/twips, dy/twips, cx/twips, cy/twips );
            
            //System.out.println( "C: " + cx + " " + cy + " " + dx + " " + dy +
            //                    "     " + (cx/twips) + " " + (cy/twips) + 
            //                    " " + (dx/twips) + " " + (dy/twips) );
            
        }
    
        public void move( int x, int y )
        {
            currx = x;
            curry = y;

            shape.move( x/twips, y/twips );
            
            //System.out.println( "M: " + x + " " + y + "     " + (x/twips) + " " + (y/twips) );
        }
    }
}
