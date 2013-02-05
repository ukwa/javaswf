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
 * A Placement holds the transformation and other values relating to the 
 * "placement" of a Symbol Instance within a particular frame.
 * 
 * They can also denote an "UndefineSymbol" operation.
 */
public class Placement
{
    protected boolean isAlteration;
    protected boolean isReplacement;
    protected int frameNumber;
    protected Instance  instance;
    protected Transform matrix;
    protected AlphaTransform cxform;
    protected String    name;
    protected int ratio     = -1;
    protected int clipDepth = -1;
    protected boolean isRemove = false; 
    protected Actions[] clipActions;
    
    protected Symbol symbolToFree;
    
    /**
     * Return true if the placement represents an UndefineSymbol operation
     */
    public boolean isUndefineSymbol() { return symbolToFree != null; }    
    
    /**
     * Return true if the placement is replacing the symbol at a given
     * depth with a new symbol
     */
    public boolean isReplacement() { return isReplacement; }
    
    /**
     * Return true if this placement is an alteration to an Instance that
     * was placed in a previous frame.
     */
    public boolean isAlteration() { return isAlteration; }
    
    /**
     * Get the number of the frame within which this placement takes place
     */
    public int getFrameNumber() { return frameNumber; }
    
    /**
     * Get the Symbol Instance represented by this Placement
     */
    public Instance getInstance() { return instance; }
    
    /**
     * The transform may be null
     */
    public Transform getTransform() { return matrix; }
    
    /**
     * The color transform may be null
     */
    public AlphaTransform getColorTransform() { return cxform; }
    
    /**
     * The name only relates to MovieClip instances and may be null.
     * The name is only present on the first Placement of an Instance.
     */
    public String getName() { return name; }
    
    /**
     * The ratio only relates to Morph Shapes and will be -1 otherwise.
     * The ratio is from zero to 65535 and denotes the degree of the morph
     * from the initial shape to the final shape.
     */
    public int getRatio() { return ratio; }
    
    /**
     * The clip depth defines the range of depths which will be clipped by this
     * symbol.  All symbols placed at depths from depth+1 to clipDepth (inclusive)
     * will be clipped.  If this symbol is not a clipping symbol then the clip
     * depth will be -1.
     * 
     * The clip depth is only present on the first Placement of an Instance.
     */
    public int getClipDepth() { return clipDepth; }
    
    /**
     * If true then this Placement denotes the removal of the Instance from
     * the stage.
     */
    public boolean isRemove() { return isRemove; }
    
    /**
     * Get the actions for a movie clip
     */
    public Actions[] getClipActions() { return clipActions; }
   
    /**
     * Set the actions for a movie clip
     */
    public void setClipActions( Actions[] clipActions ) { this.clipActions = clipActions; }
    
    /**
     * An UndefineSymbol operation
     */
    protected Placement( Symbol symbol )
    {
        this.symbolToFree = symbol;
    }
    
    protected Placement( Instance instance, int frameNumber )
    {
        this.instance    = instance;
        this.frameNumber = frameNumber;
        this.isRemove    = true;
    }
    
    protected Placement( Instance instance, Transform matrix, AlphaTransform cxform,
                         String name, int ratio, int clipDepth, int frameNumber,
                         boolean alteration, boolean replacement, Actions[] clipActions )
    {
        this.instance     = instance;
        this.frameNumber  = frameNumber;
        this.matrix       = matrix;
        this.cxform       = cxform;
        this.name         = name;
        this.ratio        = ratio;
        this.clipDepth    = clipDepth;
        this.isRemove     = false;
        this.isAlteration = alteration;
        this.isReplacement = replacement;
        this.clipActions  = clipActions;
    }

    protected void flushDefinitions( Movie movie, 
                                     SWFTagTypes timelineWriter,
                                     SWFTagTypes definitionWriter )
        throws IOException 
    {
        if( symbolToFree != null ) return;
        
        if( (! isAlteration) && ! isRemove )
        {
            //--Make sure that the symbol is defined
            Symbol symbol = instance.getSymbol();

            symbol.define( movie, timelineWriter, definitionWriter );
        }
    }    
    
    protected void write( Movie movie, 
                          SWFTagTypes movieTagWriter,
                          SWFTagTypes timelineTagWriter )
        throws IOException 
    {
        if( symbolToFree != null )
        {
            int id = symbolToFree.getId();
            timelineTagWriter.tagFreeCharacter( id );
            return;                                              
        }
        
        int depth = instance.getDepth();
        if( depth < 0 ) return;
        
        if( isRemove )
        {
            //--Remove the instance            
            timelineTagWriter.tagRemoveObject2( depth );
            return;
        }

        //--Check whether the Instance has been placed
        if( ! isAlteration )
        {
            //--Make sure that the symbol is defined
            Symbol symbol = instance.getSymbol();
            int id = symbol.define( movie, timelineTagWriter, movieTagWriter );

            int flags = 0;
                
            if( clipActions != null && clipActions.length > 0 )
            {
                for( int i = 0; i < clipActions.length; i++ )
                {
                    flags |= clipActions[i].getConditions();
                }
            }
            
            SWFActions acts = timelineTagWriter.tagPlaceObject2( 
                                               isReplacement, clipDepth, depth, id,
                                               matrix, cxform, ratio, name, flags );

            if( clipActions != null && clipActions.length > 0 )
            {
                for( int i = 0; i < clipActions.length; i++ )
                {
                    acts.start( clipActions[i].getConditions() );
                    acts.blob( clipActions[i].bytes );
                }
                
                acts.done();
            }
        }
        else
        {
            //--Instance is placed - this is just a move
            timelineTagWriter.tagPlaceObject2( true, clipDepth, depth, -1,
                                               matrix, cxform, ratio, null, 0 );
        }
    }
}
