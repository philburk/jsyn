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

package com.jsyn.instruments;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.FilterLowPass;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.shared.time.TimeStamp;

/**
 * Typical synthesizer voice with one oscillator and a biquad resonant filter. Modulate the amplitude and
 * filter using DAHDSR envelopes.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class SubtractiveSynthVoice extends Circuit implements UnitVoice {
    private UnitOscillator osc;
    private FilterLowPass filter;
    private EnvelopeDAHDSR ampEnv;
    private EnvelopeDAHDSR filterEnv;
    private Add cutoffAdder;
    private Multiply frequencyScaler;

    public UnitInputPort amplitude;
    public UnitInputPort frequency;
    /**
     * This scales the frequency value. You can use this to modulate a group of instruments using a
     * shared LFO and they will stay in tune.
     */
    public UnitInputPort pitchModulation;
    public UnitInputPort cutoff;
    public UnitInputPort cutoffRange;
    public UnitInputPort Q;

    public SubtractiveSynthVoice() {
        add(frequencyScaler = new Multiply());
        // Add a tone generator.
        add(osc = new SawtoothOscillatorBL());

        // Use an envelope to control the amplitude.
        add(ampEnv = new EnvelopeDAHDSR());

        // Use an envelope to control the filter cutoff.
        add(filterEnv = new EnvelopeDAHDSR());
        add(filter = new FilterLowPass());
        add(cutoffAdder = new Add());

        filterEnv.output.connect(cutoffAdder.inputA);
        cutoffAdder.output.connect(filter.frequency);
        frequencyScaler.output.connect(osc.frequency);
        osc.output.connect(filter.input);
        filter.output.connect(ampEnv.amplitude);

        addPort(amplitude = osc.amplitude, "Amplitude");
        addPort(frequency = frequencyScaler.inputA, "Frequency");
        addPort(pitchModulation = frequencyScaler.inputB, "PitchMod");
        addPort(cutoff = cutoffAdder.inputB, "Cutoff");
        addPort(cutoffRange = filterEnv.amplitude, "CutoffRange");
        addPort(Q = filter.Q);

        ampEnv.export(this, "Amp");
        filterEnv.export(this, "Filter");

        frequency.setup(osc.frequency);
        pitchModulation.setup(0.2, 1.0, 4.0);
        cutoff.setup(filter.frequency);
        cutoffRange.setup(filter.frequency);

        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);

        usePreset(0);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
        ampEnv.input.off(timeStamp);
        filterEnv.input.off(timeStamp);
    }

    @Override
    public void noteOn(double freq, double ampl, TimeStamp timeStamp) {
        frequency.set(freq, timeStamp);
        amplitude.set(ampl, timeStamp);

        ampEnv.input.on(timeStamp);
        filterEnv.input.on(timeStamp);
    }

    @Override
    public UnitOutputPort getOutput() {
        return ampEnv.output;
    }

    @Override
    public void usePreset(int presetIndex) {
        int n = presetIndex % presetNames.length;
        switch (n) {
            case 0:
                ampEnv.attack.set(0.01);
                ampEnv.decay.set(0.2);
                ampEnv.release.set(1.0);
                cutoff.set(500.0);
                cutoffRange.set(500.0);
                filter.Q.set(1.0);
                break;
            case 1:
                ampEnv.attack.set(0.5);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.2);
                cutoff.set(500.0);
                cutoffRange.set(500.0);
                filter.Q.set(3.0);
                break;
            case 2:
            default:
                ampEnv.attack.set(0.1);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.5);
                cutoff.set(2000.0);
                cutoffRange.set(500.0);
                filter.Q.set(2.0);
                break;
        }
    }

    static String[] presetNames = {
            "FastSaw", "SlowSaw", "BrightSaw"
    };

    static class MyVoiceDescription extends VoiceDescription {
        String[] tags = {
                "electronic", "filter", "clean"
        };

        public MyVoiceDescription() {
            super("SubtractiveSynth", presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new SubtractiveSynthVoice();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return SubtractiveSynthVoice.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        return new MyVoiceDescription();
    }

}
