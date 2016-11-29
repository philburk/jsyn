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
 * Sawtooth DPW oscillator (a sawtooth with reduced aliasing).
 * Based on a paper by Antti Huovilainen and Vesa Valimaki:
 * http://www.scribd.com/doc/33863143/New-Approaches-to-Digital-Subtractive-Synthesis
 *
 * @author Phil Burk and Lisa Tolentino (C) 2009 Mobileer Inc
 */
public class SawtoothOscillatorDPW extends UnitOscillator {
    // At a very low frequency, switch from DPW to raw sawtooth.
    private static final double VERY_LOW_FREQUENCY = 2.0 * 0.1 / 44100.0;
    private double z1;
    private double z2;

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        // Variables have a single value.
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            /* Generate raw sawtooth phaser. */
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);

            /* Square the raw sawtooth. */
            double squared = currentPhase * currentPhase;
            // Differentiate using a delayed value.
            double diffed = squared - z2;
            z2 = z1;
            z1 = squared;

            /* Calculate scaling based on phaseIncrement */
            double pinc = phaseIncrement;
            // Absolute value.
            if (pinc < 0.0) {
                pinc = 0.0 - pinc;
            }

            double dpw;
            // If the frequency is very low then just use the raw sawtooth.
            // This avoids divide by zero problems and scaling problems.
            if (pinc < VERY_LOW_FREQUENCY) {
                dpw = currentPhase;
            } else {
                dpw = diffed * 0.25 / pinc;
            }

            outputs[i] = amplitudes[i] * dpw;
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

}
