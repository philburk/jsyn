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
 * HighShelf Filter. This creates a flat response above the cutoff frequency. This filter is
 * sometimes used at the end of a bank of EQ filters.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FilterHighShelf extends FilterBiquadShelf {
    /**
     * This method is called by by Filter_BiquadShelf to update coefficients.
     */
    @Override
    public void updateCoefficients() {
        double scalar = 1.0 / (AP1 - AM1cs + beta_sn);
        a0 = factorA * (AP1 + AM1cs + beta_sn) * scalar;
        a1 = -2.0 * factorA * (AM1 + AP1cs) * scalar;
        a2 = factorA * (AP1 + AM1cs - beta_sn) * scalar;
        b1 = 2.0 * (AM1 - AP1cs) * scalar;
        b2 = (AP1 - AM1cs - beta_sn) * scalar;
    }
}
