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

import com.jsyn.data.DoubleTable;
import com.jsyn.ports.UnitFunctionPort;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.FunctionEvaluator;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.math.ChebyshevPolynomial;
import com.softsynth.math.PolynomialTableData;
import com.softsynth.shared.time.TimeStamp;

/**
 * Waveshaping oscillator with envelopes.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class WaveShapingVoice extends Circuit implements UnitVoice {
    private static final long serialVersionUID = -2704222221111608377L;
    private static final int NUM_PRESETS = 3;
    private UnitOscillator osc;
    private FunctionEvaluator waveShaper;
    private EnvelopeDAHDSR ampEnv;
    private EnvelopeDAHDSR rangeEnv;
    private Multiply frequencyScaler;

    public UnitInputPort range;
    public UnitInputPort frequency;
    public UnitInputPort amplitude;
    public UnitFunctionPort function;
    public UnitInputPort pitchModulation;

    // default Chebyshev polynomial table to share.
    private static DoubleTable chebyshevTable;
    private final static int CHEBYSHEV_ORDER = 11;

    static {
        // Make table with Chebyshev polynomial to share among voices
        PolynomialTableData chebData = new PolynomialTableData(
                ChebyshevPolynomial.T(CHEBYSHEV_ORDER), 1024);
        chebyshevTable = new DoubleTable(chebData.getData());
    }

    public WaveShapingVoice() {
        add(frequencyScaler = new Multiply());
        add(osc = new SineOscillator());
        add(waveShaper = new FunctionEvaluator());
        add(rangeEnv = new EnvelopeDAHDSR());
        add(ampEnv = new EnvelopeDAHDSR());

        addPort(amplitude = ampEnv.amplitude);
        addPort(range = osc.amplitude, "Range");
        addPort(function = waveShaper.function);
        addPort(frequency = frequencyScaler.inputA, "Frequency");
        addPort(pitchModulation = frequencyScaler.inputB, "PitchMod");

        ampEnv.export(this, "Amp");
        rangeEnv.export(this, "Range");

        function.set(chebyshevTable);

        // Connect units.
        osc.output.connect(rangeEnv.amplitude);
        rangeEnv.output.connect(waveShaper.input);
        ampEnv.output.connect(waveShaper.amplitude);
        frequencyScaler.output.connect(osc.frequency);

        // Set reasonable defaults for the ports.
        pitchModulation.setup(0.1, 1.0, 10.0);
        range.setup(0.1, 0.8, 1.0);
        frequency.setup(osc.frequency);
        amplitude.setup(0.0, 0.5, 1.0);

        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);

        usePreset(2);
    }

    @Override
    public UnitOutputPort getOutput() {
        return waveShaper.output;
    }

    @Override
    public void noteOn(double freq, double amp, TimeStamp timeStamp) {
        frequency.set(freq, timeStamp);
        amplitude.set(amp, timeStamp);
        ampEnv.input.on(timeStamp);
        rangeEnv.input.on(timeStamp);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
        ampEnv.input.off(timeStamp);
        rangeEnv.input.off(timeStamp);
    }

    @Override
    public void usePreset(int presetIndex) {
        int n = presetIndex % NUM_PRESETS;
        switch (n) {
            case 0:
                ampEnv.attack.set(0.01);
                ampEnv.decay.set(0.2);
                ampEnv.release.set(1.0);
                rangeEnv.attack.set(0.01);
                rangeEnv.decay.set(0.2);
                rangeEnv.sustain.set(0.4);
                rangeEnv.release.set(1.0);
                break;
            case 1:
                ampEnv.attack.set(0.5);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.2);
                rangeEnv.attack.set(0.03);
                rangeEnv.decay.set(0.2);
                rangeEnv.sustain.set(0.5);
                rangeEnv.release.set(1.0);
                break;
            default:
                ampEnv.attack.set(0.1);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.5);
                rangeEnv.attack.set(0.01);
                rangeEnv.decay.set(0.2);
                rangeEnv.sustain.set(0.9);
                rangeEnv.release.set(1.0);
                break;
        }
    }

    static class MyVoiceDescription extends VoiceDescription {
        static String[] presetNames = {
                "FastChebyshev", "SlowChebyshev", "BrightChebyshev"
        };
        static String[] tags = {
                "electronic", "waveshaping", "clean"
        };

        public MyVoiceDescription() {
            super("Waveshaping", presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new WaveShapingVoice();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return WaveShapingVoice.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        return new MyVoiceDescription();
    }

}
