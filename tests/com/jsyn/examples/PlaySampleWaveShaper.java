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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.data.Function;
import com.jsyn.unitgen.FunctionEvaluator;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.util.SampleLoader;

/**
 * Play a sample from a WAV file using JSyn.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlaySampleWaveShaper {
    private Synthesizer synth;
    private LineOut lineOut;

    private void test() {

        URL sampleFile;
        try {
            sampleFile = new URL("http://www.softsynth.com/samples/Clarinet.wav");
            // sampleFile = new URL("http://www.softsynth.com/samples/NotHereNow22K.wav");
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return;
        }

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

            if (sample.getChannelsPerFrame() != 1) {
                throw new RuntimeException("Can only use mono samples.");
            }

            System.out.println("eval -1.1 = " + sample.evaluate(-1.1));
            System.out.println("eval -1.0 = " + sample.evaluate(-1.0));
            System.out.println("eval 0.3 = " + sample.evaluate(0.3));
            System.out.println("eval 1.0 = " + sample.evaluate(1.0));
            System.out.println("eval 1.1 = " + sample.evaluate(1.1));

            FunctionEvaluator shaper = new FunctionEvaluator();
            shaper.function.set(sample);
            synth.add(shaper);

            shaper.output.connect(0, lineOut.input, 0);
            shaper.output.connect(0, lineOut.input, 1);

            SineOscillator osc = new SineOscillator();
            osc.frequency.set(0.2);
            osc.output.connect(shaper.input);
            synth.add(osc);

            // Start synthesizer using default stereo output at 44100 Hz.
            synth.start();

            // We only need to start the LineOut. It will pull data from the
            // sample player.
            lineOut.start();

            // Wait until the sample has finished playing.
            synth.sleepFor(5.0);

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new PlaySampleWaveShaper().test();
    }
}
