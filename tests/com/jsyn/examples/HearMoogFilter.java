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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.scope.AudioScope;
import com.jsyn.scope.AudioScopeProbe;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.FilterFourPoles;
import com.jsyn.unitgen.FilterLowPass;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a sawtooth through a 4-pole filter.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class HearMoogFilter extends JApplet {
    private Synthesizer synth;
    private UnitOscillator oscillator;
    private FilterFourPoles filterMoog;
    private FilterLowPass filterBiquad;
    private LinearRamp rampCutoff;
    private PassThrough tieQ;
    private PassThrough tieCutoff;
    private PassThrough mixer;
    private LineOut lineOut;

    private AudioScope scope;
    private boolean useCutoffRamp = false;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();
        synth.add(oscillator = new SawtoothOscillatorBL());
        synth.add(rampCutoff = new LinearRamp());
        synth.add(tieQ = new PassThrough());
        synth.add(tieCutoff = new PassThrough());
        synth.add(filterMoog = new FilterFourPoles());
        synth.add(filterBiquad = new FilterLowPass());
        synth.add(mixer = new PassThrough());
        synth.add(lineOut = new LineOut());

        oscillator.output.connect(filterMoog.input);
        oscillator.output.connect(filterBiquad.input);
        if (useCutoffRamp) {
            rampCutoff.output.connect(filterMoog.frequency);
            rampCutoff.output.connect(filterBiquad.frequency);
            rampCutoff.time.set(0.000);
        } else {
            tieCutoff.output.connect(filterMoog.frequency);
            tieCutoff.output.connect(filterBiquad.frequency);
        }
        tieQ.output.connect(filterMoog.Q);
        tieQ.output.connect(filterBiquad.Q);
        filterMoog.output.connect(mixer.input);
        mixer.output.connect(0, lineOut.input, 0);
        mixer.output.connect(0, lineOut.input, 1);

        filterBiquad.amplitude.set(0.1);
        oscillator.frequency.setup(50.0, 130.0, 3000.0);
        oscillator.amplitude.setup(0.0, 0.336, 1.0);
        rampCutoff.input.setup(filterMoog.frequency);
        tieCutoff.input.setup(filterMoog.frequency);
        tieQ.input.setup(0.1, 0.7, 10.0);
        setupGUI();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        add(new JLabel("Sawtooth through a \"Moog\" style filter."), BorderLayout.NORTH);

        JPanel rackPanel = new JPanel();
        rackPanel.setLayout(new BoxLayout(rackPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        ButtonGroup cbg = new ButtonGroup();
        JRadioButton radioButton = new JRadioButton("Moog", true);
        cbg.add(radioButton);
        radioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mixer.input.disconnectAll();
                filterMoog.output.connect(mixer.input);
            }
        });
        buttonPanel.add(radioButton);
        radioButton = new JRadioButton("Biquad", false);
        cbg.add(radioButton);
        radioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mixer.input.disconnectAll();
                filterBiquad.output.connect(mixer.input);
            }
        });
        buttonPanel.add(radioButton);

        /*
         * buttonPanel.add( new JLabel("Show:") ); cbg = new ButtonGroup(); radioButton = new
         * JRadioButton( "Waveform", true ); cbg.add( radioButton ); radioButton.addItemListener(
         * new ItemListener() { public void itemStateChanged( ItemEvent e ) { scope.setViewMode(
         * AudioScope.ViewMode.WAVEFORM ); } } ); buttonPanel.add( radioButton ); radioButton = new
         * JRadioButton( "Spectrum", true ); cbg.add( radioButton ); radioButton.addItemListener(
         * new ItemListener() { public void itemStateChanged( ItemEvent e ) { scope.setViewMode(
         * AudioScope.ViewMode.SPECTRUM ); } } ); buttonPanel.add( radioButton );
         */

        rackPanel.add(buttonPanel);

        // Arrange the knobs in a row.
        JPanel knobPanel = new JPanel();
        knobPanel.setLayout(new GridLayout(1, 0));

        knobPanel.add(setupPortKnob(oscillator.frequency, "OscFreq"));
        knobPanel.add(setupPortKnob(oscillator.amplitude, "OscAmp"));

        if (useCutoffRamp) {
            knobPanel.add(setupPortKnob(rampCutoff.input, "Cutoff"));
        } else {
            knobPanel.add(setupPortKnob(tieCutoff.input, "Cutoff"));
        }
        knobPanel.add(setupPortKnob(tieQ.input, "Q"));
        rackPanel.add(knobPanel);
        add(rackPanel, BorderLayout.SOUTH);

        scope = new AudioScope(synth);
        scope.addProbe(oscillator.output);
        scope.addProbe(filterMoog.output);
        scope.addProbe(filterBiquad.output);
        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.getView().setControlsVisible(false);
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
        HearMoogFilter applet = new HearMoogFilter();
        JAppletFrame frame = new JAppletFrame("Hear Moog Style Filter", applet);
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.test();
    }

}
