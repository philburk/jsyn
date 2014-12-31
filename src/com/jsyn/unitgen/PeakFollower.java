/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitVariablePort;

/**
 * Tracks the peaks of an input signal. This can be used to monitor the overall amplitude of a
 * signal. The output can be used to drive color organs, vocoders, VUmeters, etc. Output drops
 * exponentially when the input drops below the current output level. The output approaches zero
 * based on the value on the halfLife port.
 * 
 * @author (C) 1997-2009 Phil Burk, SoftSynth.com
 */
public class PeakFollower extends UnitGenerator {
    public UnitInputPort input;
    public UnitVariablePort current;
    public UnitInputPort halfLife;
    public UnitOutputPort output;

    private double previousHalfLife = -1.0;
    private double decayScalar = 0.99;

    /* Define Unit Ports used by connect() and set(). */
    public PeakFollower() {
        addPort(input = new UnitInputPort("Input"));
        addPort(halfLife = new UnitInputPort(1, "HalfLife", 0.1));
        addPort(current = new UnitVariablePort("Current"));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double currentHalfLife = halfLife.getValues()[0];
        double currentValue = current.getValue();

        if (currentHalfLife != previousHalfLife) {
            decayScalar = this.convertHalfLifeToMultiplier(currentHalfLife);
            previousHalfLife = currentHalfLife;
        }

        double scalar = 1.0 - decayScalar;

        for (int i = start; i < limit; i++) {
            double inputValue = inputs[i];
            if (inputValue < 0.0) {
                inputValue = -inputValue; // absolute value
            }

            if (inputValue >= currentValue) {
                currentValue = inputValue;
            } else {
                currentValue = currentValue * scalar;
            }

            outputs[i] = currentValue;
        }

        /*
         * When current gets close to zero, set current to zero to prevent FP underflow, which can
         * cause a severe performance degradation in 'C'.
         */
        if (currentValue < VERY_SMALL_FLOAT) {
            currentValue = 0.0;
        }

        current.setValue(currentValue);
    }
}
