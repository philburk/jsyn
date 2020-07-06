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
import com.jsyn.unitgen.FilterFourPoles;
import com.jsyn.unitgen.MorphingOscillatorBL;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/**
 * Synthesizer voice with two morphing oscillators and a four-pole resonant filter.
 * Modulate the amplitude and filter using DAHDSR envelopes.
 */
public class DualOscillatorSynthVoice extends Circuit implements UnitVoice {
    private Multiply frequencyMultiplier;
    private Multiply amplitudeMultiplier;
    private Multiply detuneScaler1;
    private Multiply detuneScaler2;
    private Multiply amplitudeBoost;
    private MorphingOscillatorBL osc1;
    private MorphingOscillatorBL osc2;
    private FilterFourPoles filter;
    private EnvelopeDAHDSR ampEnv;
    private EnvelopeDAHDSR filterEnv;
    private Add cutoffAdder;

    private static MyVoiceDescription voiceDescription;

    public UnitInputPort amplitude;
    public UnitInputPort frequency;
    /**
     * This scales the frequency value. You can use this to modulate a group of instruments using a
     * shared LFO and they will stay in tune. Set to 1.0 for no modulation.
     */
    public UnitInputPort frequencyScaler;
    public UnitInputPort oscShape1;
    public UnitInputPort oscShape2;
//    public UnitInputPort oscDetune1;
//    public UnitInputPort oscDetune2;
    public UnitInputPort cutoff;
    public UnitInputPort filterEnvDepth;
    public UnitInputPort Q;

    public DualOscillatorSynthVoice() {
        add(frequencyMultiplier = new Multiply());
        add(amplitudeMultiplier = new Multiply());
        add(amplitudeBoost = new Multiply());
        add(detuneScaler1 = new Multiply());
        add(detuneScaler2 = new Multiply());
        // Add tone generators.
        add(osc1 = new MorphingOscillatorBL());
        add(osc2 = new MorphingOscillatorBL());

        // Use an envelope to control the amplitude.
        add(ampEnv = new EnvelopeDAHDSR());

        // Use an envelope to control the filter cutoff.
        add(filterEnv = new EnvelopeDAHDSR());
        add(filter = new FilterFourPoles());
        add(cutoffAdder = new Add());

        filterEnv.output.connect(cutoffAdder.inputA);
        cutoffAdder.output.connect(filter.frequency);
        frequencyMultiplier.output.connect(detuneScaler1.inputA);
        frequencyMultiplier.output.connect(detuneScaler2.inputA);
        detuneScaler1.output.connect(osc1.frequency);
        detuneScaler2.output.connect(osc2.frequency);
        osc1.output.connect(amplitudeMultiplier.inputA); // mix oscillators
        osc2.output.connect(amplitudeMultiplier.inputA);
        amplitudeMultiplier.output.connect(filter.input);
        filter.output.connect(amplitudeBoost.inputA);
        amplitudeBoost.output.connect(ampEnv.amplitude);

        addPort(amplitude = amplitudeMultiplier.inputB, PORT_NAME_AMPLITUDE);
        addPort(frequency = frequencyMultiplier.inputA, PORT_NAME_FREQUENCY);
        addPort(oscShape1 = osc1.shape, "OscShape1");
        addPort(oscShape2 = osc2.shape, "OscShape2");
//        addPort(oscDetune1 = osc1.shape, "OscDetune1");
//        addPort(oscDetune2 = osc2.shape, "OscDetune2");
        addPort(cutoff = cutoffAdder.inputB, PORT_NAME_CUTOFF);
        addPortAlias(cutoff, PORT_NAME_TIMBRE);
        addPort(Q = filter.Q);
        addPort(frequencyScaler = frequencyMultiplier.inputB, PORT_NAME_FREQUENCY_SCALER);
        addPort(filterEnvDepth = filterEnv.amplitude, "FilterEnvDepth");

        filterEnv.export(this, "Filter");
        ampEnv.export(this, "Amp");

        frequency.setup(osc1.frequency);
        frequencyScaler.setup(0.2, 1.0, 4.0);
        cutoff.setup(filter.frequency);
        // Allow negative filter sweeps
        filterEnvDepth.setup(-4000.0, 2000.0, 4000.0);

        // set amplitudes slightly different so that they never entirely cancel
        osc1.amplitude.set(0.5);
        osc2.amplitude.set(0.4);
        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);
        // Add named port for mapping pressure.
        amplitudeBoost.inputB.setup(1.0, 1.0, 4.0);
        addPortAlias(amplitudeBoost.inputB, PORT_NAME_PRESSURE);

