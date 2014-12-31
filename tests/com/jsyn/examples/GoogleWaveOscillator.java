/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.examples;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Custom unit generator to create the waveform shown on the Google home page on 2/22/12.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class GoogleWaveOscillator extends UnitOscillator {
    public UnitInputPort variance;
    private double phaseIncrement = 0.1;
    private double previousY;
    private double randomAmplitude = 0.0;

    public GoogleWaveOscillator() {
        addPort(variance = new UnitInputPort("Variance", 0.0));
    }

    @Override
    public void generate(int start, int limit) {
        // Get signal arrays from ports.
        double[] freqs = frequency.getValues();
        double[] outputs = output.getValues();
        double currentPhase = phase.getValue();
        double y;

        for (int i = start; i < limit; i++) {
            if (currentPhase > 0.0) {
                double p = currentPhase;
                y = Math.sqrt(4.0 * (p * (1.0 - p)));
            } else {
                double p = -currentPhase;
                y = -Math.sqrt(4.0 * (p * (1.0 - p)));
            }

            if ((previousY * y) <= 0.0) {
                // Calculate randomly offset phaseIncrement.
                double v = variance.getValues()[0];
                double range = ((Math.random() - 0.5) * 4.0 * v);
                double scale = Math.pow(2.0, range);
                phaseIncrement = convertFrequencyToPhaseIncrement(freqs[i]) * scale;

                // Calculate random amplitude.
                scale = 1.0 + ((Math.random() - 0.5) * 1.5 * v);
                randomAmplitude = amplitude.getValues()[0] * scale;
            }

            outputs[i] = y * randomAmplitude;
            previousY = y;

            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);
        }
        phase.setValue(currentPhase);
    }
}
