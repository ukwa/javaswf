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
import com.anotherbigidea.flash.*;
import com.anotherbigidea.flash.interfaces.*;
import com.anotherbigidea.flash.writers.*;
import com.anotherbigidea.flash.readers.*;
import com.anotherbigidea.flash.structs.*;

/**
 * A Flash Movie
 */
public class Movie implements TimeLine 
{
    protected int width;
    protected int height;
    protected int frameRate;
    protected Color backColor;
    protected int version;
    protected boolean isProtected;
    
    protected Map    importLibraries;
    protected Vector exportedSymbols;
    
    protected SortedMap frames = new TreeMap();
    protected int frameCount = 0;
    
    //--Table of characters defined so far in the movie - while writing out
    protected HashMap definedSymbols = new HashMap();

    protected int depth = 1;  //the next available depth
    protected int maxId = 1;  //the next available symbol id    
    
    /**
     * Create a movie with the default values - 
     * (550x400), 12 frames/sec, white backcolor, Flash version 5.
     */
    public Movie()
    {
        width     = 550;
        height    = 400;
        frameRate = 12;
        version   = 5;
    }
    
    /**
     * Create a movie with the given properties
     */
    public Movie( int width, int height, int frameRate, int version, Color backColor )
    {
        this.width     = width;
        this.height    = height;
        this.frameRate = frameRate;
        this.version   = version;
        this.backColor = backColor;
    }
    
    public int   getWidth    () { return width; }
    public int   getHeight   () { return height; }
    public int   getFrameRate() { return frameRate; }
    public int   getVersion  () { return version; }
    public Color getBackColor() { return backColor; }

    public void setWidth    ( int width   ) { this.width     = width; }    
    public void setHeight   ( int height  ) { this.height    = height; }
    public void setFrameRate( int rate    ) { this.frameRate = rate; }    
    public void setVersion  ( int version ) { this.version   = version; }    
    public void setBackColor( Color color ) { this.backColor = color; }    
     
    /**
     * Return the protection flag.  If true then the movie cannot be imported
     * into the Flash Author.  The existence of tools such as JavaSWF makes
     * this kind of protection almost worthless.
     */
    public boolean isProtected() { return isProtected; }
    
    public void protect( boolean isProtected ) { this.isProtected = isProtected; }
    
    /**
     * Get the current number of frames in the timeline.
     */
    public int getFrameCount()
    {
        return frameCount;
    }

    /** 
     * Get the Frame object for the given frame number - or create one if
     * none exists.  If the frame number is larger than the current frame count
     * then the frame count is increased.
     * 
     * @param frameNumber must be 1 or larger
     */
    public Frame getFrame( int frameNumber )
    {
        if( frameNumber < 1 ) return null;
        
        Integer num = new Integer( frameNumber );
        Frame frame = (Frame)frames.get( num );
        
        if( frame == null )
        {
            frame = new Frame( frameNumber, this );
            frames.put( num, frame );
            if( frameNumber > frameCount ) frameCount = frameNumber;
        }
        
        return frame;
    }

    /**
     * Append a frame to the end of the timeline
     */
    public Frame appendFrame()
    {
        frameCount++;
        Frame frame = new Frame( frameCount, this );
        frames.put( new Integer(frameCount), frame );
        return frame;
    }
    
    /**
     * Get the next available depth in the timeline
     */
    public int getAvailableDepth()
    {
        return depth;
    }
    
    /**
     * Set the next available depth in the timeline
     * @param depth must be >= 1
     */
    public void setAvailableDepth( int depth )
    {
        if( depth < 1 ) return;
        this.depth = depth;
    }
    
    /**
     * Import symbols from another movie (Flash 5 only)
     * @return Symbols representing the imports
     */
    public ImportedSymbol[] importSymbols( String libraryName, String[] symbolNames )
    {
        if( importLibraries == null ) importLibraries = new HashMap();
        
        ArrayList imports = (ArrayList)importLibraries.get( libraryName );
        if( imports == null )
        {
            imports = new ArrayList();
            importLibraries.put( libraryName, imports );
        }
        
        ImportedSymbol[] symbols = new ImportedSymbol[ symbolNames.length ];
        
        for( int i = 0; i < symbolNames.length; i++ )
        {
            ImportedSymbol imp = new ImportedSymbol( 0, symbolNames[i], libraryName );
            symbols[i] = imp;
            imports.add( imp );
        }
        
        return symbols;
    }
    
    /**
     * Clear all the defined library imports
     */
    public void clearImports()
    {
        if( importLibraries != null ) importLibraries.clear();
    }

    /**
     * Access the imported symbols.
     * @return an empty array if there are no imports
     */
    public ImportedSymbol[] getImportedSymbols()
    {
        if( importLibraries == null ) return new ImportedSymbol[0];
        
        Vector imports = new Vector();
        
        for( Iterator iter = importLibraries.values().iterator(); iter.hasNext(); )
        {
            List list = (List)iter.next();
            
            for( Iterator i2 = list.iterator(); i2.hasNext(); )
            {
                imports.add( i2.next() );
            }
        }
        
        ImportedSymbol[] imps = new ImportedSymbol[ imports.size() ];
        imports.copyInto( imps );
        
        return imps;
    }
    
