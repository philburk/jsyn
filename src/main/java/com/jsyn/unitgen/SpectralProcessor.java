/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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

import com.jsyn.data.Spectrum;
import com.jsyn.ports.UnitSpectralInputPort;
import com.jsyn.ports.UnitSpectralOutputPort;

/**
 * This is a base class for implementing your own spectral processing units. You need to implement
 * the processSpectrum() method.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 * @see Spectrum
 */
public abstract class SpectralProcessor extends UnitGenerator {
    public UnitSpectralInputPort input;
    public UnitSpectralOutputPort output;
    private int counter;

    /* Define Unit Ports used by connect() and set(). */
    public SpectralProcessor() {
        addPort(output = new UnitSpectralOutputPort());
        addPort(input = new UnitSpectralInputPort());
    }

    /* Define Unit Ports used by connect() and set(). */
    public SpectralProcessor(int size) {
        addPort(output = new UnitSpectralOutputPort(size));
        addPort(input = new UnitSpectralInputPort());
    }

    @Override
    public void generate(int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (counter == 0) {
                if (input.isAvailable()) {
                    Spectrum inputSpectrum = input.getSpectrum();
                    Spectrum outputSpectrum = output.getSpectrum();
                    processSpectrum(inputSpectrum, outputSpectrum);

                    output.advance();
                    counter = inputSpectrum.size() - 1;
                }
            } else {
                counter--;
            }
        }
    }

    /**
     * Define this method to implement your own processor.
     * 
     * @param inputSpectrum
     * @param outputSpectrum
     */
    public abstract void processSpectrum(Spectrum inputSpectrum, Spectrum outputSpectrum);

}
