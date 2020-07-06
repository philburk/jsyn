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
/**
 * Aug 21, 2009
 * com.jsyn.engine.units.Filter_HighPass.java
 */

package com.jsyn.unitgen;

/**
 * Filter that allows frequencies below the center frequency to pass. This filter is based on the
 * BiQuad filter. Coefficients are updated whenever the frequency or Q changes.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 *
 * @see FilterFourPoles
 */
public class FilterLowPass extends FilterBiquadCommon {

    /**
     * This method is by FilterBiquad to update coefficients for the lowpass filter.
     */
    @Override
    public void updateCoefficients() {

        // scalar = 1.0f / (1.0f + BQCM.alpha);
        // omc = (1.0f - BQCM.cos_omega);
        // A0_A2_Value = omc * 0.5f * scalar;
        // // translating from RBJ coefficients
        // // A0 = (b0/(2*a0)
        // // = ((1 - cos_omega)/2) / (1 + alpha)
        // // = (omc*0.5) / (1 + alpha)
        // // = (omc*0.5) * (1.0/(1 + alpha))
        // // = omc * 0.5 * scalar
        // csFilter->csFBQ_A0 = A0_A2_Value;
        // csFilter->csFBQ_A1 = omc * scalar;
        // csFilter->csFBQ_A2 = A0_A2_Value;
        // csFilter->csFBQ_B1 = -2.0f * BQCM.cos_omega * scalar;
        // csFilter->csFBQ_B2 = (1.0f - BQCM.alpha) * scalar;

        double scalar = 1.0 / (1.0 + alpha);
        double oneMinusCosine = 1.0 - cos_omega;
        double a0_a2_value = oneMinusCosine * 0.5 * scalar;

        this.a0 = a0_a2_value;
        this.a1 = oneMinusCosine * scalar;
        this.a2 = a0_a2_value;
        this.b1 = -2.0 * cos_omega * scalar;
        this.b2 = (1.0 - alpha) * scalar;
    }
}
