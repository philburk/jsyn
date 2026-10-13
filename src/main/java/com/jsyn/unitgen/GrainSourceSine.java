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

package com.jsyn.unitgen;

/**
 * A simple sine wave generator for a Grain. This uses the same fast Taylor expansion that the
 * SineOscillator uses.
 * <p>
 * A rate of 1.0 plays concert A, 440 Hz, so a rate of Hz / 440 plays a given frequency. The pitch
 * depends on the frame rate, so call setFrameRate() before setRate().
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class GrainSourceSine extends GrainCommon implements GrainSource {
    /** Frequency in Hz played at a rate of 1.0. */
    public static final double CONCERT_A = 440.0;

    private static final double DEFAULT_FRAME_RATE = 44100.0;

    protected double phase;
    protected double phaseIncrement;

    public GrainSourceSine() {
        setRate(1.0);
    }

    public void setPhaseIncrement(double phaseIncrement) {
        this.phaseIncrement = phaseIncrement;
    }

    @Override
    public double next() {
        phase += phaseIncrement;
        if (phase > 1.0) {
            phase -= 2.0;
        }
        return SineOscillator.fastSin(phase);
    }

    @Override
    public void setRate(double rate) {
        // A new source has no frame rate yet, so assume the default rather than divide by zero.
        double currentFrameRate = (frameRate > 0.0) ? frameRate : DEFAULT_FRAME_RATE;
        // The phase runs from -1.0 to +1.0 over one cycle, so it advances
        // 2 * frequency / frameRate per frame.
        setPhaseIncrement(rate * 2.0 * CONCERT_A / currentFrameRate);
    }

}
