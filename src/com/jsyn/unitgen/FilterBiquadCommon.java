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

package com.jsyn.unitgen;

import com.jsyn.ports.UnitInputPort;

/**
 * Extend this class to create a filter that implements a Biquad filter with a Q port.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public abstract class FilterBiquadCommon extends FilterBiquad {
    public UnitInputPort Q;

    protected final static double MINIMUM_Q = 0.00001;
    private double previousQ;
    protected double alpha;

    /**
     * No-argument constructor instantiates the Biquad common and adds a Q port to this filter.
     */
    public FilterBiquadCommon() {
        addPort(Q = new UnitInputPort("Q"));
        Q.setup(0.1, 1.0, 10.0);
    }

    /**
     * Calculate coefficients based on the filter type, eg. LowPass.
     */
    public abstract void updateCoefficients();

    public void computeBiquadCommon(double ratio, double Q) {
        if (ratio >= FilterBiquad.RATIO_MINIMUM) // keep a minimum distance
                                                 // from Nyquist
        {
            ratio = FilterBiquad.RATIO_MINIMUM;
        }

        omega = 2.0 * Math.PI * ratio;
        cos_omega = Math.cos(omega); // compute cosine
        sin_omega = Math.sin(omega); // compute sine
        alpha = sin_omega / (2.0 * Q); // set alpha
        // System.out.println("Q = " + Q + ", omega = " + omega +
        // ", cos(omega) = " + cos_omega + ", alpha = " + alpha );
    }

    /**
     * The recalculate() method checks and ensures that the frequency and Q values are at a minimum.
     * It also only updates the Biquad coefficients if either frequency or Q have changed.
     */
    @Override
    public void recalculate() {
        double frequencyValue = frequency.getValues()[0]; // grab frequency
                                                          // element (we'll
                                                          // only use
                                                          // element[0])
        double qValue = Q.getValues()[0]; // grab Q element (we'll only use
                                          // element[0])

        if (frequencyValue < MINIMUM_FREQUENCY) // ensure a minimum frequency
        {
            frequencyValue = MINIMUM_FREQUENCY;
        }

        if (qValue < MINIMUM_Q) // ensure a minimum Q
        {
            qValue = MINIMUM_Q;
        }
        // only update changed values
        if (isRecalculationNeeded(frequencyValue, qValue)) {
            previousFrequency = frequencyValue; // hold previous frequency
            previousQ = qValue; // hold previous Q

            double ratio = frequencyValue * getFramePeriod();
            computeBiquadCommon(ratio, qValue);
            updateCoefficients();
        }
    }

    protected boolean isRecalculationNeeded(double frequencyValue, double qValue) {
        return (frequencyValue != previousFrequency) || (qValue != previousQ);
    }

}
