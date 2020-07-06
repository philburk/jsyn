/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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
 * AllPass filter using the following formula:
 * 
 * <pre>
 * y(n) = -gain * x(n) + x(n - 1) + gain * y(n - 1)
 * </pre>
 * 
 * where y(n) is Output, x(n) is Input, x(n-1) is a delayed copy of the input, and y(n-1) is a
 * delayed copy of the output. An all-pass filter will pass all frequencies with equal amplitude.
 * But it changes the phase relationships of the partials by delaying them by an amount proportional
 * to their wavelength,.
 * 
 * @author (C) 2014 Phil Burk, SoftSynth.com
 * @see FilterLowPass
 */

public class FilterAllPass extends UnitFilter {
    /** Feedback gain. Should be less than 1.0. Default is 0.8. */
    public UnitInputPort gain;

    private double x1;
    private double y1;

    public FilterAllPass() {
        addPort(gain = new UnitInputPort("Gain", 0.8));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double g = gain.getValue();

        for (int i = start; i < limit; i++) {
            double x0 = inputs[i];
            y1 = (g * (y1 - x0)) + x1;
            x1 = x0;
            outputs[i] = y1;
        }

    }
}
