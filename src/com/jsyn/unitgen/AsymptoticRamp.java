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
import com.jsyn.ports.UnitVariablePort;

/**
 * Output approaches Input exponentially. This unit provides a slowly changing value that approaches
 * its Input value exponentially. The equation is:
 * 
 * <PRE>
 * Output = Output + Rate * (Input - Output);
 * </PRE>
 * 
 * Note that the output may never reach the value of the input. It approaches the input
 * asymptotically. The Rate is calculated internally based on the value on the halfLife port. Rate
 * is generally just slightly less than 1.0.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see LinearRamp
 * @see ExponentialRamp
 * @see ContinuousRamp
 */
public class AsymptoticRamp extends UnitFilter {
    public UnitVariablePort current;
    public UnitInputPort halfLife;
    private double previousHalfLife = -1.0;
    private double decayScalar = 0.99;

    /* Define Unit Ports used by connect() and set(). */
    public AsymptoticRamp() {
        addPort(halfLife = new UnitInputPort(1, "HalfLife", 0.1));
        addPort(current = new UnitVariablePort("Current"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();
        double[] inputs = input.getValues();
        double currentHalfLife = halfLife.getValues()[0];
        double currentValue = current.getValue();
        double inputValue = currentValue;

        if (currentHalfLife != previousHalfLife) {
            decayScalar = this.convertHalfLifeToMultiplier(currentHalfLife);
            previousHalfLife = currentHalfLife;
        }

        for (int i = start; i < limit; i++) {
            inputValue = inputs[i];
            currentValue = currentValue + decayScalar * (inputValue - currentValue);
            outputs[i] = currentValue;
        }

        /*
         * When current gets close to input, set current to input to prevent FP underflow, which can
         * cause a severe performance degradation in 'C'.
         */
        if (Math.abs(inputValue - currentValue) < VERY_SMALL_FLOAT) {
            currentValue = inputValue;
        }

        current.setValue(currentValue);
    }
}
