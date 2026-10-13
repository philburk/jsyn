/*
 * Copyright 2026 Phil Burk, Mobileer Inc
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

package com.jsyn.unitgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.jsyn.ports.UnitInputPort;

public class TestGrainFarm extends NonRealTimeTestCase {

    private Grain[] makeGrains(int count) {
        Grain[] grains = new Grain[count];
        for (int i = 0; i < count; i++) {
            grains[i] = new Grain(new GrainSourceSine(), new RaisedCosineEnvelope());
        }
        return grains;
    }

    private void generateBlocks(GrainFarm farm, int blocks) {
        for (int i = 0; i < blocks; i++) {
            farm.generate();
        }
    }

    /**
     * Grains turn their duration into frames, and sample grains their pitch, using the frame
     * rate, so they must use the rate the synth actually runs at.
     */
    @Test
    public void testGrainsUseTheFrameRateTheSynthRunsAt() {
        // Set up the grains before the farm is added to the synth.
        GrainFarm farm = new GrainFarm();
        Grain[] grains = makeGrains(4);
        farm.setGrainArray(grains);
        farm.density.set(0.5);

        synthesisEngine.add(farm);
        synthesisEngine.start(48000);

        generateBlocks(farm, 2000);

        for (Grain grain : grains) {
            assertEquals(48000.0, grain.getFrameRate(), "a grain is still timed at the old frame rate");
        }
    }

    /** Tracks the peak level of its input since it was last reset. */
    static class PeakMeter extends UnitGenerator {
        final UnitInputPort input;
        double peak;

        PeakMeter() {
            addPort(input = new UnitInputPort("Input"));
        }

        @Override
        public void generate(int start, int limit) {
            double[] values = input.getValues();
            for (int i = start; i < limit; i++) {
                peak = Math.max(peak, Math.abs(values[i]));
            }
        }
    }

    private void advance(double seconds) throws InterruptedException {
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + seconds);
    }

    @Test
    public void testGrainsComeBackWhenDensityRisesFromZero() throws InterruptedException {
        // For example an envelope on the density port that falls to zero and starts again.
        GrainFarm farm = new GrainFarm();
        PeakMeter meter = new PeakMeter();
        synthesisEngine.add(farm);
        synthesisEngine.add(meter);
        farm.output.connect(meter.input);
        farm.allocate(8);
        farm.density.set(0.0);

        synthesisEngine.start();
        meter.start();
        advance(0.4);
        assertEquals(0.0, meter.peak, "no grains at density zero");

        farm.density.set(0.5);
        // Grains are 5 ms apart at most at this density, so they should be back within 0.1 s.
        advance(0.1);
        assertTrue(meter.peak > 0.01, "still silent after density rose from zero");
    }

    @Test
    public void testGrainsFollowAChangeOfFrameRate() {
        // Restarting the synth at another rate changes the frame rate under a running farm.
        GrainFarm farm = new GrainFarm();
        Grain[] grains = makeGrains(4);
        farm.density.set(0.5);

        synthesisEngine.add(farm);
        synthesisEngine.start(44100);
        farm.setGrainArray(grains);
        generateBlocks(farm, 2000);
        synthesisEngine.stop();

        synthesisEngine.start(96000);
        generateBlocks(farm, 2000);

        for (Grain grain : grains) {
            assertEquals(96000.0, grain.getFrameRate());
        }
    }
}
