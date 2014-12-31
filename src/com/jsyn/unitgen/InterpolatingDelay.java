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

package com.jsyn.unitgen;

import com.jsyn.ports.UnitInputPort;

/**
 * InterpolatingDelay
 * <P>
 * InterpolatingDelay provides a variable time delay with an input and output. The internal data
 * format is 32-bit floating point. The amount of delay can be varied from 0.0 to a time in seconds
 * corresponding to the numFrames allocated. The fractional delay values are calculated by linearly
 * interpolating between adjacent values in the delay line.
 * <P>
 * This unit can be used to implement time varying delay effects such as a flanger or a chorus. It
 * can also be used to implement physical models of acoustic instruments, or other tunable delay
 * based resonant systems.
 * <P>
 * 
 * @author (C) 1997-2011 Phil Burk, Mobileer Inc
 * @see Delay
 */

public class InterpolatingDelay extends UnitFilter {
    /**
     * Delay time in seconds. This value will converted to frames and clipped between zero and the
     * numFrames value passed to allocate(). The minimum and default delay time is 0.0.
     */
    public UnitInputPort delay;

    private float[] buffer;
    private int cursor;
    private int numFrames;

    public InterpolatingDelay() {
        addPort(delay = new UnitInputPort("Delay"));
    }

    /**
     * Allocate memory for the delay buffer. For a 2 second delay at 44100 Hz sample rate you will
     * need at least 88200 samples.
     * 
     * @param numFrames size of the float array to hold the delayed samples
     */
    public void allocate(int numFrames) {
        this.numFrames = numFrames;
        // Allocate extra frame for guard point to speed up interpolation.
        buffer = new float[numFrames + 1];
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double[] delays = delay.getValues();

        for (int i = start; i < limit; i++) {
            // This should be at the beginning of the loop
            // because the guard point should == buffer[0].
            if (cursor == numFrames) {
                // Write guard point! Must allocate one extra sample.
                buffer[numFrames] = (float) inputs[i];
                cursor = 0;
            }

            buffer[cursor] = (float) inputs[i];

            /* Convert delay time to a clipped frame offset. */
            double delayFrames = delays[i] * getFrameRate();

            // Clip to zero delay.
            if (delayFrames <= 0.0) {
                outputs[i] = buffer[cursor];
            } else {
                // Clip to maximum delay.
                if (delayFrames >= numFrames) {
                    delayFrames = numFrames - 1;
                }

                // Calculate fractional index into delay buffer.
                double readIndex = cursor - delayFrames;
                if (readIndex < 0.0) {
                    readIndex += numFrames;
                }
                // setup for interpolation.
                // We know readIndex is > 0 so we do not need to call floor().
                int iReadIndex = (int) readIndex;
                double frac = readIndex - iReadIndex;

                // Get adjacent values relying on guard point to prevent overflow.
                double val0 = buffer[iReadIndex];
                double val1 = buffer[iReadIndex + 1];

                // Interpolate new value.
                outputs[i] = val0 + (frac * (val1 - val0));
            }

            cursor += 1;
        }

    }

}
