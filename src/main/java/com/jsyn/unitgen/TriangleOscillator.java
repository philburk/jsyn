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
 * Simple triangle wave oscillator.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class TriangleOscillator extends UnitOscillator {
    int frame;

    public TriangleOscillator() {
        super();
        phase.setValue(-0.5);
    }

    @Override
    public void generate(int start, int limit) {

        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        // Variables have a single value.
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phasor to provide phase for triangle generation. */
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);

            /* Map phase to triangle waveform. */
            /* 0 - 0.999 => 0.5-p => +0.5 - -0.5 */
            /* -1.0 - 0.0 => 0.5+p => -0.5 - +0.5 */
            double triangle = (currentPhase >= 0.0) ? (0.5 - currentPhase) : (0.5 + currentPhase);

            outputs[i] = triangle * 2.0 * amplitudes[i];
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

}
