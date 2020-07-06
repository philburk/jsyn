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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a tone using a JSyn oscillator and process it using a custom unit generator.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlayCustomUnit {
    private Synthesizer synth;
    private UnitOscillator osc;
    private CustomCubeUnit cuber;
    private LineOut lineOut;

    private void test() {
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(osc = new SineOscillator());
        // Add a tone generator.
        synth.add(cuber = new CustomCubeUnit());
        // Add an output to the DAC.
        synth.add(lineOut = new LineOut());
        // Connect the oscillator to the cuber.
        osc.output.connect(0, cuber.input, 0);
        // Connect the cuber to the right output.
        cuber.output.connect(0, lineOut.input, 1);
        // Send the original to the left output for comparison.
        osc.output.connect(0, lineOut.input, 0);

        osc.frequency.set(240.0);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut.
        // It will pull data from the cuber and the oscillator.
        lineOut.start();
        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a few seconds.
            synth.sleepUntil(time + 10.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new PlayCustomUnit().test();
    }
}
