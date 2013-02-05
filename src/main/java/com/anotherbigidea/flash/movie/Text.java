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
 * A Text Symbol.
 */
public class Text extends Symbol 
{
    /**
     * A set of contiguous characters in one font, size and color.
     */
    public static class Row
    {
        protected Font.Chars chars;
        protected double x;
        protected double y;
        protected boolean hasX;
        protected boolean hasY;
        protected Color color;
        
        public Font.Chars getChars() { return chars; }
        public double getX()     { return x; }
        public double getY()     { return y; }
        public Color  getColor() { return color; }
        public boolean hasX()    { return hasX; }
        public boolean hasY()    { return hasY; }
        
        /**
         * @param chars the characters to display
         * @param color may be AlphaColor.
         * @param x new X position for text - only valid if hasX is true
         * @param y new Y position for text - only valid if hasY is true
         */
        public Row( Font.Chars chars, Color color, double x, double y, boolean hasX, boolean hasY )
        {
            this.chars = chars;
            this.color = color;
            this.x     = x;
            this.y     = y;
            this.hasX  = hasX;
            this.hasY  = hasY;
        }
        
        protected void write( SWFText text, boolean changeColor, boolean changeFont ) 
            throws IOException 
        {                
            if( changeFont )
            {                
                Font font  = chars.getFont();
                int fontid = font.getId();
                
                text.font( fontid, (int)(chars.getSize() * SWFConstants.TWIPS) );
            }
                
            if( changeColor ) text.color( color );    
            if( hasX ) text.setX( (int)(x * SWFConstants.TWIPS));
            if( hasY ) text.setY( (int)(y * SWFConstants.TWIPS));
            
            text.text( chars.indices, chars.advances );
        }
    }
    
    protected boolean hasAlpha;
    protected Transform matrix;
    protected ArrayList rows = new ArrayList();
    
    /**
     * Create a Text Symbol which is transformed by the given matrix
     * @param matrix if null then an identity transform is assumed
     */
    public Text( Transform matrix )
    {
        if( matrix == null ) matrix = new Transform();
        this.matrix = matrix;
    }
    
    /**
     * Access the list of Row instances.
     */
    public ArrayList getRows() { return rows; }
        
    /**
     * Get the transformation matrix applied to the text
     */
    public Transform getTransform() { return matrix; }
    
    public void setTransform( Transform matrix ) { this.matrix = matrix; }
    
    /**
     * Add a contiguous set of characters that have the same font, size, color
     * and vertical position.
     * 
     * @param chars the characters to display)
     * @param may be AlphaColor.
     * @param x new X position for text - only valid if hasX is true
     * @param y new Y position for text - only valid if hasY is true
     * 
     * @return the new X position after writing the chars
     */    
    public Row row( Font.Chars chars, Color color,
                          double x, double y, boolean hasX, boolean hasY )
    {
        Row row = new Row( chars, color, x, y, hasX, hasY );
        
        rows.add( row );
        
        return row;
    }
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException
    {
        Font   currentFont = null;
        double currentSize = 0.0;
        Color  currentColor = null;
        boolean hasAlpha = false;
        double currentX = 0.0;
        double currentY = 0.0;
        double minX = 0.0;
        double minY = 0.0;
        double maxX = 0.0;
        double maxY = 0.0;
        
        //--make sure that all fonts are defined and figure out the alpha
        for( Iterator it = rows.iterator(); it.hasNext(); )
        {
            Object obj = it.next();
            
            if( obj instanceof Text.Row )
            {
                Text.Row row = (Text.Row)obj;
                
                if( row.color != null 
                    && (row.color instanceof AlphaColor) ) hasAlpha = true;
                
                Font font   = row.chars.getFont();
                double size = row.chars.getSize();

                if( currentFont == null || font != currentFont )
                {
                    font.define( true, movie, definitionWriter );
                }
                
                currentFont = font;
                
                if( row.hasX ) currentX = row.x;
                if( row.hasY ) currentY = row.y;
                
                double leftEdge = currentX - row.chars.getLeftMargin();
                double rightEdge = currentX + row.chars.getTotalAdvance() 
                                            + row.chars.getRightMargin();
                double topEdge    = currentY - row.chars.getAscent();
                double bottomEdge = currentY + row.chars.getDescent();                
                
                if( leftEdge   < minX ) minX = leftEdge;
                if( rightEdge  > maxX ) maxX = rightEdge;
                if( topEdge    < minY ) minY = topEdge;
                if( bottomEdge > maxY ) maxY = bottomEdge;
                
                currentX += row.chars.getTotalAdvance();
            }
        }
        
        int id = getNextId( movie );
        Rect bounds = new Rect( (int)(minX * SWFConstants.TWIPS),
                                (int)(minY * SWFConstants.TWIPS),
                                (int)(maxX * SWFConstants.TWIPS),
                                (int)(maxY * SWFConstants.TWIPS));
        
        SWFText text = hasAlpha ?
                          definitionWriter.tagDefineText2( id, bounds, matrix ) :
                          definitionWriter.tagDefineText( id, bounds, matrix );
        
        currentFont = null;
        currentSize = 0.0;
        currentColor = null;
        
        for( Iterator it = rows.iterator(); it.hasNext(); )
        {
            Object obj = it.next();
            
            if( obj instanceof Text.Row )
            {
                Text.Row row = (Text.Row)obj;
            
                Font   font = row.chars.getFont();
                double size = row.chars.getSize();
                Color  color = row.color;
                
                boolean changeFont = currentFont == null || 
                                     font != currentFont || 
                                     size != currentSize;
                   
                boolean changeColor = currentColor == null || 
                                      ( color!=null && !color.equals( currentColor) );
                
                row.write( text, changeColor, changeFont );
                
                if( color != null ) currentColor = color;
            }
        }        
        
        text.done();        
        
        return id;
    }
}
