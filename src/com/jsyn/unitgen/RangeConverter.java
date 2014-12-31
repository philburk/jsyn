/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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

/**
 * Convert an input signal between -1.0 and +1.0 to the range min to max. This is handy when using
 * an oscillator as a modulator.
 * 
 * @author (C) 2014 Phil Burk, Mobileer Inc
 * @see EdgeDetector
 */
public class RangeConverter extends UnitFilter {
    public UnitInputPort min;
    public UnitInputPort max;

    /* Define Unit Ports used by connect() and set(). */
    public RangeConverter() {
        addPort(min = new UnitInputPort("Min", 40.0));
        addPort(max = new UnitInputPort("Max", 2000.0));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] mins = min.getValues();
        double[] maxs = max.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double low = mins[i];
            outputs[i] = low + ((maxs[i] - low) * (inputs[i] + 1) * 0.5);
        }
    }
}
