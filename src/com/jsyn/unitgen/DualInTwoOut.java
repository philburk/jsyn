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
 * This unit splits a dual (stereo) input to two discrete outputs. <br>
 * 
 * <pre>
 * outputA = input[0];
 * outputB = input[1];
 * </pre>
 * 
 * <br>
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 */

public class DualInTwoOut extends UnitGenerator {
    public UnitInputPort input;
    public UnitOutputPort outputA;
    public UnitOutputPort outputB;

    public DualInTwoOut() {
        addPort(input = new UnitInputPort(2, "Input"));
        addPort(outputA = new UnitOutputPort("OutputA"));
        addPort(outputB = new UnitOutputPort("OutputB"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] input0s = input.getValues(0);
        double[] input1s = input.getValues(1);
        double[] outputAs = outputA.getValues();
        double[] outputBs = outputB.getValues();

        for (int i = start; i < limit; i++) {
            outputAs[i] = input0s[i];
            outputBs[i] = input1s[i];
        }
    }
}
