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

package com.jsyn.unitgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.util.WaveRecorder;

/**
 * Measure actual frequency as a function of input frequency and Q.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class RecordMoogFilter extends JApplet {
    private final static boolean SWEEP_Q = false;
    private final static boolean SWEEP_FREQUENCY = true;
    private final static int NUM_STEPS = 11;

    private final static double MIN_Q = 0.0;
    private final static double DEFAULT_Q = 9.0;
    private final static double MAX_Q = 10.0;

    private final static double MIN_FREQUENCY = 100.0;
    private final static double DEFAULT_FREQUENCY = 500.0;
    private final static double MAX_FREQUENCY = 4000.0;

    private Synthesizer synth;
    private WhiteNoise source;
    private SineOscillator reference;
    private FilterFourPoles filterMoog;
    private LineOut lineOut;
    private WaveRecorder recorder;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        synth.add(source = new WhiteNoise());
        synth.add(filterMoog = new FilterFourPoles());
        synth.add(reference = new SineOscillator());
        synth.add(lineOut = new LineOut());

        source.output.connect(filterMoog.input);
        reference.output.connect(0, lineOut.input, 0);
        filterMoog.output.connect(0, lineOut.input, 1);

        reference.amplitude.set(0.5);
        source.amplitude.set(0.5);
        filterMoog.frequency.set(DEFAULT_FREQUENCY);
        filterMoog.Q.set(DEFAULT_Q);

        File waveFile = new File("temp_recording.wav");
        // Default is stereo, 16 bits.
        try {
            recorder = new WaveRecorder(synth, waveFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        lineOut.start();

        reference.output.connect(0, recorder.getInput(), 0);
        filterMoog.output.connect(0, recorder.getInput(), 1);
        recorder.start();
    }

    @Override
    public void stop() {
        if (recorder != null) {
            recorder.stop();
            try {
                recorder.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        lineOut.stop();
        synth.stop();
    }

    public void test() {
        init();
        start();
        try {
            calibrate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    private void calibrate() throws InterruptedException {
        synth.sleepFor(0.2);
        double freq = SWEEP_FREQUENCY ? MIN_FREQUENCY : DEFAULT_FREQUENCY;
        double q = SWEEP_Q ? MIN_Q : DEFAULT_Q;
        double stepQ = (MAX_Q - MIN_Q) / (NUM_STEPS - 1);
        double scaleFrequency = Math.pow((MAX_FREQUENCY / MIN_FREQUENCY), (1.0 / (NUM_STEPS - 1)));
        System.out.printf("freq, q, measured\n");
        for (int i = 0; i < NUM_STEPS; i++) {
            double refAmp = reference.amplitude.get();
            reference.amplitude.set(0.0);
            synth.sleepFor(0.1);
            reference.amplitude.set(refAmp);

            System.out.printf("%8.2f, %6.3f, \n", freq, q);
            filterMoog.frequency.set(freq);
            reference.frequency.set(freq);
            filterMoog.Q.set(q);

            synth.sleepFor(2.0);

            if (SWEEP_FREQUENCY) {
                freq *= scaleFrequency;
            }
            if (SWEEP_Q) {
                q += stepQ;
            }
        }
    }

    public static void main(String args[]) {
        new RecordMoogFilter().test();
    }

}
