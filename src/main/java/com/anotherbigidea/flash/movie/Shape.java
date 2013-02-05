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
 * A Shape Symbol
 */
public class Shape extends Symbol
{
    public abstract static class Element {}
    
    public abstract static class Style extends Shape.Element 
    {
    }
    
    public abstract static class FillStyle extends Shape.Style
    {
    }
    
    public static class ColorFill extends Shape.FillStyle
    {
        protected Color color;
        
        /**
         * @return may be Color or AlphaColor
         */
        public Color getColor() { return color; }
        public void setColor( Color color ) { this.color = color; }
        
        public ColorFill( Color color )
        {
            this.color = color;
        }
    }
    
    public static class ImageFill extends Shape.FillStyle
    {
        protected Symbol image;
        protected Transform matrix;
        protected boolean clipped;
        
        public Symbol getImage()        { return image; }
        public Transform getTransform() { return matrix; }
        public boolean isClipped()      { return clipped; }
        
        public void setImage( Symbol image ) { this.image = image; }
        public void setTransform( Transform matrix ) { this.matrix = matrix; }
        public void setClipped( boolean isClipped ) { clipped = isClipped; }
        
        public ImageFill( Symbol image, Transform matrix, boolean isClipped )
        {
            this.image   = image;
            this.matrix  = matrix;
            this.clipped = isClipped;
        }
    }
    
    public static class GradientFill extends Shape.FillStyle
    {
        protected Color[] colors;
        protected int[]   ratios;
        protected Transform matrix;
        protected boolean radial;
        
        public Color[]   getColors()    { return colors; }
        public Transform getTransform() { return matrix; }
        public int[]     getRatios()    { return ratios; }
        public boolean   isRadial()     { return radial; }
        
        public void setColors( Color[] colors ) { this.colors = colors; }
        public void setRatios( int[] ratios ) { this.ratios = ratios; }
        public void setTransform( Transform matrix ) { this.matrix = matrix; }
        public void setRadial( boolean isRadial ) { this.radial = isRadial; }
        
        public GradientFill( Color[] colors, int[] ratios, 
                             Transform matrix, boolean isRadial )
        {
            this.colors = colors;
            this.matrix = matrix;
            this.radial = isRadial;
            this.ratios = ratios;
        }
    }
    
    public static class LineStyle extends Shape.Style
    {
        protected double width;
        protected Color  color;
        
        public double getWidth() { return width; }
        public Color  getColor() { return color; }
        
        public void setWidth( double width ) { this.width = width; }
        public void setColor( Color color ) { this.color = color; }
        
        public LineStyle( double width, Color color )
        {
            this.width = width;
            this.color = color;
        }
    }
    
    public abstract static class SetStyle extends Shape.Element 
    {
        protected int index;
        
        public int getStyleIndex() { return index; }
        public void setStyleIndex( int index ) { this.index = index; }
        
        protected SetStyle( int index )
        {
            this.index = index;
        }
    }
    
    public abstract static class SetFillStyle extends Shape.SetStyle 
    {
        protected SetFillStyle( int index )
        {
            super( index );
        }
    }
    
    public static class SetLeftFillStyle extends Shape.SetFillStyle 
    {
        public SetLeftFillStyle( int index )
        {
            super( index );
        }
    }
    
    public static class SetRightFillStyle extends Shape.SetFillStyle 
    {
        public SetRightFillStyle( int index )
        {
            super( index );
        }
    }
    
    public static class SetLineStyle extends Shape.SetStyle 
    {
        public SetLineStyle( int index )
        {
            super( index );
        }
    }
    
    public abstract static class Vector extends Shape.Element 
    {
        protected double x, y;
        
        public double getX() { return x; }
        public double getY() { return y; }
        
        public void setX( double x ) { this.x = x; }
        public void setY( double y ) { this.y = y; }
        
        protected Vector( double x, double y )
        {
            this.x = x;
            this.y = y;
        }
    }
    
    public static class Move extends Shape.Vector 
    {
        public Move( double x, double y )
        {
            super( x, y );
        }
    }
    
    public static class Line extends Shape.Vector 
    {
        public Line( double x, double y )
        {
            super( x, y );
        }
    }

    public static class Curve extends Shape.Vector 
    {
        protected double cx, cy;
        
        public double getControlX() { return cx; }
        public double getControlY() { return cy; }
        
        public void setControlX( double cx ) { this.cx = cx; }
        public void setControlY( double cy ) { this.cy = cy; }
        
        public Curve( double x, double y, double controlX, double controlY )
        {
            super( x, y );
            this.cx = controlX;
            this.cy = controlY;
        }        
    }

    protected ArrayList elements = new ArrayList();
    protected double minX, maxX, minY, maxY;  //bounding rectangle
    protected boolean hasAlpha = false;
    protected double maxLineWidth;
    protected double currx, curry;
    
    public Shape() {}
    
