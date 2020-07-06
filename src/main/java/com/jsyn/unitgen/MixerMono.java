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
import com.jsyn.ports.UnitOutputPort;

/**
 * Multi-channel mixer with mono output and master amplitude.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 * @see MixerMonoRamped
 * @see MixerStereo
 */
public class MixerMono extends UnitGenerator implements UnitSink, UnitSource {
    public UnitInputPort input;
    /**
     * Linear gain for the corresponding input.
     */
    public UnitInputPort gain;
    /**
     * Master gain control.
     */
    public UnitInputPort amplitude;
    public UnitOutputPort output;

    public MixerMono(int numInputs) {
        addPort(input = new UnitInputPort(numInputs, "Input"));
        addPort(gain = new UnitInputPort(numInputs, "Gain", 1.0));
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
        addPort(output = new UnitOutputPort(getNumOutputs(), "Output"));
    }

    public int getNumOutputs() {
        return 1;
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues(0);
        double[] outputs = output.getValues(0);
        for (int i = start; i < limit; i++) {
            double sum = 0;
            for (int n = 0; n < input.getNumParts(); n++) {
                double[] inputs = input.getValues(n);
                double[] gains = gain.getValues(n);
                sum += inputs[i] * gains[i];
            }
            outputs[i] = sum * amplitudes[i];
        }
    }

    @Override
    public UnitInputPort getInput() {
        return input;
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

}
