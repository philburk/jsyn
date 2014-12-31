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

package com.jsyn.util;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;

/**
 * Play a sample from a WAV file using JSyn.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class DebugSampleLoader {
    private Synthesizer synth;
    private VariableRateDataReader samplePlayer;
    private LineOut lineOut;

    private void test() throws IOException {
        // File sampleFile = new File("samples/cello_markers.wav");
        // File sampleFile = new File("samples/Piano_A440_PT.aif");
        File sampleFile = new File("samples/sine_400_loop_i16.wav");
        // File sampleFile = new File("samples/TwoDiffPitchedSines_F32_PT.wav");
        // File sampleFile = new File("samples/sine_400_u8.aif");
        // File sampleFile = new File("samples/sine_400_s8.aif");
        // File sampleFile = new File("samples/sine_400_ulaw.aif");
        // File sampleFile = new File("samples/sine_400_ulaw.wav");

        // File sampleFile = new File("samples/aaClarinet.wav");
        // File sampleFile = new File("samples/sine_400_mono.wav");
        // File sampleFile = new File("samples/sine_200_300_i16.wav");
        // File sampleFile = new File("samples/sine_200_300_i24.wav");
        // File sampleFile = new File("samples/M1F1-int16-AFsp.wav");
        // File sampleFile = new File("samples/M1F1-int24-AFsp.wav");
        // File sampleFile = new File("samples/M1F1-float32-AFsp.wav");
        // File sampleFile = new File("samples/M1F1-int16WE-AFsp.wav");
        // File sampleFile = new File("samples/M1F1-int24WE-AFsp.wav");
        // File sampleFile = new File("samples/M1F1-float32WE-AFsp.wav");
        // File sampleFile = new File("samples/sine_200_300_i16.aif");
        // File sampleFile = new File("samples/sine_200_300_f32.wavex");
        // File sampleFile = new File("samples/Sine32bit.aif");
        // File sampleFile = new File("samples/Sine32bit.wav");
        // File sampleFile = new File("samples/smartCue.wav");

        // URL sampleFile = new URL("http://www.softsynth.com/samples/Clarinet.wav");

        synth = JSyn.createSynthesizer();

        FloatSample sample;
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

            // Start synthesizer using default stereo output at 44100 Hz.
            synth.start();

            samplePlayer.rate.set(sample.getFrameRate());

            // We only need to start the LineOut. It will pull data from the
            // sample player.
            lineOut.start();

            // We can simply queue the entire file.
            // Or if it has a loop we can play the loop for a while.
            if (sample.getSustainBegin() < 0) {
                System.out.println("queue the sample");
                samplePlayer.dataQueue.queue(sample);
            } else {
                System.out.println("queueOn the sample");
                samplePlayer.dataQueue.queueOn(sample);
                synth.sleepFor(8.0);
                System.out.println("queueOff the sample");
                samplePlayer.dataQueue.queueOff(sample);
            }

            // Wait until the sample has finished playing.
            do {
                synth.sleepFor(1.0);
            } while (samplePlayer.dataQueue.hasMore());

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        try {
            new DebugSampleLoader().test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
