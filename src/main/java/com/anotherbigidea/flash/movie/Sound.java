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
 * A Sound Symbol
 */
public class Sound extends Symbol
{
    public int     format;
    public int     frequency;
    public boolean is16bit;
    public boolean isStereo;    public int     sampleCount;
    public byte[]  soundData;
    
    public Sound( int format, int frequency, boolean is16bit, boolean isStereo, int sampleCount, byte[] soundData ) 
    {
        this.format      = format;      
        this.frequency   = frequency;   
        this.is16bit     = is16bit;     
        this.isStereo    = isStereo;            this.sampleCount = sampleCount; 
        this.soundData   = soundData;   
    }    
    
    protected int defineSymbol( Movie movie, 
                                SWFTagTypes timelineWriter,
                                SWFTagTypes definitionWriter )
        throws IOException
    {
        int id = getNextId( movie );
        definitionWriter.tagDefineSound( id, format, frequency, is16bit, isStereo, sampleCount, soundData );
        return id;
    }
    
    public int getFrequency() { return frequency; }
    public int getSampleCount() { return sampleCount; }
}