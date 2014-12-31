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

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.util.WaveRecorder;

/**
 * Modulate the amplitude of an oscillator using a segmented envelope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlaySegmentedEnvelope {
    private Synthesizer synth;
    private UnitOscillator osc;
    private LineOut lineOut;
    private SegmentedEnvelope envelope;
    private VariableRateDataReader envelopePlayer;
    private WaveRecorder recorder;
    private final static boolean useRecorder = true;

    private void test() throws IOException {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(osc = new SawtoothOscillatorBL());
        // Add an envelope player.
        synth.add(envelopePlayer = new VariableRateMonoReader());

        if (useRecorder) {
            File waveFile = new File("temp_recording.wav");
            // Default is stereo, 16 bits.
            recorder = new WaveRecorder(synth, waveFile);
            System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
        }

        // Create an envelope consisting of (duration,value) pairs.
        double[] pairs = {
                0.1, 1.0, 0.2, 0.3, 0.6, 0.0
        };
        envelope = new SegmentedEnvelope(pairs);

        // Add an output mixer.
        synth.add(lineOut = new LineOut());
        envelopePlayer.output.connect(osc.amplitude);
        // Connect the oscillator to the output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        if (useRecorder) {
            osc.output.connect(0, recorder.getInput(), 0);
            envelopePlayer.output.connect(0, recorder.getInput(), 1);
            // When we start the recorder it will pull data from the oscillator
            // and sweeper.
            recorder.start();
        }

        // We only need to start the LineOut. It will pull data from the other
        // units.
        lineOut.start();

        try {
            // ---------------------------------------------
            // Queue the entire envelope to play once.
            envelopePlayer.dataQueue.queue(envelope);
            synth.sleepFor(2.0);

            // ---------------------------------------------
            // Queue the attack, then sustain for a while, then queue the
            // release.
            osc.frequency.set(750.0);
            envelopePlayer.dataQueue.queue(envelope, 0, 2); // attack
            synth.sleepFor(2.0);
            envelopePlayer.dataQueue.queue(envelope, 2, 1); // release
            synth.sleepFor(2.0);

            // ---------------------------------------------
            // Queue the attack, then sustain for a while, then queue the
            // release. But this time use the sustain loop points.
            osc.frequency.set(950.0);
            // For noteOn, we want to play frames 0 and 1 then stop before 2.
            envelope.setSustainBegin(2);
            envelope.setSustainEnd(2);
            envelopePlayer.dataQueue.queueOn(envelope); // attack
            synth.sleepFor(2.0);
            envelopePlayer.dataQueue.queueOff(envelope); // release
            synth.sleepFor(2.0);

            // ---------------------------------------------
            // Queue the entire envelope to play 4 times (3 loops back).
            osc.frequency.set(350.0);
            envelopePlayer.dataQueue.queueLoop(envelope, 0, envelope.getNumFrames(), 3);
            synth.sleepFor(5.0);

            // ---------------------------------------------
            // Queue the entire envelope as a repeating loop.
            // It will loop until something else is queued.
            osc.frequency.set(450.0);
            envelopePlayer.dataQueue.queueLoop(envelope, 0, envelope.getNumFrames());
            envelopePlayer.rate.set(3.0);
            synth.sleepFor(5.0);
            // Queue last frame to stop the looping.
            envelopePlayer.dataQueue.queue(envelope, envelope.getNumFrames() - 1, 1);
            synth.sleepFor(1.0);

            if (recorder != null) {
                recorder.stop();
                recorder.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        try {
            new PlaySegmentedEnvelope().test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
