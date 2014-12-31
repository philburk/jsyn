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
 * IntegrateUnit unit.
 * <P>
 * Output accumulated sum of the input signal. This can be used to transform one signal into
 * another, or to generate ramps between the limits by setting the input signal positive or
 * negative. For a "leaky integrator" use a FilterOnePoleOneZero.
 * <P>
 * 
 * <pre>
 * output = output + input;
 * if (output &lt; lowerLimit)
 *     output = lowerLimit;
 * else if (output &gt; upperLimit)
 *     output = upperLimit;
 * </pre>
 * 
 * @author (C) 1997-2011 Phil Burk, Mobileer Inc
 * @see FilterOnePoleOneZero
 */
public class Integrate extends UnitGenerator {
    public UnitInputPort input;
    /**
     * Output will be stopped internally from going below this value. Default is -1.0.
     */
    public UnitInputPort lowerLimit;
    /**
     * Output will be stopped internally from going above this value. Default is +1.0.
     */
    public UnitInputPort upperLimit;
    public UnitOutputPort output;

    private double accum;

    /* Define Unit Ports used by connect() and set(). */
    public Integrate() {
        addPort(input = new UnitInputPort("Input"));
        addPort(lowerLimit = new UnitInputPort("LowerLimit", -1.0));
        addPort(upperLimit = new UnitInputPort("UpperLimit", 1.0));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] lowerLimits = lowerLimit.getValues();
        double[] upperLimits = upperLimit.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            accum += inputs[i]; // INTEGRATE

            // clip to limits
            if (accum > upperLimits[i])
                accum = upperLimits[i];
            else if (accum < lowerLimits[i])
                accum = lowerLimits[i];

            outputs[i] = accum;
        }
    }
}
