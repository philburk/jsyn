/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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
import com.jsyn.unitgen.EnvelopeAttackDecay;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SineOscillatorPhaseModulated;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.shared.time.TimeStamp;

/**
 * Drum instruments using 2 Operator FM.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class DrumWoodFM extends Circuit implements UnitVoice {
    private static final int NUM_PRESETS = 3;
    // Declare units and ports.
    EnvelopeAttackDecay ampEnv;
    SineOscillatorPhaseModulated carrierOsc;
    EnvelopeAttackDecay modEnv;
    SineOscillator modOsc;
    PassThrough freqDistributor;
    Add modSummer;
    Multiply frequencyMultiplier;

    public UnitInputPort mcratio;
    public UnitInputPort index;
    public UnitInputPort modRange;
    public UnitInputPort frequency;

    public DrumWoodFM() {
        // Create unit generators.
        add(carrierOsc = new SineOscillatorPhaseModulated());
        add(freqDistributor = new PassThrough());
        add(modSummer = new Add());
        add(ampEnv = new EnvelopeAttackDecay());
        add(modEnv = new EnvelopeAttackDecay());
        add(modOsc = new SineOscillator());
        add(frequencyMultiplier = new Multiply());

        addPort(mcratio = frequencyMultiplier.inputB, "MCRatio");
        addPort(index = modSummer.inputA, "Index");
        addPort(modRange = modEnv.amplitude, "ModRange");
        addPort(frequency = freqDistributor.input, "Frequency");

        ampEnv.export(this, "Amp");
        modEnv.export(this, "Mod");

        freqDistributor.output.connect(carrierOsc.frequency);
        freqDistributor.output.connect(frequencyMultiplier.inputA);

        carrierOsc.output.connect(ampEnv.amplitude);
        modEnv.output.connect(modSummer.inputB);
        modSummer.output.connect(modOsc.amplitude);
        modOsc.output.connect(carrierOsc.modulation);
        frequencyMultiplier.output.connect(modOsc.frequency);

        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);

        usePreset(0);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
    }

    @Override
    public void noteOn(double freq, double ampl, TimeStamp timeStamp) {
        carrierOsc.amplitude.set(ampl, timeStamp);
        ampEnv.input.trigger(timeStamp);
        modEnv.input.trigger(timeStamp);
    }

    @Override
    public UnitOutputPort getOutput() {
        return ampEnv.output;
    }

    @Override
    public void usePreset(int presetIndex) {
        mcratio.setup(0.001, 0.6875, 20.0);
        ampEnv.attack.setup(0.001, 0.005, 8.0);
        modEnv.attack.setup(0.001, 0.005, 8.0);

        int n = presetIndex % NUM_PRESETS;
        switch (n) {
            case 0:
                ampEnv.decay.setup(0.001, 0.293, 8.0);
                modEnv.decay.setup(0.001, 0.07, 8.0);
                frequency.setup(0.0, 349.0, 3000.0);
                index.setup(0.001, 0.05, 10.0);
                modRange.setup(0.001, 0.4, 10.0);
                break;
            case 1:
            default:
                ampEnv.decay.setup(0.001, 0.12, 8.0);
                modEnv.decay.setup(0.001, 0.06, 8.0);
                frequency.setup(0.0, 1400.0, 3000.0);
                index.setup(0.001, 0.16, 10.0);
                modRange.setup(0.001, 0.17, 10.0);
                break;
        }
    }

    static class MyVoiceDescription extends VoiceDescription {
        static String[] presetNames = {
                "WoodBlockFM", "ClaveFM"
        };
        static String[] tags = {
                "electronic", "drum"
        };

        public MyVoiceDescription() {
            super("DrumWoodFM", presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new DrumWoodFM();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return DrumWoodFM.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        return new MyVoiceDescription();
    }
}
