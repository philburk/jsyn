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
import com.jsyn.ports.UnitOutputPort;

/**
 * Linear CrossFade between parts of the input.
 * <P>
 * Mix input[0] and input[1] based on the value of "fade". When fade is -1, output is all input[0].
 * When fade is 0, output is half input[0] and half input[1]. When fade is +1, output is all
 * input[1].
 * <P>
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see Pan
 */
public class CrossFade extends UnitGenerator {
    public UnitInputPort input;
    public UnitInputPort fade;
    public UnitOutputPort output;

    /* Define Unit Ports used by connect() and set(). */
    public CrossFade() {
        addPort(input = new UnitInputPort(2, "Input"));
        addPort(fade = new UnitInputPort("Fade"));
        fade.setup(-1.0, 0.0, 1.0);
        addPort(output = new UnitOutputPort());
    }

    @Override
    public void generate(int start, int limit) {
        double[] input0s = input.getValues(0);
        double[] input1s = input.getValues(1);
        double[] fades = fade.getValues();
        double[] outputs = output.getValues();
        for (int i = start; i < limit; i++) {
            // Scale and offset to 0.0 to 1.0 range.
            double gain = (fades[i] * 0.5) + 0.5;
            outputs[i] = (input0s[i] * (1.0 - gain)) + (input1s[i] * gain);
        }
    }

}
