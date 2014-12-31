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
 * PhaseShifter effects processor. This unit emulates a common guitar pedal effect but without the
 * LFO modulation. You can use your own modulation source connected to the "offset" port. Different
 * frequencies are phase shifted varying amounts using a series of AllPass filters. By feeding the
 * output back to the input we can get varying phase cancellation. This implementation was based on
 * code posted to the music-dsp archive by Ross Bencina. http://www.musicdsp.org/files/phaser.cpp
 * 
 * @author (C) 2014 Phil Burk, Mobileer Inc
 * @see FilterLowPass
 * @see FilterAllPass
 * @see RangeConverter
 */

public class PhaseShifter extends UnitFilter {
    /**
     * Connect an oscillator to this port to sweep the phase. A range of 0.05 to 0.4 is a good
     * start.
     */
    public UnitInputPort offset;
    public UnitInputPort feedback;
    public UnitInputPort depth;

    private double zm1;
    private double[] xs;
    private double[] ys;

    public PhaseShifter() {
        this(6);
    }

    public PhaseShifter(int numStages) {
        addPort(offset = new UnitInputPort("Offset", 0.1));
        addPort(feedback = new UnitInputPort("Feedback", 0.7));
        addPort(depth = new UnitInputPort("Depth", 1.0));

        xs = new double[numStages];
        ys = new double[numStages];
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double[] feedbacks = feedback.getValues();
        double[] depths = depth.getValues();
        double[] offsets = offset.getValues();
        double gain;

        for (int i = start; i < limit; i++) {
            // Support audio rate modulation.
            double currentOffset = offsets[i];

            // Prevent gain from exceeding 1.0.
            gain = 1.0 - (currentOffset * currentOffset);
            if (gain < -1.0) {
                gain = -1.0;
            }

            double x = inputs[i] + (zm1 * feedbacks[i]);
            // Cascaded all-pass filters.
            for (int stage = 0; stage < xs.length; stage++) {
                double temp = ys[stage] = (gain * (ys[stage] - x)) + xs[stage];
                xs[stage] = x;
                x = temp;
            }
            zm1 = x;
            outputs[i] = inputs[i] + (x * depths[i]);
        }
    }
}
