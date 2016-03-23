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
 * SelectUnit unit. Select InputA or InputB based on value on Select port.
 *
 *<pre> <code>
   output = ( select &gt; 0.0 ) ? inputB : inputA;
</code> </pre>
 *
 * @author (C) 2004-2009 Phil Burk, SoftSynth.com
 */

public class Select extends UnitGenerator {
    public UnitInputPort inputA;
    public UnitInputPort inputB;
    public UnitInputPort select;
    public UnitOutputPort output;

    public Select() {
        addPort(inputA = new UnitInputPort("InputA"));
        addPort(inputB = new UnitInputPort("InputB"));
        addPort(select = new UnitInputPort("Select"));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputAs = inputA.getValues();
        double[] inputBs = inputB.getValues();
        double[] selects = select.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            outputs[i] = (selects[i] > UnitGenerator.FALSE) ? inputBs[i] : inputAs[i];
        }
    }
}
