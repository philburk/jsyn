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
/** 
 * Test recording to disk in non-real-time.
 * Play several frequencies of a sine wave.
 * Save data in a WAV file format.
 *
 * @author (C) 2010 Phil Burk
 */

package com.jsyn.examples;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.ExponentialRamp;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.util.WaveRecorder;

public class RecordSineSweep {
    final static double SONG_DURATION = 4.0;
    private Synthesizer synth;
    private UnitOscillator leftOsc;
    private UnitOscillator rightOsc;
    private ExponentialRamp sweeper;
    private LineOut lineOut;
    private WaveRecorder recorder;
    private final static boolean useRecorder = true;

    private void test() throws IOException {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);

        if (useRecorder) {
            File waveFile = new File("temp_recording.wav");
            // Default is stereo, 16 bits.
            recorder = new WaveRecorder(synth, waveFile);
            System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
        }
        // Add some tone generators.
        synth.add(leftOsc = new SineOscillator());
        synth.add(rightOsc = new SawtoothOscillatorBL());

        // Add a controller that will sweep up.
        synth.add(sweeper = new ExponentialRamp());
        // Add an output unit.
        synth.add(lineOut = new LineOut());

        sweeper.current.set(50.0);
        sweeper.input.set(1400.0);
        sweeper.time.set(SONG_DURATION);
        sweeper.output.connect(leftOsc.frequency);
        sweeper.output.connect(rightOsc.frequency);

        // Connect the oscillator to the left and right audio output.
        leftOsc.output.connect(0, lineOut.input, 0);
        rightOsc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        if (useRecorder) {
            leftOsc.output.connect(0, recorder.getInput(), 0);
            rightOsc.output.connect(0, recorder.getInput(), 1);
            // When we start the recorder it will pull data from the oscillator
            // and sweeper.
            recorder.start();
        }

        // We also need to start the LineOut if we want to hear it now.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Sleep while the sound is being generated in the background thread.
        try {
            synth.sleepUntil(timeNow + SONG_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Test stopping and restarting a recorder. This will cause a pop.
        if (recorder != null) {
            System.out.println("Stop and restart recorder.");
            recorder.stop();
        }
        sweeper.input.set(100.0);
        timeNow = synth.getCurrentTime();
        if (recorder != null) {
            recorder.start();
        }

        // Sleep while the sound is being generated in the background thread.
        try {
            synth.sleepUntil(timeNow + SONG_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (recorder != null) {
            recorder.stop();
            recorder.close();
        }
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        try {
            new RecordSineSweep().test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
