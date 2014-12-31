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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.ports.QueueDataCommand;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.DoubleBoundedRangeSlider;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.jsyn.util.SampleLoader;

/**
 * Play a sample from a WAV file using JSyn. Use a crossfade to play a loop at an arbitrary
 * position.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlaySampleCrossfade extends JApplet {
    private static final double LOOP_START_FRACTION = 0.2;
    private Synthesizer synth;
    private VariableRateDataReader samplePlayer;
    private LineOut lineOut;
    private FloatSample sample;
    private DoubleBoundedRangeModel rangeModelSize;
    private DoubleBoundedRangeModel rangeModelCrossfade;
    private int loopStartFrame;

    @Override
    public void init() {

        URL sampleFile;
        try {
            sampleFile = new URL("http://www.softsynth.com/samples/Clarinet.wav");
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return;
        }

        synth = JSyn.createSynthesizer(AudioDeviceFactory.createAudioDeviceManager(true));

        try {
            // Add an output mixer.
            synth.add(lineOut = new LineOut());

            // Load the sample and display its properties.
            SampleLoader.setJavaSoundPreferred(false);
            sample = SampleLoader.loadFloatSample(sampleFile);
            System.out.println("Sample has: channels  = " + sample.getChannelsPerFrame());
            System.out.println("            frames    = " + sample.getNumFrames());
            System.out.println("            rate      = " + sample.getFrameRate());
            System.out.println("            loopStart = " + sample.getSustainBegin());
            System.out.println("            loopEnd   = " + sample.getSustainEnd());

            if (sample.getChannelsPerFrame() == 1) {
                synth.add(samplePlayer = new VariableRateMonoReader());
                samplePlayer.output.connect(0, lineOut.input, 0);
            } else if (sample.getChannelsPerFrame() == 2) {
                synth.add(samplePlayer = new VariableRateStereoReader());
                samplePlayer.output.connect(0, lineOut.input, 0);
                samplePlayer.output.connect(1, lineOut.input, 1);
            } else {
                throw new RuntimeException("Can only play mono or stereo samples.");
            }

            samplePlayer.rate.set(sample.getFrameRate());

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // Start at arbitrary position near beginning of sample.
        loopStartFrame = (int) (sample.getNumFrames() * LOOP_START_FRACTION);

        // Arrange the faders in a stack.
        setLayout(new GridLayout(0, 1));

        samplePlayer.rate.setup(4000.0, sample.getFrameRate(), sample.getFrameRate() * 2.0);
        add(PortControllerFactory.createExponentialPortSlider(samplePlayer.rate));

        // Use fader to select arbitrary loop size.
        rangeModelSize = new DoubleBoundedRangeModel("LoopSize", 10000, 0.01,
                (1.0 - LOOP_START_FRACTION), 0.5);
        rangeModelSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                queueNewLoop();
            }

        });
        add(new DoubleBoundedRangeSlider(rangeModelSize, 3));

        // Use fader to set the size of the crossfade region.
        rangeModelCrossfade = new DoubleBoundedRangeModel("Crossfade", 1000, 0.0, 1000.0, 0.0);
        rangeModelCrossfade.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                queueNewLoop();
            }

        });

        add(new DoubleBoundedRangeSlider(rangeModelCrossfade, 3));

        validate();
    }

    private void queueNewLoop() {
        int loopSize = (int) (sample.getNumFrames() * rangeModelSize.getDoubleValue());
        if ((loopStartFrame + loopSize) > sample.getNumFrames()) {
            loopSize = sample.getNumFrames() - loopStartFrame;
        }
        int crossFadeSize = (int) (rangeModelCrossfade.getDoubleValue());

        // For complex queuing operations, create a command and then customize it.
        QueueDataCommand command = samplePlayer.dataQueue.createQueueDataCommand(sample,
                loopStartFrame, loopSize);
        command.setNumLoops(-1);
        command.setSkipIfOthers(true);
        command.setCrossFadeIn(crossFadeSize);

        System.out.println("Queue: " + loopStartFrame + ", #" + loopSize + ", X=" + crossFadeSize);
        synth.queueCommand(command);
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // Start the LineOut. It will pull data from the oscillator.
        lineOut.start();

        // Queue attack portion of sample.
        samplePlayer.dataQueue.queue(sample, 0, loopStartFrame);
        queueNewLoop();
    }

    @Override
    public void stop() {
        synth.stop();
        synth.stop();
    }

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        PlaySampleCrossfade applet = new PlaySampleCrossfade();
        JAppletFrame frame = new JAppletFrame("PlaySampleCrossfade", applet);
        frame.setSize(440, 300);
        frame.setVisible(true);
        frame.test();
    }

}
