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

import com.jsyn.data.SpectralWindow;
import com.jsyn.data.Spectrum;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitSpectralInputPort;
import com.softsynth.math.FourierMath;

/**
 * Periodically transform the input signal using an Inverse FFT.
 * 
 * @author Phil Burk (C) 2013 Mobileer Inc
 * @version 016
 * @see SpectralFFT
 */
public class SpectralIFFT extends UnitGenerator {
    public UnitSpectralInputPort input;
    public UnitOutputPort output;
    private Spectrum localSpectrum;
    private double[] buffer;
    private int cursor;
    private SpectralWindow window = RectangularWindow.getInstance();

    /* Define Unit Ports used by connect() and set(). */
    public SpectralIFFT() {
        addPort(output = new UnitOutputPort());
        addPort(input = new UnitSpectralInputPort("Input"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();

        if (buffer == null) {
            if (input.isAvailable()) {
                Spectrum spectrum = input.getSpectrum();
                int size = spectrum.size();
                localSpectrum = new Spectrum(size);
                buffer = localSpectrum.getReal();
                cursor = 0;
            } else {
                for (int i = start; i < limit; i++) {
                    outputs[i] = 0.0;
                }
            }
        }

        if (buffer != null) {
            for (int i = start; i < limit; i++) {
                if (cursor == 0) {
                    Spectrum spectrum = input.getSpectrum();
                    spectrum.copyTo(localSpectrum);
                    FourierMath.ifft(buffer.length, localSpectrum.getReal(),
                            localSpectrum.getImaginary());
                }

                outputs[i] = buffer[cursor] * window.get(cursor);
                cursor += 1;
                if (cursor == buffer.length) {
                    cursor = 0;
                }
            }
        }
    }

    public SpectralWindow getWindow() {
        return window;
    }

    /**
     * Multiply output data by this window after doing the FFT. The default is a RectangularWindow.
     */
    public void setWindow(SpectralWindow window) {
        this.window = window;
    }
}
