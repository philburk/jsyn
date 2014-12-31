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

package com.jsyn.unitgen;

import java.util.Arrays;

import com.jsyn.data.SpectralWindow;
import com.jsyn.data.Spectrum;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitSpectralOutputPort;
import com.softsynth.math.FourierMath;

/**
 * Periodically transform the input signal using an FFT. Output complete spectra.
 * 
 * @author Phil Burk (C) 2013 Mobileer Inc
 * @version 016
 * @see SpectralIFFT
 * @see Spectrum
 * @see SpectralFilter
 */
public class SpectralFFT extends UnitGenerator {
    public UnitInputPort input;
    /**
     * Provides complete complex spectra when the FFT completes.
     */
    public UnitSpectralOutputPort output;
    private double[] buffer;
    private int cursor;
    private SpectralWindow window = RectangularWindow.getInstance();
    private int sizeLog2;
    private int offset;
    private boolean running;

    /* Define Unit Ports used by connect() and set(). */
    public SpectralFFT() {
        this(Spectrum.DEFAULT_SIZE_LOG_2);
    }

    /**
     * @param sizeLog2 for example, pass 10 to get a 1024 bin FFT
     */
    public SpectralFFT(int sizeLog2) {
        addPort(input = new UnitInputPort("Input"));
        addPort(output = new UnitSpectralOutputPort("Output", 1 << sizeLog2));
        setSizeLog2(sizeLog2);
    }

    /**
     * Please do not change the size of the FFT while JSyn is running.
     * 
     * @param sizeLog2 for example, pass 9 to get a 512 bin FFT
     */
    public void setSizeLog2(int sizeLog2) {
        this.sizeLog2 = sizeLog2;
        output.setSize(1 << sizeLog2);
        buffer = output.getSpectrum().getReal();
        cursor = 0;
    }

    public int getSizeLog2() {
        return sizeLog2;
    }

    @Override
    public void generate(int start, int limit) {
        if (!running) {
            int mask = (1 << sizeLog2) - 1;
            if (((getSynthesisEngine().getFrameCount() - offset) & mask) == 0) {
                running = true;
                cursor = 0;
            }
        }
        // Don't use "else" because "running" may have changed in above block.
        if (running) {
            double[] inputs = input.getValues();
            for (int i = start; i < limit; i++) {
                buffer[cursor] = inputs[i] * window.get(cursor);
                ++cursor;
                // When it is full, do the FFT.
                if (cursor == buffer.length) {
                    Spectrum spectrum = output.getSpectrum();
                    Arrays.fill(spectrum.getImaginary(), 0.0);
                    FourierMath.fft(buffer.length, spectrum.getReal(), spectrum.getImaginary());
                    output.advance();
                    cursor = 0;
                }
            }
        }
    }

    public SpectralWindow getWindow() {
        return window;
    }

    /**
     * Multiply input data by this window before doing the FFT. The default is a RectangularWindow.
     */
    public void setWindow(SpectralWindow window) {
        this.window = window;
    }

    /**
     * The FFT will be performed on a frame that is a multiple of the size plus this offset.
     * 
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

}
