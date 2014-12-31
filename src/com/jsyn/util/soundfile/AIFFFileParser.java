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

public class AIFFFileParser extends AudioFileParser {
    private static final String SUPPORTED_FORMATS = "Only 16 and 24 bit PCM or 32-bit float AIF files supported.";
    static final int AIFF_ID = ('A' << 24) | ('I' << 16) | ('F' << 8) | 'F';
    static final int AIFC_ID = ('A' << 24) | ('I' << 16) | ('F' << 8) | 'C';
    static final int COMM_ID = ('C' << 24) | ('O' << 16) | ('M' << 8) | 'M';
    static final int SSND_ID = ('S' << 24) | ('S' << 16) | ('N' << 8) | 'D';
    static final int MARK_ID = ('M' << 24) | ('A' << 16) | ('R' << 8) | 'K';
    static final int INST_ID = ('I' << 24) | ('N' << 16) | ('S' << 8) | 'T';
    static final int NONE_ID = ('N' << 24) | ('O' << 16) | ('N' << 8) | 'E';
    static final int FL32_ID = ('F' << 24) | ('L' << 16) | ('3' << 8) | '2';
    static final int FL32_ID_LC = ('f' << 24) | ('l' << 16) | ('3' << 8) | '2';

    int sustainBeginID = -1;
    int sustainEndID = -1;
    int releaseBeginID = -1;
    int releaseEndID = -1;
    boolean typeFloat = false;

    @Override
    FloatSample finish() throws IOException {
        setLoops();

        if ((byteData == null)) {
            throw new IOException("No data found in audio sample.");
        }
        float[] floatData = new float[numFrames * samplesPerFrame];
        if (bitsPerSample == 16) {
            SampleLoader.decodeBigI16ToF32(byteData, 0, byteData.length, floatData, 0);
        } else if (bitsPerSample == 24) {
            SampleLoader.decodeBigI24ToF32(byteData, 0, byteData.length, floatData, 0);
        } else if (bitsPerSample == 32) {
            if (typeFloat) {
                SampleLoader.decodeBigF32ToF32(byteData, 0, byteData.length, floatData, 0);
            } else {
                SampleLoader.decodeBigI32ToF32(byteData, 0, byteData.length, floatData, 0);
            }
        } else {
            throw new IOException(SUPPORTED_FORMATS + " size = " + bitsPerSample);
        }

        return makeSample(floatData);
    }

