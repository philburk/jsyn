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

import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.VariableRateMonoReader;

/**
 * Store an envelope as a series of line segments. Each line is described as a duration and a target
 * value. The envelope can be played using a {@link VariableRateMonoReader}. Here is an example that
 * generates an envelope that looks like a traditional ADSR envelope.
 * 
 * <pre>
 * <code>
 * 	// Create an amplitude envelope and fill it with data.
 * 	double[] ampData = {
 * 		0.02, 0.9, // duration,value pair 0, "attack"
 * 		0.10, 0.5, // pair 1, "decay"
 * 		0.50, 0.0  // pair 2, "release"
 * 	};
 * 	SegmentedEnvelope ampEnvelope = new SegmentedEnvelope( ampData );
 * 	
 * 	// Hang at end of decay segment to provide a "sustain" segment.
 * 	ampEnvelope.setSustainBegin( 1 );
 * 	ampEnvelope.setSustainEnd( 1 );
 * 	
 * 	// Play the envelope using queueOn so that it uses the sustain and release information.
 * 	synth.add( ampEnv = new VariableRateMonoReader() );
 * 	ampEnv.dataQueue.queueOn( ampEnvelope );
 * </code>
 * </pre>
 * 
 * As an alternative you could use an {@link EnvelopeDAHDSR}.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @see VariableRateMonoReader
 * @see EnvelopeDAHDSR
 */
public class SegmentedEnvelope extends SequentialDataCommon {
    private double[] buffer;

    public SegmentedEnvelope(int maxFrames) {
        allocate(maxFrames);
    }

    public SegmentedEnvelope(double[] pairs) {
        this(pairs.length / 2);
        write(pairs);
    }

    public void allocate(int maxFrames) {
        buffer = new double[maxFrames * 2];
        this.maxFrames = maxFrames;
        this.numFrames = 0;
    }

    /**
     * Write frames of envelope data. A frame consists of a duration and a value.
     * 
     * @param startFrame Index of frame in envelope to write to.
     * @param data Pairs of duration and value.
     * @param startIndex Index of frame in data[] to read from.
     * @param numToWrite Number of frames (pairs) to write.
     */
    public void write(int startFrame, double[] data, int startIndex, int numToWrite) {
        System.arraycopy(data, startIndex * 2, buffer, startFrame * 2, numToWrite * 2);
        if ((startFrame + numToWrite) > numFrames) {
            numFrames = startFrame + numToWrite;
        }
    }

    public void read(int startFrame, double[] data, int startIndex, int numToRead) {
        System.arraycopy(buffer, startFrame * 2, data, startIndex * 2, numToRead * 2);
    }

    public void write(double[] data) {
        write(0, data, 0, data.length / 2);
    }

    public void read(double[] data) {
        read(0, data, 0, data.length / 2);
    }

    /** Read the value of an envelope, not the duration. */
    @Override
    public double readDouble(int index) {
        return buffer[(index * 2) + 1];
    }

    @Override
    public void writeDouble(int index, double value) {
        buffer[(index * 2) + 1] = value;
        if ((index + 1) > numFrames) {
            numFrames = index + 1;
        }
    }

    @Override
    public double getRateScaler(int index, double synthesisPeriod) {
        double duration = buffer[index * 2];
        if (duration < synthesisPeriod) {
            duration = synthesisPeriod;
        }
        return 1.0 / duration;
    }

    @Override
    public int getChannelsPerFrame() {
        return 1;
    }
}
