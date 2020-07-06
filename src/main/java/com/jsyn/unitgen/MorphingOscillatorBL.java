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

import com.jsyn.engine.MultiTable;
import com.jsyn.ports.UnitInputPort;

/**
 * Oscillator that can change its shape from sine to sawtooth to pulse.
 *
 * @author Phil Burk (C) 2016 Mobileer Inc
 */
public class MorphingOscillatorBL extends PulseOscillatorBL {
    /**
     * Controls the shape of the waveform.
     * The shape varies continuously from a sine wave at -1.0,
     * to a sawtooth at 0.0 to a pulse wave at 1.0.
     */
    public UnitInputPort shape;

    public MorphingOscillatorBL() {
        addPort(shape = new UnitInputPort("Shape"));
        shape.setMinimum(-1.0);
        shape.setMaximum(1.0);
    }

    @Override
    protected double generateBL(MultiTable multiTable, double currentPhase,
            double positivePhaseIncrement, double flevel, int i) {
        double[] shapes = shape.getValues();
        double shape = shapes[i];

        if (shape < 0.0) {
            // Squeeze flevel towards the pure sine table.
            flevel += flevel * shape;
            return multiTable.calculateSawtooth(currentPhase, positivePhaseIncrement, flevel);
        } else {
            double[] widths = width.getValues();
            double width = widths[i];
            width = (width > 0.999) ? 0.999 : ((width < -0.999) ? -0.999 : width);

            double val1 = multiTable.calculateSawtooth(currentPhase, positivePhaseIncrement, flevel);
            // Generate second sawtooth so we can add them together.
            double phase2 = currentPhase + 1.0 - width; // 180 degrees out of phase
            if (phase2 >= 1.0) {
                phase2 -= 2.0;
            }
            double val2 = multiTable.calculateSawtooth(phase2, positivePhaseIncrement, flevel);

            /*
             * Need to adjust amplitude based on positive phaseInc. little less than half at
             * Nyquist/2.0!
             */
            double scale = 1.0 - positivePhaseIncrement;
            return scale * (val1 - ((val2 + width) * shape)); // apply shape morphing
        }
    }
}
