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
 * Base class for all defined symbols
 */
public abstract class Symbol
{
    protected int id = 0;
    
    protected Symbol() {}
    
    protected Symbol( int id )
    {
       this.id = id; 
    }
    
    /**
     * Get the internal SWF id for the symbol.  This will always be
     * zero for a Movie that was not loaded from an existing SWF until
     * the Movie is written out.
     */
    public int getId() { return id; }    
    
    /**
     * Make sure that the Symbol is fully defined in the given Movie and
     * return the character id
     * @param tags a vector into which to place any definition tags required
     */
    protected int define( Movie movie, 
                          SWFTagTypes timelineWriter,
                          SWFTagTypes definitionWriter )
        throws IOException 
    {
        Integer integerId = (Integer)movie.definedSymbols.get( this );
                
        if( integerId == null )
        {
            integerId = new Integer( defineSymbol( movie, 
                                                   timelineWriter,
                                                   definitionWriter ) );
            movie.definedSymbols.put( this, integerId );
        }

        id = integerId.intValue();
        return id;
    }

    protected int getNextId( Movie movie )
    {
        return movie.maxId++;
    }

    /**
     * Override to provide symbol definition
     * @return the new symbol id
     */
    protected abstract int defineSymbol( Movie movie, 
                                         SWFTagTypes timelineWriter,
                                         SWFTagTypes definitionwriter )
        throws IOException;    
}
