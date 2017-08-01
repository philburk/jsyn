/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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
 * Pass the input through to the output unchanged. This is often used for distributing a signal to
 * multiple ports inside a circuit. It can also be used as a summing node, in other words, a mixer.
 *
 * This is just like PassThrough except the input and output ports have multiple parts.
 * The default is two parts, ie. stereo.
 *
 * @author Phil Burk (C) 2016 Mobileer Inc
 * @see Circuit
 * @see PassThrough
 */
public class MultiPassThrough extends UnitGenerator  implements UnitSink, UnitSource {
    public UnitInputPort input;
    public UnitOutputPort output;
    private int mNumParts;

    /* Define Unit Ports used by connect() and set(). */
    public MultiPassThrough(int numParts) {
        mNumParts = numParts;
        addPort(input = new UnitInputPort(numParts, "Input"));
        addPort(output = new UnitOutputPort(numParts, "Output"));
    }

    public MultiPassThrough() {
        this(2); // stereo
    }

    @Override
    public UnitInputPort getInput() {
        return input;
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    @Override
    public void generate(int start, int limit) {
        for (int partIndex = 0; partIndex < mNumParts; partIndex++) {
            double[] inputs = input.getValues(partIndex);
            double[] outputs = output.getValues(partIndex);

            for (int i = start; i < limit; i++) {
                outputs[i] = inputs[i];
            }
        }
    }
}
