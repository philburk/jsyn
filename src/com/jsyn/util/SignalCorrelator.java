/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

/**
 * Interface used to evaluate various algorithms for pitch detection.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public interface SignalCorrelator {
    /**
     * Add a sample to be analyzed. The samples will generally be held in a circular buffer.
     * 
     * @param value
     * @return true if a new period value has been generated
     */
    public boolean addSample(double value);

    /**
     * @return the estimated period of the waveform in samples
     */
    public double getPeriod();

    /**
     * Measure of how confident the analyzer is of the last result.
     * 
     * @return quality of the estimate between 0.0 and 1.0
     */
    public double getConfidence();

    /** For internal debugging. */
    public float[] getDiffs();

}
