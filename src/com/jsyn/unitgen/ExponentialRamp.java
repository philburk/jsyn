/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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
 * Output approaches Input exponentially and will reach it in the specified time.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @version 016
 * @see LinearRamp
 * @see AsymptoticRamp
 * @see ContinuousRamp
 */
public class ExponentialRamp extends UnitFilter {
    public UnitInputPort time;
    public UnitVariablePort current;

    private double target;
    private double timeHeld = 0.0;
    private double scaler = 1.0;

    public ExponentialRamp() {
        addPort(time = new UnitInputPort("Time"));
        input.setup(0.0001, 1.0, 1.0);
        addPort(current = new UnitVariablePort("Current", 1.0));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();
        double currentInput = input.getValues()[0];
        double currentTime = time.getValues()[0];
        double currentValue = current.getValue();

        if (currentTime != timeHeld) {
            scaler = convertTimeToExponentialScaler(currentTime, currentValue, currentInput);
            timeHeld = currentTime;
        }

        // If input has changed, start new segment.
        // Equality check is OK because we set them exactly equal below.
        if (currentInput != target) {
            scaler = convertTimeToExponentialScaler(currentTime, currentValue, currentInput);
            target = currentInput;
        }

        if (currentValue < target) {
            // Going up.
            for (int i = start; i < limit; i++) {
                currentValue = currentValue * scaler;
                if (currentValue > target) {
                    currentValue = target;
                    scaler = 1.0;
                }
                outputs[i] = currentValue;
            }
        } else if (currentValue > target) {
            // Going down.
            for (int i = start; i < limit; i++) {
                currentValue = currentValue * scaler;
                if (currentValue < target) {
                    currentValue = target;
                    scaler = 1.0;
                }
                outputs[i] = currentValue;
            }

        } else if (currentValue == target) {
            for (int i = start; i < limit; i++) {
                outputs[i] = target;
            }
        }

        current.setValue(currentValue);
    }

    private double convertTimeToExponentialScaler(double duration, double source, double target) {
        double product = source * target;
        if (product <= 0.0000001) {
            throw new IllegalArgumentException(
                    "Exponential ramp crosses zero or gets too close to zero.");
        }
        // Calculate scaler so that scaler^frames = target/source
        double numFrames = duration * getFrameRate();
        return Math.pow((target / source), (1.0 / numFrames));
    }
}
