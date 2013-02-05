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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.readers.ActionParser;
import com.anotherbigidea.flash.writers.ActionWriter;
import com.anotherbigidea.io.OutStream;

/**
 * A set of actions
 */
public class Actions extends ActionWriter 
{
    protected int conditions;
    protected byte[] bytes;
    
    public Actions( int conditions, int flashVersion )
    {
        super( null, flashVersion );

        this.conditions = conditions;
        count      = 0;
        bout       = new ByteArrayOutputStream();
        out        = new OutStream( bout );
        pushValues = new Vector();
        labels     = null;
        jumps      = null;
        skips      = null;
        blocks     = null;
        blockStack = null;        
    }
                                
    public Actions( int flashVersion )
    {
        this( 0, flashVersion );
    }
    
    /**
     * Parse the action contents and write them to the SWFActions interface
     */
    public void write( SWFActions swfactions ) throws IOException 
    {
        ActionParser parser = new ActionParser( swfactions, flashVersion );
        swfactions.start( conditions );
        parser.parse( bytes );
        swfactions.done();
    }
    
    /**
     * The condition flags depend on context - frame, button or clip actions
     */
    public int getConditions() { return conditions; }
    
    public void setConditions( int conds ) { this.conditions = conds; }
    
    /**
     * SWFActions interface
     */
    public void start( int conditions ) throws IOException
    {
        //do nothing
    }    
    
    protected void writeBytes( byte[] bytes ) throws IOException
    {
        this.bytes = bytes;
    }
    
    /**
     * SWFActions interface
     */
    public void done() throws IOException
    {
        //do nothing
    }    
}
