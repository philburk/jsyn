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

/**
 * Count zero crossings. Handy for unit tests.
 * 
 * @author (C) 1997-2011 Phil Burk, Mobileer Inc
 */
public class ZeroCrossingCounter extends UnitGenerator {
    private static final double THRESHOLD = 0.0001;
    public UnitInputPort input;
    public UnitOutputPort output;

    private long count;
    private boolean armed;

    /* Define Unit Ports used by connect() and set(). */
    public ZeroCrossingCounter() {
        addPort(input = new UnitInputPort("Input"));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double value = inputs[i];
            if (value < -THRESHOLD) {
                armed = true;
            } else if (armed & (value > THRESHOLD)) {
                ++count;
                armed = false;
            }
            outputs[i] = value;
        }
    }

    public long getCount() {
        return count;
    }
}
