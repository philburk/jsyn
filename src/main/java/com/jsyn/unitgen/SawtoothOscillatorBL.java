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

import com.jsyn.engine.MultiTable;

/**
 * Sawtooth oscillator that uses multiple wave tables for band limiting. This requires more CPU than
 * a plain SawtoothOscillator but has less aliasing at high frequencies.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class SawtoothOscillatorBL extends UnitOscillator {
    @Override
    public void generate(int start, int limit) {
        MultiTable multiTable = MultiTable.getInstance();

        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        // Variables have a single value.
        double currentPhase = phase.getValue();

        double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[0]);
        double positivePhaseIncrement = Math.abs(phaseIncrement);
        // This is very expensive so we moved it outside the loop.
        // Try to optimize it with a table lookup.
        double flevel = multiTable.convertPhaseIncrementToLevel(positivePhaseIncrement);

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phasor to provide phase for sine generation. */
            phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);
            positivePhaseIncrement = Math.abs(phaseIncrement);

            double val = generateBL(multiTable, currentPhase, positivePhaseIncrement, flevel, i);

            outputs[i] = val * amplitudes[i];
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

    protected double generateBL(MultiTable multiTable, double currentPhase,
            double positivePhaseIncrement, double flevel, int i) {
        /* Calculate table level then use it for lookup. */
        return multiTable.calculateSawtooth(currentPhase, positivePhaseIncrement, flevel);
    }

}
