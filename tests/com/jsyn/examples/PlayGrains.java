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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortModelFactory;
import com.jsyn.swing.RotaryTextController;
import com.jsyn.unitgen.ContinuousRamp;
import com.jsyn.unitgen.GrainFarm;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SampleGrainFarm;
import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveRecorder;

/**
 * Play with Granular Synthesis tools.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class PlayGrains extends JApplet {
    private static final long serialVersionUID = -8315903842197137926L;
    private Synthesizer synth;
    private LineOut lineOut;
    private AudioScope scope;
    private GrainFarm grainFarm;
    private ContinuousRamp ramp;
    private static final int NUM_GRAINS = 32;
    private FloatSample sample;
    private WaveRecorder recorder;
    private final static boolean useRecorder = false;

    private static final boolean useSample = false;
    // If you enable useSample then you will need to replace the file name below with a valid
    // file name on your computer.
    private File sampleFile = new File("/Users/phil/Music/samples/ChewyMonkeysWhistle.aiff");

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        PlayGrains applet = new PlayGrains();
        JAppletFrame frame = new JAppletFrame("PlayGrains", applet);
        frame.setSize(840, 500);
        frame.setVisible(true);
        frame.test();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        add(BorderLayout.NORTH,
                new JLabel("PlayGrains in an AudioScope - JSyn V" + synth.getVersion()));

        scope = new AudioScope(synth);

        // scope.addProbe( osc.output );
        scope.addProbe(grainFarm.output);

        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.getView().setControlsVisible(true);
        add(BorderLayout.CENTER, scope.getView());
        scope.start();

        // Arrange the knob in a row.
        JPanel knobPanel = new JPanel();
        knobPanel.setLayout(new GridLayout(1, 0));

        if (useSample) {
            SampleGrainFarm sampleGrainFarm = (SampleGrainFarm) grainFarm;
            knobPanel.add(setupLinearPortKnob(ramp.time, 0.001, 10.0, "Time"));
            knobPanel.add(setupLinearPortKnob(ramp.input, -1.0, 1.0, "Position"));
            knobPanel.add(setupLinearPortKnob(sampleGrainFarm.positionRange, 0.0, 0.5, "PosRange"));
        }
        knobPanel.add(setupPortKnob(grainFarm.density, 1.0, "Density"));
        knobPanel.add(setupPortKnob(grainFarm.rate, 4.0, "Rate"));
        knobPanel.add(setupPortKnob(grainFarm.rateRange, 3.0, "RateRange"));
        knobPanel.add(setupPortKnob(grainFarm.duration, 0.1, "Duration"));
        knobPanel.add(setupPortKnob(grainFarm.durationRange, 3.0, "DurRange"));
        knobPanel.add(setupPortKnob(grainFarm.amplitude, 6.0, "Amplitude"));
        knobPanel.add(setupPortKnob(grainFarm.amplitudeRange, 1.0, "AmpRange"));
        add(knobPanel, BorderLayout.SOUTH);

        validate();
    }

    private RotaryTextController setupLinearPortKnob(UnitInputPort port, double min, double max,
            String label) {
        port.setMinimum(min);
        port.setMaximum(max);

        DoubleBoundedRangeModel model = PortModelFactory.createLinearModel(port);
        RotaryTextController knob = new RotaryTextController(model, 10);
        knob.setBorder(BorderFactory.createTitledBorder(label));
        knob.setTitle(label);
        return knob;
    }

    private RotaryTextController setupPortKnob(UnitInputPort port, double max, String label) {
        port.setMinimum(0.0);
        port.setMaximum(max);

        DoubleBoundedRangeModel model = PortModelFactory.createExponentialModel(port);
        RotaryTextController knob = new RotaryTextController(model, 10);
        knob.setBorder(BorderFactory.createTitledBorder(label));
        knob.setTitle(label);
        return knob;
    }

    @Override
    public void start() {
        synth = JSyn.createSynthesizer();

        try {

            if (useRecorder) {
                File waveFile = new File("temp_recording.wav");
                // Record mono 16 bits.
                recorder = new WaveRecorder(synth, waveFile, 1);
                System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
            }

            if (useSample) {
                sample = SampleLoader.loadFloatSample(sampleFile);
                SampleGrainFarm sampleGrainFarm = new SampleGrainFarm();
                synth.add(ramp = new ContinuousRamp());
                sampleGrainFarm.setSample(sample);
                ramp.output.connect(sampleGrainFarm.position);
                grainFarm = sampleGrainFarm;
            } else {
                GrainFarm sampleGrainFarm = new GrainFarm();
                grainFarm = sampleGrainFarm;
            }

            synth.add(grainFarm);

            grainFarm.allocate(NUM_GRAINS);

            // Add an output so we can hear the grains.
            synth.add(lineOut = new LineOut());

            grainFarm.getOutput().connect(0, lineOut.input, 0);
            grainFarm.getOutput().connect(0, lineOut.input, 1);

            // Start synthesizer using default stereo output at 44100 Hz.
            synth.start();

            if (useRecorder) {
                grainFarm.output.connect(0, recorder.getInput(), 0);
                // When we start the recorder it will pull data from the
                // oscillator
                // and sweeper.
                recorder.start();
            }

            setupGUI();
            // We only need to start the LineOut. It will pull data from the
            // oscillator.
            lineOut.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        scope.stop();
        synth.stop();
    }

}
