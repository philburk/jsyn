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
 * Pulse oscillator that uses two band limited sawtooth waveforms.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class PulseOscillatorBL extends SawtoothOscillatorBL {
    /** Controls the duty cycle of the pulse waveform.
     * The width varies from -1.0 to +1.0.
     * When width is zero the output is a square wave.
     */
    public UnitInputPort width;

    public PulseOscillatorBL() {
        addPort(width = new UnitInputPort("Width"));
    }

    @Override
    protected double generateBL(MultiTable multiTable, double currentPhase,
            double positivePhaseIncrement, double flevel, int i) {
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
         * Need to adjust amplitude based on positive phaseInc and width. little less than half at
         * Nyquist/2.0!
         */
        double scale = 1.0 - positivePhaseIncrement;
        return scale * (val1 - val2 - width);
    }
}
