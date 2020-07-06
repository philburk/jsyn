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
 * Simple square wave oscillator.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class SquareOscillator extends UnitOscillator {

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        // Variables have a single value.
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phasor to provide phase for square generation. */
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);

            double ampl = amplitudes[i];
            // Either full negative or positive amplitude.
            outputs[i] = (currentPhase < 0.0) ? -ampl : ampl;
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

}
