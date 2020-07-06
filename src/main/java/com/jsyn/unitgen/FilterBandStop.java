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

/**
 * Filter that blocks frequencies around the center frequency. This filter is based on the BiQuad
 * filter. Coefficients are updated whenever the frequency or Q changes.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public class FilterBandStop extends FilterBiquadCommon {

    @Override
    public void updateCoefficients() {

        // scalar = 1.0f / (1.0f + BQCM.alpha);
        // A1_B1_Value = -2.0f * BQCM.cos_omega * scalar;
        //
        // csFilter->csFBQ_A0 = scalar;
        // csFilter->csFBQ_A1 = A1_B1_Value;
        // csFilter->csFBQ_A2 = scalar;
        // csFilter->csFBQ_B1 = A1_B1_Value;
        // csFilter->csFBQ_B2 = (1.0f - BQCM.alpha) * scalar;

        double scalar = 1.0 / (1.0 + alpha);
        double a1_b1_value = -2.0 * cos_omega * scalar;

        this.a0 = scalar;
        this.a1 = a1_b1_value;
        this.a2 = scalar;
        this.b1 = a1_b1_value;
        this.b2 = (1.0 - alpha) * scalar;
    }
}
