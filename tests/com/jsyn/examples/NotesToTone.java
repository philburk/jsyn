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
 * If you play notes fast enough they become a tone.
 * 
 * Play a sine wave modulated by an envelope.
 * Speed up the envelope until it is playing at audio rate.
 * Slow down the oscillator until it becomes an LFO amp modulator.
 * Use a LatchZeroCrossing to stop at the end of a sine wave cycle when we are finished.
 *
 * @author Phil Burk, (C) 2010 Mobileer Inc
 */

package com.jsyn.examples;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.ExponentialRamp;
import com.jsyn.unitgen.LatchZeroCrossing;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.util.WaveRecorder;

/**
 * When notes speed up they can become a new tone. <br>
 * Multiply an oscillator and an envelope. Speed up the envelope until it becomes a tone. Slow down
 * the oscillator until it acts like an envelope. Write the resulting audio to a WAV file.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */

public class NotesToTone {
    private final static double SONG_AMPLITUDE = 0.7;
    private final static double INTRO_DURATION = 2.0;
    private final static double OUTRO_DURATION = 2.0;
    private final static double RAMP_DURATION = 20.0;
    private final static double LOW_FREQUENCY = 1.0;
    private final static double HIGH_FREQUENCY = 800.0;

    private final static boolean useRecorder = true;
    private WaveRecorder recorder;

    private Synthesizer synth;
    private ExponentialRamp envSweeper;
    private ExponentialRamp oscSweeper;
    private VariableRateDataReader envelopePlayer;
    private UnitOscillator osc;
    private LatchZeroCrossing latch;
    private LineOut lineOut;
    private SegmentedEnvelope envelope;

    private void play() throws IOException {
        synth = JSyn.createSynthesizer();
        synth.setRealTime(true);

        if (useRecorder) {
            File waveFile = new File("notes_to_tone.wav");
            // Default is stereo, 16 bits.
            recorder = new WaveRecorder(synth, waveFile, 1);
            System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
        }

        createUnits();

        connectUnits();

        setupEnvelope();

        osc.amplitude.set(SONG_AMPLITUDE);

        // Ramp the rate of the envelope up until it becomes an audible tone.
        envSweeper.current.set(LOW_FREQUENCY);
        envSweeper.input.set(LOW_FREQUENCY);
        envSweeper.time.set(RAMP_DURATION);

        // Ramp the rate of the oscillator down until it becomes an LFO.
        oscSweeper.current.set(HIGH_FREQUENCY);
        oscSweeper.input.set(HIGH_FREQUENCY);
        oscSweeper.time.set(RAMP_DURATION);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        // When we start the recorder it will pull data from the oscillator and
        // sweeper.
        if (recorder != null) {
            recorder.start();
        }

        // We also need to start the LineOut if we want to hear it now.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Schedule start of ramps.
        double songDuration = INTRO_DURATION + RAMP_DURATION + OUTRO_DURATION;
        envSweeper.input.set(HIGH_FREQUENCY, timeNow + INTRO_DURATION);
        oscSweeper.input.set(LOW_FREQUENCY, timeNow + INTRO_DURATION);

        // Arm zero crossing latch
        latch.gate.set(0.0, timeNow + songDuration);

        // Sleep while the sound is being generated in the background thread.
        try {
            synth.sleepUntil(timeNow + songDuration + 2.0);
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

    private void createUnits() {
        // Add a tone generators.
        synth.add(osc = new SineOscillator());
        // Add a controller that will sweep the envelope rate up.
        synth.add(envSweeper = new ExponentialRamp());
        // Add a controller that will sweep the oscillator down.
        synth.add(oscSweeper = new ExponentialRamp());

        synth.add(latch = new LatchZeroCrossing());
        // Add an output unit.
        synth.add(lineOut = new LineOut());

        // Add an envelope player.
        synth.add(envelopePlayer = new VariableRateMonoReader());
    }

    private void connectUnits() {
        oscSweeper.output.connect(osc.frequency);
        osc.output.connect(latch.input);
        // Latch when sine LFO crosses zero.
        latch.output.connect(envelopePlayer.amplitude);

        envSweeper.output.connect(envelopePlayer.rate);

        // Connect the envelope player to the audio output.
        envelopePlayer.output.connect(0, lineOut.input, 0);
        // crossFade.output.connect( 0, lineOut.input, 1 );

        if (recorder != null) {
            envelopePlayer.output.connect(0, recorder.getInput(), 0);
            // crossFade.output.connect( 0, recorder.getInput(), 1 );
        }
    }

    private void setupEnvelope() {
        // Setup envelope. The envelope has a total duration of 1.0 seconds.
        // Values are (duration,target) pairs.
        double[] pairs = new double[5 * 2 * 2];
        int i = 0;
        // duration, target for delay
        pairs[i++] = 0.15;
        pairs[i++] = 0.0;
        // duration, target for attack
        pairs[i++] = 0.05;
        pairs[i++] = 1.0;
        // duration, target for release
        pairs[i++] = 0.1;
        pairs[i++] = 0.6;
        // duration, target for sustain
        pairs[i++] = 0.1;
        pairs[i++] = 0.6;
        // duration, target for release
        pairs[i++] = 0.1;
        pairs[i++] = 0.0;
        // Create mirror image of this envelope.
        int halfLength = i;
        while (i < pairs.length) {
            pairs[i] = pairs[i - halfLength];
            i++;
            pairs[i] = pairs[i - halfLength] * -1.0;
            i++;
        }
        envelope = new SegmentedEnvelope(pairs);

        envelopePlayer.dataQueue.queueLoop(envelope, 0, envelope.getNumFrames());
    }

    public static void main(String[] args) {
        try {
            new NotesToTone().play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
