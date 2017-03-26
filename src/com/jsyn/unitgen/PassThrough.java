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

/**
 * Pass the input through to the output unchanged. This is often used for distributing a signal to
 * multiple ports inside a circuit. It can also be used as a summing node, in other words, a mixer.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see Circuit
 * @see MultiPassThrough
 */
public class PassThrough extends UnitFilter {
    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            outputs[i] = inputs[i];
        }

    }
}
