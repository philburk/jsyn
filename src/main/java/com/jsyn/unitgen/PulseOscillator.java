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

import com.jsyn.ports.UnitInputPort;

/**
 * Simple pulse wave oscillator.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class PulseOscillator extends UnitOscillator {
    /**
     * Pulse width varies from -1.0 to +1.0. At 0.0 the pulse is actually a square wave.
     */
    public UnitInputPort width;

    public PulseOscillator() {
        addPort(width = new UnitInputPort("Width"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] widths = width.getValues();
        double[] outputs = output.getValues();

        // Variables have a single value.
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            // Generate sawtooth phaser to provide phase for pulse generation.
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);
            double ampl = amplitudes[i];
            // Either full negative or positive amplitude.
            outputs[i] = (currentPhase < widths[i]) ? -ampl : ampl;
        }

        // Value needs to be saved for next time.
        phase.setValue(currentPhase);
    }

}
