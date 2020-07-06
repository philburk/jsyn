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
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.util.PseudoRandom;

/**
 * BrownNoise unit. This unit uses a pseudo-random number generator to produce a noise related to
 * Brownian Motion. A DC blocker is used to prevent runaway drift.
 * 
 * <pre>
 * <code>
 * output = (previous * (1.0 - damping)) + (random * amplitude) 
 * </code>
 * </pre>
 * 
 * The output drifts quite a bit and will generally exceed the range of +/1 amplitude.
 * 
 * @author (C) 1997-2011 Phil Burk, Mobileer Inc
 * @see WhiteNoise
 * @see RedNoise
 * @see PinkNoise
 */
public class BrownNoise extends UnitGenerator implements UnitSource {
    private PseudoRandom randomNum;
    /** Increasing the damping will effectively increase the cutoff 
     * frequency of a high pass filter that is used to block DC bias.
     * Warning: setting this too close to zero can result in very large output values.
     */
    public UnitInputPort damping;
    public UnitInputPort amplitude;
    public UnitOutputPort output;
    private double previous;

    public BrownNoise() {
        randomNum = new PseudoRandom();
        addPort(damping = new UnitInputPort("Damping"));
        damping.setup(0.0001, 0.01, 0.1);
        addPort(amplitude = new UnitInputPort("Amplitude", UnitOscillator.DEFAULT_AMPLITUDE));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        double damper = 1.0 - damping.getValues()[0];

        for (int i = start; i < limit; i++) {
            double r = randomNum.nextRandomDouble() * amplitudes[i];
            outputs[i] = previous = (damper * previous) + r;
        }
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }
}
