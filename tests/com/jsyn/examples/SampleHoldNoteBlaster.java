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

package com.jsyn.examples;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.EdgeDetector;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.FilterLowPass;
import com.jsyn.unitgen.Latch;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.PulseOscillator;
import com.jsyn.unitgen.SawtoothOscillatorDPW;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitSource;

/**
 * Classic osc-filter-envelope voice with a sample and hold.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class SampleHoldNoteBlaster extends Circuit implements UnitSource {

    public UnitInputPort frequency;
    public UnitInputPort amplitude;
    public UnitInputPort modRate;
    public UnitInputPort modDepth;
    private UnitInputPort cutoff;
    private UnitInputPort resonance;
    private UnitInputPort pulseRate;
    private UnitInputPort sweepRate;
    private UnitInputPort sweepDepth;
    public UnitOutputPort output;

    private static SampleHoldNoteBlaster soundMaker; // singleton

    private UnitOscillator osc;
    private UnitOscillator samplee; // for sample and hold
    private PulseOscillator pulser;
    private Latch latch;
    private UnitOscillator lfo;
    private FilterLowPass filter;
    private PassThrough frequencyPin;
    private Multiply modScaler;
    private EnvelopeDAHDSR ampEnv;
    private Multiply sweepScaler;
    private EdgeDetector edgeDetector;
    private UnitInputPort pulseWidth;
    private UnitInputPort attack;
    private UnitInputPort decay;
    private UnitInputPort sustain;
    private UnitInputPort release;

    public static SampleHoldNoteBlaster getInstance() {
        if (soundMaker == null) {
            soundMaker = new SampleHoldNoteBlaster();
        }
        return soundMaker;
    }

    public SampleHoldNoteBlaster() {
        add(frequencyPin = new PassThrough());
        add(modScaler = new Multiply());
        add(sweepScaler = new Multiply());
        add(edgeDetector = new EdgeDetector());
        add(latch = new Latch());
        add(samplee = new SineOscillator());
        add(pulser = new PulseOscillator());
        add(lfo = new SineOscillator());
        add(osc = new SawtoothOscillatorDPW());
        add(filter = new FilterLowPass());
        // Use an envelope to control the amplitude.
        add(ampEnv = new EnvelopeDAHDSR());

        samplee.output.connect(latch.input);
        pulser.output.connect(edgeDetector.input);
        edgeDetector.output.connect(latch.gate);
        latch.output.connect(osc.frequency);

        frequencyPin.output.connect(osc.frequency);

        frequencyPin.output.connect(modScaler.inputA);
        modScaler.output.connect(lfo.amplitude);

        frequencyPin.output.connect(sweepScaler.inputA);
        sweepScaler.output.connect(samplee.amplitude);

        lfo.output.connect(osc.frequency);
        osc.output.connect(filter.input);
        filter.output.connect(ampEnv.amplitude);
        pulser.output.connect(ampEnv.input);

        // Setup ports ---------------
        addPort(amplitude = osc.amplitude, "amplitude");
        amplitude.set(0.6);

        addPort(frequency = frequencyPin.input, "frequency");
        frequency.setup(50.0, 800.0, 2000.0);

        addPort(modRate = lfo.frequency, "modRate");
        modRate.setup(0.0, 12, 20.0);

        addPort(modDepth = modScaler.inputB, "modDepth");
        modDepth.setup(0.0, 0.0, 0.5);

        addPort(cutoff = filter.frequency, "cutoff");
        cutoff.setup(20.0, 2000.0, 5000.0);
        addPort(resonance = filter.Q, "Q");

        addPort(sweepDepth = sweepScaler.inputB, "sweepDepth");
        sweepDepth.setup(0.0, 0.6, 1.0);
        addPort(sweepRate = samplee.frequency, "sweepRate");
        sweepRate.setup(0.2, 5.9271, 20.0);

        addPort(pulseRate = pulser.frequency, "pulseRate");
        pulseRate.setup(0.2, 7.0, 20.0);
        addPort(pulseWidth = pulser.width, "pulseWidth");
        pulseWidth.setup(-0.9, 0.9, 0.9);

        addPort(attack = ampEnv.attack, "attack");
        attack.setup(0.001, 0.001, 2.0);
        addPort(decay = ampEnv.decay, "decay");
        decay.setup(0.001, 0.26, 2.0);
        addPort(sustain = ampEnv.sustain, "sustain");
        sustain.setup(0.000, 0.24, 1.0);
        addPort(release = ampEnv.release, "release");
        release.setup(0.001, 0.2, 2.0);

        addPort(output = ampEnv.output);
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }
}
