/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

package com.jsyn.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.jsyn.data.FloatSample;

/**
 * Internal class for loading audio samples. Use SampleLoader instead.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
class JavaSoundSampleLoader implements AudioSampleLoader {
    /**
     * Load a FloatSample from a File object.
     */
    @Override
    public FloatSample loadFloatSample(File fileIn) throws IOException {
        try {
            return loadFloatSample(AudioSystem.getAudioInputStream(fileIn));
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

    /**
     * Load a FloatSample from an InputStream. This is handy when loading Resources from a JAR file.
     */
    @Override
    public FloatSample loadFloatSample(InputStream inputStream) throws IOException {
        try {
            return loadFloatSample(AudioSystem.getAudioInputStream(inputStream));
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

    /**
     * Load a FloatSample from a URL.. This is handy when loading Resources from a website.
     */
    @Override
    public FloatSample loadFloatSample(URL url) throws IOException {
        try {
            return loadFloatSample(AudioSystem.getAudioInputStream(url));
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

    private FloatSample loadFloatSample(javax.sound.sampled.AudioInputStream audioInputStream)
            throws IOException, UnsupportedAudioFileException {
        float[] floatData = null;
        FloatSample sample = null;
        int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
        }
        AudioFormat format = audioInputStream.getFormat();
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            floatData = loadSignedPCM(audioInputStream);
        }
        sample = new FloatSample(floatData, format.getChannels());
        sample.setFrameRate(format.getFrameRate());
        return sample;
    }

    private float[] loadSignedPCM(AudioInputStream audioInputStream) throws IOException,
            UnsupportedAudioFileException {
        int totalSamplesRead = 0;
        AudioFormat format = audioInputStream.getFormat();
        int numFrames = (int) audioInputStream.getFrameLength();
        int numSamples = format.getChannels() * numFrames;
        float[] data = new float[numSamples];
        final int bytesPerFrame = format.getFrameSize();
        // Set an arbitrary buffer size of 1024 frames.
        int numBytes = 1024 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];
        int numBytesRead = 0;
        int numFramesRead = 0;
        // Try to read numBytes bytes from the file.
        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
            int bytesRemainder = numBytesRead % bytesPerFrame;
            if (bytesRemainder != 0) {
                // TODO Read until you get enough data.
                throw new IOException("Read partial block of sample data!");
            }

            if (audioInputStream.getFormat().getSampleSizeInBits() == 16) {
                if (format.isBigEndian()) {
                    SampleLoader.decodeBigI16ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                } else {
                    SampleLoader.decodeLittleI16ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                }
            } else if (audioInputStream.getFormat().getSampleSizeInBits() == 24) {
                if (format.isBigEndian()) {
                    SampleLoader.decodeBigI24ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                } else {
                    SampleLoader.decodeLittleI24ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                }
            } else if (audioInputStream.getFormat().getSampleSizeInBits() == 32) {
                if (format.isBigEndian()) {
                    SampleLoader.decodeBigI32ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                } else {
                    SampleLoader.decodeLittleI32ToF32(audioBytes, 0, numBytesRead, data,
                            totalSamplesRead);
                }
            } else {
                throw new UnsupportedAudioFileException(
                        "Only 16, 24 or 32 bit PCM samples supported.");
            }

            // Calculate the number of frames actually read.
            numFramesRead = numBytesRead / bytesPerFrame;
            totalSamplesRead += numFramesRead * format.getChannels();
        }
        return data;
    }

}
