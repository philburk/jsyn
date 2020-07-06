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

package com.jsyn.instruments;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.EnvelopeAttackDecay;
import com.jsyn.unitgen.PinkNoise;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.shared.time.TimeStamp;

/**
 * Cheap synthetic cymbal sound.
 */
public class NoiseHit extends Circuit implements UnitVoice {
    EnvelopeAttackDecay ampEnv;
    PinkNoise noise;
    private static final int NUM_PRESETS = 3;

    public NoiseHit() {
        // Create unit generators.
        add(noise = new PinkNoise());
        add(ampEnv = new EnvelopeAttackDecay());
        noise.output.connect(ampEnv.amplitude);

        ampEnv.export(this, "Amp");

        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);

        usePreset(0);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
    }

    @Override
    public void noteOn(double freq, double ampl, TimeStamp timeStamp) {
        noise.amplitude.set(ampl, timeStamp);
        ampEnv.input.trigger();
    }

    @Override
    public UnitOutputPort getOutput() {
        return ampEnv.output;
    }

    @Override
    public void usePreset(int presetIndex) {
        int n = presetIndex % NUM_PRESETS;
        switch (n) {
            case 0:
                ampEnv.attack.set(0.001);
                ampEnv.decay.set(0.1);
                break;
            case 1:
                ampEnv.attack.set(0.03);
                ampEnv.decay.set(1.4);
                break;
            default:
                ampEnv.attack.set(0.9);
                ampEnv.decay.set(0.3);
                break;
        }
    }

    static class MyVoiceDescription extends VoiceDescription {
        static String[] presetNames = {
                "ShortNoiseHit", "LongNoiseHit", "SlowNoiseHit"
        };
        static String[] tags = {
                "electronic", "noise"
        };

        public MyVoiceDescription() {
            super("NoiseHit", presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new NoiseHit();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return NoiseHit.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        return new MyVoiceDescription();
    }
}
