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

/**
 * Input audio is sent to the external audio output device.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class LineOut extends UnitGenerator implements UnitSink {
    public UnitInputPort input;

    public LineOut() {
        addPort(input = new UnitInputPort(2, "Input"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs0 = input.getValues(0);
        double[] inputs1 = input.getValues(1);
        double[] buffer0 = synthesisEngine.getOutputBuffer(0);
        double[] buffer1 = synthesisEngine.getOutputBuffer(1);
        for (int i = start; i < limit; i++) {
            buffer0[i] += inputs0[i];
            buffer1[i] += inputs1[i];
        }
    }

    /**
     * This unit won't do anything unless you start() it.
     */
    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public UnitInputPort getInput() {
        return input;
    }
}
