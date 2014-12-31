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
import com.jsyn.unitgen.SpectralFilter;
import com.jsyn.unitgen.SpectralIFFT;

/**
 * Create shared windows as needed for use with FFTs and IFFTs.
 * 
 * @author Phil Burk (C) 2013 Mobileer Inc
 * @see SpectralWindow
 * @see SpectralFFT
 * @see SpectralIFFT
 * @see SpectralFilter
 */
public class SpectralWindowFactory {
    private static final int NUM_WINDOWS = 16;
    private static final int MIN_SIZE_LOG_2 = 2;
    private static HammingWindow[] hammingWindows = new HammingWindow[NUM_WINDOWS];
    private static HannWindow[] hannWindows = new HannWindow[NUM_WINDOWS];

    /** @return a shared standard HammingWindow */
    public static HammingWindow getHammingWindow(int sizeLog2) {
        int index = sizeLog2 - MIN_SIZE_LOG_2;
        if (hammingWindows[index] == null) {
            hammingWindows[index] = new HammingWindow(1 << sizeLog2);
        }
        return hammingWindows[index];
    }

    /** @return a shared HannWindow */
    public static HannWindow getHannWindow(int sizeLog2) {
        int index = sizeLog2 - MIN_SIZE_LOG_2;
        if (hannWindows[index] == null) {
            hannWindows[index] = new HannWindow(1 << sizeLog2);
        }
        return hannWindows[index];
    }
}
