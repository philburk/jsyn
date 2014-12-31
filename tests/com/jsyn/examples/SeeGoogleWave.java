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

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.unitgen.LineOut;

/**
 * Generate the waveform shown on the Google home page on 2/22/12.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class SeeGoogleWave extends JApplet {
    private static final long serialVersionUID = -831590388347137926L;
    private Synthesizer synth;
    private GoogleWaveOscillator googleWaveUnit;
    private LineOut lineOut;
    private AudioScope scope;

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        SeeGoogleWave applet = new SeeGoogleWave();
        JAppletFrame frame = new JAppletFrame("Google Wave", applet);
        frame.setSize(640, 500);
        frame.setVisible(true);
        frame.test();
        frame.validate();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        add(BorderLayout.NORTH, new JLabel("GoogleWave - elliptical segments"));

        scope = new AudioScope(synth);
        scope.addProbe(googleWaveUnit.output);
        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.getView().setShowControls(false);
        scope.start();
        add(BorderLayout.CENTER, scope.getView());

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(0, 1));
        add(BorderLayout.SOUTH, southPanel);

        southPanel.add(PortControllerFactory.createExponentialPortSlider(googleWaveUnit.frequency));
        southPanel.add(PortControllerFactory.createExponentialPortSlider(googleWaveUnit.variance));
        southPanel.add(PortControllerFactory.createExponentialPortSlider(googleWaveUnit.amplitude));

        validate();
    }

    @Override
    public void start() {
        synth = JSyn.createSynthesizer();
        synth.add(googleWaveUnit = new GoogleWaveOscillator());
        googleWaveUnit.amplitude.setup(0.02, 0.5, 1.0);
        googleWaveUnit.variance.setup(0.0, 0.0, 1.0);
        googleWaveUnit.frequency.setup(40.0, 200.0, 1000.0);

        // Add an output so we can hear it.
        synth.add(lineOut = new LineOut());

        googleWaveUnit.output.connect(0, lineOut.input, 0);
        googleWaveUnit.output.connect(0, lineOut.input, 1);

        setupGUI();

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // Start lineOut so it can pull data from other units.
        lineOut.start();

        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

    }

    @Override
    public void stop() {
        scope.stop();
        synth.stop();
    }

}
