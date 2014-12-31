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
 * PanControl unit.
 * <P>
 * Generates control signals that can be used to control a mixer or the amplitude ports of two
 * units.
 * 
 * <PRE>
 * temp = (pan * 0.5) + 0.5;
 * output[0] = temp;
 * output[1] = 1.0 - temp;
 * </PRE>
 * <P>
 * 
 * @author (C) 1997-2009 Phil Burk, SoftSynth.com
 */
public class PanControl extends UnitGenerator {
    public UnitInputPort pan;
    public UnitOutputPort output;

    /* Define Unit Ports used by connect() and set(). */
    public PanControl() {
        addPort(pan = new UnitInputPort("Pan"));
        addPort(output = new UnitOutputPort(2, "Output", 0.0));
    }

    @Override
    public void generate(int start, int limit) {
        double[] panPtr = pan.getValues();
        double[] output0s = output.getValues(0);
        double[] output1s = output.getValues(1);

        for (int i = start; i < limit; i++) {
            double gainB = (panPtr[i] * 0.5) + 0.5; /*
                                                     * Scale and offset to 0.0 to 1.0
                                                     */
            output0s[i] = 1.0 - gainB;
            output1s[i] = gainB;
        }
    }
}
