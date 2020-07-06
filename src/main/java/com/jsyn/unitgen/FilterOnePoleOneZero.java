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

import com.jsyn.ports.UnitVariablePort;

/**
 * First Order, One Pole, One Zero filter using the following formula:
 * 
 * <pre>
 * y(n) = A0 * x(n) + A1 * x(n - 1) - B1 * y(n - 1)
 * </pre>
 * 
 * where y(n) is Output, x(n) is Input, x(n-1) is a delayed copy of the input, and y(n-1) is a
 * delayed copy of the output. This filter is a recursive IIR or Infinite Impulse Response filter.
 * it can be unstable depending on the values of the coefficients. A thorough description of the
 * digital filter theory needed to fully describe this filter is beyond the scope of this document.
 * Calculating coefficients is non-intuitive; the interested user is referred to one of the standard
 * texts on filter theory (e.g., Moore, "Elements of Computer Music", section 2.4).
 * 
 * @author (C) 1997-2009 Phil Burk, SoftSynth.com
 * @see FilterLowPass
 */

public class FilterOnePoleOneZero extends UnitFilter {
    public UnitVariablePort a0;
    public UnitVariablePort a1;
    public UnitVariablePort b1;

    private double x1;
    private double y1;

    public FilterOnePoleOneZero() {
        addPort(a0 = new UnitVariablePort("A0"));
        addPort(a1 = new UnitVariablePort("A1"));
        addPort(b1 = new UnitVariablePort("B1"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double a0v = a0.getValue();
        double a1v = a1.getValue();
        double b1v = b1.getValue();

        for (int i = start; i < limit; i++) {
            double x0 = inputs[i];
            outputs[i] = y1 = (a0v * x0) + (a1v * x1) + (b1v * y1);
            x1 = x0;
        }

    }
}
