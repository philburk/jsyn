/*
 * Copyright 2009 Phil Burk, Mobileer Inc
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
 * Narrow impulse oscillator. An impulse is only one sample wide. It is useful for pinging filters
 * or generating an "impulse response".
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class ImpulseOscillator extends UnitOscillator {

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        // Variables have a single value.
        double currentPhase = phase.getValue();

        double inverseNyquist = synthesisEngine.getInverseNyquist();

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phasor to provide phase for impulse generation. */
            double phaseIncrement = frequencies[i] * inverseNyquist;
            currentPhase += phaseIncrement;

            double ampl = amplitudes[i];
            double result = 0.0;
            if (currentPhase >= 1.0) {
                currentPhase -= 2.0;
                result = ampl;
            } else if (currentPhase < -1.0) {
                currentPhase += 2.0;
                result = ampl;
            }
            outputs[i] = result;
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

}
