/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

import java.io.IOException;

/**
 * Handle IFF Chunks as they are parsed from an IFF or RIFF file.
 * 
 * @see IFFParser
 * @see AudioSampleAIFF
 * @author (C) 1997 Phil Burk, SoftSynth.com
 */
interface ChunkHandler {
    /**
     * The parser will call this when it encounters a FORM or LIST chunk that contains other chunks.
     * This handler can either read the form's chunks, or let the parser find them and call
     * handleChunk().
     * 
     * @param ID a 4 byte identifier such as FORM_ID that identifies the IFF chunk type.
     * @param numBytes number of bytes contained in the FORM, not counting the FORM type.
     * @param type a 4 byte identifier such as AIFF_ID that identifies the FORM type.
     */
    public void handleForm(IFFParser parser, int ID, int numBytes, int type) throws IOException;

    /**
     * The parser will call this when it encounters a chunk that is not a FORM or LIST. This handler
     * can either read the chunk's, or ignore it. The parser will skip over any unread data. Do NOT
     * read past the end of the chunk!
     * 
     * @param ID a 4 byte identifier such as SSND_ID that identifies the IFF chunk type.
     * @param numBytes number of bytes contained in the chunk, not counting the ID and size field.
     */
    public void handleChunk(IFFParser parser, int ID, int numBytes) throws IOException;
}
