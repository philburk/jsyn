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
 * Pan unit. The profile is constant amplitude and not constant energy.
 * <P>
 * Takes an input and pans it between two outputs based on value of pan. When pan is -1, output[0]
 * is input, and output[1] is zero. When pan is 0, output[0] and output[1] are both input/2. When
 * pan is +1, output[0] is zero, and output[1] is input.
 * <P>
 *
 * @author (C) 1997 Phil Burk, SoftSynth.com
 * @see Select
 */
public class Pan extends UnitGenerator {
    public UnitInputPort input;
    /**
     * Pan control varies from -1.0 for full left to +1.0 for full right. Set to 0.0 for center.
     */
    public UnitInputPort pan;
    public UnitOutputPort output;

    public Pan() {
        addPort(input = new UnitInputPort("Input"));
        addPort(pan = new UnitInputPort("Pan"));
        addPort(output = new UnitOutputPort(2, "Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] panPtr = pan.getValues();
        double[] outputs_0 = output.getValues(0);
        double[] outputs_1 = output.getValues(1);

        for (int i = start; i < limit; i++) {
            double gainB = (panPtr[i] * 0.5) + 0.5; /*
                                                     * Scale and offset to 0.0 to 1.0
                                                     */
            double gainA = 1.0 - gainB;
            double inVal = inputs[i];
            outputs_0[i] = inVal * gainA;
            outputs_1[i] = inVal * gainB;
        }
    }
}
