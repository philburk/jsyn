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

import javax.swing.JApplet;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.swing.ExponentialRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a sawtooth using a JSyn oscillator and some knobs.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class SawFaders extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    private UnitOscillator osc;
    private LinearRamp lag;
    private LineOut lineOut;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();

        // Add a tone generator. (band limited sawtooth)
        synth.add(osc = new SawtoothOscillatorBL());
        // Add a lag to smooth out amplitude changes and avoid pops.
        synth.add(lag = new LinearRamp());
        // Add an output mixer.
        synth.add(lineOut = new LineOut());
        // Connect the oscillator to both left and right output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Set the minimum, current and maximum values for the port.
        lag.output.connect(osc.amplitude);
        lag.input.setup(0.0, 0.5, 1.0);
        lag.time.set(0.2);

        // Arrange the faders in a stack.
        setLayout(new GridLayout(0, 1));

        ExponentialRangeModel amplitudeModel = PortModelFactory.createExponentialModel(lag.input);
        RotaryTextController knob = new RotaryTextController(amplitudeModel, 5);
        JPanel knobPanel = new JPanel();
        knobPanel.add(knob);
        add(knobPanel);

        osc.frequency.setup(50.0, 300.0, 10000.0);
        add(PortControllerFactory.createExponentialPortSlider(osc.frequency));
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
    public static void main(String args[]) {
        SawFaders applet = new SawFaders();
        JAppletFrame frame = new JAppletFrame("SawFaders", applet);
        frame.setSize(440, 200);
        frame.setVisible(true);
        frame.test();
    }

}
