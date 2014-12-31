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
 * Filter that allows frequencies above the center frequency to pass. This filter is based on the
 * BiQuad filter. Coefficients are updated whenever the frequency or Q changes.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public class FilterHighPass extends FilterBiquadCommon {
    /**
     * This method is used by Filter_Biquad to update coefficients for the Filter_HighPass filter.
     */
    @Override
    public void updateCoefficients() {
        double scalar = 1.0 / (1.0 + alpha);
        double onePlusCosine = 1.0 + cos_omega;
        double a0_a2_value = onePlusCosine * 0.5 * scalar;

        this.a0 = a0_a2_value;
        this.a1 = -onePlusCosine * scalar;
        this.a2 = a0_a2_value;
        this.b1 = -2.0 * cos_omega * scalar;
        this.b2 = (1.0 - alpha) * scalar;
    }
}