    double read80BitFloat() throws IOException {
        /*
         * This is not a full decoding of the 80 bit number but it should suffice for the range we
         * expect.
         */
        byte[] bytes = new byte[10];
        parser.read(bytes);
        int exp = ((bytes[0] & 0x3F) << 8) | (bytes[1] & 0xFF);
        int mant = ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 8) | (bytes[4] & 0xFF);
        // System.out.println( "exp = " + exp + ", mant = " + mant );
        return mant / (double) (1 << (22 - exp));
    }

    void parseCOMMChunk(IFFParser parser, int ckSize) throws IOException {
        samplesPerFrame = parser.readShortBig();
        numFrames = parser.readIntBig();
        bitsPerSample = parser.readShortBig();
        frameRate = read80BitFloat();
        if (ckSize > 18) {
            int format = parser.readIntBig();
            // Validate data format.
            if ((format == FL32_ID) || (format == FL32_ID_LC)) {
                typeFloat = true;
            } else if (format == NONE_ID) {
                typeFloat = false;
            } else {
                throw new IOException(SUPPORTED_FORMATS + " format " + IFFParser.IDToString(format));
            }
        }

        bytesPerSample = (bitsPerSample + 7) / 8;
        bytesPerFrame = bytesPerSample * samplesPerFrame;
    }

    /* parse tuning and multi-sample info */
    @SuppressWarnings("unused")
    void parseINSTChunk(IFFParser parser, int ckSize) throws IOException {
        int baseNote = parser.readByte();
        int detune = parser.readByte();
        originalPitch = baseNote + (0.01 * detune);

        int lowNote = parser.readByte();
        int highNote = parser.readByte();

        parser.skip(2); /* lo,hi velocity */
        int gain = parser.readShortBig();

        int playMode = parser.readShortBig(); /* sustain */
        sustainBeginID = parser.readShortBig();
        sustainEndID = parser.readShortBig();

        playMode = parser.readShortBig(); /* release */
        releaseBeginID = parser.readShortBig();
        releaseEndID = parser.readShortBig();
    }

    private void setLoops() {
        SampleMarker cuePoint = cueMap.get(sustainBeginID);
        if (cuePoint != null) {
            sustainBegin = cuePoint.position;
        }
        cuePoint = cueMap.get(sustainEndID);
        if (cuePoint != null) {
            sustainEnd = cuePoint.position;
        }
    }

    void parseSSNDChunk(IFFParser parser, int ckSize) throws IOException {
        long numRead;
        // System.out.println("parseSSNDChunk()");
        int offset = parser.readIntBig();
        parser.readIntBig(); /* blocksize */
        parser.skip(offset);
        dataPosition = parser.getOffset();
        int numBytes = ckSize - 8 - offset;
        if (ifLoadData) {
            byteData = new byte[numBytes];
            numRead = parser.read(byteData);
        } else {
            numRead = parser.skip(numBytes);
        }
        if (numRead != numBytes)
            throw new EOFException("AIFF data chunk too short!");
    }

    void parseMARKChunk(IFFParser parser, int ckSize) throws IOException {
        long startOffset = parser.getOffset();
        int numCuePoints = parser.readShortBig();
        // System.out.println( "parseCueChunk: numCuePoints = " + numCuePoints
        // );
        for (int i = 0; i < numCuePoints; i++) {
            // Some AIF files have a bogus numCuePoints so check to see if we
            // are at end.
            long numInMark = parser.getOffset() - startOffset;
            if (numInMark >= ckSize) {
                System.out.println("Reached end of MARK chunk with bogus numCuePoints = "
                        + numCuePoints);
                break;
            }

            int uniqueID = parser.readShortBig();
            int position = parser.readIntBig();
            int len = parser.read();
            String markerName = parseString(parser, len);
            if ((len & 1) == 0) {
                parser.skip(1); /* skip pad byte */
            }

            SampleMarker cuePoint = findOrCreateCuePoint(uniqueID);
            cuePoint.position = position;
            cuePoint.name = markerName;

            if (IFFParser.debug) {
                System.out.println("AIFF Marker at " + position + ", " + markerName);
            }
        }
    }

    /**
     * Called by parse() method to handle FORM chunks in an AIFF specific manner.
     * 
     * @param ckID four byte chunk ID such as 'data'
     * @param ckSize size of chunk in bytes
     * @exception IOException If parsing fails, or IO error occurs.
     */
    @Override
    public void handleForm(IFFParser parser, int ckID, int ckSize, int type) throws IOException {
        if ((ckID == IFFParser.FORM_ID) && (type != AIFF_ID) && (type != AIFC_ID))
            throw new IOException("Bad AIFF form type = " + IFFParser.IDToString(type));
    }

    /**
     * Called by parse() method to handle chunks in an AIFF specific manner.
     * 
     * @param ckID four byte chunk ID such as 'data'
     * @param ckSize size of chunk in bytes
     * @exception IOException If parsing fails, or IO error occurs.
     */
    @Override
    public void handleChunk(IFFParser parser, int ckID, int ckSize) throws IOException {
        switch (ckID) {
            case COMM_ID:
                parseCOMMChunk(parser, ckSize);
                break;
            case SSND_ID:
                parseSSNDChunk(parser, ckSize);
                break;
            case MARK_ID:
                parseMARKChunk(parser, ckSize);
                break;
            case INST_ID:
                parseINSTChunk(parser, ckSize);
                break;
            default:
                break;
        }
    }

}
