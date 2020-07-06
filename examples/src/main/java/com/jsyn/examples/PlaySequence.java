/*
 * Copyright 2009 Phil Burk, Mobileer Inc
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
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Use time stamps to change the frequency of an oscillator at precise times.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class PlaySequence {
    Synthesizer synth;
    UnitOscillator osc;
    LineOut lineOut;

    private void test() {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();

        // Add a tone generator.
        synth.add(osc = new SawtoothOscillatorBL());
        // Add an output mixer.
        synth.add(lineOut = new LineOut());

        // Connect the oscillator to the left and right audio output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Advance to a near future time so we have a clean start.
        double time = timeNow + 0.5;
        double freq = 400.0; // hertz
        osc.frequency.set(freq, time);

        // Schedule this to happen a bit later.
        time += 0.5;
        freq *= 1.5; // up a perfect fifth
        osc.frequency.set(freq, time);

        time += 0.5;
        freq *= 4.0 / 5.0; // down a major third
        osc.frequency.set(freq, time);

        // Sleep while the sound is being generated in the background thread.
        try {
            synth.sleepUntil(time + 0.5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new PlaySequence().test();
    }
}
