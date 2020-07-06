/*
 * Copyright 2004 Phil Burk, Mobileer Inc
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
 * This unit combines two discrete inputs into a dual (stereo) output.
 * 
 * <pre>
 *  output[0] = inputA
 *  output[1] = inputB
 * </pre>
 * 
 * @author (C) 2004-2009 Phil Burk, SoftSynth.com
 */

public class TwoInDualOut extends UnitGenerator {
    public UnitInputPort inputA;
    public UnitInputPort inputB;
    public UnitOutputPort output;

    public TwoInDualOut() {
        addPort(inputA = new UnitInputPort("InputA"));
        addPort(inputB = new UnitInputPort("InputB"));
        addPort(output = new UnitOutputPort(2, "OutputB"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputAs = inputA.getValues();
        double[] inputBs = inputB.getValues();
        double[] output0s = output.getValues(0);
        double[] output1s = output.getValues(1);

        for (int i = start; i < limit; i++) {
            output0s[i] = inputAs[i];
            output1s[i] = inputBs[i];
        }
    }
}
