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
package com.anotherbigidea.flash.interfaces;

import java.io.IOException;

/**
 * Interface for passing Action Codes
 *
 * Lifecycle is -
 *  1. start(..) is called with any condition flags (e.g. event codes) for the
 *               action array
 *  2. action methods are called
 *  3. end() is called to terminate array
 *  4. 1..3 is repeated for any subsequent condition blocks
 *  5. done() is called to terminate all action passing
 */
public interface SWFActions
{
    /**
     * Start of actions
     */
    public void start( int flags ) throws IOException;

    /**
     * End of all action blocks
     */
    public void done() throws IOException;

    /**
     * End of actions
     */
    public void end() throws IOException;

    /**
     * Pass through a blob of actions
     */
    public void blob( byte[] blob ) throws IOException;

    /**
     * Unrecognized action code
     * @param data may be null
     */
    public void unknown( int code, byte[] data ) throws IOException;

    /**
     * Target label for a jump - this method call immediately precedes the
     * target action.
     */
    public void jumpLabel( String label ) throws IOException;

    /**
     * Comment Text - useful for debugging purposes
     */
    public void comment( String comment ) throws IOException;

    //--Flash 3 Actions:
    public void gotoFrame( int frameNumber ) throws IOException;
    public void gotoFrame( String label ) throws IOException;
    public void getURL( String url, String target ) throws IOException;
    public void nextFrame() throws IOException;
    public void prevFrame() throws IOException;
    public void play() throws IOException;
    public void stop() throws IOException;
    public void toggleQuality() throws IOException;
    public void stopSounds() throws IOException;
    public void waitForFrame( int frameNumber, String jumpLabel ) throws IOException;
    public void setTarget( String target ) throws IOException;

    //--Flash 4 Actions:
    public void push( String value ) throws IOException;
    public void push( float  value ) throws IOException;
    public void pop() throws IOException;

    public void add() throws IOException;
    public void substract() throws IOException;
    public void multiply() throws IOException;
    public void divide() throws IOException;

    public void equals() throws IOException;
    public void lessThan() throws IOException;

    public void and() throws IOException;
    public void or() throws IOException;
    public void not() throws IOException;

    public void stringEquals() throws IOException;
    public void stringLength() throws IOException;
    public void concat() throws IOException;
    public void substring() throws IOException;
    public void stringLessThan() throws IOException;
    public void stringLengthMB() throws IOException;
    public void substringMB() throws IOException;

    public void toInteger() throws IOException;
    public void charToAscii() throws IOException;
    public void asciiToChar() throws IOException;
    public void charMBToAscii() throws IOException;
    public void asciiToCharMB() throws IOException;

    public void jump( String jumpLabel ) throws IOException;
    public void ifJump( String jumpLabel ) throws IOException;

    public void call() throws IOException;

    public void getVariable() throws IOException;
    public void setVariable() throws IOException;

    //----------------------------------------------------------
    public static final int GET_URL_SEND_VARS_NONE = 0;  //don't send variables
    public static final int GET_URL_SEND_VARS_GET  = 1;  //send vars using GET
    public static final int GET_URL_SEND_VARS_POST = 2;  //send vars using POST

    public static final int GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL  = 0;
    public static final int GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE = 1;
    public static final int GET_URL_MODE_LOAD_VARS_INTO_LEVEL   = 3;
    public static final int GET_URL_MODE_LOAD_VARS_INTO_SPRITE  = 4;

    public void getURL( int sendVars, int loadMode ) throws IOException;
    //----------------------------------------------------------

    public void gotoFrame( boolean play ) throws IOException;
    public void setTarget() throws IOException;
    public void getProperty() throws IOException;
    public void setProperty() throws IOException;
    public void cloneSprite() throws IOException;
    public void removeSprite() throws IOException;
    public void startDrag() throws IOException;
    public void endDrag() throws IOException;
    public void waitForFrame( String jumpLabel ) throws IOException;
    public void trace() throws IOException;
    public void getTime() throws IOException;
    public void randomNumber() throws IOException;

    //--Flash 5 Actions
    public void callFunction() throws IOException;
    public void callMethod() throws IOException;
    public void lookupTable( String[] values ) throws IOException;

    //startFunction(..) is terminated by matching endBlock()
    public void startFunction( String name, String[] paramNames ) throws IOException;
    public void endBlock() throws IOException;

    public void defineLocalValue() throws IOException;
    public void defineLocal() throws IOException;

    public void deleteProperty() throws IOException;
    public void deleteThreadVars() throws IOException;

    public void enumerate() throws IOException;
    public void typedEquals() throws IOException;
    public void getMember() throws IOException;

    public void initArray() throws IOException;
    public void initObject() throws IOException;
    public void newMethod() throws IOException;
    public void newObject() throws IOException;
    public void setMember() throws IOException;
    public void getTargetPath() throws IOException;

    public void startWith() throws IOException;  //terminated by matching endBlock()

    public void convertToNumber() throws IOException;
    public void convertToString() throws IOException;
    public void typeOf() throws IOException;
    public void typedAdd() throws IOException;
    public void typedLessThan() throws IOException;
    public void modulo() throws IOException;

    public void bitAnd() throws IOException;
    public void bitOr() throws IOException;
    public void bitXor() throws IOException;
    public void shiftLeft() throws IOException;
    public void shiftRight() throws IOException;
    public void shiftRightUnsigned() throws IOException;

    public void decrement() throws IOException;
    public void increment() throws IOException;

    public void duplicate() throws IOException;
    public void returnValue() throws IOException;
    public void swap() throws IOException;
    public void storeInRegister( int registerNumber ) throws IOException;

    public void push( double value ) throws IOException;
    public void pushNull() throws IOException;
    public void pushRegister( int registerNumber ) throws IOException;
    public void push( boolean value ) throws IOException;
    public void push( int value ) throws IOException;
    public void lookup( int dictionaryIndex ) throws IOException;
    
	//--Flash 6 Actions
	public void instanceOf() throws IOException;
	public void enumerateObject() throws IOException;
	public void strictEquals() throws IOException;
	public void greaterThan() throws IOException;
	public void stringGreaterThan() throws IOException;    
}
