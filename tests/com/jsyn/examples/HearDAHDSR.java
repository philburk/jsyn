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

package com.jsyn.examples;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a tone using a JSyn oscillator. Modulate the amplitude using a DAHDSR envelope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class HearDAHDSR extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    private UnitOscillator osc;
    // Use a square wave to trigger the envelope.
    private UnitOscillator gatingOsc;
    private EnvelopeDAHDSR dahdsr;
    private LineOut lineOut;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();

        // Add a tone generator.
        synth.add(osc = new SineOscillator());
        // Add a trigger.
        synth.add(gatingOsc = new SquareOscillator());
        // Use an envelope to control the amplitude.
        synth.add(dahdsr = new EnvelopeDAHDSR());
        // Add an output mixer.
        synth.add(lineOut = new LineOut());

        gatingOsc.output.connect(dahdsr.input);
        dahdsr.output.connect(osc.amplitude);
        dahdsr.attack.setup(0.001, 0.01, 2.0);
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        gatingOsc.frequency.setup(0.001, 0.5, 10.0);
        gatingOsc.frequency.setName("Rate");

        osc.frequency.setup(50.0, 440.0, 2000.0);
        osc.frequency.setName("Freq");

        // Arrange the knob in a row.
        setLayout(new GridLayout(1, 0));

        setupPortKnob(osc.frequency);
        setupPortKnob(gatingOsc.frequency);
        setupPortKnob(dahdsr.attack);
        setupPortKnob(dahdsr.hold);
        setupPortKnob(dahdsr.decay);
        setupPortKnob(dahdsr.sustain);
        setupPortKnob(dahdsr.release);

        validate();
    }

    private void setupPortKnob(UnitInputPort port) {

        DoubleBoundedRangeModel model = PortModelFactory.createExponentialModel(port);
        System.out.println("Make knob for " + port.getName() + ", model.getDV = "
                + model.getDoubleValue() + ", model.getV = " + model.getValue() + ", port.getV = "
                + port.get());
        RotaryTextController knob = new RotaryTextController(model, 10);
        knob.setBorder(BorderFactory.createTitledBorder(port.getName()));
        knob.setTitle(port.getName());
        add(knob);
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
    public static void main(String args[]) {
        HearDAHDSR applet = new HearDAHDSR();
        JAppletFrame frame = new JAppletFrame("Hear DAHDSR Envelope", applet);
        frame.setSize(640, 200);
        frame.setVisible(true);
        frame.test();
    }

}
