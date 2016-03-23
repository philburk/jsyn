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

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitOutputPort;

/**
 * External audio input is sent to the output of this unit. The LineIn provides a stereo signal
 * containing channels 0 and 1. For LineIn to work you must call the Synthesizer start() method with
 * numInputChannels &gt; 0.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see Synthesizer
 * @see ChannelIn
 * @see LineOut
 */
public class LineIn extends UnitGenerator {
    public UnitOutputPort output;

    public LineIn() {
        addPort(output = new UnitOutputPort(2, "Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs0 = output.getValues(0);
        double[] outputs1 = output.getValues(1);
        double[] buffer0 = synthesisEngine.getInputBuffer(0);
        double[] buffer1 = synthesisEngine.getInputBuffer(1);
        for (int i = start; i < limit; i++) {
            outputs0[i] = buffer0[i];
            outputs1[i] = buffer1[i];
        }
    }

}
