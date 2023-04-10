/*
 * Copyright 2023 Phil Burk, Mobileer Inc
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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Pan;
import com.jsyn.unitgen.PinkNoise;
import com.jsyn.unitgen.RoomReverb;
import com.jsyn.unitgen.SawtoothOscillatorDPW;
import com.jsyn.unitgen.SquareOscillator;
import java.awt.GridLayout;
import javax.swing.JApplet;

/**
 * Play various sounds interactively through a reverb.
 *
 * @author Phil Burk (C) 2023 Mobileer Inc
 */
public class TuneReverb extends JApplet {
    private Synthesizer synth;

    private PinkNoise noise;
    private SawtoothOscillatorDPW sawtooth;
    // Use a square wave to trigger the envelope.
    private SquareOscillator gatingOsc;
    private EnvelopeDAHDSR dahdsr;
    private Pan dryWet;
    private RoomReverb reverb;
    private LineOut lineOut;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();

        synth.add(noise = new PinkNoise());
        synth.add(sawtooth = new SawtoothOscillatorDPW());
        synth.add(gatingOsc = new SquareOscillator());
        synth.add(dahdsr = new EnvelopeDAHDSR());
        synth.add(dryWet = new Pan());
        synth.add(reverb = new RoomReverb());
        synth.add(lineOut = new LineOut());

        // Connect the oscillator to both channels of the output.
        gatingOsc.output.connect(dahdsr.input);
        gatingOsc.frequency.set(0.5);
        dahdsr.attack.set(0.01);
        dahdsr.decay.set(0.05);
        dahdsr.sustain.set(0.00);

        noise.output.connect(dahdsr.amplitude);
        sawtooth.output.connect(dahdsr.amplitude);
        dahdsr.output.connect(dryWet.input);
        dryWet.output.connect(1, reverb.input, 0);
        dryWet.output.connect(0, lineOut.input, 0);
        dryWet.output.connect(0, lineOut.input, 1);
        reverb.output.connect(0, lineOut.input, 0);
        reverb.output.connect(0, lineOut.input, 1);

        // Arrange the faders in a stack.
        setLayout(new GridLayout(0, 1));

        gatingOsc.frequency.setup(0.1, 0.5, 4.0);
        add(PortControllerFactory.createExponentialPortSlider(sawtooth.frequency));
        add(PortControllerFactory.createExponentialPortSlider(sawtooth.amplitude));
        add(PortControllerFactory.createExponentialPortSlider(noise.amplitude));
        add(PortControllerFactory.createExponentialPortSlider(gatingOsc.frequency));
        add(PortControllerFactory.createPortSlider(dryWet.pan));
        add(PortControllerFactory.createExponentialPortSlider(reverb.preDelayMillis));
        add(PortControllerFactory.createExponentialPortSlider(reverb.multiTap));
        add(PortControllerFactory.createExponentialPortSlider(reverb.diffusion));
        add(PortControllerFactory.createExponentialPortSlider(reverb.time));
        add(PortControllerFactory.createExponentialPortSlider(reverb.damping));
        validate();
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();
    }

    @Override
    public void stop() {
        synth.stop();
    }

    /* Can be run as either an application or as an applet. */
    public static void main(String[] args) {
        TuneReverb applet = new TuneReverb();
        JAppletFrame frame = new JAppletFrame("Tune Reverb", applet);
        frame.setSize(440, 600);
        frame.setVisible(true);
        frame.test();
    }

}
