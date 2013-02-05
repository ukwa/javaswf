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
package com.anotherbigidea.flash.writers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import com.anotherbigidea.flash.SWFActionCodes;
import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.io.OutStream;

/**
 * A writer that implements the SWFActions interface and writes
 * action bytes to an OutStream
 */
public class ActionWriter implements SWFActions, SWFActionCodes 
{
    protected TagWriter tagWriter;
    protected OutStream out;
    protected ByteArrayOutputStream bout;
    protected int count;
    protected int flashVersion;
    protected String mStringEncoding;
    
    protected Vector pushValues;
    
    protected Hashtable labels;
    protected Vector jumps;
    protected Vector skips;
    
    //--for fixing up functions and WITH blocks..
    protected Vector blocks;
    protected Stack  blockStack;
    
    public ActionWriter( TagWriter tagWriter, int flashVersion )
    {
        this.flashVersion = flashVersion;
        this.tagWriter = tagWriter;
        
        mStringEncoding = ( flashVersion >= SWFConstants.FLASH_MX_VERSION ) ?
        						SWFConstants.STRING_ENCODING_MX :
								SWFConstants.STRING_ENCODING_PRE_MX;
    }
                            
    /**
     * @return the code count
     */
    protected int writeCode( int code ) throws IOException 
    {
        if( pushValues.size() > 0 ) flushPushValues();
        out.writeUI8( code );
        count++;
        return count;
    }
    
