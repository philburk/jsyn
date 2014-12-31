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
 * This filter is based on the BiQuad filter and is used as a base class for FilterLowShelf and
 * FilterHighShelf. Coefficients are updated whenever the frequency, gain or slope changes.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public abstract class FilterBiquadShelf extends FilterBiquad {
    protected final static double MINIMUM_SLOPE = 0.00001;

    /**
     * Gain of peak. Use 1.0 for flat response.
     */
    public UnitInputPort gain;

    /**
     * Shelf Slope parameter. When S = 1, the shelf slope is as steep as you can get it and remain
     * monotonically increasing or decreasing gain with frequency.
     */
    public UnitInputPort slope;

    private double prevGain;
    private double prevSlope;

    private double beta;
    protected double alpha;
    protected double factorA;
    protected double AP1;
    protected double AM1;
    protected double beta_sn;
    protected double AP1cs;
    protected double AM1cs;

    public FilterBiquadShelf() {
        addPort(gain = new UnitInputPort("Gain", 1.0));
        addPort(slope = new UnitInputPort("Slope", 1.0));
    }

    /**
     * Abstract method. Each filter must implement its update of coefficients.
     */
    public abstract void updateCoefficients();

    /**
     * Compute coefficients for shelf filter if frequency, gain or slope have changed.
     */
    @Override
    public void recalculate() {
        // Just look at first value to save time.
        double frequencyValue = frequency.getValues()[0];
        if (frequencyValue < MINIMUM_FREQUENCY) {
            frequencyValue = MINIMUM_FREQUENCY;
        }

        double gainValue = gain.getValues()[0];
        if (gainValue < MINIMUM_GAIN) {
            gainValue = MINIMUM_GAIN;
        }

        double slopeValue = slope.getValues()[0];
        if (slopeValue < MINIMUM_SLOPE) {
            slopeValue = MINIMUM_SLOPE;
        }

        // Only do complex calculations if input changed.
        if ((frequencyValue != previousFrequency) || (gainValue != prevGain)
                || (slopeValue != prevSlope)) {
            previousFrequency = frequencyValue; // hold previous frequency
            prevGain = gainValue;
            prevSlope = slopeValue;

            double ratio = frequencyValue * getFramePeriod();
            calculateOmega(ratio);

            factorA = Math.sqrt(gainValue);

            AP1 = factorA + 1.0;
            AM1 = factorA - 1.0;

            /* Avoid sqrt(r<0) which hangs filter. */
            double beta2 = ((gainValue + 1.0) / slopeValue) - (AM1 * AM1);
            beta = (beta2 < 0.0) ? 0.0 : Math.sqrt(beta2);

            beta_sn = beta * sin_omega;
            AP1cs = AP1 * cos_omega;
            AM1cs = AM1 * cos_omega;

            updateCoefficients();
        }
    }

}
