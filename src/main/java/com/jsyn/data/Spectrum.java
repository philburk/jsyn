/*
 * Copyright 2013 Phil Burk, Mobileer Inc
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

package com.jsyn.data;

import com.jsyn.unitgen.SpectralFFT;
import com.jsyn.unitgen.SpectralIFFT;
import com.jsyn.unitgen.SpectralProcessor;

/**
 * Complex spectrum with real and imaginary parts. The frequency associated with each bin of the
 * spectrum is:
 * 
 * <pre>
 * frequency = binIndex * sampleRate / size
 * </pre>
 * 
 * Note that the upper half of the spectrum is above the Nyquist frequency. Those frequencies are
 * mirrored around the Nyquist frequency. Note that this spectral API is experimental and may change
 * at any time.
 * 
 * @author Phil Burk (C) 2013 Mobileer Inc
 * @version 016
 * @see SpectralFFT
 * @see SpectralIFFT
 * @see SpectralProcessor
 */
public class Spectrum {
    private double[] real;
    private double[] imaginary;
    public static final int DEFAULT_SIZE_LOG_2 = 9;
    public static final int DEFAULT_SIZE = 1 << DEFAULT_SIZE_LOG_2;

    public Spectrum() {
        this(DEFAULT_SIZE);
    }

    public Spectrum(int size) {
        setSize(size);
    }

    public double[] getReal() {
        return real;
    }

    public double[] getImaginary() {
        return imaginary;
    }

    /**
     * If you change the size of the spectrum then the real and imaginary arrays will be
     * reallocated.
     * 
     * @param size
     */
    public void setSize(int size) {
        if ((real == null) || (real.length != size)) {
            real = new double[size];
            imaginary = new double[size];
        }
    }

    public int size() {
        return real.length;
    }

    /**
     * Copy this spectrum to another spectrum of the same length.
     * 
     * @param destination
     */
    public void copyTo(Spectrum destination) {
        assert (size() == destination.size());
        System.arraycopy(real, 0, destination.real, 0, real.length);
        System.arraycopy(imaginary, 0, destination.imaginary, 0, imaginary.length);
    }

    public void clear() {
        for (int i = 0; i < real.length; i++) {
            real[i] = 0.0;
            imaginary[i] = 0.0;
        }
    }
}
