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
 * An Edit Field Symbol.
 * 
 * In order to limit the chars in the field use a separate font and only
 * load those glyphs that are required.
 */
public class EditField extends Symbol 
{
    protected AlphaColor textColor;
    protected int    alignment;
    protected int    charLimit;
    protected double leftMargin;
    protected double rightMargin;
    protected double indentation;
    protected double lineSpacing;
    
    protected String fieldName;
    protected String initialText;
    protected Font   font;
    protected double fontSize;
    protected double minX, minY, maxX, maxY;
    
    protected boolean isSelectable = true;    
    protected boolean hasBorder = true;
    protected boolean isHtml;
    protected boolean usesSystemFont;
    protected boolean hasWordWrap;
    protected boolean isMultiline;
    protected boolean isPassword;
    protected boolean isEditable = true;   
  
    public boolean isSelectable()   { return isSelectable;   }  
    public boolean hasBorder()      { return hasBorder;      }  
    public boolean isHtml()         { return isHtml;         }  
    public boolean usesSystemFont() { return usesSystemFont; }    
    public boolean hasWordWrap()    { return hasWordWrap;    }  
    public boolean isMultiline()    { return isMultiline;    }  
    public boolean isPassword()     { return isPassword;     }  
    public boolean isEditable()     { return isEditable;     }  
    
    public void setProperties( boolean isSelectable, boolean hasBorder, 
                               boolean isHtml, boolean usesSystemFont, 
                               boolean hasWordWrap, boolean isMultiline,
                               boolean isPassword, boolean isEditable )
    {
        this.isSelectable   = isSelectable;
        this.hasBorder      = hasBorder;  
        this.isHtml         = isHtml;     
        this.usesSystemFont = usesSystemFont;
        this.hasWordWrap    = hasWordWrap;
        this.isMultiline    = isMultiline;   
        this.isPassword     = isPassword;
        this.isEditable     = isEditable;    
    }
    
    public AlphaColor getTextColor() { return textColor; }
    public int getAlignment()        { return alignment; }            
    public int getCharLimit()        { return charLimit; }           
    public double getLeftMargin()    { return leftMargin; }           
    public double getRightMargin()   { return rightMargin; }          
    public double getIndentation()   { return indentation; }
    public double getLineSpacing()   { return lineSpacing; }         
                                                              
    public String getFieldName()     { return fieldName; }         
    public String getInitialText()   { return initialText; }           
    public Font   getFont()          { return font; }         
    public double getFontSize()      { return fontSize; }
    public double getMinX()          { return minX; }
    public double getMinY()          { return minY; }
    public double getMaxX()          { return maxX; }
    public double getMaxY()          { return maxY; }
 
    public void setTextColor  ( AlphaColor color   ) { this.textColor   = color; }
    public void setAlignment  ( int    alignment   ) { this.alignment   = alignment; }            
    public void setCharLimit  ( int    charLimit   ) { this.charLimit   = charLimit; }           
    public void setLeftMargin ( double leftMargin  ) { this.leftMargin  = leftMargin; }           
    public void setRightMargin( double rightMargin ) { this.rightMargin = rightMargin; }          
    public void setIndentation( double indentation ) { this.indentation = indentation; }
    public void setLineSpacing( double lineSpacing ) { this.lineSpacing = lineSpacing; }         
                                                              
    public void setFieldName  ( String name )  { this.fieldName   = name; }         
    public void setInitialText( String text )  { this.initialText = text; }           
    public void setFont    ( Font font )       { this.font        = font; }         
    public void setFontSize( double fontSize ) { this.fontSize    = fontSize; }
    public void setMinX    ( double minX )     { this.minX        = minX; }
    public void setMinY    ( double minY )     { this.minY        = minY; }
    public void setMaxX    ( double maxX )     { this.maxX        = maxX; }
    public void setMaxY    ( double maxY )     { this.maxY        = maxY; }    
    
    /**
     * Create an Edit Field with black text and default settings
     * 
     * @param fieldName may be null
     * @param intialText may be null
     */
    public EditField( String fieldName, String initialText, 
                      Font font, double fontSize,
                      double minX, double minY, double maxX, double maxY )                   
    {
        this.fieldName   = fieldName;
        this.initialText = initialText;
        this.font        = font;
        this.fontSize    = fontSize;
        
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        
        this.textColor = new AlphaColor(0,0,0,255);
    }
    
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException
    {
        int id = getNextId( movie );
        
        //--Make sure that the font is defined.
        int fontId = font.define( false, movie, definitionWriter );
        
        Rect bounds = new Rect( (int)(minX*SWFConstants.TWIPS),
                                (int)(minY*SWFConstants.TWIPS),
                                (int)(maxX*SWFConstants.TWIPS),
                                (int)(maxY*SWFConstants.TWIPS));
        
        int flags = 0;
        
        if( ! isSelectable   ) flags |= SWFConstants.TEXTFIELD_NO_SELECTION;
        if( hasBorder        ) flags |= SWFConstants.TEXTFIELD_DRAW_BORDER;
        if( isHtml           ) flags |= SWFConstants.TEXTFIELD_HTML;
        if( ! usesSystemFont ) flags |= SWFConstants.TEXTFIELD_FONT_GLYPHS;
        if( hasWordWrap      ) flags |= SWFConstants.TEXTFIELD_WORD_WRAP;
        if( isMultiline      ) flags |= SWFConstants.TEXTFIELD_IS_MULTILINE;
        if( isPassword       ) flags |= SWFConstants.TEXTFIELD_IS_PASSWORD;
        if( ! isEditable     ) flags |= SWFConstants.TEXTFIELD_DISABLE_EDIT;
        
        if( initialText != null && initialText.length() > 0 )
             flags |= SWFConstants.TEXTFIELD_HAS_TEXT;
        
        if( charLimit > 0 )  flags |= SWFConstants.TEXTFIELD_LIMIT_CHARS;        
        
        //--define the edit field
        definitionWriter.tagDefineTextField( 
                                      id, fieldName, initialText, bounds, flags,
                                      textColor, alignment, fontId, 
                                      (int)(fontSize * SWFConstants.TWIPS),
                                      charLimit, 
                                      (int)(leftMargin  * SWFConstants.TWIPS),
                                      (int)(rightMargin * SWFConstants.TWIPS),
                                      (int)(indentation * SWFConstants.TWIPS),
                                      (int)(lineSpacing * SWFConstants.TWIPS) );
        
        return id;
    }
               
}
