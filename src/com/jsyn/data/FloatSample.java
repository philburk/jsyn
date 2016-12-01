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

import com.jsyn.unitgen.FixedRateMonoReader;
import com.jsyn.unitgen.FixedRateStereoReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.jsyn.util.SampleLoader;

/**
 * Store multi-channel floating point audio data in an interleaved buffer. The values are stored as
 * 32-bit floats. You can play samples using one of the readers, for example VariableRateMonoReader.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @see SampleLoader
 * @see FixedRateMonoReader
 * @see FixedRateStereoReader
 * @see VariableRateMonoReader
 * @see VariableRateStereoReader
 */
public class FloatSample extends AudioSample implements Function {
    private float[] buffer;

    public FloatSample() {
    }

    /** Constructor for mono samples. */
    public FloatSample(int numFrames) {
        this(numFrames, 1);
    }

    /** Constructor for mono samples with data. */
    public FloatSample(float[] data) {
        this(data.length, 1);
        write(data);
    }

    /** Constructor for multi-channel samples with data. */
    public FloatSample(float[] data, int channelsPerFrame) {
        this(data.length / channelsPerFrame, channelsPerFrame);
        write(data);
    }

    /**
     * Create an silent sample with enough memory to hold the audio data. The number of sample
     * numbers in the array will be numFrames*channelsPerFrame.
     *
     * @param numFrames number of sample groups. A stereo frame contains 2 samples.
     * @param channelsPerFrame 1 for mono, 2 for stereo
     */
    public FloatSample(int numFrames, int channelsPerFrame) {
        allocate(numFrames, channelsPerFrame);
    }

    /**
     * Allocate memory to hold the audio data. The number of sample numbers in the array will be
     * numFrames*channelsPerFrame.
     *
     * @param numFrames number of sample groups. A stereo frame contains 2 samples.
     * @param channelsPerFrame 1 for mono, 2 for stereo
     */
    @Override
    public void allocate(int numFrames, int channelsPerFrame) {
        buffer = new float[numFrames * channelsPerFrame];
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
    public void write(int startFrame, float[] data, int startIndex, int numFrames) {
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
    public void read(int startFrame, float[] data, int startIndex, int numFrames) {
        int numSamplesToRead = numFrames * channelsPerFrame;
        int firstSampleIndexToRead = startFrame * channelsPerFrame;
        System.arraycopy(buffer, firstSampleIndexToRead, data, startIndex, numSamplesToRead);
    }

    /**
     * Write the entire array to the sample. The sample data must have already been allocated with
     * enough room to contain the data.
     *
     * @param data
     */
    public void write(float[] data) {
        write(0, data, 0, data.length / getChannelsPerFrame());
    }

    public void read(float[] data) {
        read(0, data, 0, data.length / getChannelsPerFrame());
    }

    @Override
    public double readDouble(int index) {
        return buffer[index];
    }

    @Override
    public void writeDouble(int index, double value) {
        buffer[index] = (float) value;
    }

    /*
     * @param fractionalIndex must be >=0 and < (size-1)
     */
    public double interpolate(double fractionalIndex) {
        int index = (int) fractionalIndex;
        float phase = (float) (fractionalIndex - index);
        float source = buffer[index];
        float target = buffer[index + 1];
        return ((target - source) * phase) + source;
    }

    @Override
    public double evaluate(double input) {
        // Input ranges from -1 to +1
        // Map it to range of sample with guard point.
        double normalizedInput = (input + 1.0) * 0.5;
        // Clip so it does not go out of range of the sample.
        if (normalizedInput < 0.0) normalizedInput = 0.0;
        else if (normalizedInput > 1.0) normalizedInput = 1.0;
        double fractionalIndex = (getNumFrames() - 1.01) * normalizedInput;
        return interpolate(fractionalIndex);
    }
}
