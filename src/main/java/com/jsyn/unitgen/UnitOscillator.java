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
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitVariablePort;
import com.softsynth.shared.time.TimeStamp;

/**
 * Base class for all oscillators.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public abstract class UnitOscillator extends UnitGenerator implements UnitVoice {
    /** Frequency in Hertz. */
    public UnitInputPort frequency;
    public UnitInputPort amplitude;
    public UnitVariablePort phase;
    public UnitOutputPort output;

    public static final double DEFAULT_FREQUENCY = 440.0;
    public static final double DEFAULT_AMPLITUDE = 1.0;

    /* Define Unit Ports used by connect() and set(). */
    public UnitOscillator() {
        addPort(frequency = new UnitInputPort(PORT_NAME_FREQUENCY));
        frequency.setup(40.0, DEFAULT_FREQUENCY, 8000.0);
        addPort(amplitude = new UnitInputPort(PORT_NAME_AMPLITUDE, DEFAULT_AMPLITUDE));
        addPort(phase = new UnitVariablePort(PORT_NAME_PHASE));
        addPort(output = new UnitOutputPort(PORT_NAME_OUTPUT));
    }

    /**
     * Convert a frequency in Hertz to a phaseIncrement in the range -1.0 to +1.0
     */
    public double convertFrequencyToPhaseIncrement(double freq) {
        double phaseIncrement;
        try {
            phaseIncrement = freq * synthesisEngine.getInverseNyquist();
        } catch (NullPointerException e) {
            throw new NullPointerException(
                    "Null Synth! You probably forgot to add this unit to the Synthesizer!");
        }
        // Clip to range.
        phaseIncrement = (phaseIncrement > 1.0) ? 1.0 : ((phaseIncrement < -1.0) ? -1.0
                : phaseIncrement);
        return phaseIncrement;
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    public void noteOn(double freq, double ampl) {
        frequency.set(freq);
        amplitude.set(ampl);
    }

    public void noteOff() {
        amplitude.set(0.0);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
        amplitude.set(0.0, timeStamp);
    }

    @Override
    public void noteOn(double freq, double ampl, TimeStamp timeStamp) {
        frequency.set(freq, timeStamp);
        amplitude.set(ampl, timeStamp);
    }

    @Override
    public void usePreset(int presetIndex) {
    }
}
