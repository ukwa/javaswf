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

import java.io.*;
import java.util.*;
import com.anotherbigidea.flash.structs.*;

/**
 * Interface for passing SWF tag types.
 */
public interface SWFTagTypes extends SWFSpriteTagTypes 
{           /**
     * @param format one of the SWFConstants.SOUND_FORMAT_* constants
     * @param frequency one of the SWFConstants.SOUND_FREQ_* constants
     * @param soundData format-dependent sound data
     */
    public void tagDefineSound( int id, int format, int frequency,
                                boolean bits16, boolean stereo,
                                int sampleCount, byte[] soundData ) throws IOException;        /**
     * Define the sound for a button
     */
    public void tagDefineButtonSound( int buttonId,                    int rollOverSoundId, SoundInfo rollOverSoundInfo,                    int rollOutSoundId,  SoundInfo rollOutSoundInfo,                    int pressSoundId,    SoundInfo pressSoundInfo,                    int releaseSoundId,  SoundInfo releaseSoundInfo )        throws IOException; 
        /**
     * @return SWFShape to receive shape info - or null to skip the data
     */
    public SWFShape tagDefineShape( int id, Rect outline ) throws IOException;
        /**
     * @return SWFShape to receive shape info - or null to skip the data
     */
    public SWFShape tagDefineShape2( int id, Rect outline ) throws IOException;
        /**
     * @return SWFShape to receive shape info - or null to skip the data
     */
    public SWFShape tagDefineShape3( int id, Rect outline ) throws IOException;
        /**
     * @param buttonRecords contains ButtonRecord objects
     * @return SWFActions object (may be null) to receive button actions - there is
     *         only one action array (with no conditions).
     * @see com.anotherbigidea.flash.structs.ButtonRecord
     */
    public SWFActions tagDefineButton( int id, Vector buttonRecords )
        throws IOException;
        public void tagButtonCXForm( int buttonId, ColorTransform transform ) throws IOException;
        /**
     * @param buttonRecord2s contains ButtonRecord2 objects
     * @return SWFActions object (may be null) to receive button actions - there may
     *         be multiple action arrays - each one is conditional, using the
     *         BUTTON2_* condition flags defined in SWFConstants.java
     * @see com.anotherbigidea.flash.structs.ButtonRecord2
     */        public SWFActions tagDefineButton2( int id,                                         boolean trackAsMenu,                                         Vector buttonRecord2s )
        throws IOException;        
    public void tagSetBackgroundColor( Color color ) throws IOException;        /**
     * The SWFVectors object returned will be called numGlyphs times to
     * pass the vector information for each glyph (each glyph is terminated
     * by calling SWFVectors.done() )
     */
    public SWFVectors tagDefineFont( int id, int numGlyphs ) throws IOException; 
    /**
     * @return SWFText object to receive the text style and glyph information -
     *                 this may be null if the info is not required
     */    public SWFText tagDefineText( int id, Rect bounds, Matrix matrix ) throws IOException;    /**
     * Allows alpha colors
     * @return SWFText object to receive the text style and glyph information -
     *                 this may be null if the info is not required
     */    public SWFText tagDefineText2( int id, Rect bounds, Matrix matrix ) throws IOException;        /**
     * @param flags see FONT_* constants in SWFConstants.java
     */
    public void tagDefineFontInfo( int fontId, String fontName, 
                                   int flags, int[] codes ) throws IOException;    
	/**
	 * @param flags see FONT_* constants in SWFConstants.java
	 * @param languageCode @see com.anotherbigidea.flash.SWFConstants#LANGUAGE_CODE_NONE etc.
	 */
	public void tagDefineFontInfo2( int fontId, String fontName, 
								    int flags, int[] codes, int languageCode ) throws IOException;
        
        /**
     * @param data must contain the header data - use the InputStream version
     *             when using an external JPEG
     */
    public void tagDefineBitsJPEG2( int id, byte[] data ) throws IOException;        /**
     * @param jpegImage must be a baseline JPEG (not a progressive JPEG)
     */
    public void tagDefineBitsJPEG2( int id, InputStream jpegImage ) throws IOException;        /**
     * JPEG image data only - header/encoding data is in tagJPEGTables tag
     */
    public void tagDefineBits( int id, byte[] imageData ) throws IOException;            /** 
     * Only one tag per SWF - holds common JPEG encoding data
     */
    public void tagJPEGTables( byte[] jpegEncodingData ) throws IOException;    
    /**
     * JPEG image and encoding data with alpha channel bitmap
     * @param alphaData is zlib compressed
     */
    public void tagDefineBitsJPEG3( int id, byte[] imageData, byte[] alphaData ) throws IOException;    
        /**
     * @param format one of the SWFConstants.BITMAP_FORMAT_n_BIT constants
     */
    public void tagDefineBitsLossless( int id, int format, int width, int height,                                       Color[] colors, byte[] imageData )        throws IOException; 
        /**
     * @param format one of the SWFConstants.BITMAP_FORMAT_n_BIT constants
     */
    public void tagDefineBitsLossless2( int id, int format, int width, int height,                                        Color[] colors, byte[] imageData )        throws IOException; 
        /**
     * @param password may be null
     */
    public void tagProtect( byte[] password ) throws IOException;            /**
     * @param flags see TEXTFIELD_* constants in SWFConstants.java
     */
    public void tagDefineTextField( int fieldId, String fieldName,
                    String initialText, Rect boundary, int flags,
                    AlphaColor textColor, int alignment, int fontId, int fontSize, 
                    int charLimit, int leftMargin, int rightMargin, int indentation,
                    int lineSpacing ) 
        throws IOException;
    
    public void tagDefineQuickTimeMovie( int id, String filename ) throws IOException;    
    public SWFTagTypes tagDefineSprite( int id ) throws IOException;    
    public SWFShape tagDefineMorphShape( int id, Rect startBounds, Rect endBounds )         throws IOException;
    
    public SWFVectors tagDefineFont2( int id, int flags, String name, int numGlyphs,
                                      int ascent, int descent, int leading,
                                      int[] codes, int[] advances, Rect[] bounds,
                                      int[] kernCodes1, int[] kernCodes2,
                                      int[] kernAdjustments ) throws IOException;
    
    public void tagExport( String[] names, int[] ids ) throws IOException;
    
    public void tagImport( String movieName, String[] names, int[] ids ) throws IOException;
    
    public void tagEnableDebug( byte[] password ) throws IOException;

	public void tagEnableDebug2( byte[] password ) throws IOException;
    
    /**
     * In files produced by Generator...
     */
    public void tagSerialNumber( String serialNumber ) throws IOException;            /**
     * In Generator templates.  Data is not parsed.
     */
    public void tagGenerator( byte[] data ) throws IOException;            /**
     * In Generator templates.  Data is not parsed.
     */
    public void tagGeneratorText( byte[] data ) throws IOException;    
    
    /**
     * In Generator templates.  Data is not parsed.
     */
    public void tagGeneratorCommand( byte[] data ) throws IOException;    

    /**
     * In Generator templates.  Data is not parsed.
     */
    public void tagNameCharacter( byte[] data ) throws IOException;  
        /**
     * In Generator templates.  Data is not parsed.
     */
    public void tagGeneratorFont( byte[] data ) throws IOException;     
}
