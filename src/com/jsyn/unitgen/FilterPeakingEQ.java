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
 * PeakingEQ Filter. This can be used to raise or lower the gain around the cutoff frequency. This
 * filter is sometimes used in the middle of a bank of EQ filters. This filter is based on the
 * BiQuad filter. Coefficients are updated whenever the frequency or Q changes.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FilterPeakingEQ extends FilterBiquadCommon {
    public UnitInputPort gain;

    private double previousGain;

    public FilterPeakingEQ() {
        addPort(gain = new UnitInputPort("Gain", 1.0));
    }

    @Override
    protected boolean isRecalculationNeeded(double frequencyValue, double qValue) {
        double currentGain = gain.getValues()[0];
        if (currentGain < MINIMUM_GAIN) {
            currentGain = MINIMUM_GAIN;
        }

        boolean needed = super.isRecalculationNeeded(frequencyValue, qValue);
        needed |= (previousGain != currentGain);

        previousGain = currentGain;
        return needed;
    }

    @Override
    public void updateCoefficients() {
        double factorA = Math.sqrt(previousGain);
        double alphaTimesA = alpha * factorA;
        double alphaOverA = alpha / factorA;
        // Note this is not the normal scalar!
        double scalar = 1.0 / (1.0 + alphaOverA);
        double a1_b1_value = -2.0 * cos_omega * scalar;

        this.a0 = (1.0 + alphaTimesA) * scalar;

        this.a1 = a1_b1_value;
        this.a2 = (1.0 - alphaTimesA) * scalar;

        this.b1 = a1_b1_value;
        this.b2 = (1.0 - alphaOverA) * scalar;
    }
}
