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
 * output = (2.0^input) This is useful for converting a pitch modulation value into a frequency
 * scaler. An input value of +1.0 will output 2.0 for an octave increase. An input value of -1.0
 * will output 0.5 for an octave decrease.
 *
 * This implementation uses a table lookup to optimize for
 * speed. It is accurate enough for tuning. It also checks to see if the current input value is the
 * same as the previous input value. If so then it reuses the previous computed value.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PowerOfTwo extends UnitGenerator {
    /**
     * Offset in octaves.
     */
    public UnitInputPort input;
    public UnitOutputPort output;

    private static double[] table;
    private static final int NUM_VALUES = 2048;
    // Cached computation.
    private double lastInput = 0.0;
    private double lastOutput = 1.0;

    static {
        // Add guard point for faster interpolation.
        // Add another point to handle inputs like -1.5308084989341915E-17,
        // which generate indices above range.
        table = new double[NUM_VALUES + 2];
        // Fill one octave of the table.
        for (int i = 0; i < table.length; i++) {
            double value = Math.pow(2.0, ((double) i) / NUM_VALUES);
            table[i] = value;
        }
    }

    public PowerOfTwo() {
        addPort(input = new UnitInputPort("Input"));
        input.setup(-8.0, 0.0, 8.0);
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double in = inputs[i];
            // Can we reuse a previously computed value?
            if (in == lastInput) {
                outputs[i] = lastOutput;
            } else {
                lastInput = in;
                double adjustedInput = adjustInput(in);
                int octave = (int) Math.floor(adjustedInput);
                double normal = adjustedInput - octave;
                // Do table lookup.
                double findex = normal * NUM_VALUES;
                int index = (int) findex;
                double fraction = findex - index;
                double value = table[index] + (fraction * (table[index + 1] - table[index]));

                // Adjust for octave.
                while (octave > 0) {
                    octave -= 1;
                    value *= 2.0;
                }
                while (octave < 0) {
                    octave += 1;
                    value *= 0.5;
                }
                double adjustedOutput = adjustOutput(value);
                outputs[i] = adjustedOutput;
                lastOutput = adjustedOutput;
            }
        }
    }

    public double adjustInput(double in) {
        return in;
    }

    public double adjustOutput(double out) {
        return out;
    }
}
