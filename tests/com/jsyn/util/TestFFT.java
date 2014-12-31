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

package com.jsyn.util;

import junit.framework.TestCase;

import com.softsynth.math.FourierMath;

public class TestFFT extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void checkSingleSine(int size, int bin) {
        double[] ar = new double[size];
        double[] ai = new double[size];
        double[] magnitudes = new double[size];

        double amplitude = 1.0;
        addSineWave(size, bin, ar, amplitude);

        FourierMath.transform(1, size, ar, ai);
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        assertTrue(magnitudes[bin - 1] < 0.001);
        assertTrue(magnitudes[bin] > 0.5);
        assertTrue(magnitudes[bin + 1] < 0.001);

    }

    private void addSineWave(int size, int bin, double[] ar, double amplitude) {
        double phase = 0.0;
        double phaseIncrement = 2.0 * Math.PI * bin / size;
        for (int i = 0; i < size; i++) {
            ar[i] += Math.sin(phase) * amplitude;
            // System.out.println( i + " = " + ar[i] );
            phase += phaseIncrement;
        }
    }

    public void testSingles() {
        checkSingleSine(32, 1);
        checkSingleSine(32, 2);
        checkSingleSine(64, 5);
        checkSingleSine(256, 3);
    }

    public void checkInverseFFT(int size, int bin) {
        double[] ar1 = new double[size];
        double[] ai1 = new double[size];
        double[] ar2 = new double[size];
        double[] ai2 = new double[size];

        double amplitude = 1.0;
        addSineWave(size, bin, ar1, amplitude);

        // Save a copy of the source.
        System.arraycopy(ar1, 0, ar2, 0, size);
        System.arraycopy(ai1, 0, ai2, 0, size);

        FourierMath.transform(1, size, ar1, ai1); // FFT

        FourierMath.transform(-1, size, ar1, ai1); // IFFT

        for (int i = 0; i < size; i++) {
            assertEquals(ar2[i], ar1[i], 0.00001);
            assertEquals(ai2[i], ai1[i], 0.00001);
        }
    }

    public void testInverse() {
        checkInverseFFT(32, 1);
        checkInverseFFT(32, 2);
        checkInverseFFT(128, 17);
        checkInverseFFT(512, 23);
    }
}
