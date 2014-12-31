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
import com.jsyn.unitgen.FixedRateMonoWriter;
import com.jsyn.unitgen.FixedRateStereoReader;
import com.jsyn.unitgen.FixedRateStereoWriter;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;

/**
 * Interface for objects that can be read and/or written by index. The index is not stored
 * internally so they can be shared by multiple readers.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @see FixedRateMonoReader
 * @see FixedRateStereoReader
 * @see FixedRateMonoWriter
 * @see FixedRateStereoWriter
 * @see VariableRateMonoReader
 * @see VariableRateStereoReader
 */
public interface SequentialData {
    /**
     * Write a value at the given index.
     * 
     * @param index sample index is ((frameIndex * channelsPerFrame) + channelIndex)
     * @param value the value to be written
     */
    void writeDouble(int index, double value);

    /**
     * Read a value from the sample independently from the internal storage format.
     * 
     * @param index sample index is ((frameIndex * channelsPerFrame) + channelIndex)
     */

    double readDouble(int index);

    /***
     * @return Beginning of sustain loop or -1 if no loop.
     */
    public int getSustainBegin();

    /**
     * SustainEnd value is the frame index of the frame just past the end of the loop. The number of
     * frames included in the loop is (SustainEnd - SustainBegin).
     * 
     * @return End of sustain loop or -1 if no loop.
     */
    public int getSustainEnd();

    /***
     * @return Beginning of release loop or -1 if no loop.
     */
    public int getReleaseBegin();

    /***
     * @return End of release loop or -1 if no loop.
     */
    public int getReleaseEnd();

    /**
     * Get rate to play the data. In an envelope this correspond to the inverse of the frame
     * duration and would vary frame to frame. For an audio sample it is 1.0.
     * 
     * @param index
     * @param synthesisRate
     * @return rate to scale the playback speed.
     */
    double getRateScaler(int index, double synthesisRate);

    /**
     * @return For a stereo sample, return 2.
     */
    int getChannelsPerFrame();

    /**
     * @return The number of valid frames that can be read.
     */
    int getNumFrames();
}
