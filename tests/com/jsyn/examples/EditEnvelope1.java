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
/** 
 * Test Envelope using Java Audio Synthesizer
 * Trigger attack or release portion.
 *
 * @author (C) 1997 Phil Burk
 */

package com.jsyn.examples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.swing.EnvelopeEditorPanel;
import com.jsyn.swing.EnvelopePoints;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;

public class EditEnvelope1 extends JApplet {
    private Synthesizer synth;
    private UnitOscillator osc;
    private LineOut lineOut;
    private SegmentedEnvelope envelope;
    private VariableRateDataReader envelopePlayer;

    final int MAX_FRAMES = 16;
    JButton hitme;
    JButton attackButton;
    JButton releaseButton;
    private EnvelopeEditorPanel envEditor;
    private EnvelopePoints points;

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        EditEnvelope1 applet = new EditEnvelope1();
        JAppletFrame frame = new JAppletFrame("Test SynthEnvelope", applet);
        frame.setSize(440, 200);
        frame.setVisible(true);
        frame.test();
    }

    /*
     * Setup synthesis.
     */
    @Override
    public void start() {
        setLayout(new BorderLayout());

        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(osc = new SawtoothOscillatorBL());
        // Add an envelope player.
        synth.add(envelopePlayer = new VariableRateMonoReader());

        envelope = new SegmentedEnvelope(MAX_FRAMES);

        // Add an output mixer.
        synth.add(lineOut = new LineOut());
        envelopePlayer.output.connect(osc.amplitude);
        // Connect the oscillator to the output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(hitme = new JButton("On"));
        hitme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                points.updateEnvelopeIfDirty(envelope);
                envelopePlayer.dataQueue.queueOn(envelope);
            }
        });

        bottomPanel.add(attackButton = new JButton("Off"));
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                points.updateEnvelopeIfDirty(envelope);
                envelopePlayer.dataQueue.queueOff(envelope);
            }
        });

        bottomPanel.add(releaseButton = new JButton("Queue"));
        releaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                points.updateEnvelopeIfDirty(envelope);
                envelopePlayer.dataQueue.queue(envelope);
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);
        lineOut.start();

        // Create vector of points for editor.
        points = new EnvelopePoints();
        points.setName(osc.amplitude.getName());

        // Setup initial envelope shape.
        points.add(0.5, 1.0);
        points.add(0.5, 0.2);
        points.add(0.5, 0.8);
        points.add(0.5, 0.0);
        points.updateEnvelope(envelope);

        // Add an envelope editor to the center of the panel.
        add("Center", envEditor = new EnvelopeEditorPanel(points, MAX_FRAMES));

        /* Synchronize Java display. */
        getParent().validate();
        getToolkit().sync();
    }

    @Override
    public void stop() {
        synth.stop();
    }
}
