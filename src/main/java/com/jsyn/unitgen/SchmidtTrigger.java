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
 * SchmidtTrigger unit.
 * <P>
 * Output logic level value with hysteresis. Transition high when input exceeds setLevel. Only go
 * low when input is below resetLevel. This can be used to reject low level noise on the input
 * signal. The default values for setLevel and resetLevel are both 0.0. Setting setLevel to 0.1 and
 * resetLevel to -0.1 will give some hysteresis. The outputPulse is a single sample wide pulse set
 * when the output transitions from low to high.
 * 
 * <PRE>
 * if (output == 0.0)
 *     output = (input &gt; setLevel) ? 1.0 : 0.0;
 * else if (output &gt; 0.0)
 *     output = (input &lt;= resetLevel) ? 0.0 : 1.0;
 * else
 *     output = previous_output;
 * </PRE>
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see Compare
 */
public class SchmidtTrigger extends UnitFilter {
    public UnitInputPort setLevel;
    public UnitInputPort resetLevel;
    public UnitOutputPort outputPulse;

    /* Define Unit Ports used by connect() and set(). */
    public SchmidtTrigger() {
        addPort(setLevel = new UnitInputPort("SetLevel"));
        addPort(resetLevel = new UnitInputPort("ResetLevel"));
        addPort(input = new UnitInputPort("Input"));
        addPort(outputPulse = new UnitOutputPort("OutputPulse"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inPtr = input.getValues();
        double[] pulsePtr = outputPulse.getValues();
        double[] outPtr = output.getValues();
        double[] setPtr = setLevel.getValues();
        double[] resetPtr = resetLevel.getValues();

        double outputValue = outPtr[0];
        boolean state = (outputValue > UnitGenerator.FALSE);
        for (int i = start; i < limit; i++) {
            pulsePtr[i] = UnitGenerator.FALSE;
            if (state) {
                if (inPtr[i] <= resetPtr[i]) {
                    state = false;
                    outputValue = UnitGenerator.FALSE;
                }
            } else {
                if (inPtr[i] > setPtr[i]) {
                    state = true;
                    outputValue = UnitGenerator.TRUE;
                    pulsePtr[i] = UnitGenerator.TRUE; /* Single impulse. */
                }
            }
            outPtr[i] = outputValue;
        }
    }
}
