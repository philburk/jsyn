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

import java.io.IOException;

import com.jsyn.io.AudioOutputStream;
import com.jsyn.ports.UnitInputPort;

/**
 * Write two samples per audio frame to an AudioOutputStream as interleaved samples.
 *
 * Note that you must call start() on this unit because it does not have an output for pulling data.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class StereoStreamWriter extends UnitStreamWriter {
    public StereoStreamWriter() {
        addPort(input = new UnitInputPort(2, "Input"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] leftInputs = input.getValues(0);
        double[] rightInputs = input.getValues(1);
        AudioOutputStream output = outputStream;
        if (output != null) {
            try {
                for (int i = start; i < limit; i++) {
                    output.write(leftInputs[i]);
                    output.write(rightInputs[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                output = null;
            }
        }
    }
}
