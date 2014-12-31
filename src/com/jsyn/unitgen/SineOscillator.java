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
 * Sine oscillator generates a frequency controlled sine wave. It is implemented using a fast Taylor
 * expansion.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class SineOscillator extends UnitOscillator {
    public SineOscillator() {
    }

    public SineOscillator(double freq) {
        frequency.set(freq);
    }

    public SineOscillator(double freq, double amp) {
        frequency.set(freq);
        amplitude.set(amp);
    }

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phasor to provide phase for sine generation. */
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);
            if (true) {
                double value = fastSin(currentPhase);
                outputs[i] = value * amplitudes[i];
            } else {
                // Slower but more accurate implementation.
                outputs[i] = Math.sin(currentPhase * Math.PI) * amplitudes[i];
            }
        }

        phase.setValue(currentPhase);
    }

    /**
     * Calculate sine using Taylor expansion. Do not use values outside the range.
     * 
     * @param currentPhase in the range of -1.0 to +1.0 for one cycle
     */
    public static double fastSin(double currentPhase) {
        // Factorial constants so code is easier to read.
        final double IF3 = 1.0 / (2 * 3);
        final double IF5 = IF3 / (4 * 5);
        final double IF7 = IF5 / (6 * 7);
        final double IF9 = IF7 / (8 * 9);
        final double IF11 = IF9 / (10 * 11);

        /* Wrap phase back into region where results are more accurate. */
        double yp = (currentPhase > 0.5) ? 1.0 - currentPhase : ((currentPhase < (-0.5)) ? (-1.0)
                - currentPhase : currentPhase);

        double x = yp * Math.PI;
        double x2 = (x * x);
        /* Taylor expansion out to x**11/11! factored into multiply-adds */
        double fastsin = x
                * (x2 * (x2 * (x2 * (x2 * ((x2 * (-IF11)) + IF9) - IF7) + IF5) - IF3) + 1);
        return fastsin;
    }
}
