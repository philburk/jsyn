/*
 * Copyright 2022 Phil Burk, Mobileer Inc
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
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.ImpulseOscillator;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PeakFollower;
import com.jsyn.unitgen.PinkNoise;
import com.jsyn.unitgen.PlateReverb;
import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.WhiteNoise;
import com.softsynth.math.AudioMath;

/**
 * Measure the decay time of a PlateReverb tail..
 */
public class MeasurePlateReverb {

    private double measure(double size, double time, double damping) throws InterruptedException {
        // Create a context for the synthesizer.
        Synthesizer synth = JSyn.createSynthesizer();
        synth.setRealTime(false);

        // Add a signal source.
        WhiteNoise source = new WhiteNoise();
        PlateReverb reverb = new PlateReverb(size);
        PeakFollower peak = new PeakFollower();
        LineOut lineOut = new LineOut();

        synth.add(source);
        synth.add(peak);
        synth.add(reverb);
        synth.add(lineOut);

        source.amplitude.set(1.0);
        peak.halfLife.set(0.01);
        reverb.time.set(time);
        reverb.damping.set(damping);

        source.output.connect(reverb.input);
        reverb.output.connect(peak.input);
        peak.output.connect(0, lineOut.input, 0);
        reverb.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        lineOut.start();

        // Sleep while the sound is generated in the background.
        double rt60 = 0.0;
        final double REFERENCE_DB = -60.0;
        final double TARGET_DB = -30.0;
        synth.sleepFor(1.0);
        double original = peak.output.getValue();
        source.amplitude.set(0.0);
        double startTime = synth.getCurrentTime();
//        System.out.printf("  time, ratio, db\n");
        double db = 1.0;
        double elapsed;
        int count = 0;
        do {
            synth.sleepUntil(startTime + (count++ * 0.1));
            double level = peak.output.getValue();
            elapsed =  synth.getCurrentTime() - startTime;
            double ratio = level / original;
            db = AudioMath.amplitudeToDecibels(ratio);
//                System.out.printf("  %3.3f, %6.4f, %6.3f\n",
//                        elapsed, ratio, db);
        } while (db > TARGET_DB && elapsed < 30.0);
        if (elapsed >= 30.0) {
            System.out.println("TIMEOUT!");
        }
        // Time to reach reference;
        rt60 = REFERENCE_DB * elapsed / db;
        // Stop everything.
        synth.stop();

        return rt60;
    }

//    private double estimateRT60(double size, double decay) {
//        return size * (0.52 - (4.7 *  Math.log(1.0001 - (decay * decay))));
//    }

    private void test() {
        double damping = 0.0005;
        for (double size = 0.2; size < 3.0; size *= 1.5) {
            System.out.printf("\nsize = %5.2f\n", size);
            System.out.printf("time, rt60\n");
            for (double time = 0.1; time < 30.0; time *= 1.2) {
                double rt60 = 0.0;
                try {
                    rt60 = measure(size, time, damping);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // double estimate = estimateRT60(size, decay);
                System.out.printf("%5.3f, %6.4f\n",
                        time, rt60);
            }
        }
    }

    public static void main(String[] args) {
        new MeasurePlateReverb().test();
        System.exit(0);
    }
}
