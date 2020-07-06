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
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.AsymptoticRamp;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Pan;
import com.jsyn.unitgen.SawtoothOscillatorDPW;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitSource;

/**
 * Make a bunch of oscillators that swarm around a moving frequency.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class SwarmOfOscillators {
    private Synthesizer synth;
    Follower[] followers;
    SineOscillator lfo;
    LineOut lineOut;
    private Add tiePoint;
    private static final int NUM_FOLLOWERS = 30;

    class Follower extends Circuit implements UnitSource {
        UnitOscillator osc;
        AsymptoticRamp lag;
        Pan panner;

        Follower() {
            // Add a tone generator.
            add(osc = new SawtoothOscillatorDPW());
            osc.amplitude.set(0.03);

            // Use a lag to smoothly change frequency.
            add(lag = new AsymptoticRamp());
            double hlife = 0.01 + (Math.random() * 0.9);
            lag.halfLife.set(hlife);

            // Set left/right pan randomly between -1.0 and +1.0.
            add(panner = new Pan());
            panner.pan.set((Math.random() * 2.0) - 1.0);

            // Track the frequency coming through the tiePoint.
            tiePoint.output.connect(lag.input);
            // Add the LFO offset.
            lfo.output.connect(lag.input);

            lag.output.connect(osc.frequency);

            // Connect the oscillator to the left and right audio output.
            osc.output.connect(panner.input);
        }

        @Override
        public UnitOutputPort getOutput() {
            return panner.output;
        }
    }

    private void test() {
        synth = JSyn.createSynthesizer();

        // Add an output mixer.
        synth.add(lineOut = new LineOut());

        // Add a unit just to distribute the control frequency.
        synth.add(tiePoint = new Add());
        synth.add(lfo = new SineOscillator());
        lfo.amplitude.set(40.0);
        lfo.frequency.set(2.3);

        followers = new Follower[NUM_FOLLOWERS];
        for (int i = 0; i < followers.length; i++) {
            Follower follower = new Follower();
            synth.add(follower);

            // Every follower can connect directly to the lineOut because all input ports are
            // mixers.
            follower.getOutput().connect(0, lineOut.input, 0);
            follower.getOutput().connect(1, lineOut.input, 1);

            followers[i] = follower;
        }

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Advance to a near future time so we have a clean start.
        double duration = 0.9;
        double time = timeNow + duration;
        double freq = 400.0; // hertz
        tiePoint.inputA.set(freq, time);

        // Randomly change the target frequency for the followers.
        try {
            for (int i = 0; i < 20; i++) {
                // Schedule this to happen a bit later.
                time += duration;
                freq = 200.0 + (Math.random() * 500.0);
                tiePoint.inputA.set(freq, time);

                // Sleep while the sound is being generated in the background
                // thread.
                synth.sleepUntil(time - 0.2);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.format("CPU usage = %4.2f%c\n", synth.getUsage() * 100, '%');

        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new SwarmOfOscillators().test();
    }
}