    /**
     * Get the bounding rectangle as a double[4] - (min-X,min-Y,max-X,max-Y)
     */
    public double[] getBoundingRectangle()
    {
        return new double[] { minX, minY, maxX, maxY };
    }
    
    /**
     * Set the bounding rectangle. This will be automatically calculated
     * as the geometry vectors are defined and this rectangle will be enlarged
     * if it does not contain all the vectors.
     */
    public void setBoundingRectangle( double minx, double minY, 
                                      double maxX, double maxY )
    {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    /**
     * Access the list of shape elements
     * Each object is a subclass of Shape.Element
     */
    public ArrayList getShapeElements()
    {
        return elements;
    }
    
    /**
     * Define a line style
     * @param color if null then black is assumed
     */
    public void defineLineStyle( double width, Color color )
    {
        if( color == null ) color = new Color(0,0,0);
        
        LineStyle style = new LineStyle( width, color );

        if( maxLineWidth < width ) maxLineWidth = width;

        if( color instanceof AlphaColor ) hasAlpha = true;
        
        elements.add( style );
    }
    
    /**
     * Define a color fill
     * @param color if null then white is assumed
     */
    public void defineFillStyle( Color color )
    {
        if( color == null ) color = new Color(255,255,255);
        ColorFill fill = new ColorFill( color );
        
        if( color instanceof AlphaColor ) hasAlpha = true;
        
        elements.add( fill );
    }
    
    /**
     * Define an image fill
     */
    public void defineFillStyle( Symbol image, Transform matrix, boolean clipped )
    {
        ImageFill fill = new ImageFill(image,matrix,clipped);
        
        elements.add( fill );
    }
    
    /**
     * Define a gradient fill
     */
    public void defineFillStyle( Color[] colors, int[] ratios, 
                                 Transform matrix, boolean radial )
    {
        GradientFill fill = new GradientFill( colors, ratios, matrix, radial );

        elements.add( fill );
        
        for( int i = 0; i < colors.length; i++ )
        {
            if( colors[i] == null ) continue;
            if( colors[i] instanceof AlphaColor ) hasAlpha = true;
        }        
    }
    
    /**
     * Set the left fill style
     */
    public void setLeftFillStyle( int index )
    {
        SetLeftFillStyle fill = new SetLeftFillStyle( index );
        
        elements.add( fill );
    }

    /**
     * Set the right fill style
     */
    public void setRightFillStyle( int index )
    {
        SetRightFillStyle fill = new SetRightFillStyle( index );
        
        elements.add( fill );
    }

    /**
     * Set the line style
     */
    public void setLineStyle( int index )
    {
        SetLineStyle style = new SetLineStyle( index );
        
        elements.add( style );
    }
    
    /**
     * Move the pen without drawing any line
     */
    public void move( double x, double y )
    {
        Move move = new Move( x, y );
    
        if( x < minX ) minX = x;
        if( y < minY ) minY = y;
        if( x > maxX ) maxX = x;
        if( y > maxY ) maxY = y;
        
        elements.add( move );
    }
    
    /**
     * Draw a line in the current line style (if any)
     */
    public void line( double x, double y )
    {
        Line line = new Line( x, y );
    
        if( x < minX ) minX = x;
        if( y < minY ) minY = y;
        if( x > maxX ) maxX = x;
        if( y > maxY ) maxY = y;
        
        elements.add( line );
    }

    /**
     * Draw a curve in the current line style (if any)
     */
    public void curve( double x, double y, double controlX, double controlY )
    {
        Curve curve = new Curve( x, y, controlX, controlY );
        
        if( x < minX ) minX = x;
        if( y < minY ) minY = y;
        if( x > maxX ) maxX = x;
        if( y > maxY ) maxY = y;
        
        if( controlX < minX ) minX = controlX;
        if( controlY < minY ) minY = controlY;
        if( controlX > maxX ) maxX = controlX;
        if( controlY > maxY ) maxY = controlY;
        
        elements.add( curve );
    }
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException
    {
        currx = 0.0;
        curry = 0.0;

        predefineImageFills( movie, timelineWriter, definitionWriter );
        
        int id = getNextId(movie);
        
        Rect outline = getRect();
        
        SWFShape shape = hasAlpha ? 
                             definitionWriter.tagDefineShape3( id, outline ) :
                             definitionWriter.tagDefineShape2( id, outline );

        writeShape( shape );
        
        return id;
    }

    protected Rect getRect()
    {
        double adjust = maxLineWidth/2.0;
        
        Rect outline = new Rect( (int)(minX*SWFConstants.TWIPS - adjust*SWFConstants.TWIPS),
                                 (int)(minY*SWFConstants.TWIPS - adjust*SWFConstants.TWIPS),
                                 (int)(maxX*SWFConstants.TWIPS + adjust*SWFConstants.TWIPS),
                                 (int)(maxY*SWFConstants.TWIPS + adjust*SWFConstants.TWIPS));
        
        return outline;
    }
    
    protected void predefineImageFills( Movie movie, 
                                        SWFTagTypes timelineWriter,
                                        SWFTagTypes definitionWriter )
        throws IOException 
    {
        //--Make sure any image fills are defined prior to the shape
        for( Iterator it = elements.iterator(); it.hasNext(); )
        {
            Object el = it.next();
            
            if( el instanceof Shape.ImageFill )
            {        
                Symbol image = ((Shape.ImageFill)el).getImage();
                
                if( image != null ) image.define( movie,
                                                  timelineWriter, 
                                                  definitionWriter );
            }
        }        
    }
    
    protected void writeShape( SWFShape shape ) throws IOException
    {
        for( Iterator it = elements.iterator(); it.hasNext(); )
        {
            Object el = it.next();
            
            if( el instanceof Shape.ColorFill )
            {
                Shape.ColorFill fill = (Shape.ColorFill)el;
                shape.defineFillStyle( fill.getColor() );
            }
            else if( el instanceof Shape.ImageFill )
            {
                Shape.ImageFill fill = (Shape.ImageFill)el;

                Symbol image = fill.getImage();
                int imgId = (image != null) ? image.getId() : 65535;
                
                shape.defineFillStyle( imgId, fill.getTransform(), fill.isClipped() );
            }
            else if( el instanceof Shape.GradientFill )
            {
                Shape.GradientFill fill = (Shape.GradientFill)el;

                shape.defineFillStyle( fill.getTransform(), 
                                       fill.getRatios(),
                                       fill.getColors(),
                                       fill.isRadial() );
            }
            else if( el instanceof Shape.LineStyle )
            {
                Shape.LineStyle style = (Shape.LineStyle)el;
                
                shape.defineLineStyle( (int)(style.getWidth() * SWFConstants.TWIPS),
                                       style.getColor() );
            }
            else if( el instanceof Shape.SetLeftFillStyle )
            {
                Shape.SetLeftFillStyle style = (Shape.SetLeftFillStyle)el;
                shape.setFillStyle0( style.getStyleIndex() );
            }            
            else if( el instanceof Shape.SetRightFillStyle )
            {
                Shape.SetRightFillStyle style = (Shape.SetRightFillStyle)el;
                shape.setFillStyle1( style.getStyleIndex() );
            }            
            else if( el instanceof Shape.SetLineStyle )
            {
                Shape.SetLineStyle style = (Shape.SetLineStyle)el;
                shape.setLineStyle( style.getStyleIndex() );
            }            
            else writeVector( shape, el );        
        }
        
        shape.done();
    }
    
    protected void writeVector( SWFVectors vecs, Object el ) throws IOException
    {
        if( el instanceof Shape.Move )
        {
            Shape.Move move = (Shape.Move)el;

            currx = move.getX()*SWFConstants.TWIPS;
            curry = move.getY()*SWFConstants.TWIPS;

            int x = (int)currx;
            int y = (int)curry;

            vecs.move( x, y );
                
            //System.out.println( "M: " + x + " " + y );
        }            
        else if( el instanceof Shape.Line )
        {
            Shape.Line line = (Shape.Line)el;

            double xx = line.getX()*SWFConstants.TWIPS;
            double yy = line.getY()*SWFConstants.TWIPS;
                
            int dx = (int)(xx - currx);
            int dy = (int)(yy - curry);
                
            vecs.line( dx, dy );
                
            //System.out.println( "currx=" + currx + " curry=" + curry + " xx=" + xx + " yy=" + yy + " (xx - currx)=" + (xx - currx) + "  (yy - curry)=" + (yy - curry) );
            //System.out.println( "L: " + dx + " " + dy );

            currx = xx;
            curry = yy;                
        }            
        else if( el instanceof Shape.Curve )
        {
            Shape.Curve curve = (Shape.Curve)el;

            double xx  = curve.getX()       *SWFConstants.TWIPS;
            double yy  = curve.getY()       *SWFConstants.TWIPS;
            double cxx = curve.getControlX()*SWFConstants.TWIPS;
            double cyy = curve.getControlY()*SWFConstants.TWIPS;
                
            int dx = (int)(xx - cxx);
            int dy = (int)(yy - cyy);
            int cx = (int)(cxx - currx);
            int cy = (int)(cyy - curry);       
                
            vecs.curve( cx, cy, dx, dy );
                
            currx = xx;
            curry = yy;                

            //System.out.println( "C: " + cx + " " + cy + " " + dx + " " + dy );
        }            
     }
    
    protected void writeGlyph( SWFVectors vecs ) throws IOException
    {
        currx = 0.0;
        curry = 0.0;
        
        for( Iterator it = elements.iterator(); it.hasNext(); )
        {
            writeVector( vecs, it.next() );    
        }        
        
        vecs.done();        
   }
}