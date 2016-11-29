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
import com.jsyn.ports.UnitVariablePort;

/**
 * Output approaches Input linearly.
 * <P>
 * When you change the value of the input port, the ramp will start changing from its current output
 * value toward the value of input. An internal phase value will go from 0.0 to 1.0 at a rate
 * controlled by time. When the internal phase reaches 1.0, the output will equal input.
 * <P>
 *
 * @author (C) 1997 Phil Burk, SoftSynth.com
 * @see ExponentialRamp
 * @see AsymptoticRamp
 * @see ContinuousRamp
 */
public class LinearRamp extends UnitFilter {
    /** Time in seconds to get to the input value. */
    public UnitInputPort time;
    public UnitVariablePort current;

    private double source;
    private double phase;
    private double target;
    private double timeHeld = 0.0;
    private double rate = 1.0;

    public LinearRamp() {
        addPort(time = new UnitInputPort("Time"));
        addPort(current = new UnitVariablePort("Current"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();
        double currentInput = input.getValues()[0];
        double currentValue = current.getValue();

        // If input has changed, start new segment.
        // Equality check is OK because we set them exactly equal below.
        if (currentInput != target)
        {
            source = currentValue;
            phase = 0.0;
            target = currentInput;
        }

        if (currentValue == target) {
            // at end of ramp
            for (int i = start; i < limit; i++) {
                outputs[i] = currentValue;
            }
        } else {
            // in middle of ramp
            double currentTime = time.getValues()[0];
            // Has time changed?
            if (currentTime != timeHeld) {
                rate = convertTimeToRate(currentTime);
                timeHeld = currentTime;
            }

            for (int i = start; i < limit; i++) {
                if (phase < 1.0) {
                    /* Interpolate current. */
                    currentValue = source + (phase * (target - source));
                    phase += rate;
                } else {
                    currentValue = target;
                }
                outputs[i] = currentValue;
            }
        }

        current.setValue(currentValue);
    }
}
