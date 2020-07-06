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
 * Sine oscillator with a phase modulation input. Phase modulation is similar to frequency
 * modulation but is easier to use in some ways.
 * 
 * <pre>
 * output = sin(PI * (phase + modulation))
 * </pre>
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class SineOscillatorPhaseModulated extends SineOscillator {
    public UnitInputPort modulation;

    /* Define Unit Ports used by connect() and set(). */
    public SineOscillatorPhaseModulated() {
        super();
        addPort(modulation = new UnitInputPort("Modulation"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        double[] modulations = modulation.getValues();
        double currentPhase = phase.getValue();

        for (int i = start; i < limit; i++) {
            /* Generate sawtooth phaser to provide phase for sine generation. */
            double phaseIncrement = convertFrequencyToPhaseIncrement(frequencies[i]);
            currentPhase = incrementWrapPhase(currentPhase, phaseIncrement);
            double modulatedPhase = currentPhase + modulations[i];
            double value;
            if (false) {
                // TODO Compare benchmarks.
                while (modulatedPhase >= 1.0) {
                    modulatedPhase -= 2.0;
                }
                while (modulatedPhase < -1.0) {
                    modulatedPhase += 2.0;
                }
                value = fastSin(modulatedPhase);
            } else {
                value = Math.sin(modulatedPhase * Math.PI);
            }
            outputs[i] = value * amplitudes[i];
            // System.out.format("Sine: freq = %10.4f , amp = %8.5f, out = %8.5f, phase =  %8.5f, frame =  %8d\n",
            // frequencies[i], amplitudes[i],outputs[i],currentPhase,frame++ );
        }

        phase.setValue(currentPhase);
    }

}
