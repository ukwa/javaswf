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
package com.anotherbigidea.io;

import java.io.*;

public class Byte4ByteDebugStreams extends OutputStream 
{
    protected ByteArrayInputStream in;
    
    protected byte[] bytesIn;
    protected byte[] bytesOut;
    
    protected int bytePtr = 0;
    
    public Byte4ByteDebugStreams( String filenameIn ) throws Exception 
    {
        RandomAccessFile raIn = new RandomAccessFile( filenameIn, "r" );
        
        bytesIn  = new byte[ (int)raIn.length() ];
        bytesOut = new byte[ (int)raIn.length() ];
        raIn.readFully( bytesIn );
        raIn.close();
    }
    
    public Byte4ByteDebugStreams( byte[] bytesIn )
    {
        this.bytesIn = bytesIn;
        bytesOut = new byte[ bytesIn.length ];    
    }

    public InputStream getInputStream()
    {
        if( in != null ) return in;
        
        in = new ByteArrayInputStream( bytesIn );
        
        return in;
    }
    
    public void setInputBytes( byte[] inBytes ) 
    {
        bytesIn = inBytes; 
    }
    
    public void write( int b ) throws IOException
    {
        if( b > 127 ) b = (b & 0x7f) - 128;
        
        bytesOut[bytePtr] = (byte)b;
        
        if( bytesOut[bytePtr] != bytesIn[bytePtr] )
        {
            IOException ioe = new IOException("Byte mismatch between input and output at byte #" + bytePtr 
                                              + " 0x" + Integer.toHexString( bytePtr )
                                              + "\nexpected 0x" 
                                              + Integer.toHexString(bytesIn[bytePtr]) 
                                              + " but got 0x" 
                                              +  Integer.toHexString(b));
                
            ioe.printStackTrace();
            
            throw ioe;
        }
        
        bytePtr++;
    }

    public void write( String filenameOut ) throws IOException 
    {
        FileOutputStream out = new FileOutputStream( filenameOut );
        
        out.write( bytesOut );
        out.flush();
        out.close();
    }
}