    /**
     * Export a number of symbols with the given names so that other movies
     * can import and use them.  Flash version 5 only.
     */
    public void exportSymbols( String[] exportNames, Symbol[] symbols )
    {
        if( exportedSymbols == null ) exportedSymbols = new Vector();
        
        for( int i = 0; i < exportNames.length && i < symbols.length; i++ )
        {
            exportedSymbols.add( new ExportedSymbol( symbols[i], exportNames[i] ));
        }
    }
    
    /**
     * Get the symbols exported from the movie
     * @return an empty array if there are no exports
     */
    public ExportedSymbol[] getExportedSymbols()
    {
        if( exportedSymbols == null ) return new ExportedSymbol[0];
        
        ExportedSymbol[] exports = new ExportedSymbol[ exportedSymbols.size() ];
        
        exportedSymbols.copyInto( exports );
        
        return exports;
    }
    
    /**
     * Clear all the symbol exports
     */
    public void clearExports()        
    {
        if( exportedSymbols != null ) exportedSymbols.clear();
    }
    
    /**
     * Write the movie in SWF format.
     */
    public void write( SWFTagTypes tagwriter ) throws IOException
    {
        //--Reset state
        definedSymbols.clear();
        maxId = 1;

        tagwriter.header( version, 
                          -1,  //force length calculation
                          width  * SWFConstants.TWIPS,
                          height * SWFConstants.TWIPS,
                          frameRate,
                          -1); //force frame calculation
        
        //default backColor is white
        if( backColor == null ) backColor = new Color(255,255,255);
        
        tagwriter.tagSetBackgroundColor( backColor );

        if( isProtected ) tagwriter.tagProtect( null );
        
        //--Process Imports
        if( importLibraries != null && ! importLibraries.isEmpty() )
        {
            for( Iterator keys = importLibraries.keySet().iterator(); keys.hasNext();)
            {
                String libName = (String)keys.next();
                List   imports = (List)importLibraries.get( libName );
                
                String[] names = new String[imports.size()];
                int[]    ids   = new int[imports.size()];
                
                int i = 0;
                for( Iterator it = imports.iterator(); it.hasNext(); )
                {
                    ImportedSymbol imp = (ImportedSymbol)it.next();
                    
                    names[i] = imp.getName();
                    ids[i]   = imp.define( this, tagwriter, tagwriter );
                    
                    i++;
                }
                
                tagwriter.tagImport( libName, names, ids );
            }
        }
        
        //--Process Exports
        if( exportedSymbols != null && ! exportedSymbols.isEmpty() )
        {
            String[] names = new String[exportedSymbols.size()];
            int[]    ids   = new int[exportedSymbols.size()];

            int i = 0;
            for( Iterator it = exportedSymbols.iterator(); it.hasNext(); )
            {
                ExportedSymbol exp = (ExportedSymbol)it.next();
                    
                names[i] = exp.getExportName();
                ids[i]   = exp.getSymbol().define( this, tagwriter, tagwriter );
                    
                i++;
            }            
            
            tagwriter.tagExport( names, ids );
        }
        
        int lastFrame = 0;
        for( Iterator iter = frames.values().iterator(); iter.hasNext(); )
        {            
            Frame frame = (Frame)iter.next();
            
            int number = frame.getFrameNumber();
            
            //write any intermediate empty frames
            while( number > lastFrame + 1 )
            {
                tagwriter.tagShowFrame();
                lastFrame++;
            }
            
            frame.write( this, tagwriter, tagwriter );
            
            lastFrame = number;
        }
        
        //end of time line
        tagwriter.tagEnd();
    }
    
    /**
     * Write the movie in uncompressed SWF format to the given file.
     */
    public void write( String filename ) throws IOException {
    	write( filename, false );
    }
    
    /**
     * Write the movie in uncompressed SWF format to the given output stream.
     */
    public void write( OutputStream out ) throws IOException {
    	write( out, false );
    }

	/**
	 * Write the movie in SWF format to the given file.
	 * 
	 * @param compressed true for Flash MX+ compression.
	 */
	public void write( String filename, boolean compressed ) throws IOException {
		SWFWriter swfwriter = new SWFWriter( filename );
		TagWriter tagwriter = new TagWriter( swfwriter );
		swfwriter.setCompression( compressed );
		write( tagwriter );
	}
    
	/**
	 * Write the movie in SWF format to the given output stream.
	 * 
	 * @param compressed true for Flash MX+ compression.
	 */
	public void write( OutputStream out, boolean compressed ) throws IOException {
		SWFWriter swfwriter = new SWFWriter( out );
		TagWriter tagwriter = new TagWriter( swfwriter );
		swfwriter.setCompression( compressed );
		write( tagwriter );
	}


}
