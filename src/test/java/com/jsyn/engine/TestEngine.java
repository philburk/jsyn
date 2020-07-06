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

import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PitchDetector;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.ZeroCrossingCounter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    @Test
    public void testInitialization() {
        final int DEFAULT_FRAME_RATE = 44100;
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        assertEquals(0, synthesisEngine.getFrameCount(), "frameCount zero before starting");
        assertEquals(DEFAULT_FRAME_RATE, synthesisEngine.getFrameRate(), "default frameRate");
        assertTrue(synthesisEngine.isPullDataEnabled(), "default pullData");
    }

    public void checkPullData(boolean pullData) {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        assertTrue(synthesisEngine.isRealTime(), "default realTime");
        synthesisEngine.setRealTime(false);

        assertTrue(synthesisEngine.isPullDataEnabled(), "default pullData");
        synthesisEngine.setPullDataEnabled(pullData);

        SineOscillator sineOscillator = new SineOscillator();
        synthesisEngine.add(sineOscillator);

        LineOut lineOut = new LineOut();
        synthesisEngine.add(lineOut);
        sineOscillator.output.connect(0, lineOut.input, 0);

        assertEquals(0.0, sineOscillator.output.getValue(), "initial sine value");

        synthesisEngine.start();
        if (!pullData) {
            sineOscillator.start();
        }
        // We always have to start the LineOut.
        lineOut.start();
        synthesisEngine.generateNextBuffer();
        synthesisEngine.generateNextBuffer();

        double value = sineOscillator.output.getValue();
        assertTrue(value > 0.0, "sine value after generation = " + value);
    }

    @Test
    public void testPullDataFalse() {
        checkPullData(false);
    }

    @Test
    public void testPullDataTrue() {
        checkPullData(true);
    }

    @Test
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
            assertTrue(e.getMessage().contains("different synths"), "informative MPE message");
        }

        assertTrue(gotCaught, "caught NPE caused by forgetting synth.add");
    }

    @Test
    public void testNotAdding() {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        synthesisEngine.setPullDataEnabled(true);

        // Create a sineOscillator but do not add it to the synth!
        SineOscillator sineOscillator = new SineOscillator();

        LineOut lineOut = new LineOut();
        sineOscillator.output.connect(0, lineOut.input, 0);
        synthesisEngine.add(lineOut);

        assertEquals(0.0, sineOscillator.output.getValue(), "initial sine value");

        synthesisEngine.start();
        // We always have to start the LineOut.
        lineOut.start();
        boolean gotCaught = false;
        try {
            synthesisEngine.generateNextBuffer();
            synthesisEngine.generateNextBuffer();
        } catch (NullPointerException e) {
            gotCaught = true;
            assertTrue(e.getMessage().contains("forgot to add"), "informative MPE message");
        }

        assertTrue(gotCaught, "caught NPE caused by forgetting synth.add");
    }

    @Test
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

        assertEquals(0, counter.getCount(), "initial count");

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
            LOGGER.debug(msg);
            assertEquals(oscFreq, frequencyMeasured, oscFreq * 0.1, msg);
            assertEquals(0.9, confidenceMeasured, 0.1, "pitch confidence");

            double expectedCount = interval * oscFreq;
            double framesMeasured = counter.getCount() - previousFrameCount;
            msg = "count at " + rate + " Hz";
            LOGGER.debug(msg);
            assertEquals(expectedCount, framesMeasured, expectedCount * 0.1, msg);

            synth.stop();
        }

    }

    @Test
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
        assertEquals(14.0, adder.output.get(), 0.01, "simple add");

        // Schedule a set() in the near future.
        double time = synth.getCurrentTime();
        adder.inputA.set(7.0, time + 1.0);
        synth.sleepFor(0.5);
        assertEquals(14.0, adder.output.get(), 0.01, "before scheduled set");
        synth.sleepFor(1.0);
        assertEquals(17.0, adder.output.get(), 0.01, "after scheduled set");

        // Schedule a set() in the near future then cancel it.
        time = synth.getCurrentTime();
        adder.inputA.set(5.0, time + 1.0);
        synth.sleepFor(0.5);
        assertEquals(17.0, adder.output.get(), 0.01, "before scheduled set");
        synth.clearCommandQueue();
        synth.sleepFor(1.0);
        assertEquals(17.0, adder.output.get(), 0.01, "after canceled set");

        synth.stop();
    }
}
