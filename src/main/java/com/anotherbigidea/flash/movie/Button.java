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

/**
 * A Button Symbol
 */
public class Button extends Symbol
{
    /**
     * A layer of a button.  The layer defines a symbol (Shape etc.) with
     * associated Transform and color transform.  There may be many layers
     * in a button and each layer may take part in one or more of the 4 button
     * states (up,over,down,hit-test).
     */
    public static class Layer
    {
        protected Symbol symbol;
        protected Transform matrix;
        protected AlphaTransform cxform;
        protected int depth;
        protected boolean usedForHitArea, usedForUp, usedForDown, usedForOver;
                          
        public Symbol         getSymbol()    { return symbol; }
        public Transform      getTransform() { return matrix; }
        public AlphaTransform getColoring()  { return cxform; }
        public int            getDepth()     { return depth;  }
        public boolean isUsedForHitArea() { return usedForHitArea; }
        public boolean isUsedForUp()      { return usedForUp; }
        public boolean isUsedForDown()    { return usedForDown; }
        public boolean isUsedForOver()    { return usedForOver; }

        public void setSymbol     ( Symbol symbol )         { this.symbol = symbol; }
        public void setTransform  ( Transform matrix )      { this.matrix = matrix; }
        public void setColoring   ( AlphaTransform cxform ) { this.cxform = cxform; }
        public void setDepth      ( int depth )             { this.depth  = depth;  }
        public void usedForHitArea( boolean f ) { usedForHitArea = f; }
        public void usedForUp     ( boolean f ) { usedForUp      = f; }
        public void usedForDown   ( boolean f ) { usedForDown    = f; }
        public void usedForOver   ( boolean f ) { usedForOver    = f; }        

        /**
         * @param depth should be >= 1 and there should only be one symbol on any layer
         */
        public Layer( Symbol symbol, Transform matrix, AlphaTransform cxform,
                      int depth, boolean usedForHitArea, boolean usedForUp, 
                      boolean usedForDown, boolean usedForOver )
        {
            if( matrix == null ) matrix = new Transform();
            if( cxform == null ) cxform = new AlphaTransform();
            
            this.symbol         = symbol;        
            this.matrix         = matrix;       
            this.cxform         = cxform;        
            this.depth          = depth;         
            this.usedForHitArea = usedForHitArea;
            this.usedForUp      = usedForUp;     
            this.usedForDown    = usedForDown;   
            this.usedForOver    = usedForOver;          
        }
        
        protected ButtonRecord2 getRecord( Movie movie, 
                                           SWFTagTypes timelineWriter,
                                           SWFTagTypes definitionWriter )
            throws IOException 
        {
            //--Make sure symbol is defined
            int symId = symbol.define( movie, timelineWriter, definitionWriter );
            
            int flags = 0;
            if( usedForHitArea ) flags |= ButtonRecord.BUTTON_HITTEST;
            if( usedForUp      ) flags |= ButtonRecord.BUTTON_UP; 
            if( usedForDown    ) flags |= ButtonRecord.BUTTON_DOWN;
            if( usedForOver    ) flags |= ButtonRecord.BUTTON_OVER;
            
            return new ButtonRecord2( symId, depth, matrix, cxform, flags );
        }
    }
    
    protected ArrayList actions = new ArrayList();
    protected ArrayList layers = new ArrayList();    
    protected boolean trackAsMenu;
    
    public Button( boolean trackAsMenu )
    {
        this.trackAsMenu = trackAsMenu;
    }    
    
    public boolean isTrackedAsMenu()        { return trackAsMenu; }
    public void    trackAsMenu( boolean f ) { trackAsMenu = f; }
    
    /**
     * Access the list of Button.Layer objects
     */
    public ArrayList getButtonLayers() { return layers; }

    /**
     * Access the list of Actions objects
     */
    public ArrayList getActions() { return actions; }
        
    /**
     * Add a layer to the button.
     * @param depth should be >= 1 and there should only be one symbol on any layer
     */
    public Button.Layer addLayer( Symbol symbol, Transform matrix, 
                      AlphaTransform cxform,
                      int depth, boolean usedForHitArea, boolean usedForUp, 
                      boolean usedForDown, boolean usedForOver )
    {
        Layer layer = new Layer( symbol, matrix, cxform, depth,
                                 usedForHitArea, usedForUp, usedForDown, usedForOver );
        
        layers.add( layer );
        return layer;
    }
    
    public Actions addActions( int conditionFlags, int flashVersion )
    {
        Actions acts = new Actions( conditionFlags, flashVersion );
        actions.add( acts );
        return acts;
    }
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException
    {
        int id = getNextId( movie );
        
        Vector recs = new Vector();
        for( Iterator it = layers.iterator(); it.hasNext(); )
        {            
            Layer layer = (Layer)it.next();
            recs.addElement( layer.getRecord( movie, timelineWriter, definitionWriter ) );
        }
        
        SWFActions acts = definitionWriter.tagDefineButton2( id, trackAsMenu, recs );
        
        for( Iterator it = actions.iterator(); it.hasNext(); )
        {            
            Actions actions = (Actions)it.next();
            
            acts.start( actions.getConditions());
            acts.blob ( actions.bytes );
        }
        
        acts.done();
        
        return id;
    }
}
