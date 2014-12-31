/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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
 * Latches when input crosses zero.
 * <P>
 * Pass a value unchanged if gate true, otherwise pass input unchanged until input crosses zero then
 * output zero. This can be used to turn off a sound at a zero crossing so there is no pop.
 * <P>
 * 
 * @author (C) 2010 Phil Burk, Mobileer Inc
 * @see Latch
 * @see Minimum
 */
public class LatchZeroCrossing extends UnitGenerator {
    public UnitInputPort input;
    public UnitInputPort gate;
    public UnitOutputPort output;
    private double held;
    private boolean crossed;

    /* Define Unit Ports used by connect() and set(). */
    public LatchZeroCrossing() {
        addPort(input = new UnitInputPort("Input"));
        addPort(gate = new UnitInputPort("Gate", 1.0));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] gates = gate.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double current = inputs[i];
            if (gates[i] > 0.0) {
                held = current;
                crossed = false;
            } else {
                // If we haven't already seen a zero crossing then look for one.
                if (!crossed) {
                    if ((held * current) <= 0.0) {
                        held = 0.0;
                        crossed = true;
                    } else {
                        held = current;
                    }
                }
            }
            outputs[i] = held;
        }
    }
}
