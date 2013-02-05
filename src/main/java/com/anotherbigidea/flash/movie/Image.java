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
 * Base class for Image symbols.
 * Note that Images cannot be placed directly on the stage - they have to be
 * used as image fills for shapes.
 */
public abstract class Image extends Symbol
{
    /**
     * A lossless image (similar to PNG).
     * 
     * There are 3 formats - 8, 16 and 32 bit.  For 8 and 16 bit images
     * there is a color table and the image data consists of either an 8 or
     * 16 bit index into the table for each pixel.
     * 
     * 32 bit images have no color table - each pixel consists of 4 bytes:
     * (alpha,red,green,blue).  If there is no alpha then the first byte will
     * be 255.
     * 
     * For all formats, the length of each row of pixel data must be a multiple
     * of 32 bits.  If the actual row data is smaller then it should be padded 
     * up to next multiple of 32 bits.
     */
    public static class Lossless extends Image 
    {
        protected byte[] imageData;
        protected Color[] colorTable;
        protected double width;
        protected double height;
        protected boolean hasAlpha;
        protected int format;
        
        
        /**
         * @param colorTable may be null for 32 bit bitmaps
         * @param imageData the pixel data
         * @param width in pixels
         * @param height in pixels
         * @param hasAlpha whether the image contains alpha values
         * @param format one of: SWFConstants.BITMAP_FORMAT_8_BIT,
         *                       SWFConstants.BITMAP_FORMAT_16_BIT,
         *                       SWFConstants.BITMAP_FORMAT_32_BIT
         */
        public Lossless( Color[] colorTable, byte[] imageData, double width,
                         double height, boolean hasAlpha, int format )
        {
             this.colorTable = colorTable;
             this.imageData  = imageData;
             this.width      = width;
             this.height     = height;
             this.hasAlpha   = hasAlpha;
             this.format     = format;
        }
        
        public byte[]  getImageData()  { return imageData; }
        public Color[] getColorTable() { return colorTable; }
        public double  getWidth()      { return width; }
        public double  getHeight()     { return height; }
        public boolean hasAlpha()      { return hasAlpha; }
        public int     getFormat()     { return format; }        
        
        protected int defineSymbol( Movie movie, 
                                    SWFTagTypes timelineWriter,
                                    SWFTagTypes definitionWriter )
            throws IOException
        {
            int id = getNextId(movie);

            if( hasAlpha )
            {
                definitionWriter.tagDefineBitsLossless2( 
                    id, format, (int)width, (int)height,
                    colorTable, imageData );
            }
            else
            {
                definitionWriter.tagDefineBitsLossless( 
                    id, format, (int)width, (int)height,
                    colorTable, imageData );
            }
            
            return id;
        }        
    }
    
    /**
     * A JPEG Image that can be used as a fill for Shapes.  The JPEG image must
     * be "baseline" - a "progressive" JPEG will cause the Flash player to have
     * runtime problems.
     */
    public static class JPEG extends Image 
    {
        protected InputStream jpegIn;
        protected byte[] jpegData;    
        
        /**
         * A JPEG image that will read from an input stream.
         */
        public JPEG( InputStream jpegImage )
        {
            jpegIn = jpegImage;
        }

        /**
         * Construct a JPEG image from byte data.  Note that the
         * data must include the JPEG header ( 0xff,0xd9,0xff,0xd8 ).
         */
        public JPEG( byte[] imageData )
        {
            jpegData = imageData;
        }
        
        /**
         * Get the raw image data.  This will include the JPEG stream header(s)
         * ( 0xff,0xd9,0xff,0xd8 ).
         */
        public byte[] getImageData() { return jpegData; }
        
        protected int defineSymbol( Movie movie, 
                                    SWFTagTypes timelineWriter,
                                    SWFTagTypes definitionWriter )
            throws IOException
        {
            int id = getNextId(movie);

            if( jpegData != null )
            {
                definitionWriter.tagDefineBitsJPEG2( id, jpegData );
            }
            else if( jpegIn != null )
            {
                definitionWriter.tagDefineBitsJPEG2( id, jpegIn );
            }
            
            return id;
        }
    }
    
}
