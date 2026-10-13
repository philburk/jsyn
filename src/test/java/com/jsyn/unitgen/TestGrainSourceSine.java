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

import org.junit.jupiter.api.Test;

public class TestGrainSourceSine {

    private static final int SECONDS = 10;

    /**
     * Frequency in Hz, from the rising zero crossings in SECONDS of output. The crossing at the
     * very start is not seen, so this can read up to 1 / SECONDS Hz low.
     */
    private double measureFrequency(double rate, int frameRate) {
        GrainSourceSine source = new GrainSourceSine();
        source.setFrameRate(frameRate);
        source.setRate(rate);
        int crossings = 0;
        double previous = source.next();
        for (int i = 0; i < (frameRate * SECONDS) - 1; i++) {
            double value = source.next();
            if (previous < 0.0 && value >= 0.0) {
                crossings++;
            }
            previous = value;
        }
        return crossings / (double) SECONDS;
    }

    private void assertFrequency(double expected, double actual, String message) {
        assertEquals(expected, actual, 1.5 / SECONDS, message);
    }

    @Test
    public void testRateOfOneIsConcertA() {
        // The same pitch whatever rate the synth runs at.
        for (int frameRate : new int[] {
                44100, 48000, 96000
        }) {
            assertFrequency(440.0, measureFrequency(1.0, frameRate), "at " + frameRate + " Hz");
        }
    }

    @Test
    public void testRateScalesTheFrequency() {
        // So rate = Hz / 440 plays a given frequency.
        assertFrequency(880.0, measureFrequency(2.0, 48000), "octave up");
        assertFrequency(220.0, measureFrequency(0.5, 48000), "octave down");
        assertFrequency(261.6, measureFrequency(261.6256 / 440.0, 48000), "middle C");
    }
}
