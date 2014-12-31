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
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JLabel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Display waveforms using the AudioScope. The frequency of the oscillators is modulated by an LFO.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class ShowWaves extends JApplet {
    private static final long serialVersionUID = -8315903842197137926L;
    private Synthesizer synth;
    private UnitOscillator lfo;
    private Add adder;
    private ArrayList<UnitOscillator> oscillators = new ArrayList<UnitOscillator>();
    private LineOut lineOut;
    private AudioScope scope;

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        ShowWaves applet = new ShowWaves();
        JAppletFrame frame = new JAppletFrame("ShowWaves", applet);
        frame.setSize(640, 300);
        frame.setVisible(true);
        frame.test();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        add(BorderLayout.NORTH, new JLabel("ShowWaves in an AudioScope Mod001"));

        scope = new AudioScope(synth);
        for (UnitOscillator osc : oscillators) {
            scope.addProbe(osc.output);
        }
        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.start();

        // Turn on the gain and trigger control GUI.
        scope.getView().setControlsVisible(true);
        add(BorderLayout.CENTER, scope.getView());
        validate();
    }

    @Override
    public void start() {
        synth = JSyn.createSynthesizer();

        // Add an LFO.
        synth.add(lfo = new SineOscillator());
        synth.add(adder = new Add());

        // Add an output so we can hear the oscillators.
        synth.add(lineOut = new LineOut());

        lfo.frequency.set(0.1);
        lfo.amplitude.set(200.0);
        adder.inputB.set(400.0);
        lfo.output.connect(adder.inputA);

        oscillators.add(new SawtoothOscillatorBL());
        oscillators.add(new SineOscillator());
        oscillators.add(new TriangleOscillator());
        for (UnitOscillator osc : oscillators) {
            synth.add(osc);
            adder.output.connect(osc.frequency);
            osc.output.connect(0, lineOut.input, 0);
            osc.amplitude.set(0.2);
        }

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // Start lineOut so it can pull data from other units.
        lineOut.start();
        setupGUI();

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