        usePreset(0);
    }

    /**
     * The first oscillator will be tuned UP by semitoneOffset/2.
     * The second oscillator will be tuned DOWN by semitoneOffset/2.
     * @param semitoneOffset
     */
    private void setDetunePitch(double semitoneOffset) {
        double halfOffset = semitoneOffset * 0.5;
        setDetunePitch1(halfOffset);
        setDetunePitch2(-halfOffset);
    }

    /**
     * Set the detuning for osc1 in semitones.
     * @param semitoneOffset
     */
    private void setDetunePitch1(double semitoneOffset) {
        double scale = AudioMath.semitonesToFrequencyScaler(semitoneOffset);
        detuneScaler1.inputB.set(scale);
    }

    /**
     * Set the detuning for osc2 in semitones.
     * @param semitoneOffset
     */
    private void setDetunePitch2(double semitoneOffset) {
        double scale = AudioMath.semitonesToFrequencyScaler(semitoneOffset);
        detuneScaler2.inputB.set(scale);
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

    // Reset to basic voice.
    public void reset() {
        osc1.shape.set(0.0);
        osc2.shape.set(0.0);
        ampEnv.attack.set(0.005);
        ampEnv.decay.set(0.2);
        ampEnv.sustain.set(0.5);
        ampEnv.release.set(1.0);
        filterEnv.attack.set(0.01);
        filterEnv.decay.set(0.6);
        filterEnv.sustain.set(0.4);
        filterEnv.release.set(1.0);
        cutoff.set(500.0);
        filterEnvDepth.set(3000.0);
        filter.reset();
        filter.Q.set(3.9);
        setDetunePitch(0.02);
    }

    @Override
    public void usePreset(int presetIndex) {
        reset(); // start from known configuration
        int n = presetIndex % presetNames.length;
        switch (n) {
            case 0:
                break;
            case 1:
                ampEnv.attack.set(0.1);
                ampEnv.decay.set(0.9);
                ampEnv.sustain.set(0.1);
                ampEnv.release.set(0.1);
                cutoff.set(500.0);
                filterEnvDepth.set(500.0);
                filter.Q.set(3.0);
                break;
            case 2:
                ampEnv.attack.set(0.1);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.5);
                cutoff.set(2000.0);
                filterEnvDepth.set(500.0);
                filter.Q.set(2.0);
                break;
            case 3:
                osc1.shape.set(-0.9);
                osc2.shape.set(-0.8);
                ampEnv.attack.set(0.3);
                ampEnv.decay.set(0.8);
                ampEnv.release.set(0.2);
                filterEnv.sustain.set(0.7);
                cutoff.set(500.0);
                filterEnvDepth.set(500.0);
                filter.Q.set(3.0);
                break;
            case 4:
                osc1.shape.set(1.0);
                osc2.shape.set(0.0);
                break;
            case 5:
                osc1.shape.set(1.0);
                setDetunePitch1(0.0);
                osc2.shape.set(0.9);
                setDetunePitch1(7.0);
                break;
            case 6:
                osc1.shape.set(0.6);
                osc2.shape.set(-0.2);
                setDetunePitch1(0.01);
                ampEnv.attack.set(0.005);
                ampEnv.decay.set(0.09);
                ampEnv.sustain.set(0.0);
                ampEnv.release.set(1.0);
                filterEnv.attack.set(0.005);
                filterEnv.decay.set(0.1);
                filterEnv.sustain.set(0.4);
                filterEnv.release.set(1.0);
                cutoff.set(2000.0);
                filterEnvDepth.set(5000.0);
                filter.Q.set(7.02);
                break;
            default:
                break;
        }
    }

    private static final String[] presetNames = {
            "FastSaw", "SlowSaw", "BrightSaw",
            "SoftSine", "SquareSaw", "SquareFifth",
            "Blip"
    };

    static class MyVoiceDescription extends VoiceDescription {
        String[] tags = {
                "electronic", "filter", "analog", "subtractive"
        };

        public MyVoiceDescription() {
            super(DualOscillatorSynthVoice.class.getName(), presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new DualOscillatorSynthVoice();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return DualOscillatorSynthVoice.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        if (voiceDescription == null) {
            voiceDescription = new MyVoiceDescription();
        }
        return voiceDescription;
    }


}
