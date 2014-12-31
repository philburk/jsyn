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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.jsyn.data.FloatSample;
import com.jsyn.util.AudioSampleLoader;

public class CustomSampleLoader implements AudioSampleLoader {

    @Override
    public FloatSample loadFloatSample(File fileIn) throws IOException {
        FileInputStream fileStream = new FileInputStream(fileIn);
        BufferedInputStream inputStream = new BufferedInputStream(fileStream);
        return loadFloatSample(inputStream);
    }

    @Override
    public FloatSample loadFloatSample(URL url) throws IOException {
        InputStream rawStream = url.openStream();
        BufferedInputStream inputStream = new BufferedInputStream(rawStream);
        return loadFloatSample(inputStream);
    }

    @Override
    public FloatSample loadFloatSample(InputStream inputStream) throws IOException {
        AudioFileParser fileParser;
        IFFParser parser = new IFFParser(inputStream);
        parser.readHead();
        if (parser.isRIFF()) {
            fileParser = new WAVEFileParser();
        } else if (parser.isIFF()) {
            fileParser = new AIFFFileParser();
        } else {
            throw new IOException("Unsupported audio file type.");
        }
        return fileParser.load(parser);
    }

}
