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
 * A ramp whose function over time is continuous in value and in slope. Also called an "S curve".
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see LinearRamp
 * @see ExponentialRamp
 * @see AsymptoticRamp
 */
public class ContinuousRamp extends UnitFilter {
    public UnitVariablePort current;
    /**
     * Time it takes to get from current value to input value when input is changed. Default value
     * is 1.0 seconds.
     */
    public UnitInputPort time;
    private double previousInput = Double.MIN_VALUE;
    // Coefficients for cubic polynomial.
    private double a;
    private double b;
    private double d;
    private int framesLeft;

    /* Define Unit Ports used by connect() and set(). */
    public ContinuousRamp() {
        addPort(time = new UnitInputPort(1, "Time", 1.0));
        addPort(current = new UnitVariablePort("Current"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();
        double[] inputs = input.getValues();
        double currentTime = time.getValues()[0];
        double currentValue = current.getValue();
        double inputValue = currentValue;

        for (int i = start; i < limit; i++) {
            inputValue = inputs[i];
            double x;
            if (inputValue != previousInput) {
                x = framesLeft;
                // Calculate coefficients.
                double currentSlope = x * ((3 * a * x) + (2 * b));

                framesLeft = (int) (getSynthesisEngine().getFrameRate() * currentTime);
                if (framesLeft < 1) {
                    framesLeft = 1;
                }
                x = framesLeft;
                // Calculate coefficients.
                d = inputValue;
                double xsq = x * x;
                b = ((3 * currentValue) - (currentSlope * x) - (3 * d)) / xsq;
                a = (currentSlope - (2 * b * x)) / (3 * xsq);
                previousInput = inputValue;
            }

            if (framesLeft > 0) {
                x = --framesLeft;
                // Cubic polynomial. c==0
                currentValue = (x * (x * ((x * a) + b))) + d;
            }

            outputs[i] = currentValue;
        }

        current.setValue(currentValue);
    }
}
