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

/**
 * This can be used to block the execution of upstream units. It can be placed at the output of a
 * circuit and driven with an amplitude envelope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class EnablingGate extends UnitFilter {
    public UnitInputPort gate;

    /* Define Unit Ports used by connect() and set(). */
    public EnablingGate() {
        super();
        addPort(gate = new UnitInputPort("Gate"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] aValues = input.getValues();
        double[] bValues = gate.getValues();
        double[] outputs = output.getValues();
        for (int i = start; i < limit; i++) {
            outputs[i] = aValues[i] * bValues[i];
        }
        // If we end up at zero then disable pulling of data.
        // We do this at the end so that envelope can get started.
        if (outputs[limit - 1] <= 0.0) {
            setEnabled(false);
        }
    }

}
