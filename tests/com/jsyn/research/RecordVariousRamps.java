/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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
 * Generate steps, linear ramps and smooth ramps.
 *
 * @author (C) 2014 Phil Burk
 */

package com.jsyn.research;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.ContinuousRamp;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitFilter;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.util.WaveRecorder;

public class RecordVariousRamps {
    private Synthesizer synth;
    private UnitOscillator osc;
    private Multiply multiplier;
    private UnitFilter ramp;
    private LinearRamp linearRamp;
    private ContinuousRamp continuousRamp;
    private LineOut lineOut;
    private WaveRecorder recorder;
    private PowerOfTwo powerOfTwo;
    private static final int MODE_STEP = 0;
    private static final int MODE_LINEAR = 1;
    private static final int MODE_SMOOTH = 2;
    private static final String[] modeNames = {
            "step", "linear", "smooth"
    };

    private RampEvent[] rampData = {
            new RampEvent(1.0, 1.5, 2.0), new RampEvent(-0.9, 0.5, 1.0),
            new RampEvent(0.9, 0.5, 0.8), new RampEvent(-0.3, 0.5, 0.8),
            new RampEvent(0.9, 0.5, 0.3), new RampEvent(-0.5, 0.5, 0.3),
            new RampEvent(0.8, 2.0, 1.0),
    };

    private static class RampEvent {
        double target;
        double eventDuration;
        double rampDuration;

        RampEvent(double target, double eventDuration, double rampDuration) {
            this.target = target;
            this.eventDuration = eventDuration;
            this.rampDuration = rampDuration;
        }
    }

    private void test(int mode) throws IOException {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);

        File waveFile = new File("ramp_pitch_" + modeNames[mode] + ".wav");
        // Mono 16 bits.
        recorder = new WaveRecorder(synth, waveFile, 1, 16);
        System.out.println("Writing to 16-bit WAV file " + waveFile.getAbsolutePath());

        // Add some tone generators.
        synth.add(osc = new SawtoothOscillatorBL());

        // Add a controller that will sweep up.
        synth.add(multiplier = new Multiply());
        synth.add(powerOfTwo = new PowerOfTwo());
        // Add an output unit.
        synth.add(lineOut = new LineOut());
        multiplier.inputB.set(660.0);

        switch (mode) {
            case MODE_STEP:
                synth.add(ramp = new PassThrough());
                break;
            case MODE_LINEAR:
                synth.add(ramp = linearRamp = new LinearRamp());
                linearRamp.current.set(-1.0);
                linearRamp.time.set(10.0);
                break;
            case MODE_SMOOTH:
                synth.add(ramp = continuousRamp = new ContinuousRamp());
                continuousRamp.current.set(-1.0);
                continuousRamp.time.set(10.0);
                break;
        }

        ramp.getInput().set(-1.0);
        ramp.getOutput().connect(powerOfTwo.input);

        powerOfTwo.output.connect(multiplier.inputA);
        multiplier.output.connect(osc.frequency);

        // Connect the oscillator to the left and right audio output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        osc.output.connect(0, recorder.getInput(), 0);
        // When we start the recorder it will pull data from the oscillator
        // and sweeper.
        recorder.start();

        // We also need to start the LineOut if we want to hear it now.
        lineOut.start();

        // Get synthesizer time in seconds.
        double nextEventTime = synth.getCurrentTime() + 1.0;
        try {
            synth.sleepUntil(nextEventTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (RampEvent rampEvent : rampData) {

            switch (mode) {
                case MODE_STEP:
                    break;
                case MODE_LINEAR:
                    linearRamp.time.set(rampEvent.rampDuration);
                    break;
                case MODE_SMOOTH:
                    continuousRamp.time.set(rampEvent.rampDuration);
                    break;
            }
            ramp.getInput().set(rampEvent.target);

            nextEventTime += rampEvent.eventDuration;
            System.out.println("target = " + rampEvent.target + ", rampDur = "
                    + rampEvent.rampDuration + ", eventDur = " + rampEvent.eventDuration);
            try {
                synth.sleepUntil(nextEventTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            new RecordVariousRamps().test(MODE_STEP);
            new RecordVariousRamps().test(MODE_LINEAR);
            new RecordVariousRamps().test(MODE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
