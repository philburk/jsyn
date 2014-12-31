/*
 * Copyright 2009 Phil Burk, Mobileer Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsyn.util.soundfile;

import java.io.EOFException;
import java.io.IOException;

import com.jsyn.data.FloatSample;
import com.jsyn.data.SampleMarker;
import com.jsyn.util.SampleLoader;

class WAVEFileParser extends AudioFileParser implements ChunkHandler {
    static final short WAVE_FORMAT_PCM = 1;
    static final short WAVE_FORMAT_IEEE_FLOAT = 3;
    static final short WAVE_FORMAT_EXTENSIBLE = (short) 0xFFFE;

    static final byte[] KSDATAFORMAT_SUBTYPE_IEEE_FLOAT = {
            3, 0, 0, 0, 0, 0, 16, 0, -128, 0, 0, -86, 0, 56, -101, 113
    };
    static final byte[] KSDATAFORMAT_SUBTYPE_PCM = {
            1, 0, 0, 0, 0, 0, 16, 0, -128, 0, 0, -86, 0, 56, -101, 113
    };

    static final int WAVE_ID = ('W' << 24) | ('A' << 16) | ('V' << 8) | 'E';
    static final int FMT_ID = ('f' << 24) | ('m' << 16) | ('t' << 8) | ' ';
    static final int DATA_ID = ('d' << 24) | ('a' << 16) | ('t' << 8) | 'a';
    static final int CUE_ID = ('c' << 24) | ('u' << 16) | ('e' << 8) | ' ';
    static final int FACT_ID = ('f' << 24) | ('a' << 16) | ('c' << 8) | 't';
    static final int SMPL_ID = ('s' << 24) | ('m' << 16) | ('p' << 8) | 'l';
    static final int LTXT_ID = ('l' << 24) | ('t' << 16) | ('x' << 8) | 't';
    static final int LABL_ID = ('l' << 24) | ('a' << 16) | ('b' << 8) | 'l';

    int samplesPerBlock = 0;
    int blockAlign = 0;
    private int numFactSamples = 0;
    private short format;

    WAVEFileParser() {
    }

    @Override
    FloatSample finish() throws IOException {
        if ((byteData == null)) {
            throw new IOException("No data found in audio sample.");
        }
        float[] floatData = new float[numFrames * samplesPerFrame];
        if (bitsPerSample == 16) {
            SampleLoader.decodeLittleI16ToF32(byteData, 0, byteData.length, floatData, 0);
        } else if (bitsPerSample == 24) {
            SampleLoader.decodeLittleI24ToF32(byteData, 0, byteData.length, floatData, 0);
        } else if (bitsPerSample == 32) {
            if (format == WAVE_FORMAT_IEEE_FLOAT) {
                SampleLoader.decodeLittleF32ToF32(byteData, 0, byteData.length, floatData, 0);
            } else if (format == WAVE_FORMAT_PCM) {
                SampleLoader.decodeLittleI32ToF32(byteData, 0, byteData.length, floatData, 0);
            } else {
                throw new IOException("WAV: Unsupported format = " + format);
            }
        } else {
            throw new IOException("WAV: Unsupported bitsPerSample = " + bitsPerSample);
        }

        return makeSample(floatData);
    }

    // typedef struct {
    // long dwIdentifier;
    // long dwPosition;
    // ID fccChunk;
    // long dwChunkStart;
    // long dwBlockStart;
    // long dwSampleOffset;
    // } CuePoint;

    /* Parse various chunks encountered in WAV file. */
    void parseCueChunk(IFFParser parser, int ckSize) throws IOException {
        int numCuePoints = parser.readIntLittle();
        if (IFFParser.debug) {
            System.out.println("WAV: numCuePoints = " + numCuePoints);
        }
        if ((ckSize - 4) != (6 * 4 * numCuePoints))
            throw new EOFException("Cue chunk too short!");
        for (int i = 0; i < numCuePoints; i++) {
            int dwName = parser.readIntLittle(); /* dwName */
            int position = parser.readIntLittle(); // dwPosition
            parser.skip(3 * 4); // fccChunk, dwChunkStart, dwBlockStart
            int sampleOffset = parser.readIntLittle(); // dwPosition

            if (IFFParser.debug) {
                System.out.println("WAV: parseCueChunk: #" + i + ", dwPosition = " + position
                        + ", dwName = " + dwName + ", dwSampleOffset = " + sampleOffset);
            }
            SampleMarker cuePoint = findOrCreateCuePoint(dwName);
            cuePoint.position = position;
        }
    }

    void parseLablChunk(IFFParser parser, int ckSize) throws IOException {
        int dwName = parser.readIntLittle();
        int textLength = (ckSize - 4) - 1; // don't read NUL terminator
        String text = parseString(parser, textLength);
        if (IFFParser.debug) {
            System.out.println("WAV: label id = " + dwName + ", text = " + text);
        }
        SampleMarker cuePoint = findOrCreateCuePoint(dwName);
        cuePoint.name = text;
    }

    void parseLtxtChunk(IFFParser parser, int ckSize) throws IOException {
        int dwName = parser.readIntLittle();
        int dwSampleLength = parser.readIntLittle();
        parser.skip(4 + (4 * 2)); // purpose through codepage
        int textLength = (ckSize - ((4 * 4) + (4 * 2))) - 1; // don't read NUL
                                                             // terminator
        if (textLength > 0) {
            String text = parseString(parser, textLength);
            if (IFFParser.debug) {
                System.out.println("WAV: ltxt id = " + dwName + ", dwSampleLength = "
                        + dwSampleLength + ", text = " + text);
            }
            SampleMarker cuePoint = findOrCreateCuePoint(dwName);
            cuePoint.comment = text;
        }
    }

    void parseFmtChunk(IFFParser parser, int ckSize) throws IOException {
        format = parser.readShortLittle();
        samplesPerFrame = parser.readShortLittle();
        frameRate = parser.readIntLittle();
        parser.readIntLittle(); /* skip dwAvgBytesPerSec */
        blockAlign = parser.readShortLittle();
        bitsPerSample = parser.readShortLittle();

        if (IFFParser.debug) {
            System.out.println("WAV: format = 0x" + Integer.toHexString(format));
            System.out.println("WAV: bitsPerSample = " + bitsPerSample);
            System.out.println("WAV: samplesPerFrame = " + samplesPerFrame);
        }
        bytesPerFrame = blockAlign;
        bytesPerSample = bytesPerFrame / samplesPerFrame;
        samplesPerBlock = (8 * blockAlign) / bitsPerSample;

        if (format == WAVE_FORMAT_EXTENSIBLE) {
            int extraSize = parser.readShortLittle();
            short validBitsPerSample = parser.readShortLittle();
            int channelMask = parser.readIntLittle();
            byte[] guid = new byte[16];
            parser.read(guid);
            if (IFFParser.debug) {
                System.out.println("WAV: extraSize = " + extraSize);
                System.out.println("WAV: validBitsPerSample = " + validBitsPerSample);
                System.out.println("WAV: channelMask = " + channelMask);
                System.out.print("guid = {");
                for (int i = 0; i < guid.length; i++) {
                    System.out.print(guid[i] + ", ");
                }
                System.out.println("}");
            }
            if (matchBytes(guid, KSDATAFORMAT_SUBTYPE_IEEE_FLOAT)) {
                format = WAVE_FORMAT_IEEE_FLOAT;
            } else if (matchBytes(guid, KSDATAFORMAT_SUBTYPE_PCM)) {
                format = WAVE_FORMAT_PCM;
            }
        }
        if ((format != WAVE_FORMAT_PCM) && (format != WAVE_FORMAT_IEEE_FLOAT)) {
            throw new IOException(
                    "Only WAVE_FORMAT_PCM and WAVE_FORMAT_IEEE_FLOAT supported. format = " + format);
        }
        if ((bitsPerSample != 16) && (bitsPerSample != 24) && (bitsPerSample != 32)) {
            throw new IOException(
                    "Only 16 and 24 bit PCM or 32-bit float WAV files supported. width = "
                            + bitsPerSample);
        }
    }

    private boolean matchBytes(byte[] bar1, byte[] bar2) {
        if (bar1.length != bar2.length)
            return false;
        for (int i = 0; i < bar1.length; i++) {
            if (bar1[i] != bar2[i])
                return false;
        }
        return true;
    }

    private int convertByteToFrame(int byteOffset) throws IOException {
        if (blockAlign == 0) {
            throw new IOException("WAV file has bytesPerBlock = zero");
        }
        if (samplesPerFrame == 0) {
            throw new IOException("WAV file has samplesPerFrame = zero");
        }
        int nFrames = (samplesPerBlock * byteOffset) / (samplesPerFrame * blockAlign);
        return nFrames;
    }

    private int calculateNumFrames(int numBytes) throws IOException {
        int nFrames;
        if (numFactSamples > 0) {
            // nFrames = numFactSamples / samplesPerFrame;
            nFrames = numFactSamples; // FIXME which is right
        } else {
            nFrames = convertByteToFrame(numBytes);
        }
        return nFrames;
    }

    // Read fraction in range of 0 to 0xFFFFFFFF and
    // convert to 0.0 to 1.0 range.
    private double readFraction(IFFParser parser) throws IOException {
        // Put L at end or we get -1.
        long maxFraction = 0x0FFFFFFFFL;
        // Get unsigned fraction. Have to fit in long.
        long fraction = (parser.readIntLittle()) & maxFraction;
        return (double) fraction / (double) maxFraction;
    }

    void parseSmplChunk(IFFParser parser, int ckSize) throws IOException {
        parser.readIntLittle(); // Manufacturer
        parser.readIntLittle(); // Product
        parser.readIntLittle(); // Sample Period
        int unityNote = parser.readIntLittle();
        double pitchFraction = readFraction(parser);
        originalPitch = unityNote + pitchFraction;

        parser.readIntLittle(); // SMPTE Format
        parser.readIntLittle(); // SMPTE Offset
        int numLoops = parser.readIntLittle();
        parser.readIntLittle(); // Sampler Data

        int lastCueID = Integer.MAX_VALUE;
        for (int i = 0; i < numLoops; i++) {
            int cueID = parser.readIntLittle();
            parser.readIntLittle(); // type
            int loopStartPosition = parser.readIntLittle();
            // Point to sample one after.
            int loopEndPosition = parser.readIntLittle() + 1;
            // TODO handle fractional loop sizes?
            double endFraction = readFraction(parser);
            parser.readIntLittle(); // playCount

            // Use lowest numbered cue.
            if (cueID < lastCueID) {
                sustainBegin = loopStartPosition;
                sustainEnd = loopEndPosition;
            }
        }
    }

    void parseFactChunk(IFFParser parser, int ckSize) throws IOException {
        numFactSamples = parser.readIntLittle();
    }

    void parseDataChunk(IFFParser parser, int ckSize) throws IOException {
        long numRead;
        dataPosition = parser.getOffset();
        if (ifLoadData) {
            byteData = new byte[ckSize];
            numRead = parser.read(byteData);
        } else {
            numRead = parser.skip(ckSize);
        }
        if (numRead != ckSize) {
            throw new EOFException("WAV data chunk too short! Read " + numRead + " instead of "
                    + ckSize);
        }
        numFrames = calculateNumFrames(ckSize);
    }

    @Override
    public void handleForm(IFFParser parser, int ckID, int ckSize, int type) throws IOException {
        if ((ckID == IFFParser.RIFF_ID) && (type != WAVE_ID))
            throw new IOException("Bad WAV form type = " + IFFParser.IDToString(type));
    }

    /**
     * Called by parse() method to handle chunks in a WAV specific manner.
     * 
     * @param ckID four byte chunk ID such as 'data'
     * @param ckSize size of chunk in bytes
     * @return number of bytes left in chunk
     */
    @Override
    public void handleChunk(IFFParser parser, int ckID, int ckSize) throws IOException {
        switch (ckID) {
            case FMT_ID:
                parseFmtChunk(parser, ckSize);
                break;
            case DATA_ID:
                parseDataChunk(parser, ckSize);
                break;
            case CUE_ID:
                parseCueChunk(parser, ckSize);
                break;
            case FACT_ID:
                parseFactChunk(parser, ckSize);
                break;
            case SMPL_ID:
                parseSmplChunk(parser, ckSize);
                break;
            case LABL_ID:
                parseLablChunk(parser, ckSize);
                break;
            case LTXT_ID:
                parseLtxtChunk(parser, ckSize);
                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.softsynth.javasonics.util.AudioSampleLoader#isLittleEndian()
     */
    boolean isLittleEndian() {
        return true;
    }

}
