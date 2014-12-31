/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

package com.jsyn.apps;

import java.awt.GridLayout;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Show the version of JSyn and play some sine waves. This program will be run if you double click
 * the JSyn jar file.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class AboutJSyn extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    private UnitOscillator osc1;
    private UnitOscillator osc2;
    private LinearRamp lag;
    private LineOut lineOut;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();

        // Add a tone generator.
        synth.add(osc1 = new SineOscillator());
        synth.add(osc2 = new SineOscillator());
        // Add a lag to smooth out amplitude changes and avoid pops.
        synth.add(lag = new LinearRamp());
        // Add an output mixer.
        synth.add(lineOut = new LineOut());
        // Connect the oscillator to the output.
        osc1.output.connect(0, lineOut.input, 0);
        osc2.output.connect(0, lineOut.input, 1);

        // Arrange the faders in a stack.
        setLayout(new GridLayout(0, 1));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(0, 1));
        infoPanel.add(new JLabel("About: " + synth, SwingConstants.CENTER));
        infoPanel.add(new JLabel("From: http://www.softsynth.com/", SwingConstants.CENTER));
        infoPanel.add(new JLabel("(C) 1997-2011 Mobileer Inc", SwingConstants.CENTER));
        add(infoPanel);

        // Set the minimum, current and maximum values for the port.
        lag.output.connect(osc1.amplitude);
        lag.output.connect(osc2.amplitude);
        lag.input.setup(0.001, 0.5, 1.0);
        lag.time.set(0.1);
        lag.input.setName("Amplitude");
        add(PortControllerFactory.createExponentialPortSlider(lag.input));

        osc1.frequency.setup(50.0, 300.0, 3000.0);
        osc1.frequency.setName("Frequency (Left)");
        add(PortControllerFactory.createExponentialPortSlider(osc1.frequency));
        osc2.frequency.setup(50.0, 302.0, 3000.0);
        osc2.frequency.setName("Frequency (Right)");
        add(PortControllerFactory.createExponentialPortSlider(osc2.frequency));
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
        AboutJSyn applet = new AboutJSyn();
        JAppletFrame frame = new JAppletFrame("About JSyn", applet);
        frame.setSize(440, 300);
        frame.setVisible(true);
        frame.test();
    }

}
