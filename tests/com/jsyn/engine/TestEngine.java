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

package com.jsyn.engine;

import junit.framework.TestCase;

import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PitchDetector;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.ZeroCrossingCounter;

public class TestEngine extends TestCase {

    public void testInitialization() {
        final int DEFAULT_FRAME_RATE = 44100;
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        assertEquals("frameCount zero before starting", 0, synthesisEngine.getFrameCount());
        assertEquals("default frameRate", DEFAULT_FRAME_RATE, synthesisEngine.getFrameRate());
        assertEquals("default pullData", true, synthesisEngine.isPullDataEnabled());
    }

    public void checkPullData(boolean pullData) {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        assertEquals("default realTime", true, synthesisEngine.isRealTime());
        synthesisEngine.setRealTime(false);

        assertEquals("default pullData", true, synthesisEngine.isPullDataEnabled());
        synthesisEngine.setPullDataEnabled(pullData);

        SineOscillator sineOscillator = new SineOscillator();
        synthesisEngine.add(sineOscillator);

        LineOut lineOut = new LineOut();
        synthesisEngine.add(lineOut);
        sineOscillator.output.connect(0, lineOut.input, 0);

        assertEquals("initial sine value", 0.0, sineOscillator.output.getValue());

        synthesisEngine.start();
        if (!pullData) {
            sineOscillator.start();
        }
        // We always have to start the LineOut.
        lineOut.start();
        synthesisEngine.generateNextBuffer();
        synthesisEngine.generateNextBuffer();

        double value = sineOscillator.output.getValue();
        assertTrue("sine value after generation = " + value, (value > 0.0));
    }

    public void testPullDataFalse() {
        checkPullData(false);
    }

    public void testPullDataTrue() {
        checkPullData(true);
    }

    public void testMixedAdding() {
        boolean gotCaught = false;
        SynthesisEngine synthesisEngine1 = new SynthesisEngine();
        synthesisEngine1.setRealTime(false);
        synthesisEngine1.setPullDataEnabled(true);
        SynthesisEngine synthesisEngine2 = new SynthesisEngine();
        synthesisEngine2.setRealTime(false);
        synthesisEngine2.setPullDataEnabled(true);

        // Create a sineOscillator but do not add it to the synth!
        SineOscillator sineOscillator = new SineOscillator();
        LineOut lineOut = new LineOut();

        synthesisEngine1.add(lineOut);
        synthesisEngine2.add(sineOscillator);
        try {
            sineOscillator.output.connect(0, lineOut.input, 0);
        } catch (RuntimeException e) {
            gotCaught = true;
            assertTrue("informative MPE message", e.getMessage().contains("different synths"));
        }

        assertTrue("caught NPE caused by forgetting synth.add", gotCaught);
    }

    public void testNotAdding() {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        synthesisEngine.setPullDataEnabled(true);

        // Create a sineOscillator but do not add it to the synth!
        SineOscillator sineOscillator = new SineOscillator();

        LineOut lineOut = new LineOut();
        sineOscillator.output.connect(0, lineOut.input, 0);
        synthesisEngine.add(lineOut);

        assertEquals("initial sine value", 0.0, sineOscillator.output.getValue());

        synthesisEngine.start();
        // We always have to start the LineOut.
        lineOut.start();
        boolean gotCaught = false;
        try {
            synthesisEngine.generateNextBuffer();
            synthesisEngine.generateNextBuffer();
        } catch (NullPointerException e) {
            gotCaught = true;
            assertTrue("informative MPE message", e.getMessage().contains("forgot to add"));
        }

        assertTrue("caught NPE caused by forgetting synth.add", gotCaught);
    }

    public void testMultipleStarts() throws InterruptedException {
        SynthesisEngine synth = new SynthesisEngine();

        // Create a sineOscillator but do not add it to the synth!
        SineOscillator osc = new SineOscillator();
        ZeroCrossingCounter counter = new ZeroCrossingCounter();
        PitchDetector pitchDetector = new PitchDetector();
        LineOut lineOut = new LineOut();
        synth.add(osc);
        synth.add(counter);
        synth.add(lineOut);
        synth.add(pitchDetector);
        osc.output.connect(counter.input);
        osc.output.connect(pitchDetector.input);
        counter.output.connect(0, lineOut.input, 0);

        assertEquals("initial count", 0, counter.getCount());

        int[] rates = {
                32000, 48000, 44100, 22050
        };
        for (int rate : rates) {
            synth.start(rate);
            lineOut.start();
            pitchDetector.start();

            double time = synth.getCurrentTime();
            double interval = 1.0;
            time += interval;

            long previousFrameCount = counter.getCount();
            synth.sleepUntil(time);

            double frequencyMeasured = pitchDetector.frequency.get();
            double confidenceMeasured = pitchDetector.confidence.get();
            double oscFreq = osc.frequency.get();
            String msg = "freq at " + rate + " Hz";
            System.out.println(msg);
            assertEquals(msg, oscFreq, frequencyMeasured, oscFreq * 0.1);
            assertEquals("pitch confidence", 0.9, confidenceMeasured, 0.1);

            double expectedCount = interval * oscFreq;
            double framesMeasured = counter.getCount() - previousFrameCount;
            msg = "count at " + rate + " Hz";
            System.out.println(msg);
            assertEquals(msg, expectedCount, framesMeasured, expectedCount * 0.1);

            synth.stop();
        }

    }


    public void testScheduler() throws InterruptedException {
        SynthesisEngine synth = new SynthesisEngine();
        synth.setRealTime(false);
        Add adder = new Add();
        synth.add(adder);
        synth.start();
        adder.start();
        adder.inputA.set(4.0);
        adder.inputB.set(10.0);
        synth.sleepFor(0.1);
        assertEquals("simple add", 14.0, adder.output.get(), 0.01);

        // Schedule a set() in the near future.
        double time = synth.getCurrentTime();
        adder.inputA.set(7.0, time + 1.0);
        synth.sleepFor(0.5);
        assertEquals("before scheduled set", 14.0, adder.output.get(), 0.01);
        synth.sleepFor(1.0);
        assertEquals("after scheduled set", 17.0, adder.output.get(), 0.01);

        // Schedule a set() in the near future then cancel it.
        time = synth.getCurrentTime();
        adder.inputA.set(5.0, time + 1.0);
        synth.sleepFor(0.5);
        assertEquals("before scheduled set", 17.0, adder.output.get(), 0.01);
        synth.clearCommandQueue();
        synth.sleepFor(1.0);
        assertEquals("after canceled set", 17.0, adder.output.get(), 0.01);

        synth.stop();
    }
}
