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
 * WhiteNoise unit. This unit uses a pseudo-random number generator to produce white noise. The
 * energy in a white noise signal is distributed evenly across the spectrum. A new random number is
 * generated every frame.
 * 
 * @author (C) 1997-2011 Phil Burk, Mobileer Inc
 * @see RedNoise
 */
public class WhiteNoise extends UnitGenerator implements UnitSource {
    private PseudoRandom randomNum;
    public UnitInputPort amplitude;
    public UnitOutputPort output;

    public WhiteNoise() {
        randomNum = new PseudoRandom();
        addPort(amplitude = new UnitInputPort("Amplitude", UnitOscillator.DEFAULT_AMPLITUDE));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            outputs[i] = randomNum.nextRandomDouble() * amplitudes[i];
        }
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }
}
