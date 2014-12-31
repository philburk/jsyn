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
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a enharmonic sine tones using JSyn oscillators.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlayPartials {
    private Synthesizer synth;
    private UnitOscillator[] osc;
    private Multiply[] multipliers;
    private LinearRamp ramp;
    private LineOut lineOut;
    private double[] amps = {
            0.2, 0.1, 0.3, 0.4
    };
    private double[] ratios = {
            1.0, Math.sqrt(2.0), Math.E, Math.PI
    };

    private void test() {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        // Add a stereo audio output unit.
        synth.add(lineOut = new LineOut());
        synth.add(ramp = new LinearRamp());

        // Add a tone generator.
        osc = new SineOscillator[amps.length];
        multipliers = new Multiply[ratios.length];

        for (int i = 0; i < osc.length; i++) {
            // Create unit generators and store them in arrays.
            synth.add(osc[i] = new SineOscillator());
            synth.add(multipliers[i] = new Multiply());

            // Connect each oscillator to both channels of the output.
            // They will be mixed automatically.
            osc[i].output.connect(0, lineOut.input, 0);
            osc[i].output.connect(0, lineOut.input, 1);

            // Use a multiplier to scale the output of the ramp.
            // output = inputA * inputB
            ramp.output.connect(multipliers[i].inputA);
            multipliers[i].output.connect(osc[i].frequency);
            multipliers[i].inputB.set(ratios[i]);

            osc[i].amplitude.set(amps[i]);
        }

        // start ramping up
        ramp.current.set(100.0);
        ramp.time.set(3.0);
        ramp.input.set(700.0);

        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        System.out.println("You should now be hearing a sine wave. ---------");

        // Sleep while the sound is generated in the background.
        try {
            // Sleep for a few seconds.
            synth.sleepFor(4.0);
            // ramp down
            ramp.input.set(100.0);
            synth.sleepFor(4.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stop playing. -------------------");
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        System.out.println("Java version = " + System.getProperty("java.version"));
        new PlayPartials().test();
    }
}