    /**
     * SWFActions interface
     */
    public void start( int conditions ) throws IOException
    {
        //ignore conditions
        
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
    
    /**
     * SWFActions interface
     */
    public void end() throws IOException
    {
        writeCode( 0 );
        out.flush();
        byte[] bytes = bout.toByteArray();
     
        //--Fix up jumps and skips
        if( labels != null )
        {
            if( jumps != null ) fixupJumps(bytes);
            if( skips != null ) fixupSkips(bytes);
        }
        
        if( blocks != null ) fixupBlocks(bytes);

        writeBytes( bytes );
    }
    
    /**
     * Pass through a blob of actions
     */
    public void blob( byte[] blob ) throws IOException
    {
        writeBytes( blob );
    }
        
    protected void writeBytes( byte[] bytes ) throws IOException
    {
        tagWriter.getOutStream().write( bytes );                       
    }
    
    /**
     * SWFActions interface
     */
    public void done() throws IOException
    {
        tagWriter.completeTag();
    }
        
    protected void fixupBlocks( byte[] bytes )
    {
        for( Enumeration enum = blocks.elements(); enum.hasMoreElements(); )
        {
            int[] info = (int[])enum.nextElement();
            
            int codeSize = info[1];
            int offset   = info[0];
            byte[] sizeBytes = OutStream.sintTo2Bytes( codeSize );
            
            bytes[ offset     ] = sizeBytes[0];
            bytes[ offset + 1 ] = sizeBytes[1];
        }
    }
    
    protected void fixupJumps( byte[] bytes )
    {
        for( Enumeration enum = jumps.elements(); enum.hasMoreElements(); )
        {
            Object[] obja = (Object[])enum.nextElement();
            String label  = (String)obja[0];
            int    target = ((Integer)obja[1]).intValue();
            
            int[] labelInfo = (int[])labels.get( label );
            
            if( labelInfo == null )
            {
                System.out.println( "Missing label '" + label + "' in action code" );
                continue;
            }
            
            int absolute = labelInfo[0];  //offset of the label            
            int relative = absolute - ( target + 2 );  //relative jump
            
            byte[] val = OutStream.sintTo2Bytes( relative );
            bytes[target  ] = val[0];
            bytes[target+1] = val[1];
        }
    }
    
    protected void fixupSkips( byte[] bytes )
    {
        for( Enumeration enum = skips.elements(); enum.hasMoreElements(); )
        {
            Object[] obja = (Object[])enum.nextElement();
            String label  = (String)obja[0];
            
            int[] skipInfo  = (int[])obja[1];
            int   skipIndex = skipInfo[0];
            int   skipLoc   = skipInfo[1];            
            
            int[] labelInfo = (int[])labels.get( label );
            
            if( labelInfo == null )
            {
                System.out.println( "Missing label '" + label + "' in action code" );
                continue;
            }
            
            int labelIndex = labelInfo[1];  //index of the labelled action
            int skip = labelIndex - skipIndex - 1;

            byte val = OutStream.uintToByte( skip );
            bytes[skipLoc] = val;
        }
    }

    /**
     * SWFActions interface
     */
    public void comment( String comment ) throws IOException
    {
        //ignore comments
    }    
    
    /**
     * SWFActions interface
     */
    public void unknown( int code, byte[] data ) throws IOException
    {
        writeCode( code );
        
        int length = (data != null) ? data.length : 0;
        
        if( code >= 0x80 || length > 0 )
        {
            out.writeUI16( length );
        }
        
        if( length > 0 ) out.write( data );
    }
    
    /**
     * SWFActions interface
     */
    public void initArray() throws IOException 
    {
        writeCode( INIT_ARRAY );
    }    
        
    /**
     * SWFActions interface
     */
    public void jumpLabel( String label ) throws IOException
    {
        if( pushValues.size() > 0 ) flushPushValues();
        
        int offset = (int)out.getCount();
        
        if( labels == null ) labels = new Hashtable();        
        labels.put( label, new int[] { offset, count + 1 } );
    }    
  
    /**
     * SWFActions interface
     */
    public void gotoFrame( int frameNumber ) throws IOException
    {
        writeCode( GOTO_FRAME );
        out.writeUI16( 2 );
        out.writeUI16( frameNumber );
    }
    
    /**
     * SWFActions interface
     */
    public void gotoFrame( String label ) throws IOException
    {
        writeCode( GOTO_LABEL );
        out.writeUI16  ( OutStream.getStringLength( label ) );
        out.writeString( label, mStringEncoding );
    }
    
    /**
     * SWFActions interface
     */
    public void getURL( String url, String target ) throws IOException
    {
        writeCode( GET_URL );
        out.writeUI16  ( OutStream.getStringLength(url) + OutStream.getStringLength(target) );
        out.writeString( url, mStringEncoding );
        out.writeString( target, mStringEncoding );
    }
    
    /**
     * SWFActions interface
     */
    public void nextFrame() throws IOException
    {
        writeCode( NEXT_FRAME );
    }
    
    /**
     * SWFActions interface
     */
    public void prevFrame() throws IOException
    {
        writeCode( PREVIOUS_FRAME );
    }
    
    /**
     * SWFActions interface
     */
    public void play() throws IOException
    {
        writeCode( PLAY );
    }
    
    /**
     * SWFActions interface
     */
    public void stop() throws IOException
    {
        writeCode( STOP );
    }
    
    /**
     * SWFActions interface
     */
    public void toggleQuality() throws IOException
    {
        writeCode( TOGGLE_QUALITY );
    }
    
    /**
     * SWFActions interface
     */
    public void stopSounds() throws IOException
    {
        writeCode( STOP_SOUNDS );
    }
    
    /**
     * SWFActions interface
     */
    public void setTarget( String target ) throws IOException
    {
        writeCode( SET_TARGET );
        out.writeUI16  ( OutStream.getStringLength( target ) );
        out.writeString( target, mStringEncoding );
    }

    protected void writeJump( String label, int code ) throws IOException 
    {
        writeCode( code );
        out.writeUI16( 2 );
        
        int here = (int)out.getCount();
        out.writeUI16( 0 );   //will be fixed up later
        
        //--save jump info for later fix-up logic
        if( jumps == null ) jumps = new Vector();
        jumps.addElement( new Object[] { label, new Integer( here ) } );
    }
    
    /**
     * SWFActions interface
     */
    public void jump( String jumpLabel ) throws IOException
    {
        writeJump( jumpLabel, JUMP );
    }
    
    /**
     * SWFActions interface
     */
    public void ifJump( String jumpLabel ) throws IOException
    {
        writeJump( jumpLabel, IF );
    }
    
    /**
     * SWFActions interface
     */
    public void waitForFrame( int frameNumber, String jumpLabel ) throws IOException
    {
        writeCode( WAIT_FOR_FRAME );
        out.writeUI16( 3 );
        out.writeUI16( frameNumber );

        int here = (int)out.getCount();
        out.writeUI8 ( 0 ); //will be fixed up later
        
        //--save skip info for later fix-up logic
        if( skips == null ) skips = new Vector();
        skips.addElement( new Object[] { jumpLabel, new int[] { count, here }} );        
    }
    
    /**
     * SWFActions interface
     */
    public void waitForFrame( String jumpLabel ) throws IOException
    {
        writeCode( WAIT_FOR_FRAME_2 );
        out.writeUI16( 1 );

        int here = (int)out.getCount();
        out.writeUI8 ( 0 ); //will be fixed up later
        
        //--save skip info for later fix-up logic
        if( skips == null ) skips = new Vector();
        skips.addElement( new Object[] { jumpLabel, new int[] { count, here }} );        
    }
    
    /**
     * SWFActions interface
     */
    public void pop() throws IOException
    {
        writeCode( POP );
    }
    
    /**
     * SWFActions interface
     */
    public void add() throws IOException
    {
        writeCode( ADD );
    }

    /**
     * SWFActions interface
     */
    public void substract() throws IOException
    {
        writeCode( SUBTRACT );
    }

    /**
     * SWFActions interface
     */
    public void multiply() throws IOException
    {
        writeCode( MULTIPLY );
    }

    /**
     * SWFActions interface
     */
    public void divide() throws IOException
    {
        writeCode( DIVIDE );
    }
    
    /**
     * SWFActions interface
     */
    public void equals() throws IOException
    {
        writeCode( EQUALS );
    }
   
    /**
     * SWFActions interface
     */
    public void lessThan() throws IOException
    {
        writeCode( LESS );
    }
    
    /**
     * SWFActions interface
     */
    public void and() throws IOException
    {
        writeCode( AND );
    }
   
    /**
     * SWFActions interface
     */
    public void or() throws IOException
    {
        writeCode( OR );
    }
    
    /**
     * SWFActions interface
     */
    public void not() throws IOException
    {
        writeCode( NOT );
    }
    
    /**
     * SWFActions interface
     */
    public void stringEquals() throws IOException
    {
        writeCode( STRING_EQUALS );
    }

    /**
     * SWFActions interface
     */
    public void stringLength() throws IOException
    {
        writeCode( STRING_LENGTH );
    }
    
    /**
     * SWFActions interface
     */
    public void concat() throws IOException
    {
        writeCode( STRING_ADD );
    }
    
    /**
     * SWFActions interface
     */
    public void substring() throws IOException
    {
        writeCode( STRING_EXTRACT );
    }
    
    /**
     * SWFActions interface
     */
    public void stringLessThan() throws IOException
    {
        writeCode( STRING_LESS );
    }
    
    /**
     * SWFActions interface
     */
    public void stringLengthMB() throws IOException
    {
        writeCode( MB_STRING_LENGTH );
    }

    /**
     * SWFActions interface
     */
    public void substringMB() throws IOException
    {
        writeCode( MB_STRING_EXTRACT );
    }    

    /**
     * SWFActions interface
     */
    public void toInteger() throws IOException
    {
        writeCode( TO_INTEGER );
    }

    /**
     * SWFActions interface
     */
    public void charToAscii() throws IOException
    {
        writeCode( CHAR_TO_ASCII );
    }

    /**
     * SWFActions interface
     */
    public void asciiToChar() throws IOException
    {
        writeCode( ASCII_TO_CHAR );
    }

    /**
     * SWFActions interface
     */
    public void charMBToAscii() throws IOException
    {
        writeCode( MB_CHAR_TO_ASCII );
    }

    /**
     * SWFActions interface
     */
    public void asciiToCharMB() throws IOException
    {
        writeCode( MB_ASCII_TO_CHAR );
    }
    
    /**
     * SWFActions interface
     */
    public void call() throws IOException
    {
        writeCode( CALL );
        out.writeUI16( 0 );   //SWF File Format anomaly
    }

    /**
     * SWFActions interface
     */
    public void getVariable() throws IOException
    {
        writeCode( GET_VARIABLE );
    }

    /**
     * SWFActions interface
     */
    public void setVariable() throws IOException
    {
        writeCode( SET_VARIABLE );
    }
    
    /**
     * SWFActions interface
     */
    public void getURL( int sendVars, int loadMode ) throws IOException
    {
        writeCode( GET_URL_2 );
        out.writeUI16( 1 );

        int flags = 0;
        
        String sendVars_ = null;
        switch( sendVars )
        {
            case GET_URL_SEND_VARS_GET:  flags = 1; break;
            case GET_URL_SEND_VARS_POST: flags = 2; break;
                                         
            case GET_URL_SEND_VARS_NONE:
            default: break;
        }
        
        String mode = null;
        switch( loadMode )
        {
            case GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL:  break;                
            case GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE: flags |= 0x40; break;
            case GET_URL_MODE_LOAD_VARS_INTO_LEVEL :  flags |= 0x80; break;
            case GET_URL_MODE_LOAD_VARS_INTO_SPRITE:  flags |= 0xC0; break;
            default: break;
        }
        
        out.writeUI8( flags );        
    }
    
    
    /**
     * SWFActions interface
     */
    public void gotoFrame( boolean play ) throws IOException
    {
        writeCode( GOTO_FRAME_2 );
        out.writeUI16( 1 );
        out.writeUI8( play ? 1 : 0 );
    }
    
    /**
     * SWFActions interface
     */
    public void setTarget() throws IOException
    {
        writeCode( SET_TARGET_2 );
    }
    
    /**
     * SWFActions interface
     */
    public void getProperty() throws IOException
    {
        writeCode( GET_PROPERTY );
    }
    
    /**
     * SWFActions interface
     */
    public void setProperty() throws IOException
    {
        writeCode( SET_PROPERTY );
    }
    
    /**
     * SWFActions interface
     */
    public void cloneSprite() throws IOException
    {
        writeCode( CLONE_SPRITE );
    }
    
    /**
     * SWFActions interface
     */
    public void removeSprite() throws IOException
    {
        writeCode( REMOVE_SPRITE );
    }
    
    /**
     * SWFActions interface
     */
    public void startDrag() throws IOException
    {
        writeCode( START_DRAG );
    }
    
    /**
     * SWFActions interface
     */
    public void endDrag() throws IOException
    {
        writeCode( END_DRAG );
    }     
    
    /**
     * SWFActions interface
     */
    public void trace() throws IOException
    {
        writeCode( TRACE );
    }     
    
    /**
     * SWFActions interface
     */
    public void getTime() throws IOException
    {
        writeCode( GET_TIME );
    }     
     
    /**
     * SWFActions interface
     */
    public void randomNumber() throws IOException
    {
        writeCode( RANDOM_NUMBER );
    }         
     
    /**
     * SWFActions interface
     */
    public void lookupTable( String[] values ) throws IOException
    {
        writeCode( LOOKUP_TABLE );
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutStream bout = new OutStream( baout );
        
        bout.writeUI16( values.length );
        
        for( int i = 0; i < values.length; i++ )
        {
            bout.writeString( values[i], mStringEncoding );
        }

        bout.flush();
        byte[] data = baout.toByteArray();
        out.writeUI16( data.length );
        out.write( data );        
    }
     
    /**
     * SWFActions interface
     */    
    public void callFunction() throws IOException
    {
        writeCode( CALL_FUNCTION );
    }         
     
    /**
     * SWFActions interface
     */                                      
    public void callMethod() throws IOException
    {
        writeCode( CALL_METHOD );
    }         
     
    /**
     * SWFActions interface
     */     
    public void startFunction( String name, String[] paramNames ) throws IOException
    {
        if( blockStack == null ) blockStack = new Stack();
        
        writeCode( DEFINE_FUNCTION );        
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutStream bout = new OutStream( baout );
        
        bout.writeString( name, mStringEncoding );
        bout.writeUI16( paramNames.length );
        
        for( int i = 0; i < paramNames.length; i++ )
        {
            bout.writeString( paramNames[i], mStringEncoding );
        }

        bout.writeUI16( 0 );  //code size - will be fixed up later
        
        bout.flush();
        byte[] data = baout.toByteArray();
        out.writeUI16( data.length );
        out.write( data );
        
        blockStack.push( new int[]{ (int)out.getCount(), 0 } );
    }    

    /**
     * SWFActions interface
     */     
    public void endBlock() throws IOException 
    {
        if( blockStack == null || blockStack.isEmpty() ) return;  //nothing to do
        int[] blockInfo = (int[])blockStack.pop();
                
        if( blocks == null ) blocks = new Vector();
        
        int offset =  blockInfo[0];
        int codeSize = ((int)out.getCount()) - offset;
        
        //--store this info for later fix-up
        blockInfo[0] = offset - 2;
        blockInfo[1] = codeSize;
        blocks.addElement( blockInfo );
    }
    
    /**
     * SWFActions interface
     */     
    public void defineLocalValue() throws IOException
    {
        writeCode( DEFINE_LOCAL_VAL );
    }

    /**
     * SWFActions interface
     */     
    public void defineLocal() throws IOException
    {
        writeCode( DEFINE_LOCAL );
    }   

    /**
     * SWFActions interface
     */     
    public void deleteProperty() throws IOException
    {
        writeCode( DEL_VAR );
    }   

    /**
     * SWFActions interface
     */     
    public void deleteThreadVars() throws IOException
    {
        writeCode( DEL_THREAD_VARS );
    }   

    /**
     * SWFActions interface
     */     
    public void enumerate() throws IOException
    {
        writeCode( ENUMERATE );
    }   

    /**
     * SWFActions interface
     */     
    public void typedEquals() throws IOException
    {
        writeCode( TYPED_EQUALS );
    }   

    /**
     * SWFActions interface
     */     
    public void getMember() throws IOException
    {
        writeCode( GET_MEMBER );
    }   

    /**
     * SWFActions interface
     */         
    public void initObject() throws IOException
    {
        writeCode( INIT_OBJECT );
    }   

    /**
     * SWFActions interface
     */     
    public void newMethod() throws IOException
    {
        writeCode( CALL_NEW_METHOD );
    }   

    /**
     * SWFActions interface
     */     
    public void newObject() throws IOException
    {
        writeCode( NEW_OBJECT );
    }   

    /**
     * SWFActions interface
     */     
    public void setMember() throws IOException
    {
        writeCode( SET_MEMBER );
    }   

    /**
     * SWFActions interface
     */     
    public void getTargetPath() throws IOException
    {
        writeCode( GET_TARGET_PATH );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void startWith() throws IOException
    {
        writeCode( WITH );
        out.writeUI16( 2 );
        out.writeUI16( 0 );  //codeSize - will be fixed up later
        
        //--push the block start info
        if( blockStack == null ) blockStack = new Stack();
        blockStack.push( new int[]{ (int)out.getCount(), 0 } );        
    }   
    
    /**
     * SWFActions interface
     */ 
    public void duplicate() throws IOException
    {
        writeCode( DUPLICATE );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void returnValue() throws IOException
    {
        writeCode( RETURN );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void swap() throws IOException
    {
        writeCode( SWAP );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void storeInRegister( int registerNumber ) throws IOException
    {
        writeCode( REGISTER );
        out.writeUI16( 1 );
        out.writeUI8( registerNumber );
    }   
        
    /**
     * SWFActions interface
     */ 
    public void convertToNumber() throws IOException
    {
        writeCode( CONVERT_TO_NUMBER );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void convertToString() throws IOException
    {
        writeCode( CONVERT_TO_STRING );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void typeOf() throws IOException
    {
        writeCode( TYPEOF );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void typedAdd() throws IOException
    {
        writeCode( TYPED_ADD );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void typedLessThan() throws IOException
    {
        writeCode( TYPED_LESS_THAN );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void modulo() throws IOException
    {
        writeCode( MODULO );
    }   
        
    /**
     * SWFActions interface
     */ 
    public void bitAnd() throws IOException
    {
        writeCode( BIT_AND );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void bitOr() throws IOException
    {
        writeCode( BIT_OR );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void bitXor() throws IOException
    {
        writeCode( BIT_XOR );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void shiftLeft() throws IOException
    {
        writeCode( SHIFT_LEFT );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void shiftRight() throws IOException
    {
        writeCode( SHIFT_RIGHT );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void shiftRightUnsigned() throws IOException
    {
        writeCode( SHIFT_UNSIGNED );
    }   
        
    /**
     * SWFActions interface
     */ 
    public void decrement() throws IOException
    {
        writeCode( DECREMENT );
    }   
    
    /**
     * SWFActions interface
     */ 
    public void increment() throws IOException
    {
        writeCode( INCREMENT );
    }   
    
    protected void flushPushValues() throws IOException
    {
        out.writeUI8( PUSH );
        count++;
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutStream bout = new OutStream( baout );
        
        for( Enumeration enum = pushValues.elements(); enum.hasMoreElements(); )
        {
            Object value = enum.nextElement();
            
            if( value instanceof String )
            {
                bout.writeUI8( PUSHTYPE_STRING );
                bout.writeString( value.toString(), mStringEncoding );
            }
            else if( value instanceof Boolean )
            {
                bout.writeUI8( PUSHTYPE_BOOLEAN );
                bout.writeUI8( ((Boolean)value).booleanValue() ? 1 : 0 );
            }
            else if( value instanceof Integer )
            {
                bout.writeUI8( PUSHTYPE_INTEGER );
                bout.writeSI32( ((Integer)value).intValue() );                
            }
            else if( value instanceof Short )
            {
                bout.writeUI8( PUSHTYPE_LOOKUP );
                bout.writeUI8( ((Short)value).intValue() );                
            }
            else if( value instanceof Byte )
            {
                bout.writeUI8( PUSHTYPE_REGISTER );
                bout.writeUI8( ((Byte)value).intValue() );                                   
            }
            else if( value instanceof Float )
            {
                bout.writeUI8( PUSHTYPE_FLOAT );
                bout.writeFloat( ((Float)value).floatValue() );                
            }
            else if( value instanceof Double )
            {
                bout.writeUI8( PUSHTYPE_DOUBLE );
                bout.writeDouble( ((Double)value).doubleValue() );
            }
            else
            {
                bout.writeUI8( PUSHTYPE_NULL );
            }
        }

        pushValues.removeAllElements();
        
        bout.flush();
        byte[] data = baout.toByteArray();
        out.writeUI16( data.length );
        out.write( data );
    }
        
    /**
     * SWFActions interface
     */
    public void push( String value ) throws IOException
    {
        pushValues.addElement( value );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void push( float  value ) throws IOException
    {
        pushValues.addElement( new Float( value ) );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void push( double value ) throws IOException
    {
        pushValues.addElement( new Double( value ) );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void pushNull() throws IOException
    {
        pushValues.addElement( new Object() );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void pushRegister( int registerNumber ) throws IOException
    {
        pushValues.addElement( new Byte( (byte)registerNumber ) );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void push( boolean value ) throws IOException
    {
        pushValues.addElement( new Boolean( value ) );
        if( flashVersion < 5 ) flushPushValues();
    }
    
    /**
     * SWFActions interface
     */
    public void push( int value ) throws IOException
    {
        pushValues.addElement( new Integer( value ) );
        if( flashVersion < 5 ) flushPushValues();
    }

    /**
     * SWFActions interface
     */
    public void lookup( int dictionaryIndex ) throws IOException
    {
        pushValues.addElement( new Short( (short)dictionaryIndex ) );
        if( flashVersion < 5 ) flushPushValues();
    }    
    
	/**
	 * @see com.anotherbigidea.flash.interfaces.SWFActions#enumerateObject()
	 */
	public void enumerateObject() throws IOException {
		writeCode( ENUMERATE_OBJECT );
	}

	/**
	 * @see com.anotherbigidea.flash.interfaces.SWFActions#greaterThan()
	 */
	public void greaterThan() throws IOException {
		writeCode( GREATER );
	}

	/**
	 * @see com.anotherbigidea.flash.interfaces.SWFActions#instanceOf()
	 */
	public void instanceOf() throws IOException {
		writeCode( INSTANCE_OF );
	}

	/**
	 * @see com.anotherbigidea.flash.interfaces.SWFActions#strictEquals()
	 */
	public void strictEquals() throws IOException {
		writeCode( STRICT_EQUALS );
	}

	/**
	 * @see com.anotherbigidea.flash.interfaces.SWFActions#stringGreaterThan()
	 */
	public void stringGreaterThan() throws IOException {
		writeCode( STRING_GREATER );
	}

}
