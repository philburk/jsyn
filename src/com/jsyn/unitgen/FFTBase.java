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

import com.jsyn.data.Spectrum;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.softsynth.math.FourierMath;

/**
 * Periodically transform the complex input signal using an FFT to a complex spectral stream.
 * 
 * @author Phil Burk (C) 2013 Mobileer Inc
 * @version 016
 * @see IFFT
 */
public abstract class FFTBase extends UnitGenerator {
    public UnitInputPort inputReal;
    public UnitInputPort inputImaginary;
    public UnitOutputPort outputReal;
    public UnitOutputPort outputImaginary;
    protected double[] realInput;
    protected double[] realOutput;
    protected double[] imaginaryInput;
    protected double[] imaginaryOutput;
    protected int cursor;

    protected FFTBase() {
        addPort(inputReal = new UnitInputPort("InputReal"));
        addPort(inputImaginary = new UnitInputPort("InputImaginary"));
        addPort(outputReal = new UnitOutputPort("OutputReal"));
        addPort(outputImaginary = new UnitOutputPort("OutputImaginary"));
        setSize(Spectrum.DEFAULT_SIZE);
    }

    public void setSize(int size) {
        realInput = new double[size];
        realOutput = new double[size];
        imaginaryInput = new double[size];
        imaginaryOutput = new double[size];
        cursor = 0;
    }

    public int getSize() {
        return realInput.length;
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputRs = inputReal.getValues();
        double[] inputIs = inputImaginary.getValues();
        double[] outputRs = outputReal.getValues();
        double[] outputIs = outputImaginary.getValues();
        for (int i = start; i < limit; i++) {
            realInput[cursor] = inputRs[i];
            imaginaryInput[cursor] = inputIs[i];
            outputRs[i] = realOutput[cursor];
            outputIs[i] = imaginaryOutput[cursor];
            cursor += 1;
            // When it is full, do the FFT.
            if (cursor == realInput.length) {
                // Copy to output buffer so we can do the FFT in place.
                System.arraycopy(realInput, 0, realOutput, 0, realInput.length);
                System.arraycopy(imaginaryInput, 0, imaginaryOutput, 0, imaginaryInput.length);
                FourierMath.transform(getSign(), realOutput.length, realOutput, imaginaryOutput);
                cursor = 0;
            }
        }
    }

    protected abstract int getSign();
}
