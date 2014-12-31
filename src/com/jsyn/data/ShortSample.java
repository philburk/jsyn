/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.data;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.FixedRateMonoReader;
import com.jsyn.unitgen.FixedRateStereoReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.jsyn.util.SampleLoader;

/**
 * Store multi-channel short audio data in an interleaved buffer.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @see SampleLoader
 * @see FixedRateMonoReader
 * @see FixedRateStereoReader
 * @see VariableRateMonoReader
 * @see VariableRateStereoReader
 */
public class ShortSample extends AudioSample {
    private short[] buffer;

    public ShortSample() {
    }

    public ShortSample(int numFrames, int channelsPerFrame) {
        allocate(numFrames, channelsPerFrame);
    }

    /** Constructor for mono samples with data. */
    public ShortSample(short[] data) {
        this(data.length, 1);
        write(data);
    }

    /** Constructor for multi-channel samples with data. */
    public ShortSample(short[] data, int channelsPerFrame) {
        this(data.length / channelsPerFrame, channelsPerFrame);
        write(data);
    }

    @Override
    public void allocate(int numFrames, int channelsPerFrame) {
        buffer = new short[numFrames * channelsPerFrame];
        this.numFrames = numFrames;
        this.channelsPerFrame = channelsPerFrame;
    }

    /**
     * Note that in a stereo sample, a frame has two values.
     * 
     * @param startFrame index of frame in the sample
     * @param data data to be written
     * @param startIndex index of first value in array
     * @param numFrames
     */
    public void write(int startFrame, short[] data, int startIndex, int numFrames) {
        int numSamplesToWrite = numFrames * channelsPerFrame;
        int firstSampleIndexToWrite = startFrame * channelsPerFrame;
        System.arraycopy(data, startIndex, buffer, firstSampleIndexToWrite, numSamplesToWrite);
    }

    /**
     * Note that in a stereo sample, a frame has two values.
     * 
     * @param startFrame index of frame in the sample
     * @param data array to receive the data from the sample
     * @param startIndex index of first location in array to start filling
     * @param numFrames
     */
    public void read(int startFrame, short[] data, int startIndex, int numFrames) {
        int numSamplesToRead = numFrames * channelsPerFrame;
        int firstSampleIndexToRead = startFrame * channelsPerFrame;
        System.arraycopy(buffer, firstSampleIndexToRead, data, startIndex, numSamplesToRead);
    }

    public void write(short[] data) {
        write(0, data, 0, data.length);
    }

    public void read(short[] data) {
        read(0, data, 0, data.length);
    }

    public short readShort(int index) {
        return buffer[index];
    }

    public void writeShort(int index, short value) {
        buffer[index] = value;
    }

    /** Read a sample converted to a double in the range -1.0 to almost 1.0. */
    @Override
    public double readDouble(int index) {
        return SynthesisEngine.convertShortToDouble(buffer[index]);
    }

    /**
     * Write a double that will be clipped to the range -1.0 to almost 1.0 and converted to a short.
     */
    @Override
    public void writeDouble(int index, double value) {
        buffer[index] = SynthesisEngine.convertDoubleToShort(value);
    }

}
