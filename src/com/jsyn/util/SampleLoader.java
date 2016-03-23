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

import com.jsyn.data.FloatSample;
import com.jsyn.util.soundfile.CustomSampleLoader;

/**
 * Load a FloatSample from various sources. The default loader uses custom code to load WAV or AIF
 * files. Supported data formats are 16, 24 and 32 bit PCM, and 32-bit float. Compressed formats
 * such as unsigned 8-bit, uLaw, A-Law and MP3 are not support. If you need to load one of those
 * files try setJavaSoundPreferred(true). Or convert it to a supported format using Audacity or Sox
 * or some other sample file tool. Here is an example of loading a sample from a file.
 *
 * <pre>
 * <code>
 *     File sampleFile = new File("guitar.wav");
 *     FloatSample sample = SampleLoader.loadFloatSample( sampleFile );
 * </code>
 * </pre>
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class SampleLoader {
    private static boolean javaSoundPreferred = false;
    private static final String JS_LOADER_NAME = "com.jsyn.util.JavaSoundSampleLoader";

    /**
     * Try to create an implementation of AudioSampleLoader.
     *
     * @return A device supported on this platform.
     */
    private static AudioSampleLoader createLoader() {
        AudioSampleLoader loader = null;
        try {
            if (javaSoundPreferred) {
                loader = (AudioSampleLoader) JavaTools.loadClass(JS_LOADER_NAME).newInstance();
            } else {
                loader = new CustomSampleLoader();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return loader;
    }

    /**
     * Load a FloatSample from a File object.
     */
    public static FloatSample loadFloatSample(File fileIn) throws IOException {
        AudioSampleLoader loader = SampleLoader.createLoader();
        return loader.loadFloatSample(fileIn);
    }

    /**
     * Load a FloatSample from an InputStream. This is handy when loading Resources from a JAR file.
     */
    public static FloatSample loadFloatSample(InputStream inputStream) throws IOException {
        AudioSampleLoader loader = SampleLoader.createLoader();
        return loader.loadFloatSample(inputStream);
    }

    /**
     * Load a FloatSample from a URL.. This is handy when loading Resources from a website.
     */
    public static FloatSample loadFloatSample(URL url) throws IOException {
        AudioSampleLoader loader = SampleLoader.createLoader();
        return loader.loadFloatSample(url);
    }

    public static boolean isJavaSoundPreferred() {
        return javaSoundPreferred;
    }

    /**
     * If set true then the audio file parser from JavaSound will be used. Note that JavaSound
     * cannot load audio files containing floating point data. But it may be able to load some
     * compressed data formats such as uLaw.
     *
     * Note: JavaSound is not supported on Android.
     *
     * @param javaSoundPreferred
     */
    public static void setJavaSoundPreferred(boolean javaSoundPreferred) {
        SampleLoader.javaSoundPreferred = javaSoundPreferred;
    }

    /**
     * Decode 24 bit samples from a BigEndian byte array into a float array. The samples will be
     * normalized into the range -1.0 to +1.0.
     *
     * @param audioBytes raw data from an audio file
     * @param offset first element of byte array
     * @param numBytes number of bytes to process
     * @param data array to be filled with floats
     * @param outputOffset first element of float array to be filled
     */
    public static void decodeBigI24ToF32(byte[] audioBytes, int offset, int numBytes, float[] data,
            int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int hi = ((audioBytes[byteCursor++]) & 0x00FF);
            int mid = ((audioBytes[byteCursor++]) & 0x00FF);
            int lo = ((audioBytes[byteCursor++]) & 0x00FF);
            int value = (hi << 24) | (mid << 16) | (lo << 8);
            data[floatCursor++] = value * (1.0f / Integer.MAX_VALUE);
        }
    }

    public static void decodeBigI16ToF32(byte[] audioBytes, int offset, int numBytes, float[] data,
            int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int hi = ((audioBytes[byteCursor++]) & 0x00FF);
            int lo = ((audioBytes[byteCursor++]) & 0x00FF);
            short value = (short) ((hi << 8) | lo);
            data[floatCursor++] = value * (1.0f / 32768);
        }
    }

    public static void decodeBigF32ToF32(byte[] audioBytes, int offset, int numBytes, float[] data,
            int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int bits = audioBytes[byteCursor++];
            bits = (bits << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            bits = (bits << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            bits = (bits << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            data[floatCursor++] = Float.intBitsToFloat(bits);
        }
    }

    public static void decodeBigI32ToF32(byte[] audioBytes, int offset, int numBytes, float[] data,
            int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int value = audioBytes[byteCursor++]; // MSB
            value = (value << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            value = (value << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            value = (value << 8) | ((audioBytes[byteCursor++]) & 0x00FF);
            data[floatCursor++] = value * (1.0f / Integer.MAX_VALUE);
        }
    }

    public static void decodeLittleF32ToF32(byte[] audioBytes, int offset, int numBytes,
            float[] data, int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int bits = ((audioBytes[byteCursor++]) & 0x00FF); // LSB
            bits += ((audioBytes[byteCursor++]) & 0x00FF) << 8;
            bits += ((audioBytes[byteCursor++]) & 0x00FF) << 16;
            bits += (audioBytes[byteCursor++]) << 24;
            data[floatCursor++] = Float.intBitsToFloat(bits);
        }
    }

    public static void decodeLittleI32ToF32(byte[] audioBytes, int offset, int numBytes,
            float[] data, int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int value = ((audioBytes[byteCursor++]) & 0x00FF);
            value += ((audioBytes[byteCursor++]) & 0x00FF) << 8;
            value += ((audioBytes[byteCursor++]) & 0x00FF) << 16;
            value += (audioBytes[byteCursor++]) << 24;
            data[floatCursor++] = value * (1.0f / Integer.MAX_VALUE);
        }
    }

    public static void decodeLittleI24ToF32(byte[] audioBytes, int offset, int numBytes,
            float[] data, int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int lo = ((audioBytes[byteCursor++]) & 0x00FF);
            int mid = ((audioBytes[byteCursor++]) & 0x00FF);
            int hi = ((audioBytes[byteCursor++]) & 0x00FF);
            int value = (hi << 24) | (mid << 16) | (lo << 8);
            data[floatCursor++] = value * (1.0f / Integer.MAX_VALUE);
        }
    }

    public static void decodeLittleI16ToF32(byte[] audioBytes, int offset, int numBytes,
            float[] data, int outputOffset) {
        int lastByte = offset + numBytes;
        int byteCursor = offset;
        int floatCursor = outputOffset;
        while (byteCursor < lastByte) {
            int lo = ((audioBytes[byteCursor++]) & 0x00FF);
            int hi = ((audioBytes[byteCursor++]) & 0x00FF);
            short value = (short) ((hi << 8) | lo);
            float sample = value * (1.0f / 32768);
            data[floatCursor++] = sample;
        }
    }

}
