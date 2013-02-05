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

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;
import com.anotherbigidea.flash.SWFConstants;

/**
 * Utilities for dealing with images
 */
public class ImageUtil
{
    /**
     * Normalize a JPEG to ensure that it is "Baseline" rather than
     * "Progressive".  The Flash player doesn't like "Progressive".
     * 
     * @param size null or an int[2] to receive the (width,height) of the image
     * @return the image data - with the necessary header for SWF
     */
    public static byte[] normalizeJPEG( InputStream jpegImage, int[] size )
        throws IOException 
    {
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( jpegImage );
        BufferedImage buff = decoder.decodeAsBufferedImage();
        
        if( size != null && size.length >= 2 )
        {
            size[0] = decoder.getJPEGDecodeParam().getWidth();
            size[1] = decoder.getJPEGDecodeParam().getHeight();
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        //--write stream headers/terminators
        out.write( 0xff );
        out.write( 0xd9 );
        out.write( 0xff );
        out.write( 0xd8 );
        
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( out );
        
        encoder.encode( buff );
        out.flush();
        
        return out.toByteArray();
    }
    
    /**
     * Create a Shape for the image.  The resulting shape uses the image as
     * a clipped image fill.  The shape is a rectangle just the right size
     * to show the entire image and its origin is at the top left.
     * 
     * Additional geometry vectors may be added to the shape if required.
     * 
     * @param size null or an int[2] to receive the (width,height) of the image
     */
    public static Shape shapeForImage( InputStream jpegImage, int[] size )
        throws IOException 
    {
        if( size == null || size.length < 2 ) size = new int[2];
        byte[] data = normalizeJPEG( jpegImage, size );
        
        int width = size[0];
        int height = size[1];
        
        Image img = new Image.JPEG( data );
        
        return shapeForImage( img, (double)width, (double)height );
    }

    /**
     * Create a Shape for the image.  The resulting shape uses the image as
     * a clipped image fill.  The shape is a rectangle just the right size
     * to show the entire image and its origin is at the top left.
     * 
     * Additional geometry vectors may be added to the shape if required.
     */
    public static Shape shapeForImage( Image image, double width, double height )
        //throws IOException 
    {
        Shape s = new Shape();

        Transform matrix = new Transform( SWFConstants.TWIPS,
                                          SWFConstants.TWIPS,
                                          0.0, 0.0 );
        
        s.defineFillStyle( image, matrix, true );
        s.setRightFillStyle(1);  //use image fill
        s.setLineStyle(0);       //no line
        s.line(width,0);
        s.line(width,height);
        s.line(0,height);
        s.line(0,0);
        
        return s;
    }
        
    /**
     * Create a lossless Image object from an AWT Image - the awt image
     * must be fully loaded and ready.
     * 
     * @param format one of: SWFConstants.BITMAP_FORMAT_8_BIT,
     *                       SWFConstants.BITMAP_FORMAT_16_BIT,
     *                       SWFConstants.BITMAP_FORMAT_32_BIT
     */
    public static Image.Lossless createLosslessImage( 
                                     java.awt.Image image,
                                     int format,
                                     boolean hasAlpha )
    {
        int width  = image.getWidth(null);
        int height = image.getHeight(null);
              
        PixelGrabber grabber = new PixelGrabber( image, 0, 0, width, height, true );
        grabber.startGrabbing();
        
        try{ grabber.grabPixels(); } catch( InterruptedException ie ) {}
        
        int[] pixelData = (int[])grabber.getPixels();
        
        int size = 0;
        switch( format )
        {
            case SWFConstants.BITMAP_FORMAT_8_BIT:  size = 1; break;
            case SWFConstants.BITMAP_FORMAT_16_BIT: size = 2; break;
            case SWFConstants.BITMAP_FORMAT_32_BIT: 
            default:                                size = 4; break;
        }  
        
        int rowBytes = width * size;
        int modulo = rowBytes % 4;
        if( modulo > 0 ) rowBytes += 4 - modulo; //pad up to next 32 bits
        //>>>Padding bug spotted by Joachim Sauer        
        
        byte[] imageData = new byte[ height * rowBytes ];
        
        //--color lookup table
        Map table = ( format == SWFConstants.BITMAP_FORMAT_32_BIT ) ? null :
                        new HashMap();
        
        for( int y = 0; y < height; y++ )
        {
            int rowStart = y * width;
            int rowByte  = y * rowBytes;
            
            for( int x = 0; x < width; x++ )
            {
                int pix = pixelData[ rowStart + x ];
                int bite = rowByte + ( x * size );
                
                if( format == SWFConstants.BITMAP_FORMAT_32_BIT )
                {                    
                    imageData[ bite   ] = hasAlpha ? (byte)( pix >> 24 ) : -1;
                    imageData[ bite+1 ] = (byte)((pix >> 16) & 0xff );
                    imageData[ bite+2 ] = (byte)((pix >> 8 ) & 0xff  );
                    imageData[ bite+3 ] = (byte)( pix & 0xff );
                }                
                else
                {
                    //--find the pixel color in the lookup table
                    Integer pixI  = new Integer( pix );
                    Integer index = (Integer)table.get( pixI );
                    
                    if( index == null )
                    {
                        index = new Integer( table.size() );
                        table.put( pixI, index );
                    }
                    
                    int idx = index.intValue();

                    if( format == SWFConstants.BITMAP_FORMAT_8_BIT )
                    {
                        imageData[ bite ] = (byte)idx;
                    }
                    else
                    {
                        imageData[bite  ] = (byte)(( idx >> 8 )& 0xff);
                        imageData[bite+1] = (byte)( idx & 0xff );
                    }
                }
            }        
        }
        
        com.anotherbigidea.flash.structs.Color[] colors = 
            ( format == SWFConstants.BITMAP_FORMAT_32_BIT ) ? null :
                new com.anotherbigidea.flash.structs.Color[ table.size() ];
        
        if( colors != null )
        {
            for( Iterator it = table.keySet().iterator(); it.hasNext(); )
            {              
                Integer key = (Integer)it.next();
                int argb = key.intValue();
                
                int index = ((Integer)table.get(key)).intValue();
                
                int alpha = (argb >> 24) & 0xff;
                int red   = (argb >> 16) & 0xff;
                int green = (argb >> 8 ) & 0xff;
                int blue  = argb & 0xff;
                
                com.anotherbigidea.flash.structs.Color color = hasAlpha ? 
                    new com.anotherbigidea.flash.structs.AlphaColor(red,green,blue,alpha):
                    new com.anotherbigidea.flash.structs.Color(red,green,blue);
                
                colors[index] = color;
                
                System.out.println( "Color " + index + " = " + color );
            }
        }

        return new Image.Lossless( colors, imageData,
                                   (double)width, (double)height,
                                   hasAlpha, format );
    }                                
}
