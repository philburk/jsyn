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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.jsyn.Synthesizer;
import com.softsynth.math.ChebyshevPolynomial;
import com.softsynth.math.Polynomial;

public class TestChebyshevOscillator extends NonRealTimeTestCase {

    /** The oscillators need an engine for the frame rate. The engine is not started. */
    private ChebyshevOscillator createOscillator() {
        ChebyshevOscillator cheby = new ChebyshevOscillator();
        synthesisEngine.add(cheby);
        return cheby;
    }

    /**
     * Runs the oscillator alongside a plain sine at the same frequency and drive, and checks that
     * every output sample is amplitude * T(order)(sine).
     */
    private void checkMatchesPolynomial(int order, double drive, double amplitude) {
        ChebyshevOscillator cheby = createOscillator();
        cheby.setOrder(order);
        cheby.frequency.set(441.0);
        cheby.drive.set(drive);
        cheby.amplitude.set(amplitude);

        SineOscillator reference = new SineOscillator(441.0, drive);
        synthesisEngine.add(reference);
        Polynomial polynomial = ChebyshevPolynomial.T(order);

        // Linear interpolation error grows with the curvature of T(n), which peaks near
        // the ends of the table at n^2 (n^2 - 1) / 3.
        double tolerance = 1.0e-3;
        for (int block = 0; block < 200; block++) {
            cheby.generate();
            reference.generate();
            for (int i = 0; i < Synthesizer.FRAMES_PER_BLOCK; i++) {
                double x = reference.output.getValues()[i];
                double expected = amplitude * polynomial.evaluate(x);
                double actual = cheby.output.getValues()[i];
                assertTrue(Math.abs(expected - actual) < tolerance, "order " + order + ", drive "
                        + drive + ": expected " + expected + ", got " + actual);
            }
        }
    }

    @Test
    public void testOutputIsTheChebyshevPolynomialOfASine() {
        for (int order : ChebyshevOscillator.ORDERS) {
            checkMatchesPolynomial(order, 1.0, 0.5);
            checkMatchesPolynomial(order, 0.5, 1.0);
        }
    }

    @Test
    public void testOrderOneIsAPlainSine() {
        checkMatchesPolynomial(1, 0.8, 0.5);
    }

    @Test
    public void testEvenOrdersAreRejected() {
        ChebyshevOscillator cheby = createOscillator();
        for (int order : Arrays.asList(0, 2, 4, 12, 17, -1)) {
            assertThrows(IllegalArgumentException.class, () -> cheby.setOrder(order));
        }
        assertEquals(ChebyshevOscillator.DEFAULT_ORDER, cheby.getOrder());
    }

    @Test
    public void testOddOrdersHaveNoDcOffset() {
        // Odd polynomials are odd functions, so a full cycle averages to zero at any drive.
        ChebyshevOscillator cheby = createOscillator();
        cheby.setOrder(11);
        cheby.drive.set(0.7);
        cheby.amplitude.set(1.0);
        // 441 Hz at the default 44100 frame rate is exactly 100 frames per cycle.
        cheby.frequency.set(441.0);
        int framesPerCycle = 100;
        int blocks = framesPerCycle * Synthesizer.FRAMES_PER_BLOCK; // a whole number of cycles
        double sum = 0.0;
        for (int block = 0; block < blocks; block++) {
            cheby.generate();
            for (int i = 0; i < Synthesizer.FRAMES_PER_BLOCK; i++) {
                sum += cheby.output.getValues()[i];
            }
        }
        double mean = sum / (blocks * Synthesizer.FRAMES_PER_BLOCK);
        assertTrue(Math.abs(mean) < 1.0e-4, "mean should be zero, got " + mean);
    }

    @Test
    public void testTablesAreSharedBetweenOscillators() {
        for (int order : ChebyshevOscillator.ORDERS) {
            assertSame(ChebyshevOscillator.tableForOrder(order),
                    ChebyshevOscillator.tableForOrder(order));
        }
    }

    @Test
    public void testPortsAreExposedByName() {
        ChebyshevOscillator cheby = createOscillator();
        assertSame(cheby.frequency, cheby.getPortByName("frequency"));
        assertSame(cheby.drive, cheby.getPortByName("drive"));
        assertSame(cheby.amplitude, cheby.getPortByName("amplitude"));
        assertSame(cheby.output, cheby.getPortByName("output"));
    }
}
