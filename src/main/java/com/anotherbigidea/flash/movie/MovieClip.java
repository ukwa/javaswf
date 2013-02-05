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
 * A Movie Clip (aka Sprite) Symbol
 */
public class MovieClip extends Symbol implements TimeLine 
{
    protected SortedMap frames = new TreeMap();
    protected int frameCount = 0;    
    
    protected int depth = 1;  //the next available depth    
    
    public MovieClip()
    {
    }
    
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
    
    public Frame appendFrame( Frame frame )
    {
        frameCount++;
        frame.timeline = this;
        frame.frameNumber = frameCount;
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
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException 
    {
        //--flush all symbol definitions
        for( Iterator iter = frames.values().iterator(); iter.hasNext(); )
        {            
            Frame frame = (Frame)iter.next();
            frame.flushDefinitions( movie, timelineWriter, definitionWriter );
        }
        
        int id = getNextId( movie );
        SWFTagTypes spriteWriter = definitionWriter.tagDefineSprite( id );

        int lastFrame = 0;
        for( Iterator iter = frames.values().iterator(); iter.hasNext(); )
        {            
            Frame frame = (Frame)iter.next();
            
            int number = frame.getFrameNumber();
            
            //write any intermediate empty frames
            while( number > lastFrame + 1 )
            {
                spriteWriter.tagShowFrame();
                lastFrame++;
            }
            
            frame.write( movie, definitionWriter, spriteWriter );
            
            lastFrame = number;
        }
        
        //end of time line
        spriteWriter.tagEnd();        
        
        return id;
    }
}
