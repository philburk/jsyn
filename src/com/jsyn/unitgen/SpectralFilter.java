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

import com.jsyn.data.SpectralWindow;
import com.jsyn.data.SpectralWindowFactory;
import com.jsyn.data.Spectrum;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitSpectralInputPort;
import com.jsyn.ports.UnitSpectralOutputPort;

/**
 * Process a signal using multiple overlapping FFT and IFFT pairs. For passthrough, you can connect
 * the spectral outputs to the spectral inputs. Or you can connect one or more SpectralProcessors
 * between them.
 * 
 * <pre>
 * for (int i = 0; i &lt; numFFTs; i++) {
 *     filter.getSpectralOutput(i).connect(processors[i].input);
 *     processors[i].output.connect(filter.getSpectralInput(i));
 * }
 * </pre>
 * 
 * See the example program "HearSpectralFilter.java". Note that this spectral API is experimental
 * and may change at any time.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 * @see SpectralProcessor
 */
public class SpectralFilter extends Circuit implements UnitSink, UnitSource {
    public UnitInputPort input;
    public UnitOutputPort output;

    private SpectralFFT[] ffts;
    private SpectralIFFT[] iffts;
    private PassThrough inlet; // fan out to FFTs
    private PassThrough sum; // mix output of IFFTs

    /**
     * Create a default sized filter with 2 FFT/IFFT pairs and a sizeLog2 of
     * Spectrum.DEFAULT_SIZE_LOG_2.
     */
    public SpectralFilter() {
        this(2, Spectrum.DEFAULT_SIZE_LOG_2);
    }

    /**
     * @param numFFTs number of FFT/IFFT pairs for the overlap and add
     * @param sizeLog2 for example, use 10 to get a 1024 bin FFT, 12 for 4096
     */
    public SpectralFilter(int numFFTs, int sizeLog2) {
        add(inlet = new PassThrough());
        add(sum = new PassThrough());
        ffts = new SpectralFFT[numFFTs];
        iffts = new SpectralIFFT[numFFTs];
        int offset = (1 << sizeLog2) / numFFTs;
        for (int i = 0; i < numFFTs; i++) {
            add(ffts[i] = new SpectralFFT(sizeLog2));
            inlet.output.connect(ffts[i].input);
            ffts[i].setOffset(i * offset);

            add(iffts[i] = new SpectralIFFT());
            iffts[i].output.connect(sum.input);
        }
        setWindow(SpectralWindowFactory.getHammingWindow(sizeLog2));

        addPort(input = inlet.input);
        addPort(output = sum.output);
    }

    public SpectralWindow getWindow() {
        return ffts[0].getWindow();
    }

    /**
     * Specify one window to be used for all FFTs and IFFTs. The window should be the same size as
     * the FFTs.
     * 
     * @param window default is HammingWindow
     * @see SpectralWindowFactory
     */
    public void setWindow(SpectralWindow window) {
        // Use the same window everywhere.
        for (int i = 0; i < ffts.length; i++) {
            ffts[i].setWindow(window); // TODO review, both sides or just one
            iffts[i].setWindow(window);
        }
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    @Override
    public UnitInputPort getInput() {
        return input;
    }

    /**
     * @param i
     * @return the output of the indexed FFT
     */
    public UnitSpectralOutputPort getSpectralOutput(int i) {
        return ffts[i].output;
    }

    /**
     * @param i
     * @return the input of the indexed IFFT
     */
    public UnitSpectralInputPort getSpectralInput(int i) {
        return iffts[i].input;
    }
}
