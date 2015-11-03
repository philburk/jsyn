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

    public void checkSingleSineDouble(int size, int bin) {
        double[] ar = new double[size];
        double[] ai = new double[size];
        double[] magnitudes = new double[size];

        double amplitude = 1.0;
        addSineWave(size, bin, ar, amplitude);

        FourierMath.transform(1, size, ar, ai);
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        assertEquals("magnitude", 0.0, magnitudes[bin-1], 0.000001);
        assertEquals("magnitude", amplitude, magnitudes[bin], 0.000001);
        assertEquals("magnitude", 0.0, magnitudes[bin+1], 0.000001);
 /*
        for (int i = 0; i < magnitudes.length; i++) {
            System.out.printf("%d = %9.7f\n", i, magnitudes[i]);
        }
*/

    }

    public void checkSingleSineFloat(int size, int bin) {
        float[] ar = new float[size];
        float[] ai = new float[size];
        float[] magnitudes = new float[size];

        double amplitude = 1.0;
        addSineWave(size, bin, ar, amplitude);

        FourierMath.transform(1, size, ar, ai);
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        assertEquals("magnitude", 0.0f, magnitudes[bin-1], 0.000001);
        assertEquals("magnitude", amplitude, magnitudes[bin], 0.000001);
        assertEquals("magnitude", 0.0f, magnitudes[bin+1], 0.000001);
/*
        for (int i = 0; i < magnitudes.length; i++) {
            System.out.printf("%d = %9.7f\n", i, magnitudes[i]);
        }
*/
    }

    public void checkMultipleSine(int size, int[] bins, double[] amplitudes) {
        double[] ar = new double[size];
        double[] ai = new double[size];
        double[] magnitudes = new double[size];

        for(int i = 0; i<bins.length; i++) {
            addSineWave(size, bins[i], ar, amplitudes[i]);
        }

        FourierMath.transform(1, size, ar, ai);
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        for(int bin = 0; bin<size; bin++) {
            System.out.printf("%d = %9.7f\n", bin, magnitudes[bin]);

            double amplitude = 0.0;
            for(int i = 0; i<bins.length; i++) {
                if ((bin == bins[i]) || (bin == (size - bins[i]))) {
                    amplitude = amplitudes[i];
                    break;
                }
            }
            assertEquals("magnitude", amplitude, magnitudes[bin], 0.000001);
        }

    }

    private void addSineWave(int size, int bin, double[] ar, double amplitude) {
        double phase = 0.0;
        double phaseIncrement = 2.0 * Math.PI * bin / size;
        for (int i = 0; i < size; i++) {
            ar[i] += Math.sin(phase) * amplitude;
            phase += phaseIncrement;
        }
    }
    private void addSineWave(int size, int bin, float[] ar, double amplitude) {
        double phase = 0.0;
        double phaseIncrement = 2.0 * Math.PI * bin / size;
        for (int i = 0; i < size; i++) {
            ar[i] += (float) (Math.sin(phase) * amplitude);
            phase += phaseIncrement;
        }
    }

    public void testSinglesDouble() {
        checkSingleSineDouble(32, 1);
        checkSingleSineDouble(32, 4);
        checkSingleSineDouble(64, 5);
        checkSingleSineDouble(256, 3);
    }

    public void testSinglesFloat() {
        checkSingleSineFloat(32, 1);
        checkSingleSineFloat(32, 4);
        checkSingleSineFloat(64, 5);
        checkSingleSineFloat(256, 3);
    }

    public void testMultipleSines32() {
        int[] bins = { 1, 5 };
        double[] amplitudes = { 1.0, 2.0 };
        checkMultipleSine(32, bins, amplitudes);
    }

    public void testMultipleSines64() {
        int[] bins = { 2, 4, 7 };
        double[] amplitudes = { 1.0, 0.3, 0.5 };
        checkMultipleSine(64, bins, amplitudes);
    }

    public void checkInverseFftDouble(int size, int bin) {
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

    public void checkInverseFftFloat(int size, int bin) {
        float[] ar1 = new float[size];
        float[] ai1 = new float[size];
        float[] ar2 = new float[size];
        float[] ai2 = new float[size];

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

    public void testInverseDouble() {
        checkInverseFftDouble(32, 1);
        checkInverseFftDouble(32, 2);
        checkInverseFftDouble(128, 17);
        checkInverseFftDouble(512, 23);
    }

    public void testInverseFloat() {
        checkInverseFftFloat(32, 1);
        checkInverseFftFloat(32, 2);
        checkInverseFftFloat(128, 17);
        checkInverseFftFloat(512, 23);
    }
}
