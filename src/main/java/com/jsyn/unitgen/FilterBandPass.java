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
 * Filter that allows frequencies around the center frequency to pass and blocks others. This filter
 * is based on the BiQuad filter. Coefficients are updated whenever the frequency or Q changes.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public class FilterBandPass extends FilterBiquadCommon {
    /**
     * This method is by Filter_Biquad to update coefficients for the Filter_BandPass filter.
     */
    @Override
    public void updateCoefficients() {
        double scalar = 1.0 / (1.0 + alpha);

        a0 = alpha * scalar;
        a1 = 0.0;
        a2 = -a0;
        b1 = -2.0 * cos_omega * scalar;
        b2 = (1.0 - alpha) * scalar;
    }
}
