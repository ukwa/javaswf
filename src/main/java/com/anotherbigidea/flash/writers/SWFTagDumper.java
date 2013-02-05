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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.interfaces.SWFFileSignature;
import com.anotherbigidea.flash.interfaces.SWFShape;
import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.interfaces.SWFTags;
import com.anotherbigidea.flash.interfaces.SWFText;
import com.anotherbigidea.flash.interfaces.SWFVectors;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.AlphaTransform;
import com.anotherbigidea.flash.structs.ButtonRecord;
import com.anotherbigidea.flash.structs.ButtonRecord2;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.ColorTransform;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.structs.SoundInfo;
import com.anotherbigidea.util.Hex;

/**
 * An implementation of the SWFTagTypes interface that produces a debug dump
 */
public class SWFTagDumper 
	implements SWFTagTypes, SWFShape, SWFText, SWFFileSignature 
{
    protected PrintWriter writer;
    protected String dashes = "---------------";
    protected boolean dumpHex;
    protected String indent = "";
    protected boolean decompileActions = false;
    
    /** 
     * Dump to System.out
     * @param dumpHex if true then binary data will dumped as hex - otherwise skipped
     */
    public SWFTagDumper( boolean dumpHex, boolean decompileActions )
    {
        this( System.out, dumpHex, decompileActions );
    }
    
    /**
     * Dump to the given output stream
     * @param dumpHex if true then binary data will dumped as hex - otherwise skipped
     */
    public SWFTagDumper( OutputStream out, 
                         boolean dumpHex, 
                         boolean decompileActions )
    {
        writer = new PrintWriter( out );
        this.dumpHex = dumpHex;
        this.decompileActions = decompileActions;
    }
    
    public SWFTagDumper( PrintWriter writer, 
                         boolean dumpHex, 
                         boolean decompileActions )
    {
        this.writer = writer;
        this.dumpHex = dumpHex;
        this.decompileActions = decompileActions;
    }

	/**
	 * @see SWFFileSignature#signature(String)
	 */
	public void signature( String sig ) {
		println( "signature: " + sig );
	}

    protected void println( String line )
    {
        writer.println( indent + line );
    }
    
    /**
     * SWFTags interface
     */    
    public void tag( int tagType, boolean longTag, byte[] contents ) 
        throws IOException
    {
        println( "Tag " + tagType + " length=" + contents.length );
        
        if( dumpHex )
        {
            Hex.dump( writer, contents, 0L, indent + "    ", false );
            println( dashes );
        }
    }

    /**
     * SWFHeader interface.
     */
    public void header( int version, long length,
                        int twipsWidth, int twipsHeight,
                        int frameRate, int frameCount ) throws IOException
    {
        println( "header: version=" + version + 
                 " length=" + length + " width=" + twipsWidth +
                 " height=" + twipsHeight + " rate=" + frameRate +
                 " frame-count=" + frameCount );
    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagEnd() throws IOException
    {
        println( "end" );
        println( dashes );
    }

    /**
     * SWFTagTypes interface
     */
    public void tagStartSound( int soundId, SoundInfo info ) throws IOException    {
        println( "start-sound id=" + soundId + " " + info );    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamHead(         int playbackFrequency, boolean playback16bit, boolean playbackStereo,        int streamFormat, int streamFrequency, boolean stream16bit, boolean streamStereo,        int averageSampleCount ) throws IOException    {
        printSoundStreamHead( "sound-stream-head", 
            playbackFrequency, playback16bit, playbackStereo,            streamFormat, streamFrequency, stream16bit, streamStereo,            averageSampleCount );    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamHead2(         int playbackFrequency, boolean playback16bit, boolean playbackStereo,        int streamFormat, int streamFrequency, boolean stream16bit, boolean streamStereo,        int averageSampleCount ) throws IOException    {
        printSoundStreamHead( "sound-stream-head-2", 
            playbackFrequency, playback16bit, playbackStereo,            streamFormat, streamFrequency, stream16bit, streamStereo,            averageSampleCount );    }
    
    public void printSoundStreamHead( String name,        int playbackFrequency, boolean playback16bit, boolean playbackStereo,        int streamFormat, int streamFrequency, boolean stream16bit, boolean streamStereo,        int averageSampleCount ) throws IOException    {        String playFreq = "5.5";
        if( playbackFrequency == SWFConstants.SOUND_FREQ_11KHZ ) playFreq = "11";        if( playbackFrequency == SWFConstants.SOUND_FREQ_22KHZ ) playFreq = "22";        if( playbackFrequency == SWFConstants.SOUND_FREQ_44KHZ ) playFreq = "44";                String streamFreq = "5.5";
        if( streamFrequency == SWFConstants.SOUND_FREQ_11KHZ ) streamFreq = "11";        if( streamFrequency == SWFConstants.SOUND_FREQ_22KHZ ) streamFreq = "22";        if( streamFrequency == SWFConstants.SOUND_FREQ_44KHZ ) streamFreq = "44";                        String format = "RawSamples";
        if( streamFormat == SWFConstants.SOUND_FORMAT_ADPCM ) format = "ADPCM";        if( streamFormat == SWFConstants.SOUND_FORMAT_MP3   ) format = "MP3";        
        println( name + " play at " + playFreq + "kHz stereo=" + playbackStereo +
                 " 16bit=" + playback16bit + " | Stream at " + streamFreq +
                 "kHz format=" + format + " stereo=" + streamStereo +
                 " 16bit=" + stream16bit + " Avg-Samples=" + averageSampleCount );    }
    
        /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamBlock( byte[] soundData ) throws IOException    {
        println( "sound-stream-block" );
        
        if( dumpHex )
        {
            Hex.dump( writer, soundData, 0L, indent + "    ", false );
            println( dashes );
        }            }            /**
     * SWFTagTypes interface
     */
    public void tagSerialNumber( String serialNumber ) throws IOException    {
        println( "serial number =" + serialNumber );           }    
                                                                      /**
     * SWFTagTypes interface
     */
    public void tagGenerator( byte[] data ) throws IOException    {
        println( "generator tag" );
        
        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }            }               /**
     * SWFTagTypes interface
     */
    public void tagGeneratorText( byte[] data ) throws IOException    {
        println( "generator text" );
        
        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }            }        
        /**
     * SWFTagTypes interface
     */
    public void tagGeneratorFont( byte[] data ) throws IOException    {
        println( "generator font" );
        
        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }            }        
    /**
     * SWFTagTypes interface
     */
    public void tagGeneratorCommand( byte[] data ) throws IOException    {
        println( "generator command" );
        
        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }            }            

    /**
     * SWFTagTypes interface
     */
    public void tagNameCharacter( byte[] data ) throws IOException    {
        println( "generator name character" );
        
        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }            }                        /**
     * SWFTagTypes interface
     */
    public void tagDefineBits( int id, byte[] imageData ) throws IOException    {
        println( "jpeg bits" );
        
        if( dumpHex )
        {
            Hex.dump( writer, imageData, 0L, indent + "    ", false );
            println( dashes );
        }            }                    /**
     * SWFTagTypes interface
     */
    public void tagJPEGTables( byte[] jpegEncodingData ) throws IOException    {
        println( "jpeg encoding data" );
        
        if( dumpHex )
        {
            Hex.dump( writer, jpegEncodingData, 0L, indent + "    ", false );
            println( dashes );
        }            }                
    /**
     * SWFTagTypes interface
     */
    public void tagDefineBitsJPEG3( int id, byte[] imageData, byte[] alphaData ) throws IOException    {
        println( "jpeg with alpha" );
        
        if( dumpHex )
        {
            Hex.dump( writer, imageData, 0L, indent + "    ", false );
            println( "--- Alpha Channel follows ---" );
            Hex.dump( writer, alphaData, 0L, indent + "    ", false );
            println( dashes );
        }             }                
    
    /**
     * SWFTagTypes interface
     */
    public void tagDefineSound( int id, int format, int frequency,
                                boolean bits16, boolean stereo,
                                int sampleCount, byte[] soundData ) throws IOException    {        String freq = "5.5";
        if( frequency == SWFConstants.SOUND_FREQ_11KHZ ) freq = "11";        if( frequency == SWFConstants.SOUND_FREQ_22KHZ ) freq = "22";        if( frequency == SWFConstants.SOUND_FREQ_44KHZ ) freq = "44";                        String formatS = "RawSamples";
        if( format == SWFConstants.SOUND_FORMAT_ADPCM ) formatS = "ADPCM";        if( format == SWFConstants.SOUND_FORMAT_MP3   ) formatS = "MP3";                        
        println( "define sound: id=" + id + " format=" + formatS +                  " freq=" + freq + "kHz 16bit=" + bits16 +                 " stereo=" + stereo + " samples=" + sampleCount );
        
        if( dumpHex )
        {
            Hex.dump( writer, soundData, 0L, indent + "    ", false );
            println( dashes );
        }            }                        /**
     * SWFTagTypes interface
     */
    public void tagDefineButtonSound( int buttonId,                    int rollOverSoundId, SoundInfo rollOverSoundInfo,                    int rollOutSoundId,  SoundInfo rollOutSoundInfo,                    int pressSoundId,    SoundInfo pressSoundInfo,                    int releaseSoundId,  SoundInfo releaseSoundInfo )        throws IOException    {
        println( "define button sound: id=" + buttonId );
        println( "    roll-over sound=" + rollOverSoundId + " " + rollOverSoundInfo );
        println( "    roll-out  sound=" + rollOutSoundId  + " " + rollOutSoundInfo );
        println( "    press     sound=" + pressSoundId    + " " + pressSoundInfo );
        println( "    release   sound=" + releaseSoundId  + " " + releaseSoundInfo );           }                
    
    /**
     * SWFTagTypes interface
     */
    public void tagShowFrame() throws IOException
    {
        println( "---------- frame ----------" );    }    
    /**
     * SWFTagTypes interface
     */
    public SWFActions tagDoAction() throws IOException    {
        println( "actions:" );
        
        ActionTextWriter acts = new ActionTextWriter( writer );
        acts.indent = "    " + indent ;
        return acts;
    }
    
	/**
	 * SWFTagTypes interface
	 */
	public SWFActions tagDoInitAction( int spriteId ) throws IOException
	{
		println( "init actions for sprite " + spriteId + ":" );
        
		ActionTextWriter acts = new ActionTextWriter( writer );
		acts.indent = "    " + indent ;
		return acts;
	}
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape( int id, Rect outline ) throws IOException    {
        println( "shape id=" + id + "   " + outline );
        return this;    }
    
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape2( int id, Rect outline ) throws IOException    {        println( "shape2 id=" + id + "   " + outline );
        return this;    }
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape3( int id, Rect outline ) throws IOException    {
        println( "shape3 id=" + id + "   " + outline );
        return this;    }    
    
    /**
     * SWFTagTypes interface
     */    
    public void tagFreeCharacter( int charId ) throws IOException 
    {
        println( "free character id=" + charId );
    }
    
    /**
     * SWFTagTypes interface
     */    
    public void tagPlaceObject( int charId, int depth,                                 Matrix matrix, AlphaTransform cxform )         throws IOException
    {        println( "place-object id=" + charId +                  " depth=" + depth + "  " + matrix + "  " + cxform );
    }    
    /**
     * SWFTagTypes interface
     */    
    public SWFActions tagPlaceObject2( boolean isMove,
                                       int clipDepth,
                                       int depth,
                                       int charId,
                                       Matrix matrix,
                                       AlphaTransform cxform,
                                       int ratio,
                                       String name,
                                       int clipActionFlags )  
        throws IOException    
    {
        println( "place-object2 move=" + isMove +
                 " id=" + charId +                                          " depth=" + depth +                  " clip=" + clipDepth +
                 " ratio=" + ratio +
                 " name=" + name +                 "  " + matrix + "  " + cxform );
        
        if( clipActionFlags != 0 )
        {
            println( "  clip-actions:" );                        ActionTextWriter acts = new ActionTextWriter( writer );
            acts.indent = "    " + indent ;
            return acts;
        }
        
        return null;
    }
        
    /**
     * SWFTagTypes interface
     */     public void tagRemoveObject( int charId, int depth ) throws IOException
    {        println( "remove-object id=" + charId + " depth=" + depth );
    }
        
    /**
     * SWFTagTypes interface
     */     public void tagRemoveObject2(int depth ) throws IOException
    {        println( "remove-object2 depth=" + depth );
    }

    /**
     * SWFTagTypes interface
     */     public void tagSetBackgroundColor( Color color ) throws IOException
    {
        println( "background-color  " + color );        
    }

    /**
     * SWFTagTypes interface
     */     public void tagFrameLabel( String label ) throws IOException    {
        println( "frame-label " + label );    }
    
	/**
	 * SWFTagTypes interface
	 */     
	public void tagFrameLabel( String label, boolean isAnchor ) throws IOException {    
		println( "frame-label " + label + ( isAnchor ? " (anchor)" : "") );
	}     
    
    /**
     * SWFTagTypes interface
     */     public SWFTagTypes tagDefineSprite( int id ) throws IOException
    {
        println( "sprite id=" + id );
        
        SWFTagDumper dumper = new SWFTagDumper( writer, dumpHex, decompileActions );
        dumper.indent = indent + "    ";
        return dumper;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagProtect( byte[] password ) throws IOException
    {        println( "protect" );
    }    
    /**
     * SWFTagTypes interface
     */     public void tagEnableDebug( byte[] password ) throws IOException
    {
        println( "enable-debug" );            }        
	/**
	 * SWFTagTypes interface
	 */ 
	public void tagEnableDebug2( byte[] password ) throws IOException
	{
		println( "enable-debug-2" );        
	}
        
    /**
     * SWFTagTypes interface
     */     public SWFVectors tagDefineFont( int id, int numGlyphs ) throws IOException
    {        println( "font id=" + id );
        return this;    }

    /**
     * SWFTagTypes interface
     */     public void tagDefineFontInfo( int fontId, String fontName, int flags, int[] codes )
        throws IOException
    {
        println( "font-info id=" + fontId + " name=" + fontName + 
                 " flags=" + Integer.toBinaryString( flags ) + " codes=" + codes.length );    }
	/**
	 * SWFTagTypes interface
	 */ 
	public void tagDefineFontInfo2( int fontId, String fontName, int flags, int[] codes, int languageCode )
		throws IOException
	{
		println( "font-info2 id=" + fontId + " name=" + fontName + 
				 " flags=" + Integer.toBinaryString( flags ) + 
                 " codes=" + codes.length + " language=" + languageCode );
	}
    
    /**
     * SWFTagTypes interface
     */     public SWFVectors tagDefineFont2( int id, int flags, String name, int numGlyphs,
                                      int ascent, int descent, int leading,
                                      int[] codes, int[] advances, Rect[] bounds,
                                      int[] kernCodes1, int[] kernCodes2,
                                      int[] kernAdjustments ) throws IOException
    {
        println( "font2 id=" + id + " flags=" + Integer.toBinaryString( flags ) +
                 " name=" + name + " ascent=" + ascent + " descent=" + descent +                 " leading=" + leading + " has-kerns=" + (kernCodes1 != null));
        
        return this;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineTextField( int fieldId, String fieldName,
                    String initialText, Rect boundary, int flags,
                    AlphaColor textColor, int alignment, int fontId, int fontSize, 
                    int charLimit, int leftMargin, int rightMargin, int indentation,
                    int lineSpacing ) 
        throws IOException
    {
    	if( initialText != null ) {
    		initialText = initialText.replace( '\r', ' ' );
			initialText = initialText.replace( '\n', ' ' );
			initialText = initialText.replace( '\b', ' ' );
     	}
    	
        println( "edit-field id=" + fieldId + " name=" + fieldName +                 " text=" + initialText + " font=" + fontId + " size=" + fontSize +                 " chars=" + charLimit + " left=" + leftMargin +                 " right=" + rightMargin + " indent=" + indentation +                 " spacing=" + lineSpacing + " alignment=" + alignment +
                 " flags=" + Integer.toBinaryString( flags ) +                 " " + textColor );        
    }

    /**
     * SWFTagTypes interface
     */     public SWFText tagDefineText( int id, Rect bounds, Matrix matrix )
        throws IOException
    { 
        println( "text id=" + id + " " + bounds + " " + matrix );
        return this;    }    /**
     * SWFTagTypes interface
     */     public SWFText tagDefineText2( int id, Rect bounds, Matrix matrix ) throws IOException
    { 
        println( "text2 id=" + id + " " + bounds + " " + matrix );
        return this;    }    
    /**
     * SWFTagTypes interface
     */     public SWFActions tagDefineButton( int id, Vector buttonRecords )
        throws IOException
    {
        println( "button id=" + id );
        
        for( Enumeration enum = buttonRecords.elements(); enum.hasMoreElements(); )
        {            ButtonRecord rec = (ButtonRecord)enum.nextElement();            println( "  " + rec );
        }
        println( "  actions:" );                ActionTextWriter acts = new ActionTextWriter( writer );
        acts.indent = "    " + indent ;
        return acts;
    }
    
    /**
     * SWFTagTypes interface
     */     public void tagButtonCXForm( int buttonId, ColorTransform transform ) 
        throws IOException
    {
        println( "button-cxform id=" + buttonId + "  " + transform );    }        
    /**
     * SWFTagTypes interface
     */     public SWFActions tagDefineButton2( int id,                                         boolean trackAsMenu,                                         Vector buttonRecord2s )
        throws IOException
    {        println( "button2 id=" + id + " track-as-menu=" + trackAsMenu );
        
        for( Enumeration enum = buttonRecord2s.elements(); enum.hasMoreElements(); )
        {            ButtonRecord2 rec = (ButtonRecord2)enum.nextElement();            println( "  " + rec );
        }
        println( "  actions:" );                ActionTextWriter acts = new ActionTextWriter( writer );
        acts.indent = "    " + indent ;
        return acts;
            
    }
    /**
     * SWFTagTypes interface
     */     public void tagExport( String[] names, int[] ids ) throws IOException
    {
        println( "export" );
        
        for( int i = 0; i < names.length && i < ids.length; i++ )
        {
            println( "  id=" + ids[i] + " name=" + names[i] );
        }
    }
    
    /**
     * SWFTagTypes interface
     */     public void tagImport( String movieName, String[] names, int[] ids ) 
        throws IOException
    {
        println( "import library-movie=" + movieName );
        
        for( int i = 0; i < names.length && i < ids.length; i++ )
        {
            println( "  id=" + ids[i] + " name=" + names[i] );
        }
    }
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineQuickTimeMovie( int id, String filename ) throws IOException
    {        println( "quicktime-movie id=" + id + " name=" + filename );
    }    
    /**
     * SWFTagTypes interface
     */     public void tagDefineBitsJPEG2( int id, byte[] data ) throws IOException
    {
        println( "jpeg2 id=" + id );

        if( dumpHex )
        {
            Hex.dump( writer, data, 0L, indent + "    ", false );
            println( dashes );
        }
    }        /**
     * SWFTagTypes interface
     */     public void tagDefineBitsJPEG2( int id, InputStream jpegImage ) throws IOException
    {
        println( "jpeg2 id=" + id + " (from input stream)" );            }
        /**
     * SWFTagTypes interface
     */     public SWFShape tagDefineMorphShape( int id, Rect startBounds, Rect endBounds )         throws IOException
    {
        println( "morph-shape id=" + id + " start: " + 
                 startBounds + "  end: " + endBounds );
        return this;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineBitsLossless( int id, int format, int width, int height,                                       Color[] colors, byte[] imageData )        throws IOException
    {        dumpBitsLossless( "bits-lossless", id, format, width, height, colors, imageData );    }
        /**
     * SWFTagTypes interface
     */ 
    public void tagDefineBitsLossless2( int id, int format, int width, int height,                                        Color[] colors, byte[] imageData )        throws IOException
    {
        dumpBitsLossless( "bits-lossless2", id, format, width, height, colors, imageData );    }    
    
    public void dumpBitsLossless( String name, int id, int format, 
                                  int width, int height,                                  Color[] colors, byte[] imageData )        throws IOException
    {
        int size = 0;
        if     ( format == SWFConstants.BITMAP_FORMAT_8_BIT  ) size = 8;
        else if( format == SWFConstants.BITMAP_FORMAT_16_BIT ) size = 16;
        else if( format == SWFConstants.BITMAP_FORMAT_32_BIT ) size = 32;
        
        println( name + " id=" + id + " bits=" + size + 
                 " width=" + width + " height=" + height );
        
        if( dumpHex )
        {
            for( int i = 0; i < colors.length; i++ )
            {
                println( "    " + i + ": " + colors[i] );
            }
            
            Hex.dump( writer, imageData, 0L, indent + "    ", false );
            println( dashes );
        }
    }
    
    /**
     * SWFVectors interface
     * SWFText interface
     */     
    public void done() throws IOException
    {
        println( "    " + dashes );
    }
    
    /**
     * SWFVectors interface
     */     
    public void line( int dx, int dy ) throws IOException
    {
        println( "    line  " + dx + "," + dy );        
    }
    
    /**
     * SWFVectors interface
     */     
    public void curve( int cx, int cy, int dx, int dy ) throws IOException
    {
        println( "    curve " + cx + "," + cy + " - " + dx + "," + dy );                
    }
    
    /**
     * SWFVectors interface
     */     
    public void move( int x, int y ) throws IOException
    {
        println( "    move  " + x + "," + y );        
    }
    
    /**
     * SWFShape interface
     */     
    public void setFillStyle0( int styleIndex ) throws IOException
    {
        println( "    fill0 = " + styleIndex );
    }
    
    /**
     * SWFShape interface
     */     
    public void setFillStyle1( int styleIndex ) throws IOException
    {
        println( "    fill1 = " + styleIndex );
    }    
    
    /**
     * SWFShape interface
     */     
    public void setLineStyle( int styleIndex ) throws IOException
    {
        println( "    line  = " + styleIndex );
    }    

    /**
     * SWFShape interface
     */     
    public void defineFillStyle( Color color ) throws IOException
    {
        println( "    fill " + color );
    }    

    /**
     * SWFShape interface
     */     
    public void defineFillStyle( Matrix matrix, int[] ratios,
                                 Color[] colors, boolean radial )
        throws IOException
    {
        println( "    fill radial=" + radial + "  " + matrix );
        
        for( int i = 0; i < ratios.length && i < colors.length; i++ )
        {
            if( colors[i] == null ) continue;
            println( "         ratio=" + ratios[i] + " " + colors[i] );
        }
    }    

    /**
     * SWFShape interface
     */     
    public void defineFillStyle( int bitmapId, Matrix matrix, boolean clipped )
        throws IOException
    {
        println( "    fill clipped=" + clipped + " image=" + bitmapId + " " + matrix );
    }
    
    /**
     * SWFShape interface
     */     
    public void defineLineStyle( int width, Color color ) throws IOException
    {
        println( "    line-style width=" + width + "  " + color );
    }
    
    /**
     * SWFText interface
     */     
    public void font( int fontId, int textHeight ) throws IOException
    {
        println( "    font id=" + fontId + " size=" + textHeight );
    }
    
    /**
     * SWFText interface
     */     
    public void color( Color color ) throws IOException
    {
        println( "    color " + color );
    }
    
    /**
     * SWFText interface
     */     
    public void setX( int x ) throws IOException
    {
        println( "    x = " + x );
    }

    /**
     * SWFText interface
     */     
    public void setY( int y ) throws IOException
    {
        println( "    y = " + y );
    }
    
    /**
     * SWFText interface
     */     
    public void text( int[] glyphIndices, int[] glyphAdvances ) throws IOException
    {
        StringBuffer buff1 = new StringBuffer();
        StringBuffer buff2 = new StringBuffer();
        
        buff1.append( "(" );
        buff2.append( "(" );
        
        for( int i = 0; i < glyphIndices.length && i < glyphAdvances.length; i++ )
        {
            buff1.append( " " );
            buff2.append( " " );
            
            buff1.append( glyphIndices[i] );
            buff2.append( glyphAdvances[i] );
        }
        
        buff1.append( " )" );
        buff2.append( " )" );

        println( "    text" );
        println( "        glyph indices = " + buff1 );
        println( "        advances      = " + buff2 );
    }
    
    public void flush() throws IOException
    {
        writer.flush();
    }
    
    /**
     * args[0] = name of SWF file to dump to System.out
     * args[1] = if exists then dump-hex is true (dumps binary as hex - otherwise skips)
     * args[2] = if exists then decompiles action codes
     */
    public static void main( String[] args ) throws IOException
    {
        SWFTagDumper dumper = new SWFTagDumper( args.length > 1, args.length > 2 );
        
        FileInputStream in  = new FileInputStream( args[0] );
        SWFTags tagparser = new TagParser( dumper );
        SWFReader reader = new SWFReader( tagparser, in );
        
        try
        {
            reader.readFile();
        }
        finally
        {        
            dumper.flush();
            in.close();
        }        
    }
}
