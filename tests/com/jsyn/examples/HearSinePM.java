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

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SineOscillatorPhaseModulated;

/**
 * Play a tone using a phase modulated sinewave oscillator. Phase modulation (PM) is very similar to
 * frequency modulation (FM) but is easier to control.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class HearSinePM extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    SineOscillatorPhaseModulated carrier;
    SineOscillator modulator;
    LineOut lineOut;
    AudioScope scope;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(modulator = new SineOscillator());
        // Add a trigger.
        synth.add(carrier = new SineOscillatorPhaseModulated());
        // Add an output mixer.
        synth.add(lineOut = new LineOut());

        modulator.output.connect(carrier.modulation);
        carrier.output.connect(0, lineOut.input, 0);
        carrier.output.connect(0, lineOut.input, 1);
        modulator.amplitude.setup(0.0, 1.0, 10.0);
        carrier.amplitude.setup(0.0, 0.25, 1.0);
        setupGUI();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        add(new JLabel("Show Phase Modulation in an AudioScope"), BorderLayout.NORTH);

        // Arrange the knob in a row.
        JPanel knobPanel = new JPanel();
        knobPanel.setLayout(new GridLayout(1, 0));

        knobPanel.add(setupPortKnob(modulator.frequency, "MFreq"));
        knobPanel.add(setupPortKnob(modulator.amplitude, "MAmp"));
        knobPanel.add(setupPortKnob(carrier.frequency, "CFreq"));
        knobPanel.add(setupPortKnob(carrier.amplitude, "CAmp"));
        add(knobPanel, BorderLayout.SOUTH);

        scope = new AudioScope(synth);
        scope.addProbe(carrier.output);
        scope.addProbe(modulator.output);
        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.getView().setControlsVisible(true);
        add(scope.getView(), BorderLayout.CENTER);
        scope.start();
        validate();
    }

    private RotaryTextController setupPortKnob(UnitInputPort port, String label) {
        DoubleBoundedRangeModel model = PortModelFactory.createExponentialModel(port);
        RotaryTextController knob = new RotaryTextController(model, 10);
        knob.setBorder(BorderFactory.createTitledBorder(label));
        knob.setTitle(label);
        return knob;
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        scope.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();
    }

    @Override
    public void stop() {
        scope.stop();
        synth.stop();
    }

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        HearSinePM applet = new HearSinePM();
        JAppletFrame frame = new JAppletFrame("Hear Phase Modulation", applet);
        frame.setSize(640, 400);
        frame.setVisible(true);
        frame.test();
    }

}
